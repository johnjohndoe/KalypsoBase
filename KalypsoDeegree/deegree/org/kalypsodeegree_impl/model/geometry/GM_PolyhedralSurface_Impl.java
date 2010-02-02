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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.NotImplementedException;
import org.deegree.crs.transformations.CRSTransformation;
import org.deegree.model.crs.UnknownCRSException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.transformation.CRSHelper;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.geometry.GM_Boundary;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree.model.geometry.GM_SurfaceBoundary;
import org.kalypsodeegree.model.geometry.GM_SurfacePatch;
import org.kalypsodeegree.model.geometry.ISurfacePatchVisitor;
import org.kalypsodeegree_impl.tools.Debug;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.quadtree.Quadtree;

/**
 * @author skurzbach
 */
public class GM_PolyhedralSurface_Impl<T extends GM_Polygon> extends GM_OrientableSurface_Impl implements GM_Surface<T>
{
  protected SpatialIndex m_index = new Quadtree();

  private final List<T> m_items;

  // Optimization: we do not always want to recalculate the complete envelope, so we manage it ourselfs
  private GM_Envelope m_envelope = null;

  public GM_PolyhedralSurface_Impl( final String crs ) throws GM_Exception
  {
    this( new ArrayList<T>(), crs );
  }

  public GM_PolyhedralSurface_Impl( final List<T> items, final String crs ) throws GM_Exception
  {
    super( crs );

    m_items = items;

    for( final T polygon : items )
      insertToIndex( polygon );
  }

  /**
   * @see org.kalypsodeegree.model.geometry.GM_Object#getDimension()
   */
  public int getDimension( )
  {
    return 2;
  }

  /**
   * @see org.kalypsodeegree.model.geometry.GM_Object#isEmpty()
   */
  @Override
  public boolean isEmpty( )
  {
    return m_items.isEmpty();
  }

  /**
   * @see java.util.List#add(java.lang.Object)
   */
  public boolean add( final T o )
  {
    m_items.add( o );

    insertToIndex( o );

    return true;
  }

  /**
   * @see java.util.List#add(int, java.lang.Object)
   */
  public void add( final int index, final T element )
  {
    m_items.add( index, element );
    insertToIndex( element );
  }

  /**
   * @see java.util.List#addAll(java.util.Collection)
   */
  public boolean addAll( final Collection< ? extends T> c )
  {
    for( final T polygon : c )
      add( polygon );

    return !c.isEmpty();
  }

  /**
   * @see java.util.List#addAll(int, java.util.Collection)
   */
  public boolean addAll( final int index, final Collection< ? extends T> c )
  {
    m_items.addAll( index, c );

    for( final T polygon : c )
      insertToIndex( polygon );

    return !c.isEmpty();
  }

  /**
   * @see java.util.List#clear()
   */
  public void clear( )
  {
    m_items.clear();

    m_index = new Quadtree();

    invalidate();
  }

  /**
   * @see java.util.List#contains(java.lang.Object)
   */
  public boolean contains( final Object o )
  {
    return m_items.contains( o );
  }

  /**
   * @see java.util.List#containsAll(java.util.Collection)
   */
  public boolean containsAll( final Collection< ? > c )
  {
    return m_items.containsAll( c );
  }

  /**
   * @see java.util.List#get(int)
   */
  public T get( final int index )
  {
    return m_items.get( index );
  }

  /**
   * @see java.util.List#indexOf(java.lang.Object)
   */
  public int indexOf( final Object o )
  {
    return m_items.indexOf( o );
  }

  /**
   * TODO: if this surface is changed via this iterator, the index does not gets updated<br>
   * 
   * @see java.util.List#iterator()
   */
  public Iterator<T> iterator( )
  {
    // TODO + CHECK: see TODO above; beter do this
// return Collections.unmodifiableList( m_items ).iterator();
    return m_items.iterator();
  }

  /**
   * @see java.util.List#lastIndexOf(java.lang.Object)
   */
  public int lastIndexOf( final Object o )
  {
    // TODO: see iterator()
    return m_items.lastIndexOf( o );
  }

  /**
   * TODO: if this surface is changed via this iterator, the index does not gets updated
   * 
   * @see java.util.List#listIterator()
   */
  public ListIterator<T> listIterator( )
  {
    // TODO: see iterator()
    return m_items.listIterator();
  }

  /**
   * TODO: if this surface is changed via this iterator, the index does not gets updated
   * 
   * @see java.util.List#listIterator(int)
   */
  public ListIterator<T> listIterator( final int index )
  {
    // TODO: see iterator()
    return m_items.listIterator( index );
  }

  /**
   * @see java.util.List#remove(java.lang.Object)
   */
  public boolean remove( final Object o )
  {
    removeFromIndex( o );

    return m_items.remove( o );
  }

  /**
   * @see java.util.List#remove(int)
   */
  public T remove( final int index )
  {
    final T polygon = get( index );
    removeFromIndex( polygon );

    return m_items.remove( index );
  }

  /**
   * @see java.util.List#removeAll(java.util.Collection)
   */
  public boolean removeAll( final Collection< ? > c )
  {
    boolean hasRemoved = false;
    for( final Object object : c )
      hasRemoved |= remove( object );

    return hasRemoved;
  }

  /**
   * @see java.util.List#retainAll(java.util.Collection)
   */
  public boolean retainAll( final Collection< ? > c )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see java.util.List#set(int, java.lang.Object)
   */
  public T set( final int index, final T element )
  {
    final T polygon = get( index );
    removeFromIndex( polygon );

    m_items.set( index, element );

    insertToIndex( element );
    return polygon;
  }

  /**
   * @see java.util.List#size()
   */
  public int size( )
  {
    return m_items.size();
  }

  /**
   * @see java.util.List#subList(int, int)
   */
  public List<T> subList( final int fromIndex, final int toIndex )
  {
    try
    {
      return new GM_PolyhedralSurface_Impl<T>( m_items.subList( fromIndex, toIndex ), getCoordinateSystem() );
    }
    catch( final GM_Exception e )
    {
      // should never happen
      throw new IllegalStateException( e );
    }
  }

  /**
   * @see java.util.List#toArray()
   */
  public Object[] toArray( )
  {
    return m_items.toArray();
  }

  /**
   * @see java.util.List#toArray(T[])
   */
  public <otherT> otherT[] toArray( final otherT[] a )
  {
    return m_items.toArray( a );
  }

  /**
   * @see java.lang.Object#clone()
   */
  @SuppressWarnings("unchecked")
  @Override
  public Object clone( ) throws CloneNotSupportedException
  {
    try
    {
      final GM_PolyhedralSurface_Impl<T> clone = new GM_PolyhedralSurface_Impl<T>( getCoordinateSystem() );

      for( final T polygon : this )
        clone.add( (T) polygon.clone() );

      return clone;
    }
    catch( final GM_Exception e )
    {
      e.printStackTrace();

      throw new CloneNotSupportedException( e.getLocalizedMessage() );
    }
  }

  /**
   * @see org.kalypsodeegree_impl.model.geometry.GM_Object_Impl#getEnvelope()
   */
  @Override
  public GM_Envelope getEnvelope( )
  {
    if( m_envelope == null )
      m_envelope = JTSAdapter.wrap( recalcEnvelope( m_items ), getCoordinateSystem() );

    return m_envelope;
  }

  /**
   * @see org.kalypsodeegree_impl.model.geometry.GM_Object_Impl#calculateEnvelope()
   */
  @Override
  protected GM_Envelope calculateEnvelope( )
  {
    // We overwrite getEnvelope, so this should never be called
    throw new NotImplementedException();
  }

  /**
   * @see org.kalypsodeegree_impl.model.geometry.GM_Object_Impl#calculateBoundary()
   */
  @Override
  protected GM_Boundary calculateBoundary( )
  {
    // TODO: implement, what is the boundary this?
    return GM_Object_Impl.EMPTY_BOUNDARY;
  }

  /**
   * @see org.kalypsodeegree_impl.model.geometry.GM_Object_Impl#calculateCentroid()
   */
  @Override
  protected GM_Point calculateCentroid( )
  {
    // TODO: implement, what is the centroid of this?
    return EMPTY_CENTROID;
  }

  private static <T extends GM_Polygon> Envelope recalcEnvelope( final List<T> items )
  {
    if( items.isEmpty() )
      return new Envelope();

    Envelope bbox = null;
    for( final T gmPolygon : items )
    {
      final GM_Envelope env = gmPolygon.getEnvelope();
      final Envelope envelope = JTSAdapter.export( env );
      if( envelope.isNull() )
        continue;

      if( bbox == null )
        bbox = envelope;
      else
        bbox.expandToInclude( envelope );
    }

    if( bbox == null )
      return new Envelope();

    return bbox;
  }

  /**
   * @see org.kalypsodeegree.model.geometry.GM_Object#getCoordinateDimension()
   */
  public int getCoordinateDimension( )
  {
    try
    {
      return CRSHelper.getDimension( getCoordinateSystem() );
    }
    catch( final UnknownCRSException e )
    {
      // TODO How to deal with this error? What to return?
      e.printStackTrace();
      return 0;
    }
  }

  /**
   * @see org.kalypsodeegree_impl.model.geometry.GM_Primitive_Impl#getAdapter(java.lang.Class)
   */
  @SuppressWarnings("unchecked")
  @Override
  public Object getAdapter( final Class adapter )
  {
    if( adapter == GM_SurfacePatch[].class || adapter == GM_Polygon[].class )
    {
      return m_items.toArray( new GM_Polygon[m_items.size()] );
    }

    // for points: get centroids of the polygons
    if( adapter == GM_Point[].class )
    {
      final List<GM_Point> pointList = new LinkedList<GM_Point>();

      final T[] polygons = (T[]) m_items.toArray( new GM_Polygon[m_items.size()] );
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
    if( adapter == GM_Surface[].class )
    {
      try
      {
        final GM_Surface[] surfaces = new GM_Surface[m_items.size()];
        for( int i = 0; i < surfaces.length; i++ )
        {
          surfaces[i] = GeometryFactory.createGM_Surface( m_items.get( i ) );
        }

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

  private void insertToIndex( final T polygon )
  {
    final Envelope env = JTSAdapter.export( polygon.getEnvelope() );
    m_index.insert( env, polygon );

    if( isValid() )
    {
      final GM_Envelope polygonEnv = polygon.getEnvelope();

      if( m_envelope == null )
        m_envelope = polygonEnv;
      else
        m_envelope = m_envelope.getMerged( polygonEnv );
    }
  }

  @SuppressWarnings("unchecked")
  private void removeFromIndex( final Object o )
  {
    // TODO: consider envelope
    // probably we should delegate the whole envelope stuff to the index
    if( o instanceof GM_Polygon )
    {
      final T gmPoly = (T) o;
      final Envelope env = JTSAdapter.export( gmPoly.getEnvelope() );
      m_index.remove( env, o );
    }

    m_envelope = null;
  }

  /**
   * @see org.kalypsodeegree.model.geometry.GM_OrientableSurface#getSurfaceBoundary()
   */
  public GM_SurfaceBoundary getSurfaceBoundary( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypsodeegree.model.geometry.GM_GenericSurface#getArea()
   */
  public double getArea( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypsodeegree.model.geometry.GM_GenericSurface#getPerimeter()
   */
  public double getPerimeter( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypsodeegree.model.geometry.ISurfacePatchVisitable#acceptSurfacePatches(org.kalypsodeegree.model.geometry.GM_Envelope,
   *      org.kalypsodeegree.model.geometry.ISurfacePatchVisitor, org.eclipse.core.runtime.IProgressMonitor)
   */
  public void acceptSurfacePatches( final GM_Envelope envToVisit, final ISurfacePatchVisitor<T> surfacePatchVisitor, final IProgressMonitor monitor ) throws CoreException
  {
    monitor.beginTask( "", IProgressMonitor.UNKNOWN );

    final ItemVisitor visitor = new ItemVisitor()
    {
      @SuppressWarnings("unchecked")
      public void visitItem( final Object item )
      {
        final T t = (T) item;
        surfacePatchVisitor.visit( t, Double.NaN );
        try
        {
          ProgressUtilities.worked( monitor, 1 );
        }
        catch( final CoreException e )
        {
          // We cannot throw a CoreException here, so we just throw to an runtime-exception....
          throw new OperationCanceledException();
        }
      }
    };

    try
    {
      final Envelope searchEnv = JTSAdapter.export( envToVisit );
      m_index.query( searchEnv, visitor );
    }
    catch( final OperationCanceledException e )
    {
      // ... which is catched here an we re-throw a true CoreException(Cancel)
      throw new CoreException( Status.CANCEL_STATUS );
    }
  }

  /**
   * @see org.kalypsodeegree.model.geometry.GM_Object#transform(org.deegree.crs.transformations.CRSTransformation,
   *      java.lang.String)
   */
  @SuppressWarnings("unchecked")
  public GM_Object transform( final CRSTransformation trans, final String targetOGCCS ) throws Exception
  {
    /* If the target is the same coordinate system, do not transform. */
    final String coordinateSystem = getCoordinateSystem();
    if( coordinateSystem == null || coordinateSystem.equalsIgnoreCase( targetOGCCS ) )
      return this;

    Debug.debugMethodBegin( this, "transformTriangulatedSurface" );

    final int cnt = size();
    final T[] polygons = (T[]) new GM_Polygon[cnt];

    for( int i = 0; i < cnt; i++ )
    {
      polygons[i] = (T) get( i ).transform( trans, targetOGCCS );
    }

    Debug.debugMethodEnd();
    return GeometryFactory.createGM_PolyhedralSurface( polygons, targetOGCCS );
  }
}
