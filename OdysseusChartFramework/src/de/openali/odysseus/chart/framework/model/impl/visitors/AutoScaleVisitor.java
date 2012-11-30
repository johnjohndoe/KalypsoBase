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

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.impl.IAxisVisitorBehavior;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisAdjustment;
import de.openali.odysseus.chart.framework.model.mapper.registry.IAxisVisitor;

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
    final IAxisVisitorBehavior visitorBehavior = axis.getAxisVisitorBehavior();
    if( visitorBehavior != null && !visitorBehavior.isAutoscaleEnabled() )
      return;

    final IDataRange<Number> mergedDataRange = getChartRanges( axis );
    if( mergedDataRange == null )
      return;

    // now check if axis has a preferred Adjustment
    final IAxisAdjustment adj = axis.getPreferredAdjustment();

    final IDataRange<Number> adjustedRange = adjustRange( adj, mergedDataRange );
    axis.setNumericRange( adjustedRange );
  }

  private IDataRange<Number> adjustRange( final IAxisAdjustment adj, final IDataRange<Number> mergedDataRange )
  {
    if( adj == null || mergedDataRange == null || mergedDataRange.getMax() == null || mergedDataRange.getMin() == null )
      return mergedDataRange;

    // prozentualer offset vor und hinter dem Datenbereich beim maximieren
    // before+range+after=100
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

    return DataRange.createFromComparable( (Number)newMin, (Number)newMax );
  }

  private IDataRange<Number> getChartRanges( final IAxis axis )
  {
    final AxisRangeVisitor visitor = new AxisRangeVisitor( axis );

    m_model.accept( visitor );

    return visitor.getRange();
  }
}