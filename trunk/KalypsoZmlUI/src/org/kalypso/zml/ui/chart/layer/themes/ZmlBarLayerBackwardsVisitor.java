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
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.visitor.IObservationValueContainer;
import org.kalypso.ogc.sensor.visitor.IObservationVisitor;
import org.kalypso.zml.ui.KalypsoZmlUI;

import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.figure.impl.FullRectangleFigure;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;

/**
 * @author Dirk Kuch
 */
class ZmlBarLayerBackwardsVisitor implements IObservationVisitor
{
  private final BarLayerRectangleIndex m_index = new BarLayerRectangleIndex();

  private final IAxis m_dateAxis;

  private final IAxis m_valueAxis;

  private final ZmlBarLayerRangeHandler m_range;

  private final int m_baseLine;

  private final FullRectangleFigure m_figure;

  private final GC m_gc;

  private final ICoordinateMapper m_mapper;

  private Rectangle m_currentRectangle;

  private final IProgressMonitor m_monitor;

  private final Period m_timestep;

  public ZmlBarLayerBackwardsVisitor( final ICoordinateMapper mapper, final ZmlBarLayerRangeHandler range, final GC gc, final FullRectangleFigure figure, final IObservation observation, final Period timestep, final IProgressMonitor monitor )
  {
    m_mapper = mapper;
    m_range = range;
    m_gc = gc;
    m_figure = figure;
    m_timestep = timestep;
    m_monitor = monitor;

    m_valueAxis = AxisUtils.findValueAxis( observation.getAxes() );
    m_dateAxis = AxisUtils.findDateAxis( observation.getAxes() );

    m_baseLine = mapper.numericToScreen( 0.0, 0.0 ).y;
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
      final int currentY = screenCurrent.y;

      initRectangleFirstTime( domainValue, currentX );

      /* construct new rectangle */
      final int newX = screenCurrent.x;
      final int newY = Math.min( m_baseLine, currentY );
      final int newHeight = Math.abs( currentY - m_baseLine );
      final Rectangle rectangle = new Rectangle( newX, newY, 0, newHeight );

      // REMARK: union needed to be sure negative/positive bars are both correctly handled

      m_currentRectangle.add( rectangle );

      /* Paint or store last rectangle */
      if( m_currentRectangle.width > 0 )
      {
        if( m_currentRectangle.height == 0 )
          m_currentRectangle.height = 1;

        final Rectangle clipping = m_gc.getClipping();
        if( clipping.intersects( m_currentRectangle ) )
        {
          m_index.addElement( m_currentRectangle, container.getIndex() );

          m_figure.paint( m_gc );
        }

        /* reset rectangle */
        m_currentRectangle.x = m_currentRectangle.x + m_currentRectangle.width;
        m_currentRectangle.y = m_baseLine;
        m_currentRectangle.width = 0;
        m_currentRectangle.height = 0;
      }
      else
      {
        /*
         * nothing: just store rectangle for next paint: x stays at old place -> we cummulate rectangles until we have
         * at least 1px to paint
         */
      }

    }
    catch( final Throwable t )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
    }
  }

  /**
   * (Re-)Paint the last rectangle, for the case that is has not yet been painted yet. Even if we paint it a second
   * time, this does not cost much, but the user is always able to see the last value.
   */
  public void paintLast( )
  {
    if( m_currentRectangle != null )
      m_figure.paint( m_gc );
  }

  private void initRectangleFirstTime( final Date currentTime, final int screenCurrentX )
  {
    if( m_currentRectangle == null )
    {
      final int lastX = calculateFirstX( currentTime, screenCurrentX );

      m_currentRectangle = new Rectangle( lastX, m_baseLine, 0, 0 );
      m_figure.setRectangle( m_currentRectangle );
    }
  }

  private int calculateFirstX( final Date currentTime, final int screenCurrentX )
  {
    if( m_timestep == null )
    {
      // REMARK: should now happen very seldom: no timestep known, just use -2 pixel for first value
      return screenCurrentX - 2;
    }
    else
    {
      /* Calculate lastX from timestep */
      final long widthMillis = m_timestep.toStandardSeconds().getSeconds() * 1000;

      final Date prevTime = new Date( currentTime.getTime() - widthMillis );

      final IDataOperator<Date> dateDataOperator = m_range.getDateDataOperator();
      final Number numericPrev = dateDataOperator.logicalToNumeric( prevTime );

      return m_mapper.numericToScreen( numericPrev, 0 ).x;
    }
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

  public BarLayerRectangleIndex getRectangles( )
  {
    return m_index;
  }
}