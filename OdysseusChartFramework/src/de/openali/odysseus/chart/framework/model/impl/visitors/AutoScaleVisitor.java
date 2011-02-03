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
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;
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

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.registry.IAxisVisitor#visit(de.openali.odysseus.chart.framework.model.mapper.IAxis)
   */
  @Override
  public void visit( final IAxis axis )
  {
    final IChartLayer[] layers = m_model.getLayerManager().getLayers( axis );

    final List<IDataRange<Number>> ranges = new ArrayList<IDataRange<Number>>( layers.length );

    for( final IChartLayer layer : layers )
    {
      if( layer.isVisible() )
      {
        final IDataRange<Number> range = getRangeFor( layer, axis );
        if( range != null )
        {
          ranges.add( range );
        }
      }
    }

    IDataRange<Number> mergedDataRange = ChartUtilities.mergeDataRanges( ranges.toArray( new IDataRange[ranges.size()] ) );
    if( mergedDataRange == null )
    {
      // if mergedDataRange is null, we keep the old range - if there is any
      if( axis.getNumericRange() != null )
      {
        return;
      }
      else
      {
        // otherwise, we use a default range
        mergedDataRange = new ComparableDataRange<Number>( new Number[] { 0, 1 } );
      }
    }

    // now check if axis has a preferred Adjustment
    final IAxisAdjustment adj = axis.getPreferredAdjustment();
    if( adj != null )
    {
      final double adjBefore = adj.getBefore();
      final double adjRange = adj.getRange();
      final double adjAfter = adj.getAfter();

      final double rangeMin = Math.min( adj.getMinValue().doubleValue(), mergedDataRange.getMin().doubleValue() );
      final double rangeMax = Math.max( adj.getMaxValue().doubleValue(), mergedDataRange.getMax().doubleValue() );

      // computing preferred adjustment failed if rangesize==0.0, so we set a range minimum depends on adjustment
      final double rangeSize = rangeMax == rangeMin ? 1.0 : rangeMax - rangeMin;
      final double newMin = rangeMin - rangeSize * (adjBefore / adjRange);
      final double newMax = rangeMax + rangeSize * (adjAfter / adjRange);

      axis.setNumericRange( new ComparableDataRange<Number>( new Number[] { newMin, newMax } ) );
    }
    else
    {
      axis.setNumericRange( mergedDataRange );
    }
  }

  /**
   * @return DataRange of all domain or target data available in the given layer
   */
  private IDataRange<Number> getRangeFor( final IChartLayer layer, final IAxis axis )
  {
    if( axis == layer.getCoordinateMapper().getDomainAxis() )
      return layer.getDomainRange();

    if( axis == layer.getCoordinateMapper().getTargetAxis() )
      return layer.getTargetRange( null );

    return null;
  }
}
