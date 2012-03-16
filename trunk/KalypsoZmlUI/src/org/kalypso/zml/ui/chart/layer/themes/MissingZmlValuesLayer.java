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

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Range;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.DateRange;

import de.openali.odysseus.chart.ext.base.layer.AbstractBarLayer;
import de.openali.odysseus.chart.framework.model.ILayerContainer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSetVisitor;

/**
 * @author Dirk Kuch
 */
public class MissingZmlValuesLayer extends AbstractBarLayer implements IChartLayer
{
  public MissingZmlValuesLayer( final ILayerProvider provider, final IStyleSet styleSet )
  {
    super( provider, styleSet );
  }

  @Override
  protected IAreaStyle getAreaStyle( )
  {
    final IStyleSet styleSet = getStyleSet();
    final int index = ZmlLayerHelper.getLayerIndex( getIdentifier() );

    final StyleSetVisitor visitor = new StyleSetVisitor( true );
    final IAreaStyle style = visitor.visit( styleSet, IAreaStyle.class, index );

    return style;
  }

  @Override
  public void paint( final GC gc )
  {
    final DateRange[] missing = findMissingValues();
    if( ArrayUtils.isEmpty( missing ) )
      return;

    final Rectangle[] rectangles = toRectangles( missing );
    for( final Rectangle rectangle : rectangles )
    {
      paint( gc, rectangle );
    }
  }

  private Rectangle[] toRectangles( final DateRange[] missing )
  {
    final ICoordinateMapper mapper = getCoordinateMapper();
    final IAxis domainAxis = mapper.getDomainAxis();
    final IAxis targetAxis = mapper.getTargetAxis();

    final IDataRange<Number> domainRange = domainAxis.getNumericRange();
    final IDataRange<Number> targetRange = targetAxis.getNumericRange();

    final Number min = domainRange.getMin();
    final Number max = domainRange.getMax();

    final Set<Rectangle> rectangles = new LinkedHashSet<>();

    for( final DateRange daterange : missing )
    {
      final Range<Long> range = Range.between( daterange.getFrom().getTime(), daterange.getTo().getTime() );
      final Rectangle rectangle = toRectangle( range );
      if( Objects.isNotNull( rectangle ) )
        rectangles.add( rectangle );
    }

    return rectangles.toArray( new Rectangle[] {} );
  }

  private Rectangle toRectangle( final Range<Long> daterange )
  {
    final ICoordinateMapper mapper = getCoordinateMapper();
    final IAxis domainAxis = mapper.getDomainAxis();
    final IAxis targetAxis = mapper.getTargetAxis();

    final Range<Long> domainRange = getRange( domainAxis.getNumericRange() );
    final Range<Long> targetRange = getRange( targetAxis.getNumericRange() );
    if( Objects.isNull( domainRange, targetRange ) )
      return null;

    if( !domainRange.isOverlappedBy( daterange ) )
      return null;

    final Range<Long> intersection = domainRange.intersectionWith( daterange );

    final Integer x0 = Math.abs( domainAxis.numericToScreen( intersection.getMinimum() ) );
    final Integer x1 = Math.abs( domainAxis.numericToScreen( intersection.getMaximum() ) );

    final Integer y0 = targetAxis.numericToScreen( targetRange.getMinimum() );
// final Integer y1 = targetAxis.numericToScreen( targetRange.getMaximum() );

    return new Rectangle( x0, y0, x1 - x0, 10 );
  }

  private Range<Long> getRange( final IDataRange<Number> range )
  {
    final Number min = range.getMin();
    final Number max = range.getMax();
    if( Objects.isNull( min, max ) )
      return null;

    return Range.between( min.longValue(), max.longValue() );
  }

  private DateRange[] findMissingValues( )
  {
    final ILayerContainer parent = getParent();
    if( !(parent instanceof IChartLayer) )
      return new DateRange[] {};

    final IChartLayer base = (IChartLayer) parent;

    final FindMissingValuesVisitor visitor = new FindMissingValuesVisitor();
    base.getLayerManager().accept( visitor );

    return visitor.getMissingValues();
  }
}
