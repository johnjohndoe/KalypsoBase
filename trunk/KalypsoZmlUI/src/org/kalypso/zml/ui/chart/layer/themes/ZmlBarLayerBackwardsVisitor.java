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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.joda.time.Period;
import org.kalypso.commons.exception.CancelVisitorException;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.visitor.IObservationValueContainer;
import org.kalypso.ogc.sensor.visitor.IObservationVisitor;
import org.kalypso.zml.ui.KalypsoZmlUI;

import de.openali.odysseus.chart.ext.base.layer.BarPaintManager;
import de.openali.odysseus.chart.ext.base.layer.BarPaintManager.ITooltipCallback;
import de.openali.odysseus.chart.ext.base.layer.BarRectangle;
import de.openali.odysseus.chart.ext.base.layer.IBarLayerPainter;
import de.openali.odysseus.chart.framework.model.figure.IPaintable;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IChartLayerFilter;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;

/**
 * Supported parameters
 * <ul>
 * <li>fixedHeight: replaces the value of the target axis with a fixed value</li>
 * </ul>
 * 
 * @author Dirk Kuch
 * @author Gernot Belger
 */
class ZmlBarLayerBackwardsVisitor implements IObservationVisitor, IBarLayerPainter
{
  private final ITooltipCallback m_tooltipCallback = new ZmlBarLayerTooltipCallback();

  private final Map<String, IChartLayerFilter> m_filters = new HashMap<>();

  private final IAxis m_dateAxis;

  private final IAxis m_valueAxis;

  private final int m_baseLine;

  private final ICoordinateMapper<Date, Number> m_mapper;

  private BarRectangle m_currentBar;

  private final Period m_timestep;

  private final IObservation m_observation;

  private final IRequest m_request;

  private IProgressMonitor m_monitor;

  private final BarPaintManager m_paintManager;

  private final String[] m_styleNames;

  private final Number m_fixedHeight;

  private final ZmlBarLayer m_layer;

  public ZmlBarLayerBackwardsVisitor( final ZmlBarLayer layer, final BarPaintManager paintManager, final IObservation observation, final IAxis valueAxis, final IRequest request, final Period timestep, final String[] styleNames )
  {
    m_observation = observation;
    m_request = request;
    m_timestep = timestep;
    m_styleNames = styleNames;
    m_fixedHeight = layer.getFixedHeight();
    m_valueAxis = valueAxis;
    m_dateAxis = AxisUtils.findDateAxis( observation.getAxes() );

    m_layer = layer;
    m_paintManager = paintManager;
    m_mapper = layer.getCoordinateMapper();
    m_baseLine = m_mapper.normalizedToScreen(0.0,ALIGNMENT.TOP.doubleValue()).y;
  }

  @Override
  public void execute( final IProgressMonitor monitor )
  {
    try
    {
      m_monitor = monitor;
      m_observation.accept( this, m_request, 1 );

      paintLast();
    }
    catch( final CancelVisitorException e )
    {
      throw new OperationCanceledException();
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  @Override
  public void visit( final IObservationValueContainer container )
  {
    if( m_monitor.isCanceled() )
      throw new CancelVisitorException();

    try
    {
      final Date domainValue = (Date)container.get( m_dateAxis );
      final Number targetValue = getTargetValue( container );
      if( Objects.isNull( domainValue, targetValue ) )
        return;

      /* current x */
      final Point screenCurrent = m_mapper.logicalToScreen( domainValue, targetValue );

      final int currentX = screenCurrent.x;
      final int currentY = screenCurrent.y;

      initRectangleFirstTime( domainValue, currentX );

      /* set data to current bar: the absolute max wins */
      final EditInfo oldInfo = m_currentBar.getEditInfo();
      final IObservationValueContainer oldContainer = oldInfo == null ? null : (IObservationValueContainer)(oldInfo.getData());

      /* update current bar */
      final Rectangle currentRectangle = m_currentBar.getRectangle();

      /* construct new rectangle */
      final int newX = screenCurrent.x;
      final int newY = Math.min( m_baseLine, currentY );
      final int newHeight = Math.abs( currentY - m_baseLine );
      final Rectangle rectangle = new Rectangle( newX, newY, 0, newHeight );

      // REMARK: we continue even off-screen, in order to correctly handle the first rectangle

      // REMARK: union needed to be sure negative/positive bars are both correctly handled
      currentRectangle.add( rectangle );

      /* Only check for style if rectangle is visible at all */
      // REMRK: always use currentRectangle that is already inflated with last width
      if( m_paintManager.isInScreen( currentRectangle ) )
      {
        /* find styles and set current max data */
        final String[] currentStyles = getCurrentStyles( container );
        m_currentBar.addStyle( currentStyles );

        final IObservationValueContainer currentMaxData = calculateCurrentMaxData( oldContainer, container, targetValue );
        final EditInfo info = createInfo( currentMaxData );
        m_currentBar.setEditInfo( info );
      }

      /* Paint or store last rectangle */
      if( currentRectangle.width > 0 )
      {
        if( currentRectangle.height == 0 )
          currentRectangle.height = 1;

        m_paintManager.addRectangle( m_currentBar, m_tooltipCallback );

        /* reset rectangle */
        currentRectangle.x = currentRectangle.x + currentRectangle.width;
        currentRectangle.y = m_baseLine;
        currentRectangle.width = 0;
        currentRectangle.height = 0;

        m_currentBar = new BarRectangle( m_currentBar.getRectangle(), new String[] {}, null );
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

  private Number getTargetValue( final IObservationValueContainer container ) throws SensorException
  {
    if( m_fixedHeight != null )
      return m_fixedHeight;

    return (Number)container.get( m_valueAxis );
  }

  private String[] getCurrentStyles( final IObservationValueContainer container )
  {
    final Collection<String> styles = new ArrayList<>( m_styleNames.length );

    for( final String style : m_styleNames )
    {
      final IChartLayerFilter filter = getFilter( style );
      if( filter == null || filter.isFiltered( container ) )
        styles.add( style );
    }

    return styles.toArray( new String[styles.size()] );
  }

  private IChartLayerFilter getFilter( final String style )
  {
    if( !m_filters.containsKey( style ) )
    {
      final IChartLayerFilter filter = m_layer.getStyleFilter( style, m_observation );

      m_filters.put( style, filter );
    }

    return m_filters.get( style );
  }

  private IObservationValueContainer calculateCurrentMaxData( final IObservationValueContainer oldContainer, final IObservationValueContainer container, final Number targetValue ) throws SensorException
  {
    if( oldContainer == null )
      return container;
    else
    {
      final Number oldValue = getTargetValue( oldContainer );
      final double oldDouble = oldValue.doubleValue();
      final double currentDouble = targetValue.doubleValue();

      if( Math.abs( currentDouble ) > Math.abs( oldDouble ) )
        return container;
      else
        return oldContainer;
    }
  }

  /**
   * (Re-)Paint the last rectangle, for the case that is has not yet been painted yet. Even if we paint it a second
   * time, this does not cost much, but the user is always able to see the last value.
   */
  public void paintLast( )
  {
    if( m_currentBar != null )
    {
      // FIXME: use real data object
      m_paintManager.addRectangle( m_currentBar, m_tooltipCallback );
    }
  }

  private void initRectangleFirstTime( final Date currentTime, final int screenCurrentX )
  {
    if( m_currentBar == null )
    {
      final int lastX = calculateFirstX( currentTime, screenCurrentX );

      final Rectangle currentRectangle = new Rectangle( lastX, m_baseLine, 0, 0 );

      m_currentBar = new BarRectangle( currentRectangle, new String[] {}, null );
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

      return m_mapper.getDomainAxis().logicalToScreen( prevTime );// .numericToScreen( numericPrev, 0.0 ).x;
    }
  }

  private EditInfo createInfo( final IObservationValueContainer editData )
  {
    if( editData == null )
      return null;

    // REMARK: the hover figure will be set later (in the paint manager) , because the real rectangle is still not known now
    final IPaintable hoverFigure = null;
    final IPaintable editFigure = null;

    // REMARK: also the tooltip will be processed later for performance reasons (only create as many tooltips as necessary)
    final String tooltip = null;

    return new EditInfo( m_layer, hoverFigure, editFigure, editData, tooltip, null );
  }
}