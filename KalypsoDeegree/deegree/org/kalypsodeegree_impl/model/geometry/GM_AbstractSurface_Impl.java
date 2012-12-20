/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 *
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always.
 *
 * If you intend to use this software in other ways than in kalypso
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree,
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.model.geometry;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.kalypsodeegree.model.geometry.GM_AbstractSurface;
import org.kalypsodeegree.model.geometry.GM_AbstractSurfacePatch;
import org.kalypsodeegree.model.geometry.GM_Exception;

/**
 * default implementation of the GM_OrientableSurface interface from package jago.model. the implementation is abstract
 * because only initialization of the spatial reference system is unique to all orientated surfaces
 * <p>
 * -----------------------------------------------------------------------
 * </p>
 * 
 * @version 05.04.2002
 * @author Andreas Poth
 */
abstract class GM_AbstractSurface_Impl<T extends GM_AbstractSurfacePatch> extends GM_AbstractGeometricPrimitive_Impl implements GM_AbstractSurface<T>
{
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 4169996004405925850L;

  /**
   * Creates a new GM_OrientableSurface_Impl object.
   * 
   * @param crs
   * @throws GM_Exception
   */
  protected GM_AbstractSurface_Impl( final String crs ) throws GM_Exception
  {
    super( crs, '+' );
  }

  /**
   * Creates a new GM_OrientableSurface_Impl object.
   * 
   * @param crs
   * @param orientation
   * @throws GM_Exception
   */
  protected GM_AbstractSurface_Impl( final String crs, final char orientation ) throws GM_Exception
  {
    super( crs, orientation );
  }

  @Override
  public int size( )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean contains( Object o )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<T> iterator( )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object[] toArray( )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T2> T2[] toArray( T2[] a )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean add( T e )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove( Object o )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsAll( Collection< ? > c )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll( Collection< ? extends T> c )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll( int index, Collection< ? extends T> c )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll( Collection< ? > c )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll( Collection< ? > c )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear( )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public T get( int index )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public T set( int index, T element )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add( int index, T element )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public T remove( int index )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int indexOf( Object o )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int lastIndexOf( Object o )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public ListIterator<T> listIterator( )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public ListIterator<T> listIterator( int index )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<T> subList( int fromIndex, int toIndex )
  {
    throw new UnsupportedOperationException();
  }
}