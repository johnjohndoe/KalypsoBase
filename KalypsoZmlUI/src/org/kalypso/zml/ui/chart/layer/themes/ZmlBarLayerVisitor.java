/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.joda.time.Period;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.visitor.IObservationValueContainer;
import org.kalypso.ogc.sensor.visitor.IObservationVisitor;
import org.kalypso.zml.ui.KalypsoZmlUI;

import de.openali.odysseus.chart.framework.model.figure.impl.FullRectangleFigure;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;

/**
 * @author Dirk Kuch
 */
class ZmlBarLayerVisitor implements IObservationVisitor
{
  private Integer m_lastScreenX = null;

  private final IAxis m_dateAxis;

  private final IAxis m_valueAxis;

  private final ZmlBarLayerRangeHandler m_range;

  private final int m_baseLine;

  private final FullRectangleFigure m_figure;

  private final GC m_gc;

  private final ICoordinateMapper m_mapper;

  private final Period m_timestep;

  private int m_barWidth = 0;

  private boolean m_initDone = false;

  final Rectangle m_rectangle = new Rectangle( 0, 0, 0, 0 );

  public ZmlBarLayerVisitor( final ICoordinateMapper mapper, final ZmlBarLayerRangeHandler range, final GC gc, final FullRectangleFigure figure, final IObservation observation )
  {
    m_mapper = mapper;
    m_range = range;
    m_gc = gc;
    m_figure = figure;

    m_figure.setRectangle( m_rectangle );

    m_timestep = MetadataHelper.getTimestep( observation.getMetadataList() );

    m_valueAxis = AxisUtils.findValueAxis( observation.getAxes() );
    m_dateAxis = AxisUtils.findDateAxis( observation.getAxes() );

    m_baseLine = mapper.numericToScreen( 0.0, 0.0 ).y;
    m_rectangle.y = m_baseLine;
  }

  @Override
  public void visit( final IObservationValueContainer container )
  {
    try
    {
      final Object domainValue = container.get( m_dateAxis );
      final Object targetValue = getTargetValue( container );
      if( Objects.isNull( domainValue, targetValue ) )
        return;

      final Number numericDomain = m_range.getDateDataOperator().logicalToNumeric( (Date) domainValue );
      final Number numericTarget = m_range.getNumberDataOperator().logicalToNumeric( (Number) targetValue );
      final Point screenCurrent = m_mapper.numericToScreen( numericDomain, numericTarget );

      final boolean isFirstvisit = !m_initDone;

      if( isFirstvisit )
        initFirstValue( screenCurrent.x, numericDomain );

      final int width = getCurrentWidth( screenCurrent.x );

      if( isFirstvisit )
        adjustLinevisibilty( m_rectangle.width );

      m_rectangle.x = screenCurrent.x;
      m_rectangle.height = screenCurrent.y - m_baseLine;
      m_rectangle.width = width;

      // REAMRK: adjust widht a bit, because else rounding leads to ugly artifacts
      if( m_lastScreenX != null && m_lastScreenX - m_rectangle.width != screenCurrent.x && Math.abs( width ) > 1 )
      {
        final int diff = m_lastScreenX - width - screenCurrent.x;
        if( Math.abs( diff ) <= 1 )
        {
          m_rectangle.width += diff;
        }
      }

      m_figure.paint( m_gc );

      // FIXME: remove
      m_lastScreenX = screenCurrent.x;
    }
    catch( final Throwable t )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
    }
  }

  private int getCurrentWidth( final int screenTimeCurrent )
  {
    // If width determined by timestep, just return this value
    if( m_barWidth != 0 )
      return m_barWidth;

    // Else, calculate by last value (backwards compatibility)
    final int lastX = getLastX( screenTimeCurrent );
    return Math.max( 1, screenTimeCurrent - lastX );
  }

  private void initFirstValue( final int screenTimeCurrent, final Number numericTimeCurrent )
  {
    if( m_timestep != null )
    {
      final long widthMillis = m_timestep.toStandardSeconds().getSeconds() * 1000;

      final long numericTimeNext = numericTimeCurrent.longValue() + widthMillis;
      final int screenTimeNext = m_mapper.numericToScreen( numericTimeNext, 0 ).x;

      final int basicWidth = screenTimeNext - screenTimeCurrent;

      /* width must have at least 1 pixel */

      // TODO/REMARK: are there other better strategies to paint elements below one pixel width (building mean value or
      // similar?)

      final int widthWithoutSign = basicWidth == 0 ? 1 : basicWidth;

      /* depending on data type, forward/backward */
      if( isForward() )
        m_barWidth = widthWithoutSign;
      else
        m_barWidth = -widthWithoutSign;

      m_rectangle.width = m_barWidth;
    }

    m_initDone = true;
  }

  /** Sets line visibility dependent on width (else we get only lines if width is too small) */
  private void adjustLinevisibilty( final int width )
  {
    final ILineStyle stroke = m_figure.getStyle().getStroke();

    /* We only use the first width once, because width may vary due to rounding */
    if( stroke != null && stroke.isVisible() )
      m_figure.getStyle().setFillVisible( Math.abs( width ) > 1 );
    else
      m_figure.getStyle().setFillVisible( true );

    // stroke.setVisible( Math.abs( width ) > 4 );
  }

  private Object getTargetValue( final IObservationValueContainer container ) throws SensorException
  {
    final Object value = container.get( m_valueAxis );

    // FIXME + ugly! logicalToNumeric should take care of that!

    /** @hack for polder control */
    if( value instanceof Boolean )
    {
      if( Boolean.valueOf( (Boolean) value ) )
        return 1;
      else
        return 0;
    }

    return value;
  }

  private boolean isForward( )
  {
    // FIXME: bad and ugly!
    final String parameterType = m_valueAxis.getType();
    return ITimeseriesConstants.TYPE_POLDER_CONTROL.equals( parameterType );
  }

  /**
   * TODO old projects don't define time step. not removed because of backward compatibility of old projects.
   */
  private int getLastX( final int currentX )
  {
    if( m_lastScreenX != null )
      return m_lastScreenX;

    return currentX;
  }
}