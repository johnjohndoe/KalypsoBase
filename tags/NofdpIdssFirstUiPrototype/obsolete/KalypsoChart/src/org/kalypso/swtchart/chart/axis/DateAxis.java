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
package org.kalypso.swtchart.chart.axis;

import java.util.Comparator;
import java.util.Date;

import org.eclipse.swt.graphics.Point;
import org.kalypso.swtchart.chart.axis.IAxisConstants.DIRECTION;
import org.kalypso.swtchart.chart.axis.IAxisConstants.POSITION;
import org.kalypso.swtchart.chart.axis.IAxisConstants.PROPERTY;
import org.kalypso.swtchart.chart.axis.component.IAxisComponent;

/**
 * @author schlienger
 * @author burtscher
 */
public class DateAxis extends AbstractAxis<Date>
{
  public DateAxis( String id, String label, PROPERTY prop, POSITION pos, DIRECTION dir )
  {
    super( id, label, prop, pos, dir, Date.class );
  }

  public DateAxis( String id, String label, PROPERTY prop, POSITION pos, Comparator<Date> comp, DIRECTION dir )
  {
    super( id, label, prop, pos, dir, comp, Date.class );
  }

  public double logicalToNormalized( final Date value )
  {
    // r should not be 0 here (see AbstractAxis)
    final long r = getTo().getTime() - getFrom().getTime();

    // Die rechte Seite muss unbedingt nach double gecastet werden, da sonst auf die Werte 0 oder 1 gerundet wird.
    final double norm = ((double) (value.getTime() - getFrom().getTime())) / r;
    // System.out.println("Normalization: "+DateFormat.getInstance().format(value)+" => "+norm+"; from:
    // "+getFrom().getTime()+ " to: "+getTo().getTime());
    return norm;
  }

  public Date normalizedToLogical( final double value )
  {
    final double r = getTo().getTime() - getFrom().getTime();

    final long logical = (long) (value * r + getFrom().getTime());

    return new Date( logical );
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxis#logicalToScreen(T)
   */
  public int logicalToScreen( final Date value )
  {
    if( m_registry == null )
      return 0;

    final IAxisComponent comp = m_registry.getComponent( this );
    if( comp == null )
      return 0;

    return comp.normalizedToScreen( logicalToNormalized( value ) );
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxis#screenToLogical(int)
   */
  public Date screenToLogical( final int value )
  {
    if( m_registry == null )
      return null;

    final IAxisComponent comp = m_registry.getComponent( this );
    if( comp == null )
      return null;

    return normalizedToLogical( comp.screenToNormalized( value ) );
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxis#logicalToScreenInterval(T, T, double)
   */
  public Point logicalToScreenInterval( Date value, Date fixedPoint, double intervalSize )
  {
    // TODO Auto-generated method stub
    return null;
  }

}
