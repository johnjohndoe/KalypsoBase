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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.DateRange;

import de.openali.odysseus.chart.factory.layer.AbstractChartLayer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.figure.impl.PolygonFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;

/**
 * @author Dirk Kuch
 */
public class ZmlSelectionLayer extends AbstractChartLayer
{
  private final ILineStyle m_lineStyle;

  private Date m_selection;

  private DateRange m_selectedDateRange;

  private final IAreaStyle m_areaStyle;

  public ZmlSelectionLayer( final ILayerProvider layerProvider, final ILineStyle lineStyle, final IAreaStyle areaStyle )
  {
    super( layerProvider );

    m_lineStyle = lineStyle;
    m_areaStyle = areaStyle;
  }

  @Override
  public void paint( final GC gc )
  {
    if( Objects.allNull( m_selection, m_selectedDateRange ) )
      return;

    if( Objects.isNotNull( m_selection ) )
      paintSingleSelect( gc );
    else if( Objects.isNotNull( m_selectedDateRange ) )
      paintMultiSelect( gc );
  }

  private void paintMultiSelect( final GC gc )
  {
    final ICoordinateMapper mapper = getCoordinateMapper();

    final IAxis domainAxis = mapper.getDomainAxis();
    final IAxis targetAxis = mapper.getTargetAxis();

    final IDataRange<Number> domainRange = domainAxis.getNumericRange();
    final IDataRange<Number> targetRange = targetAxis.getNumericRange();

    final Number min = domainRange.getMin();
    final Number max = domainRange.getMax();
    if( Objects.isNull( min, max ) )
      return;

    final DateRange dateRange = new DateRange( new Date( min.longValue() ), new Date( max.longValue() ) );
    if( !dateRange.intersects( m_selectedDateRange ) )
      return;

    final Integer x1 = Math.abs( domainAxis.numericToScreen( m_selectedDateRange.getFrom().getTime() ) );
    final Integer x2 = Math.abs( domainAxis.numericToScreen( m_selectedDateRange.getTo().getTime() ) );

    final Integer yMin = targetAxis.numericToScreen( targetRange.getMin() );
    final Integer yMax = targetAxis.numericToScreen( targetRange.getMax() );

    final PolygonFigure figure = new PolygonFigure();
    figure.setStyle( m_areaStyle );

    final List<Point> points = new ArrayList<Point>();
    points.add( new Point( x1, yMin ) );
    points.add( new Point( x1, yMax ) );
    points.add( new Point( x2, yMax ) );
    points.add( new Point( x2, yMin ) );

    figure.setPoints( points.toArray( new Point[] {} ) );
    figure.paint( gc );
  }

  private void paintSingleSelect( final GC gc )
  {
    final ICoordinateMapper mapper = getCoordinateMapper();

    final IAxis domainAxis = mapper.getDomainAxis();
    final IAxis targetAxis = mapper.getTargetAxis();

    final IDataRange<Number> domainRange = domainAxis.getNumericRange();
    final IDataRange<Number> targetRange = targetAxis.getNumericRange();

    final Number min = domainRange.getMin();
    final Number max = domainRange.getMax();
    if( Objects.isNull( min, max ) )
      return;

    final DateRange dateRange = new DateRange( new Date( min.longValue() ), new Date( max.longValue() ) );
    if( !dateRange.containsLazyInclusive( m_selection ) )
      return;

    final double logicalX = min.doubleValue() + m_selection.getTime() - min.doubleValue();
    final Integer x = Math.abs( domainAxis.numericToScreen( logicalX ) );

    final Integer yMin = targetAxis.numericToScreen( targetRange.getMin() );
    final Integer yMax = targetAxis.numericToScreen( targetRange.getMax() );

    final PolylineFigure polylineFigure = new PolylineFigure();
    polylineFigure.setStyle( m_lineStyle );
    polylineFigure.setPoints( new Point[] { new Point( x, yMin ), new Point( x, yMax ) } );
    polylineFigure.paint( gc );
  }

  public void setSelection( final Date selection )
  {
    m_selection = selection;
    m_selectedDateRange = null;

    getEventHandler().fireLayerContentChanged( this );
  }

  public void setSelection( final DateRange dateRange )
  {
    m_selection = null;
    m_selectedDateRange = dateRange;

    getEventHandler().fireLayerContentChanged( this );
  }

  public void purgeSelection( )
  {
    m_selection = null;
    m_selectedDateRange = null;

    getEventHandler().fireLayerContentChanged( this );
  }

  @Override
  public IDataRange< ? > getDomainRange( )
  {
    return null;
  }

  @Override
  public IDataRange< ? > getTargetRange( final IDataRange< ? > domainIntervall )
  {
    return null;
  }

  @Override
  protected ILegendEntry[] createLegendEntries( )
  {
    return new ILegendEntry[] {};
  }

}
