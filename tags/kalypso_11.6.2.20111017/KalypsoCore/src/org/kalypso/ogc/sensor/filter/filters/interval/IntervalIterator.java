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
package org.kalypso.ogc.sensor.filter.filters.interval;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.lang.NotImplementedException;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

/**
 * @author Gernot Belger
 */
public class IntervalIterator implements Iterator<Interval>, Iterable<Interval>
{
  private final DateTime m_end;

  private final Period m_step;

  private Interval m_next;

  public IntervalIterator( final DateTime start, final DateTime end, final Period step )
  {
    m_end = end;
    m_step = step;

    final DateTime nextEnd = start.plus( step );

    // TODO check: is that what we want? If start/end is smaller than the step; the iteration is empty
    if( nextEnd.isAfter( end ) )
      m_next = null;
    else
      m_next = new Interval( start, nextEnd );
  }

  @Override
  public Iterator<Interval> iterator( )
  {
    return this;
  }

  @Override
  public void remove( )
  {
    throw new NotImplementedException();
  }

  /**
   * @see java.util.Iterator#hasNext()
   */
  @Override
  public boolean hasNext( )
  {
    return m_next != null;
  }

  /**
   * @see java.util.Iterator#next()
   */
  @Override
  public Interval next( )
  {
    return updateNext();
  }

  private Interval updateNext( )
  {
    if( m_next == null )
      throw new NoSuchElementException();

    final Interval oldNext = m_next;

    final DateTime end = m_next.getEnd();
    final DateTime nextEnd = end.plus( m_step );

    if( nextEnd.isAfter( m_end ) )
      m_next = null;
    else
      m_next = new Interval( end, nextEnd );

    return oldNext;
  }
}