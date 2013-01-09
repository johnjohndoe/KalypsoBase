/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.DateRange;

import de.openali.odysseus.chart.factory.layer.AbstractChartLayer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener.ContentChangeType;
import de.openali.odysseus.chart.framework.model.figure.impl.PolygonFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSetVisitor;
import de.openali.odysseus.chart.framework.util.img.ChartImageInfo;

/**
 * @author Dirk Kuch
 */
// FIXME: bad name; has nothing to do with zml
public class ZmlSelectionLayer extends AbstractChartLayer
{
  private final ILineStyle m_lineStyle;

  private Date m_selection;

  private DateRange m_selectedDateRange;

  private final IAreaStyle m_areaStyle;

  public ZmlSelectionLayer( final ILayerProvider layerProvider, final IStyleSet styleSet )
  {
    super( layerProvider, styleSet );

    final StyleSetVisitor visitor = new StyleSetVisitor( false );
    m_lineStyle = visitor.visit( getStyleSet(), ILineStyle.class, 0 );
    m_areaStyle = visitor.visit( getStyleSet(), IAreaStyle.class, 0 );
  }

  @Override
  public void paint( final GC gc, final ChartImageInfo chartImageInfo, final IProgressMonitor monitor )
  {
    if( Objects.allNull( m_selection, m_selectedDateRange ) )
      return;

    if( Objects.isNotNull( m_selection ) )
      paintSingleSelect( gc );
    else if( Objects.isNotNull( m_selectedDateRange ) )
      paintMultiSelect( gc );
  }

  @SuppressWarnings( { "unchecked", "rawtypes" } )
  private void paintMultiSelect( final GC gc )
  {
    final ICoordinateMapper mapper = getCoordinateMapper();

    final IAxis domainAxis = mapper.getDomainAxis();
    final IAxis targetAxis = mapper.getTargetAxis();

    final IDataRange<Date> domainRange = domainAxis.getLogicalRange();
    final IDataRange<Integer> targetRange = targetAxis.getLogicalRange();
    final DateRange dateRange = new DateRange( domainRange.getMin(), domainRange.getMax() );
    if( !dateRange.intersects( m_selectedDateRange ) )
      return;

    final Integer x1 = domainAxis.logicalToScreen( m_selectedDateRange.getFrom() );
    final Integer x2 = domainAxis.logicalToScreen( m_selectedDateRange.getTo() );
    final Integer yMin = targetAxis.logicalToScreen( targetRange.getMin() );
    final Integer yMax = targetAxis.logicalToScreen( targetRange.getMax() );

    final PolygonFigure figure = new PolygonFigure();
    figure.setStyle(m_areaStyle);
    figure.setPoints( new Point[] { new Point( x1, yMin ), new Point( x1, yMax ), new Point( x2, yMax ), new Point( x2, yMin ) } );
    figure.paint( gc );
  }

  @SuppressWarnings( "rawtypes" )
  private void paintSingleSelect( final GC gc )
  {
    final ICoordinateMapper<Date,Integer> mapper = getCoordinateMapper();
    final IAxis targetAxis = mapper.getTargetAxis();
    final PolylineFigure polylineFigure = new PolylineFigure();
    polylineFigure.setStyle( m_lineStyle );
    final IDataRange<Integer> targetRange = targetAxis.getLogicalRange();
    final Point p1 = mapper.logicalToScreen( m_selection, targetRange.getMin() );
    final Point p2 = mapper.logicalToScreen( m_selection, targetRange.getMax() );
    polylineFigure.setPoints( new Point[] { p1, p2 } );
    polylineFigure.paint( gc );
  }

  public void setSelection( final Date selection )
  {
    m_selection = selection;
    m_selectedDateRange = null;

    getEventHandler().fireLayerContentChanged( this, ContentChangeType.value );
  }

  public void setSelection( final DateRange dateRange )
  {
    m_selection = null;
    m_selectedDateRange = dateRange;

    getEventHandler().fireLayerContentChanged( this, ContentChangeType.value );
  }

  public void purgeSelection( )
  {
    m_selection = null;
    m_selectedDateRange = null;

    getEventHandler().fireLayerContentChanged( this, ContentChangeType.value );
  }

  @Override
  public IDataRange<Double> getDomainRange( )
  {
    return null;
  }

  @Override
  public IDataRange<Double> getTargetRange( final IDataRange<Double> domainIntervall )
  {
    return null;
  }
}