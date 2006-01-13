/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and
 
 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de
 
 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.contribs.java.lang;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class UnmodifiableLinkedList<E> extends LinkedList<E>
{
  private static final long serialVersionUID = 2405730477279453036L;

  protected final LinkedList<? extends E> m_list;

  public UnmodifiableLinkedList( final LinkedList<? extends E> list )
  {
    m_list = list;
  }

  public int size( )
  {
    return m_list.size();
  }

  public boolean equals( Object o )
  {
    return m_list.equals( o );
  }

  public int hashCode( )
  {
    return m_list.hashCode();
  }

  public E get( int index )
  {
    return m_list.get( index );
  }

  public E set( int index, E element )
  {
    throw new UnsupportedOperationException();
  }

  public void add( int index, E element )
  {
    throw new UnsupportedOperationException();
  }

  public E remove( int index )
  {
    throw new UnsupportedOperationException();
  }

  public int indexOf( Object o )
  {
    return m_list.indexOf( o );
  }

  public int lastIndexOf( Object o )
  {
    return m_list.lastIndexOf( o );
  }

  public boolean addAll( int index, Collection<? extends E> c )
  {
    throw new UnsupportedOperationException();
  }

  public ListIterator<E> listIterator( )
  {
    return listIterator( 0 );
  }

  public ListIterator<E> listIterator( final int index )
  {
    return new ListIterator<E>()
    {
      ListIterator<? extends E> i = m_list.listIterator( index );

      public boolean hasNext( )
      {
        return i.hasNext();
      }

      public E next( )
      {
        return i.next();
      }

      public boolean hasPrevious( )
      {
        return i.hasPrevious();
      }

      public E previous( )
      {
        return i.previous();
      }

      public int nextIndex( )
      {
        return i.nextIndex();
      }

      public int previousIndex( )
      {
        return i.previousIndex();
      }

      public void remove( )
      {
        throw new UnsupportedOperationException();
      }

      public void set( E o )
      {
        throw new UnsupportedOperationException();
      }

      public void add( E o )
      {
        throw new UnsupportedOperationException();
      }
    };
  }

  public List<E> subList( int fromIndex, int toIndex )
  {
    return Collections.unmodifiableList(  m_list.subList(
        fromIndex, toIndex ) );
  }

  public boolean addAll( Collection<? extends E> c )
  {
    throw new UnsupportedOperationException();
  }

  public void clear( )
  {
    throw new UnsupportedOperationException();
  }

  public Object clone( )
  {
    throw new UnsupportedOperationException();
  }

  public Iterator<E> iterator( )
  {
    return listIterator();
  }

  public E remove( )
  {
    throw new UnsupportedOperationException();
  }

  public boolean remove( Object o )
  {
    throw new UnsupportedOperationException();
  }

  public boolean removeAll( Collection<?> c )
  {
    throw new UnsupportedOperationException();
  }

  public E removeFirst( )
  {
    throw new UnsupportedOperationException();
  }

  public E removeLast( )
  {
    throw new UnsupportedOperationException();
  }

  public boolean retainAll( Collection<?> c )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addFirst( E o )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addLast( E o )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public E poll( )
  {
    throw new UnsupportedOperationException();
  }

  public boolean contains( Object o )
  {
    return m_list.contains( o );
  }

  public boolean containsAll( Collection<?> c )
  {
    return m_list.containsAll( c );
  }

  public E element( )
  {
    return m_list.element();
  }

  public E getFirst( )
  {
    return m_list.getFirst();
  }

  public E getLast( )
  {
    return m_list.getLast();
  }

  public boolean isEmpty( )
  {
    return m_list.isEmpty();
  }

  public E peek( )
  {
    return m_list.peek();
  }

  public Object[] toArray( )
  {
    return m_list.toArray();
  }

  public <T> T[] toArray( T[] a )
  {
    return m_list.toArray( a );
  }

  public String toString( )
  {
    return m_list.toString();
  }

  @Override
  public boolean add( E o )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean offer( E o )
  {
    throw new UnsupportedOperationException();
  }
}
