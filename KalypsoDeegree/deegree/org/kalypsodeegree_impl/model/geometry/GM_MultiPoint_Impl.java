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

import org.deegree.crs.transformations.CRSTransformation;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_MultiPoint;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;

/**
 * default implementierung of the GM_MultiPoint interface of package jago.model.
 * <p>
 * ------------------------------------------------------------
 * </p>
 * 
 * @version 12.6.2001
 * @author Andreas Poth href="mailto:poth@lat-lon.de"
 *         <p>
 */
final class GM_MultiPoint_Impl extends GM_MultiPrimitive_Impl implements GM_MultiPoint, Serializable
{
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = -1105623021535230655L;

  /**
   * Creates a new GM_MultiPoint_Impl object.
   * 
   * @param crs
   */
  public GM_MultiPoint_Impl( final String crs )
  {
    super( crs );
  }

  /**
   * Creates a new GM_MultiPoint_Impl object.
   * 
   * @param gmp
   */
  public GM_MultiPoint_Impl( final GM_Point[] gmp )
  {
    super( gmp, null );
  }

  /**
   * Creates a new GM_MultiPoint_Impl object.
   * 
   * @param gmp
   * @param crs
   */
  public GM_MultiPoint_Impl( final GM_Point[] gmp, final String crs )
  {
    super( gmp, crs );
  }

  /**
   * adds a GM_Point to the aggregation
   */
  @Override
  public void addPoint( final GM_Point gmp )
  {
    super.add( gmp );
  }

  /**
   * inserts a GM_Point into the aggregation. all elements with an index equal or larger index will be moved. if index
   * is larger then getSize() - 1 or smaller then 0 or gmp equals null an exception will be thrown.
   * 
   * @param gmp
   *          GM_Point to insert.
   * @param index
   *          position where to insert the new GM_Point
   */
  @Override
  public void insertPointAt( final GM_Point gmp, final int index ) throws GM_Exception
  {
    super.insertObjectAt( gmp, index );
  }

  /**
   * sets the submitted GM_Point at the submitted index. the element at the position <code>index</code> will be removed.
   * if index is larger then getSize() - 1 or smaller then 0 or gmp equals null an exception will be thrown.
   * 
   * @param gmp
   *          GM_Point to set.
   * @param index
   *          position where to set the new GM_Point
   */
  @Override
  public void setPointAt( final GM_Point gmp, final int index ) throws GM_Exception
  {
    setObjectAt( gmp, index );
  }

  /**
   * removes the submitted GM_Point from the aggregation
   * 
   * @return the removed GM_Point
   */
  @Override
  public GM_Point removePoint( final GM_Point gmp )
  {
    return (GM_Point) super.removeObject( gmp );
  }

  /**
   * removes the GM_Point at the submitted index from the aggregation. if index is larger then getSize() - 1 or smaller
   * then 0 an exception will be thrown.
   * 
   * @return the removed GM_Point
   */
  @Override
  public GM_Point removePointAt( final int index ) throws GM_Exception
  {
    return (GM_Point) super.removeObjectAt( index );
  }

  /**
   * returns the GM_Point at the submitted index.
   */
  @Override
  public GM_Point getPointAt( final int index )
  {
    return (GM_Point) super.getPrimitiveAt( index );
  }

  /**
   * returns all GM_Points as array
   */
  @Override
  public GM_Point[] getAllPoints( )
  {
    return m_aggregate.toArray( new GM_Point[getSize()] );
  }

  /**
   * calculates the centroid of the surface
   */
  @Override
  protected GM_Point calculateCentroid( )
  {
    final GM_Point gmp = getPointAt( 0 );

    final double[] cen = new double[gmp.getAsArray().length];

    for( int i = 0; i < getSize(); i++ )
    {
      final double[] pos = getPointAt( i ).getAsArray();

      for( int j = 0; j < pos.length; j++ )
      {
        cen[j] += (pos[j] / getSize());
      }
    }

    return GeometryFactory.createGM_Point( GeometryFactory.createGM_Position( cen ), getCoordinateSystem() );
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
    return 0;
  }

  /**
   * The operation "coordinateDimension" shall return the dimension of the coordinates that define this GM_Object, which
   * must be the same as the coordinate dimension of the coordinate reference system for this GM_Object.
   */
  @Override
  public int getCoordinateDimension( )
  {
    GM_Point sp = null;

    try
    {
      sp = getPointAt( 0 );
    }
    catch( final Exception ex )
    {
      // nothing
    }

    return sp.getCoordinateDimension();
  }

  /**
   * returns a deep copy of the geometry
   */
  @Override
  public Object clone( ) throws CloneNotSupportedException
  {
    final GM_Point[] points = getAllPoints();
    final GM_Point[] clonedPoints = new GM_Point[points.length];
    for( int i = 0; i < points.length; i++ )
      clonedPoints[i] = (GM_Point) points[i].clone();

    return new GM_MultiPoint_Impl( clonedPoints, getCoordinateSystem() );
  }

  /**
   * @see org.kalypsodeegree_impl.model.geometry.GM_MultiPrimitive_Impl#transform(org.deegree.crs.transformations.CRSTransformation,
   *      java.lang.String)
   */
  @Override
  public GM_Object transform( final CRSTransformation trans, final String targetOGCCS ) throws Exception
  {
    /* If the target is the same coordinate system, do not transform. */
    final String coordinateSystem = getCoordinateSystem();
    if( coordinateSystem == null || coordinateSystem.equalsIgnoreCase( targetOGCCS ) )
      return this;

    final GM_Point[] points = new GM_Point[getSize()];

    for( int i = 0; i < getSize(); i++ )
      points[i] = (GM_Point) getPointAt( i ).transform( trans, targetOGCCS );

    return GeometryFactory.createGM_MultiPoint( points );
  }
}