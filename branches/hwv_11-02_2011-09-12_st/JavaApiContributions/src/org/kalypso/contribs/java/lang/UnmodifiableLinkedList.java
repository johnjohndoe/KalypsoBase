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

  @Override
  public int size( )
  {
    return m_list.size();
  }

  @Override
  public boolean equals( Object o )
  {
    return m_list.equals( o );
  }

  @Override
  public int hashCode( )
  {
    return m_list.hashCode();
  }

  @Override
  public E get( int index )
  {
    return m_list.get( index );
  }

  @Override
  public E set( int index, E element )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add( int index, E element )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public E remove( int index )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int indexOf( Object o )
  {
    return m_list.indexOf( o );
  }

  @Override
  public int lastIndexOf( Object o )
  {
    return m_list.lastIndexOf( o );
  }

  @Override
  public boolean addAll( int index, Collection<? extends E> c )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public ListIterator<E> listIterator( )
  {
    return listIterator( 0 );
  }

  @Override
  public ListIterator<E> listIterator( final int index )
  {
    return new ListIterator<E>()
    {
      ListIterator<? extends E> i = m_list.listIterator( index );

      @Override
      public boolean hasNext( )
      {
        return i.hasNext();
      }

      @Override
      public E next( )
      {
        return i.next();
      }

      @Override
      public boolean hasPrevious( )
      {
        return i.hasPrevious();
      }

      @Override
      public E previous( )
      {
        return i.previous();
      }

      @Override
      public int nextIndex( )
      {
        return i.nextIndex();
      }

      @Override
      public int previousIndex( )
      {
        return i.previousIndex();
      }

      @Override
      public void remove( )
      {
        throw new UnsupportedOperationException();
      }

      @Override
      public void set( E o )
      {
        throw new UnsupportedOperationException();
      }

      @Override
      public void add( E o )
      {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public List<E> subList( int fromIndex, int toIndex )
  {
    return Collections.unmodifiableList(  m_list.subList(
        fromIndex, toIndex ) );
  }

  @Override
  public boolean addAll( Collection<? extends E> c )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear( )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object clone( )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<E> iterator( )
  {
    return listIterator();
  }

  @Override
  public E remove( )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove( Object o )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll( Collection<?> c )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public E removeFirst( )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public E removeLast( )
  {
    throw new UnsupportedOperationException();
  }

  @Override
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

  @Override
  public boolean contains( Object o )
  {
    return m_list.contains( o );
  }

  @Override
  public boolean containsAll( Collection<?> c )
  {
    return m_list.containsAll( c );
  }

  @Override
  public E element( )
  {
    return m_list.element();
  }

  @Override
  public E getFirst( )
  {
    return m_list.getFirst();
  }

  @Override
  public E getLast( )
  {
    return m_list.getLast();
  }

  @Override
  public boolean isEmpty( )
  {
    return m_list.isEmpty();
  }

  @Override
  public E peek( )
  {
    return m_list.peek();
  }

  @Override
  public Object[] toArray( )
  {
    return m_list.toArray();
  }

  @Override
  public <T> T[] toArray( T[] a )
  {
    return m_list.toArray( a );
  }

  @Override
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
