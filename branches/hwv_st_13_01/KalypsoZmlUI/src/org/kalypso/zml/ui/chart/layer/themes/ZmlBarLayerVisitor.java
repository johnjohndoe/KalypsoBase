/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypso.zml.ui.chart.layer.themes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.graphics.Point;
import org.joda.time.Period;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.visitor.IObservationValueContainer;
import org.kalypso.ogc.sensor.visitor.IObservationVisitor;
import org.kalypso.zml.ui.KalypsoZmlUI;

/**
 * @author Dirk Kuch
 */
public class ZmlBarLayerVisitor implements IObservationVisitor
{
  @Deprecated
  /** @deprecated don't use m_lastScreen anymore. calcualte polygone by timestep of timeseries */
  Point m_lastScreen;

  IAxis m_dateAxis;

  private IAxis m_valueAxis;

  private final ZmlBarLayerRangeHandler m_range;

  private final ZmlBarLayer m_layer;

  List<Point[]> m_points = new ArrayList<Point[]>();

  private final Point m_baseLine;

  public ZmlBarLayerVisitor( final ZmlBarLayer layer, final ZmlBarLayerRangeHandler range )
  {
    m_layer = layer;
    m_range = range;

    m_baseLine = m_layer.getCoordinateMapper().numericToScreen( 0.0, 0.0 );
  }

  @Override
  public void visit( final IObservationValueContainer container )
  {
    try
    {
      final Object domainValue = container.get( getDateAxis( container ) );
      final Object targetValue = getTargetValue( container );
      if( Objects.isNull( domainValue, targetValue ) )
        return;

      final Number logicalDomain = m_range.getDateDataOperator().logicalToNumeric( (Date) domainValue );
      final Number logicalTarget = m_range.getNumberDataOperator().logicalToNumeric( (Number) targetValue );
      final Point p1 = m_layer.getCoordinateMapper().numericToScreen( logicalDomain, logicalTarget );

      // don't draw empty lines only rectangles
      if( p1.y != m_baseLine.y )
      {
        // TODO: performance: read only once from metadata?
        final Period timestep = MetadataHelper.getTimestep( container.getMetaData() );

        final int x0 = getX( p1, logicalDomain, timestep );

        final List<Point> points = new ArrayList<Point>();
        points.add( new Point( x0, m_baseLine.y ) );
        points.add( new Point( x0, p1.y ) );
        points.add( p1 );
        points.add( new Point( p1.x, m_baseLine.y ) );

        m_points.add( points.toArray( new Point[] {} ) );
      }

      m_lastScreen = p1;
    }
    catch( final Throwable t )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
    }
  }

  public Point[][] getPoints( )
  {
    return m_points.toArray( new Point[][] {} );
  }

  private Object getTargetValue( final IObservationValueContainer container ) throws SensorException
  {
    Object value = container.get( getValueAxis( container ) );

    /** @hack for polder control */
    if( value instanceof Boolean )
    {
      if( Boolean.valueOf( (Boolean) value ) )
        value = 1;
      else
        value = 0;
    }

    return value;
  }

  private int getX( final Point point, final Number logicalTime, final Period timestep )
  {
    final String parameterType = m_valueAxis.getType();
    if( ITimeseriesConstants.TYPE_POLDER_CONTROL.equals( parameterType ) )
      return getForwardX( point, logicalTime, timestep );

    return getBackwardX( point, logicalTime, timestep );

  }

  private int getForwardX( final Point point, final Number logicalTime, final Period timestep )
  {
    if( Objects.isNotNull( timestep ) )
    {
      final long ms = timestep.toStandardSeconds().getSeconds() * 1000;

      final long x0 = logicalTime.longValue() + ms;
      final Point screen = m_layer.getCoordinateMapper().numericToScreen( x0, 0 );

      return screen.x;
    }

    return point.x;
  }

  private int getBackwardX( final Point point, final Number logicalTime, final Period timestep )
  {
    if( Objects.isNotNull( timestep ) )
    {
      final long ms = timestep.toStandardSeconds().getSeconds() * 1000;

      final long x0 = logicalTime.longValue() - ms;
      final Point screen = m_layer.getCoordinateMapper().numericToScreen( x0, 0 );

      return screen.x;
    }
    /**
     * TODO old projects doesn't define time step. not removed because of backward compatibility of old projects.
     */
    else if( Objects.isNotNull( m_lastScreen ) )
      return m_lastScreen.x;

    return point.x;
  }

  private IAxis getValueAxis( final IObservationValueContainer container )
  {
    if( Objects.isNull( m_valueAxis ) )
      m_valueAxis = AxisUtils.findValueAxis( container.getAxes() );

    return m_valueAxis;
  }

  private IAxis getDateAxis( final IObservationValueContainer container )
  {
    if( Objects.isNull( m_dateAxis ) )
      m_dateAxis = AxisUtils.findDateAxis( container.getAxes() );

    return m_dateAxis;
  }
}
