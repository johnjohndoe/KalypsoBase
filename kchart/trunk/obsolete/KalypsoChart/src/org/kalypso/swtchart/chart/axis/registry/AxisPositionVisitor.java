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
package org.kalypso.swtchart.chart.axis.registry;

import java.util.ArrayList;
import java.util.List;

import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.IAxisConstants;
import org.kalypso.swtchart.chart.axis.IAxisConstants.POSITION;

/**
 * @author schlienger
 * @author burtscher
 */
public class AxisPositionVisitor implements IAxisVisitor
{
  private final List<IAxis> m_axes = new ArrayList<IAxis>();

  private final POSITION m_pos;

  public AxisPositionVisitor( IAxisConstants.POSITION pos )
  {
    m_pos = pos;
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxisVisitor#visitAxis(org.kalypso.swtchart.axis.IAxis)
   */
  public void visitAxis( final IAxis axis )
  {
    if( axis.getPosition() == m_pos )
      m_axes.add( axis );
  }

  public IAxis[] getAxes( )
  {
    return m_axes.toArray( new IAxis[m_axes.size()] );
  }
}
