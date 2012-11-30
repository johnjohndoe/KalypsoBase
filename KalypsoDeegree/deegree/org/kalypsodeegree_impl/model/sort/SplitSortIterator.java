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
package org.kalypsodeegree_impl.model.sort;

import java.util.ConcurrentModificationException;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * @author Gernot Belger
 */
public class SplitSortIterator implements ListIterator<Object>
{
  private final SplitSort m_splitSort;

  /**
   * Index of element to be returned by subsequent call to next.
   */
  private int m_cursor;

  /**
   * Index of element returned by most recent call to next or
   * previous. Reset to -1 if this element is deleted by a call
   * to remove.
   */
  private int m_lastRet = -1;

  /**
   * The modCount value that the iterator believes that the backing
   * List should have. If this expectation is violated, the iterator
   * has detected concurrent modification.
   */
  // int m_expectedModCount = m_modCount;

  public SplitSortIterator( final SplitSort splitSort, final int index )
  {
    m_splitSort = splitSort;
    m_cursor = index;
  }

  private Object get( final int index )
  {
    return m_splitSort.get( index );
  }

  @Override
  public boolean hasNext( )
  {
    return m_cursor < m_splitSort.size();
  }

  @Override
  public Object next( )
  {
    checkForComodification();

    try
    {
      final int i = m_cursor;
      final Object next = get( i );
      m_lastRet = i;
      m_cursor = i + 1;
      return next;
    }
    catch( final IndexOutOfBoundsException e )
    {
      checkForComodification();

      throw new NoSuchElementException();
    }

  }

  @Override
  public boolean hasPrevious( )
  {
    return m_cursor != 0;
  }

  @Override
  public Object previous( )
  {
    checkForComodification();

    try
    {
      final int i = m_cursor - 1;
      final Object previous = get( i );
      m_lastRet = m_cursor = i;
      return previous;
    }
    catch( final IndexOutOfBoundsException e )
    {
      checkForComodification();
      throw new NoSuchElementException();
    }
  }

  @Override
  public int nextIndex( )
  {
    return m_cursor;
  }

  @Override
  public int previousIndex( )
  {
    return m_cursor - 1;
  }

  @Override
  public void remove( )
  {
    if( m_lastRet < 0 )
      throw new IllegalStateException();

    checkForComodification();

    try
    {
      m_splitSort.remove( m_lastRet );
      if( m_lastRet < m_cursor )
        m_cursor--;
      m_lastRet = -1;
      // m_expectedModCount = modCount;
    }
    catch( final IndexOutOfBoundsException e )
    {
      throw new ConcurrentModificationException();
    }
  }

  @Override
  public void set( final Object e )
  {
    if( m_lastRet < 0 )
      throw new IllegalStateException();

    checkForComodification();

    try
    {
      m_splitSort.set( m_lastRet, e );
      // m_expectedModCount = modCount;
    }
    catch( final IndexOutOfBoundsException ex )
    {
      throw new ConcurrentModificationException();
    }
  }

  @Override
  public void add( final Object e )
  {
    checkForComodification();

    try
    {
      final int i = m_cursor;
      m_splitSort.add( i, e );
      m_lastRet = -1;
      m_cursor = i + 1;
      // m_expectedModCount = modCount;
    }
    catch( final IndexOutOfBoundsException ex )
    {
      throw new ConcurrentModificationException();
    }
  }

  private final void checkForComodification( )
  {
//    if( m_modCount != m_m_expectedModCount )
//      throw new ConcurrentModificationException();
  }
}