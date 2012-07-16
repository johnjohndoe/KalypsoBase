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

import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.joda.time.Period;
import org.kalypso.commons.exception.CancelVisitorException;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.visitor.IObservationValueContainer;
import org.kalypso.ogc.sensor.visitor.IObservationVisitor;
import org.kalypso.zml.ui.KalypsoZmlUI;

import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.figure.impl.FullRectangleFigure;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;

/**
 * @author Dirk Kuch
 */
class ZmlBarLayerBackwardsVisitor implements IObservationVisitor
{
  private final IAxis m_dateAxis;

  private final IAxis m_valueAxis;

  private final ZmlBarLayerRangeHandler m_range;

  private final int m_baseLine;

  private final FullRectangleFigure m_figure;

  private final GC m_gc;

  private final ICoordinateMapper m_mapper;

  final Rectangle m_rectangle = new Rectangle( 0, 0, 0, 0 );

  private final IProgressMonitor m_monitor;

  private boolean m_isFirst = true;

  private final Period m_timestep;

  private int m_lastX;

  public ZmlBarLayerBackwardsVisitor( final ICoordinateMapper mapper, final ZmlBarLayerRangeHandler range, final GC gc, final FullRectangleFigure figure, final IObservation observation, final IProgressMonitor monitor )
  {
    m_mapper = mapper;
    m_range = range;
    m_gc = gc;
    m_figure = figure;
    m_monitor = monitor;

    m_figure.setRectangle( m_rectangle );

    m_valueAxis = AxisUtils.findValueAxis( observation.getAxes() );
    m_dateAxis = AxisUtils.findDateAxis( observation.getAxes() );

    m_timestep = MetadataHelper.getTimestep( observation.getMetadataList() );

    m_baseLine = mapper.numericToScreen( 0.0, 0.0 ).y;
    m_rectangle.y = m_baseLine;
  }

  @Override
  public void visit( final IObservationValueContainer container )
  {
    if( m_monitor.isCanceled() )
      throw new CancelVisitorException();

    try
    {
      final Date domainValue = (Date) container.get( m_dateAxis );
      final Object targetValue = getTargetValue( container );
      if( Objects.isNull( domainValue, targetValue ) )
        return;

      final Number numericTarget = m_range.getNumberDataOperator().logicalToNumeric( (Number) targetValue );

      /* current x */
      final IDataOperator<Date> dateDataOperator = m_range.getDateDataOperator();
      final Number numericDomain = dateDataOperator.logicalToNumeric( domainValue );

      final Point screenCurrent = m_mapper.numericToScreen( numericDomain, numericTarget );

      final int currentX = screenCurrent.x;

      if( m_isFirst )
        initFirst( domainValue, currentX );

      final int width = Math.max( 1, Math.abs( currentX - m_lastX ) );

      adjustLinevisibilty( width );

      m_rectangle.x = currentX;
      m_rectangle.height = screenCurrent.y - m_baseLine;
      m_rectangle.width = width;

      m_figure.paint( m_gc );

      m_lastX = currentX;
    }
    catch( final Throwable t )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
    }
  }

  private void initFirst( final Date currentTime, final int screenCurrent )
  {
    m_isFirst = false;

    if( m_timestep == null )
    {
      m_lastX = screenCurrent - 1;
      return;
    }

    final long widthMillis = m_timestep.toStandardSeconds().getSeconds() * 1000;

    final Date prevTime = new Date( currentTime.getTime() - widthMillis );

    final IDataOperator<Date> dateDataOperator = m_range.getDateDataOperator();
    final Number numericPrev = dateDataOperator.logicalToNumeric( prevTime );

    m_lastX = m_mapper.numericToScreen( numericPrev, 0 ).x;
  }

  /** Sets line visibility dependent on width (else we get only lines if width is too small) */
  private void adjustLinevisibilty( final int width )
  {
    final ILineStyle stroke = m_figure.getStyle().getStroke();

    /* We only use the first width once, because width may vary due to rounding */
    if( stroke != null && stroke.isVisible() )
      m_figure.getStyle().setFillVisible( width > 1 );
    else
      m_figure.getStyle().setFillVisible( true );

    // stroke.setVisible( Math.abs( width ) > 4 );
  }

  private Object getTargetValue( final IObservationValueContainer container ) throws SensorException
  {
    final Object value = container.get( m_valueAxis );

    // FIXME + ugly! logicalToNumeric should take care of that!

    if( value instanceof Boolean )
    {
      if( Boolean.valueOf( (Boolean) value ) )
        return 1;
      else
        return 0;
    }

    return value;
  }
}