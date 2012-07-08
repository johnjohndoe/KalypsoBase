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
package de.openali.odysseus.chart.framework.model.impl.visitors;

import java.util.ArrayList;
import java.util.List;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.impl.IAxisVisitorBehavior;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisAdjustment;
import de.openali.odysseus.chart.framework.model.mapper.registry.IAxisVisitor;
import de.openali.odysseus.chart.framework.util.ChartUtilities;

/**
 * @author Dirk Kuch
 */
public class AutoScaleVisitor implements IAxisVisitor
{
  private final IChartModel m_model;

  public AutoScaleVisitor( final IChartModel model )
  {
    m_model = model;
  }

  @Override
  public void visit( final IAxis axis )
  {
    final IChartLayer[] layers = m_model.getLayerManager().getLayers( axis, true );
    final IAxisVisitorBehavior visitorBehavior = axis.getAxisVisitorBehavior();
    if( visitorBehavior != null && !visitorBehavior.isAutoscaleEnabled() )
      return;

    final IDataRange< ? >[] chartRanges = getChartRanges( axis, layers );

    final IDataRange<Number> mergedDataRange = mergeChartRanges( axis, chartRanges );
    if( mergedDataRange == null )
      return;

    // now check if axis has a preferred Adjustment
    final IAxisAdjustment adj = axis.getPreferredAdjustment();

    final IDataRange<Number> adjustedRange = adjustRange( adj, mergedDataRange );
    axis.setNumericRange( adjustedRange );
  }

  private IDataRange<Number> adjustRange( final IAxisAdjustment adj, final IDataRange<Number> mergedDataRange )
  {
    if( adj == null )
      return mergedDataRange;

    // FIXME: comments!
    final double adjBefore = adj.getBefore();
    final double adjRange = adj.getRange();
    final double adjAfter = adj.getAfter();

    final double mergedRange = mergedDataRange.getMax().doubleValue() - mergedDataRange.getMin().doubleValue();
    final double minMergedRange = adj.getMinValue().doubleValue();
    final double maxMergedRange = adj.getMaxValue().doubleValue();
    final double rangeMin;
    final double rangeMax;
    if( mergedRange < minMergedRange )
    {
      final double delta = (minMergedRange - mergedRange) / 2.0;
      rangeMin = mergedDataRange.getMin().doubleValue() - delta;
      rangeMax = mergedDataRange.getMax().doubleValue() + delta;
    }
    else if( mergedRange > maxMergedRange )
    {
      final double delta = (mergedRange - maxMergedRange) / 2.0;
      rangeMin = mergedDataRange.getMin().doubleValue() + delta;
      rangeMax = mergedDataRange.getMax().doubleValue() - delta;
    }
    else
    {
      rangeMin = mergedDataRange.getMin().doubleValue();
      rangeMax = mergedDataRange.getMax().doubleValue();
    }
    // computing preferred adjustment failed if rangesize==0.0, so we set a range minimum depends on adjustment
    final double rangeSize = mergedRange == 0.0 ? 1.0 : mergedRange;
    final double newMin = rangeMin - rangeSize * adjBefore / adjRange;
    final double newMax = rangeMax + rangeSize * adjAfter / adjRange;

    return new ComparableDataRange<Number>( new Number[] { newMin, newMax } );
  }

  private IDataRange<Number> mergeChartRanges( final IAxis axis, final IDataRange< ? >[] chartRanges )
  {

// final IDataRange<Number>[] numericRanges = toNumeric( axis, chartRanges );

    final IDataRange<Number> mergedDataRange = ChartUtilities.mergeDataRanges( chartRanges );
    if( mergedDataRange != null )
      return mergedDataRange;

    // if mergedDataRange is null, we keep the old range - if there is any
    if( axis.getNumericRange() != null )
      return null;

    // otherwise, we use a default range
    return new ComparableDataRange<Number>( new Number[] { 0, 1 } );
  }

  private IDataRange<Number>[] toNumeric( final IAxis axis, final IDataRange< ? >[] chartRanges )
  {
    final IDataRange<Number>[] numericRanges = new IDataRange[chartRanges.length];

    // FIXME: awful design, needs to be fixed: the axis should know which data type it works on, we cannot decide it
    // from outside!
    // It MUSt be possible to calc the numeric values from a generic axis!

    final IDataOperator<Object> dataOperator = axis.getDataOperator( null );

    for( int i = 0; i < numericRanges.length; i++ )
    {
      final IDataRange< ? > logicalRange = chartRanges[i];

      final Number numMin = dataOperator.logicalToNumeric( logicalRange.getMin() );
      final Number numMax = dataOperator.logicalToNumeric( logicalRange.getMax() );

      numericRanges[i] = new DataRange<Number>( numMin, numMax );
    }

    return numericRanges;
  }

  private IDataRange< ? >[] getChartRanges( final IAxis axis, final IChartLayer[] layers )
  {
    final List<IDataRange< ? >> ranges = new ArrayList<IDataRange< ? >>( layers.length );
    for( final IChartLayer layer : layers )
    {
      if( layer.isVisible() && layer.isAutoScale() )
      {
        final IDataRange< ? > range = getRangeFor( layer, axis );
        if( range != null )
        {
          ranges.add( range );
        }
      }
    }

    return ranges.toArray( new IDataRange[ranges.size()] );
  }

  /**
   * @return DataRange of all domain or target data available in the given layer
   */
  private IDataRange< ? > getRangeFor( final IChartLayer layer, final IAxis axis )
  {
    if( axis == layer.getCoordinateMapper().getDomainAxis() )
      return layer.getDomainRange();

    if( axis == layer.getCoordinateMapper().getTargetAxis() )
      return layer.getTargetRange( null );

    return null;
  }
}