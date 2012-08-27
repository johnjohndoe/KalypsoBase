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

import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * @author Gernot Belger
 */
public class SplitSortIterator implements ListIterator<Object>
{
  private final SplitSort m_splitSort;

  private int m_index;

  public SplitSortIterator( final SplitSort splitSort, final int index )
  {
    m_splitSort = splitSort;
    m_index = index;
  }

  @Override
  public boolean hasNext( )
  {
    return m_index < m_splitSort.size();
  }

  @Override
  public Object next( )
  {
    try
    {
      return m_splitSort.get( m_index++ );
    }
    catch( final IndexOutOfBoundsException e )
    {
      throw new NoSuchElementException();
    }
  }

  @Override
  public boolean hasPrevious( )
  {
    return m_index > 0;
  }

  @Override
  public Object previous( )
  {
    try
    {
      return m_splitSort.get( --m_index );
    }
    catch( final IndexOutOfBoundsException e )
    {
      throw new NoSuchElementException();
    }
  }

  @Override
  public int nextIndex( )
  {
    return m_index;
  }

  @Override
  public int previousIndex( )
  {
    return m_index - 1;
  }

  @Override
  public void remove( )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void set( final Object e )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add( final Object e )
  {
    throw new UnsupportedOperationException();
  }
}