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

import org.kalypso.commons.exception.CancelVisitorException;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor2;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.util.ChartUtilities;

/**
 * @author Gernot Belger
 */
public class AxisRangeVisitor implements IChartLayerVisitor2
{
  private final List<IDataRange< ? >> m_ranges = new ArrayList<>();

  private final IAxis m_axis;

  public AxisRangeVisitor( final IAxis axis )
  {
    m_axis = axis;
  }

  @Override
  public boolean getVisitDirection( )
  {
    return true;
  }

  @Override
  public boolean visit( final IChartLayer layer ) throws CancelVisitorException
  {
    if( !layer.isVisible() )
      return false;

    if( layer.isAutoScale() && isOnAxis( layer ) )
    {
      final IDataRange< ? > range = getRangeFor( layer );
      if( range != null )
      {
        m_ranges.add( range );
      }
    }

    return true;
  }

  private boolean isOnAxis( final IChartLayer layer )
  {
    final ICoordinateMapper coordinateMapper = layer.getCoordinateMapper();
    if( coordinateMapper == null )
      return false;

    if( coordinateMapper.getDomainAxis() == m_axis )
      return true;

    if( coordinateMapper.getTargetAxis() == m_axis )
      return true;

    return false;
  }

  /**
   * @return DataRange of all domain or target data available in the given layer
   */
  private IDataRange< ? > getRangeFor( final IChartLayer layer )
  {
    if( m_axis == layer.getCoordinateMapper().getDomainAxis() )
      return layer.getDomainRange();

    if( m_axis == layer.getCoordinateMapper().getTargetAxis() )
      return layer.getTargetRange( null );

    return null;
  }

  public IDataRange<Number> getRange( )
  {
    final IDataRange< ? >[] ranges = m_ranges.toArray( new IDataRange< ? >[m_ranges.size()] );
    return mergeChartRanges( ranges );
  }

  private IDataRange<Number> mergeChartRanges( final IDataRange< ? >[] chartRanges )
  {
    // final IDataRange<Number>[] numericRanges = toNumeric( axis, chartRanges );

    final IDataRange<Number> mergedDataRange = ChartUtilities.mergeDataRanges( chartRanges );
    if( mergedDataRange != null )
      return mergedDataRange;

    // if mergedDataRange is null, we keep the old range - if there is any
    if( m_axis.getNumericRange() != null )
      return m_axis.getNumericRange();

    return null;// DataRange.createFromComparable( (Number) 0, (Number) 1 );
  }
}