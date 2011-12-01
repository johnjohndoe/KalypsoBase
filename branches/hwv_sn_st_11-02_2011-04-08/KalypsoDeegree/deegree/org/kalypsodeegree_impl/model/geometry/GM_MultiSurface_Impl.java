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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_MultiSurface;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree.model.geometry.GM_SurfacePatch;

/**
 * default implementation of the GM_MultiSurface interface from package jago.model.
 * <p>
 * ------------------------------------------------------------
 * </p>
 * 
 * @version 12.6.2001
 * @author Andreas Poth
 *         <p>
 */
final class GM_MultiSurface_Impl extends GM_MultiPrimitive_Impl implements GM_MultiSurface, Serializable
{
  /** Use serialVersionUID for interoperability. */
  private static final long serialVersionUID = -6471121873087659850L;

  private double m_area = 0.0;

  /**
   * Creates a new GM_MultiSurface_Impl object.
   * 
   * @param crs
   */
  public GM_MultiSurface_Impl( final String crs )
  {
    super( crs );
  }

  /**
   * Creates a new GM_MultiSurface_Impl object.
   * 
   * @param surface
   */
  public GM_MultiSurface_Impl( final GM_Surface< ? >[] surfaces )
  {
    this( surfaces, null );
  }

  /**
   * Creates a new GM_MultiSurface_Impl object.
   * 
   * @param surface
   * @param crs
   */
  public GM_MultiSurface_Impl( final GM_Surface< ? >[] surfaces, final String crs )
  {
    super( surfaces, crs );
  }

  /**
   * adds an GM_Surface to the aggregation
   */
  @Override
  public void addSurface( final GM_Surface< ? > gms )
  {
    super.add( gms );
  }

  /**
   * inserts a GM_Surface in the aggregation. all elements with an index equal or larger index will be moved. if index
   * is larger then getSize() - 1 or smaller then 0 or gms equals null an exception will be thrown.
   * 
   * @param gms
   *          GM_Surface to insert.
   * @param index
   *          position where to insert the new GM_Surface
   */
  @Override
  public void insertSurfaceAt( final GM_Surface< ? > gms, final int index ) throws GM_Exception
  {
    super.insertObjectAt( gms, index );
  }

  /**
   * sets the submitted GM_Surface at the submitted index. the element at the position <code>index</code> will be
   * removed. if index is larger then getSize() - 1 or smaller then 0 or gms equals null an exception will be thrown.
   * 
   * @param gms
   *          GM_Surface to set.
   * @param index
   *          position where to set the new GM_Surface
   */
  @Override
  public void setSurfaceAt( final GM_Surface< ? > gms, final int index ) throws GM_Exception
  {
    setObjectAt( gms, index );
  }

  /**
   * removes the submitted GM_Surface from the aggregation
   * 
   * @return the removed GM_Surface
   */
  @Override
  public GM_Surface< ? > removeSurface( final GM_Surface< ? > gms )
  {
    return (GM_Surface< ? >) super.removeObject( gms );
  }

  /**
   * removes the GM_Surface at the submitted index from the aggregation. if index is larger then getSize() - 1 or
   * smaller then 0 an exception will be thrown.
   * 
   * @return the removed GM_Surface
   */
  @Override
  public GM_Surface< ? > removeSurfaceAt( final int index ) throws GM_Exception
  {
    return (GM_Surface< ? >) super.removeObjectAt( index );
  }

  /**
   * returns the GM_Surface at the submitted index.
   */
  @Override
  public GM_Surface< ? > getSurfaceAt( final int index )
  {
    return (GM_Surface< ? >) super.getPrimitiveAt( index );
  }

  /**
   * returns all GM_Surfaces as array
   */
  @Override
  public GM_Surface< ? >[] getAllSurfaces( )
  {
    return m_aggregate.toArray( new GM_Surface[getSize()] );
  }

  /**
   * @see org.kalypsodeegree_impl.model.geometry.GM_Primitive_Impl#getAdapter(java.lang.Class)
   */
  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class adapter )
  {
    if( adapter == GM_SurfacePatch[].class )
    {
      final List<GM_SurfacePatch> patchList = new LinkedList<GM_SurfacePatch>();

      final GM_Surface< ? >[] surfaces = getAllSurfaces();

      for( final GM_Surface< ? > surface : surfaces )
      {
        final GM_SurfacePatch[] surfacePatches = (GM_SurfacePatch[]) surface.getAdapter( GM_SurfacePatch[].class );
        for( final GM_SurfacePatch surfacePatch : surfacePatches )
        {
          patchList.add( surfacePatch );
        }
      }
      return patchList.toArray( new GM_SurfacePatch[patchList.size()] );
    }

    if( adapter == GM_Curve.class )
    {
      final List<GM_Curve> curveList = new LinkedList<GM_Curve>();

      final GM_Surface< ? >[] surfaces = getAllSurfaces();

      for( final GM_Surface< ? > surface : surfaces )
      {
        final GM_SurfacePatch[] surfacePatches = (GM_SurfacePatch[]) surface.getAdapter( GM_SurfacePatch[].class );
        for( final GM_SurfacePatch surfacePatch : surfacePatches )
        {
          final GM_Position[] exteriorRing = surfacePatch.getExteriorRing();
          try
          {
            curveList.add( GeometryFactory.createGM_Curve( exteriorRing, getCoordinateSystem() ) );
          }
          catch( final GM_Exception e )
          {
            final IStatus status = StatusUtilities.statusFromThrowable( e );
            KalypsoDeegreePlugin.getDefault().getLog().log( status );
            return null;
          }
        }
      }
      return curveList.toArray( new GM_Curve[curveList.size()] );

    }

    return super.getAdapter( adapter );
  }

  /**
   * calculates the centroid and area of the aggregation
   */
  @Override
  protected GM_Point calculateCentroid( )
  {
    m_area = 0;

    if( getSize() == 0 )
      return GM_Constants.EMPTY_CENTROID;

    // REMARK: we reduce to dimension 2 here, because everyone else (GM_Surface, GM_Curve)
    // always only produce 2-dim centroids, causing an ArrayOutOfBoundsException here...
    // Maybe it would be nice to always have a 3-dim centroid if possible
    final int cnt = Math.min( 2, getCoordinateDimension() );
    final double[] cen = new double[cnt];

    for( int i = 0; i < getSize(); i++ )
    {
      final double a = getSurfaceAt( i ).getArea();
      m_area = m_area + a;

      final double[] pos = getSurfaceAt( i ).getCentroid().getAsArray();

      for( int j = 0; j < cnt; j++ )
      {
        cen[j] = cen[j] + (pos[j] * a);
      }
    }

    for( int j = 0; j < cnt; j++ )
    {
      cen[j] = cen[j] / m_area;
    }

    return GeometryFactory.createGM_Point( GeometryFactory.createGM_Position( cen ), getCoordinateSystem() );
  }

  /**
   * returns the area of the multi surface. this is calculate as the sum of all containing surface areas.
   */
  @Override
  public double getArea( )
  {
    // TODO: Still a bit hacky: centroid and area are calculated at the same moment;
    // we just make sure that the centroid is recalculated by calling getCentroid
    getCentroid();
    return m_area;
  }

  /**
   * returns a shallow copy of the geometry
   */
  @Override
  public Object clone( ) throws CloneNotSupportedException
  {
    final GM_Surface< ? >[] surfaces = getAllSurfaces();
    final GM_Surface< ? >[] clonedSurfaces = new GM_Surface[surfaces.length];
    for( int i = 0; i < surfaces.length; i++ )
      clonedSurfaces[i] = (GM_Surface< ? >) surfaces[i].clone();

    return new GM_MultiSurface_Impl( clonedSurfaces, getCoordinateSystem() );
  }

  /**
   * The operation "dimension" shall return the inherent dimension of this GM_Object, which shall be less than or equal
   * to the coordinate dimension. The dimension of a collection of geometric objects shall be the largest dimension of
   * any of its pieces. Points are 0-dimensional, curves are 1-dimensional, surfaces are 2-dimensional, and solids are
   * 3-dimensional.
   */
  @Override
  public int getDimension( )
  {
    return 2;
  }

  /**
   * The operation "coordinateDimension" shall return the dimension of the coordinates that define this GM_Object, which
   * must be the same as the coordinate dimension of the coordinate reference system for this GM_Object.
   */
  @Override
  public int getCoordinateDimension( )
  {
    final GM_SurfacePatch sp = getSurfaceAt( 0 ).get( 0 );
    return sp.getExteriorRing()[0].getCoordinateDimension();
  }

  /**
   * @see org.kalypsodeegree_impl.model.geometry.GM_MultiPrimitive_Impl#transform(java.lang.String)
   */
  @Override
  public GM_Object transform( final String targetCRS ) throws Exception
  {
    /* If the target is the same coordinate system, do not transform. */
    final String sourceCRS = getCoordinateSystem();
    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( targetCRS ) )
      return this;

    final GM_Surface< ? >[] surfaces = new GM_Surface[getSize()];

    for( int i = 0; i < getSize(); i++ )
      surfaces[i] = (GM_Surface< ? >) getSurfaceAt( i ).transform( targetCRS );

    return GeometryFactory.createGM_MultiSurface( surfaces, targetCRS );
  }
}