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

import gnu.trove.TIntProcedure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.transformation.CRSHelper;
import org.kalypso.transformation.transformer.GeoTransformerException;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.geometry.GM_AbstractSurfacePatch;
import org.kalypsodeegree.model.geometry.GM_Boundary;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree.model.geometry.GM_PolygonPatch;
import org.kalypsodeegree.model.geometry.GM_PolyhedralSurface;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_SurfaceBoundary;
import org.kalypsodeegree.model.geometry.ISurfacePatchVisitor;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;

/**
 * @author skurzbach
 */
class GM_PolyhedralSurface_Impl<T extends GM_PolygonPatch> extends GM_AbstractSurface_Impl<T> implements GM_PolyhedralSurface<T>
{
  private final SpatialIndex m_index = new RTree();

  private final List<T> m_items;

  public GM_PolyhedralSurface_Impl( final String crs ) throws GM_Exception
  {
    this( new ArrayList<T>(), crs );
  }

  public GM_PolyhedralSurface_Impl( final List<T> items, final String crs ) throws GM_Exception
  {
    super( crs );

    m_index.init( null );
    m_items = items;

    for( int i = 0; i < items.size(); i++ )
    {
      final T polygon = items.get( i );
      insertToIndex( i, polygon );
    }
  }

  @Override
  public int getDimension( )
  {
    return 2;
  }

  @Override
  public boolean isEmpty( )
  {
    return m_items.isEmpty();
  }

  @Override
  public boolean add( final T o )
  {
    final int index = size();

    m_items.add( o );

    insertToIndex( index, o );

    return true;
  }

  @Override
  public boolean addAll( final Collection< ? extends T> c )
  {
    int index = size();

    m_items.addAll( c );

    for( final T polygon : c )
      insertToIndex( index++, polygon );

    return !c.isEmpty();
  }

  @Override
  public void clear( )
  {
    for( int i = 0; i < m_items.size(); i++ )
      removeFromIndex( i, m_items.get( i ) );

    m_items.clear();

    invalidate();
  }

  @Override
  public boolean contains( final Object o )
  {
    // FIXME: implement with index

    return m_items.contains( o );
  }

  @Override
  public boolean containsAll( final Collection< ? > c )
  {
    // FIXME: implement with index

    return m_items.containsAll( c );
  }

  @Override
  public T get( final int index )
  {
    return m_items.get( index );
  }

  @Override
  public int indexOf( final Object o )
  {
    // FIXME: implement with index

    return m_items.indexOf( o );
  }

  /**
   * IMPORTANT: it is forbidden to change this surface with this iterator.
   */
  @Override
  public Iterator<T> iterator( )
  {
    return Collections.unmodifiableList( m_items ).iterator();
  }

  @Override
  public int lastIndexOf( final Object o )
  {
    // FIXME: implement with index

    return m_items.lastIndexOf( o );
  }

  /**
   * IMPORTANT: it is forbidden to change this surface with this iterator.
   */
  @Override
  public ListIterator<T> listIterator( )
  {
    return Collections.unmodifiableList( m_items ).listIterator();
  }

  /**
   * IMPORTANT: it is forbidden to change this surface with this iterator.
   */
  @Override
  public ListIterator<T> listIterator( final int index )
  {
    return Collections.unmodifiableList( m_items ).listIterator( index );
  }

  @Override
  public boolean remove( final Object o )
  {
    final int index = indexOf( o );

    final T removed = m_items.remove( index );

    if( removed != null )
      removeFromIndex( index, removed );

    return removed != null;
  }

  @Override
  public T remove( final int index )
  {
    final T polygon = get( index );

    removeFromIndex( index, polygon );

    return m_items.remove( index );
  }

  @Override
  public boolean removeAll( final Collection< ? > c )
  {
    boolean hasRemoved = false;
    for( final Object object : c )
      hasRemoved |= remove( object );

    return hasRemoved;
  }

  @Override
  public T set( final int index, final T element )
  {
    final T polygon = get( index );
    removeFromIndex( index, polygon );

    m_items.set( index, element );

    insertToIndex( index, element );
    return polygon;
  }

  @Override
  public int size( )
  {
    return m_items.size();
  }

  @Override
  public List<T> subList( final int fromIndex, final int toIndex )
  {
    try
    {
      return new GM_PolyhedralSurface_Impl<>( m_items.subList( fromIndex, toIndex ), getCoordinateSystem() );
    }
    catch( final GM_Exception e )
    {
      // should never happen
      throw new IllegalStateException( e );
    }
  }

  @Override
  public Object[] toArray( )
  {
    return m_items.toArray();
  }

  @Override
  public <T2> T2[] toArray( final T2[] a )
  {
    return m_items.toArray( a );
  }

  @Override
  public Object clone( ) throws CloneNotSupportedException
  {
    try
    {
      final GM_PolyhedralSurface<T> clone = createCloneInstance();
      for( final T polygon : this )
        clone.add( (T)polygon.clone() );
      return clone;
    }
    catch( final GM_Exception e )
    {
      e.printStackTrace();

      throw new CloneNotSupportedException( e.getLocalizedMessage() );
    }
  }

  protected <S extends T> GM_PolyhedralSurface<T> createCloneInstance( ) throws GM_Exception
  {
    return new GM_PolyhedralSurface_Impl<>( getCoordinateSystem() );
  }

  @Override
  public GM_Envelope getEnvelope( )
  {
    final Rectangle bounds = m_index.getBounds();
    return GeometryUtilities.toEnvelope( bounds, getCoordinateSystem() );
  }

  @Override
  protected GM_Envelope calculateEnvelope( )
  {
    // We overwrite getEnvelope, so this should never be called
    throw new UnsupportedOperationException();
  }

  @Override
  protected GM_Boundary calculateBoundary( )
  {
    // TODO: implement, what is the boundary this?
    return GM_Constants.EMPTY_BOUNDARY;
  }

  @Override
  protected GM_Point calculateCentroid( )
  {
    // TODO: implement, what is the centroid of this?
    // -> should be the mean of all centroids of its children
    return GM_Constants.EMPTY_CENTROID;
  }

  @Override
  public int getCoordinateDimension( )
  {
    return CRSHelper.getDimension( getCoordinateSystem() );
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    if( adapter == GM_AbstractSurfacePatch[].class || adapter == GM_PolygonPatch[].class )
    {
      return m_items.toArray( new GM_PolygonPatch[m_items.size()] );
    }

    // for points: get centroids of the polygons
    if( adapter == GM_Point[].class )
    {
      final List<GM_Point> pointList = new LinkedList<>();

      final T[] polygons = (T[])m_items.toArray( new GM_PolygonPatch[m_items.size()] );
      for( final T polygon : polygons )
      {
        pointList.add( polygon.getCentroid() );
      }
      return pointList.toArray( new GM_Point[pointList.size()] );
    }

    // NO: behaviour assymmetric to GM_Surface
    if( adapter == GM_Curve[].class )
    {
      try
      {
        final GM_Curve[] curves = new GM_Curve[m_items.size()];
        for( int i = 0; i < curves.length; i++ )
        {
          final GM_Position[] polygon = m_items.get( i ).getExteriorRing();
          curves[i] = GeometryFactory.createGM_Curve( polygon, getCoordinateSystem() );
        }

        return curves;
      }
      catch( final GM_Exception e )
      {
        final IStatus statusFromThrowable = StatusUtilities.statusFromThrowable( e );
        KalypsoDeegreePlugin.getDefault().getLog().log( statusFromThrowable );
      }
    }

    // NO: behaviour assymmetric to GM_Surface
    if( adapter == GM_Polygon[].class )
    {
      try
      {
        final GM_Polygon[] surfaces = new GM_Polygon[m_items.size()];
        for( int i = 0; i < surfaces.length; i++ )
          surfaces[i] = GeometryFactory.createGM_Surface( m_items.get( i ) );

        return surfaces;
      }
      catch( final GM_Exception e )
      {
        final IStatus statusFromThrowable = StatusUtilities.statusFromThrowable( e );
        KalypsoDeegreePlugin.getDefault().getLog().log( statusFromThrowable );
      }
    }

    return super.getAdapter( adapter );
  }

  private void insertToIndex( final int index, final T polygon )
  {
    final Rectangle bounds = GeometryUtilities.toRectangle( polygon.getEnvelope() );
    m_index.add( bounds, index );
  }

  private void removeFromIndex( final int index, final T polygon )
  {
    final Rectangle bounds = GeometryUtilities.toRectangle( polygon.getEnvelope() );
    m_index.delete( bounds, index );
  }

  @Override
  public GM_SurfaceBoundary getSurfaceBoundary( )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public double getArea( )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public double getPerimeter( )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void acceptSurfacePatches( final GM_Envelope envToVisit, final ISurfacePatchVisitor<T> surfacePatchVisitor, final IProgressMonitor pm ) throws CoreException
  {
    final SubMonitor monitor = SubMonitor.convert( pm );
    monitor.beginTask( StringUtils.EMPTY, IProgressMonitor.UNKNOWN );

    final TIntProcedure ip = new TIntProcedure()
    {
      @Override
      public boolean execute( final int value )
      {
        final T t = get( value );
        surfacePatchVisitor.visit( t );
        ProgressUtilities.worked( monitor, 1 );
        return true;
      }
    };

    try
    {
      final Rectangle searchRect = GeometryUtilities.toRectangle( envToVisit );
      m_index.intersects( searchRect, ip );
    }
    catch( final OperationCanceledException e )
    {
      // ... which is catched here an we re-throw a true CoreException(Cancel)
      throw new CoreException( Status.CANCEL_STATUS );
    }
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public GM_Object transform( final String targetCRS ) throws GeoTransformerException
  {
    try
    {
      /* If the target is the same coordinate system, do not transform. */
      final String sourceCRS = getCoordinateSystem();
      if( sourceCRS == null || sourceCRS.equalsIgnoreCase( targetCRS ) )
        return this;

      final int cnt = size();
      final T[] polygons = (T[])new GM_PolygonPatch[cnt];

      for( int i = 0; i < cnt; i++ )
        polygons[i] = (T)get( i ).transform( targetCRS );

      return GeometryFactory.createGM_PolyhedralSurface( polygons, targetCRS );
    }
    catch( final GM_Exception e )
    {
      throw new GeoTransformerException( e );
    }
  }

  protected SpatialIndex getIndex( )
  {
    return m_index;
  }

  @SuppressWarnings( "rawtypes" )
  @Override
  public boolean equals( final Object that )
  {
    if( that == this )
      return true;

    if( that == null || !(that instanceof GM_PolyhedralSurface_Impl) )
      return false;

    if( getCoordinateSystem() != null )
    {
      if( !getCoordinateSystem().equals( ((GM_Object)that).getCoordinateSystem() ) )
        return false;
    }
    else
    {
      if( ((GM_Object)that).getCoordinateSystem() != null )
      {
        return false;
      }
    }

    return m_items.equals( ((GM_PolyhedralSurface_Impl)that).m_items );
  }
}