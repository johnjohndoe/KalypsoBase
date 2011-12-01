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
package de.openali.odysseus.chart.framework.model.mapper.impl;

import de.openali.odysseus.chart.framework.model.mapper.IAxisAdjustment;

/**
 * @author burtscher1 AxisAdjustment is used when axes are adjusted automatically (e.g. maximize action); in general,
 *         auto adjustment means that axes are ranged in a way that all data from all layers can be seen; if an axis
 *         contains a preferred axis adjustment, than the auto adjustment will range the axis in a way that there can
 *         also be some space before and after the data range;
 */
public class AxisAdjustment implements IAxisAdjustment
{

  private final double m_before;

  private final double m_range;

  private final double m_after;

  private final Number m_minValue;

  private final Number m_maxValue;

  public AxisAdjustment( final int before, final int range, final int after )
  {
    this( before, range, after, 0.0, Double.MAX_VALUE );
  }

  public AxisAdjustment( final int before, final int range, final int after, final Number minValue, final Number maxValue )
  {
    m_before = before;
    m_range = range;
    m_after = after;
    m_minValue = minValue;
    m_maxValue = maxValue;
  }

  @Override
  public double getBefore( )
  {
    return m_before;
  }

  @Override
  public double getRange( )
  {
    return m_range;
  }

  @Override
  public double getAfter( )
  {
    return m_after;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.IAxisAdjustment#getMinValue()
   */
  @Override
  public Number getMinValue( )
  {
    return m_minValue;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.IAxisAdjustment#getMaxValue()
   */
  @Override
  public Number getMaxValue( )
  {
    return m_maxValue;
  }

}
