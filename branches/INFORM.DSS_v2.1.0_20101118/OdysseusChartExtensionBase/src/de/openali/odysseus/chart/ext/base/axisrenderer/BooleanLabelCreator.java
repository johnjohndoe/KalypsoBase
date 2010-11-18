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
package de.openali.odysseus.chart.ext.base.axisrenderer;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;

/**
 * @author alibu
 */
public class BooleanLabelCreator implements ILabelCreator
{

  @Override
  public ALIGNMENT getLabelPosition( )
  {
    if( m_alignment == null )
      m_alignment = ALIGNMENT.TICK_CENTERED;
    return m_alignment;
  }

  private ALIGNMENT m_alignment = null;

  public BooleanLabelCreator( )
  {
  }

  /**
   * @see org.kalypso.chart.ext.test.axisrenderer.ILabelCreator#getLabel(java.lang.Number,
   *      org.kalypso.chart.framework.model.data.IDataRange)
   */
  @Override
  public String getLabel( final Number[] ticks, final int i, final IDataRange<Number> range )
  {
    if( ticks == null )
      return "";

    if( ticks[i].intValue() == 0 )
      return "zu";
    else if( ticks[i].intValue() == 1 )
      return "auf";

    return String.format( "%d", ticks[i].intValue() );
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.axisrenderer.ILabelCreator#getLabel(java.lang.Number,
   *      de.openali.odysseus.chart.framework.model.data.IDataRange)
   */
  @Override
  public String getLabel( final Number value, final IDataRange<Number> range )
  {
    return getLabel( new Number[] { value }, 0, range );
  }

}
