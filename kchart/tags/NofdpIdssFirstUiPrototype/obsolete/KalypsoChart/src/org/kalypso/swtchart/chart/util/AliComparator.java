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
package org.kalypso.swtchart.chart.util;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.collections.comparators.ComparableComparator;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/**
 * @author burtscher
 * 
 * Badly named comparator class used to compare "certain" objects 
 * which do not implement the Comparable interface;
 * right now, it only compares XMLGergorianCalendarObjects;
 * 
 * TODO: Rename
 * 
 */
public class AliComparator extends ComparableComparator
{
  /**
   * @see java.util.Comparator#compare(T, T)
   */
  public int compare( Date o1, Date o2 )
  {
    return o1.compareTo( o2 );
  }

  public int compare( Object o1, Object o2 )
  {
    if( o1 instanceof XMLGregorianCalendar )
    {
      if( o2 instanceof XMLGregorianCalendar )
        return compare( ((XMLGregorianCalendar) o1).toGregorianCalendar(), ((XMLGregorianCalendar) o2).toGregorianCalendar() );
      else
      {
        System.out.println( "Can't compare: different classes: " + o1.getClass().getName() + " " + o2.getClass().getName() );
        return 0;
      }
    }
    else if( o1 instanceof XMLGregorianCalendarImpl )
    {
      if( o2 instanceof XMLGregorianCalendarImpl )
        return compare( ((XMLGregorianCalendarImpl) o1).toGregorianCalendar(), ((XMLGregorianCalendarImpl) o2).toGregorianCalendar() );
      else
      {
        System.out.println( "Can't compare: different classes: " + o1.getClass().getName() + " " + o2.getClass().getName() );
        return 0;
      }
    }
    else
      return super.compare( o1, o2 );
  }

}
