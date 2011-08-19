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

import org.eclipse.core.runtime.Assert;
import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.model.geometry.GM_Aggregate;
import org.kalypsodeegree.model.geometry.GM_Boundary;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Surface;

/**
 * default implementation of the GM_Point interface.
 * <p>
 * ------------------------------------------------------------
 * </p>
 * 
 * @version 5.6.2001
 * @author Andreas Poth
 */
final class GM_Point_Impl extends GM_Primitive_Impl implements GM_Point, Serializable
{
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 6106017748940535740L;

  private final GM_Position m_position;

  /**
   * constructor for initializing a point within a two-dimensional coordinate system
   * 
   * @param x
   *          x-value of the point
   * @param y
   *          y-value of the point
   * @param crs
   *          spatial reference system of the point
   */
  public GM_Point_Impl( final double x, final double y, final String crs )
  {
    this( GeometryFactory.createGM_Position( x, y ), crs );
  }

  /**
   * constructor for initializing a point within a three-dimensional coordinate system
   * 
   * @param x
   *          x-value of the point
   * @param y
   *          y-value of the point
   * @param z
   *          z-value of the point
   * @param crs
   *          spatial reference system of the point
   */
  public GM_Point_Impl( final double x, final double y, final double z, final String crs )
  {
    this( GeometryFactory.createGM_Position( x, y, z ), crs );
  }

  /**
   * constructor
   * 
   * @param gmo
   *          existing GM_Point
   */
  public GM_Point_Impl( final GM_Point gmo )
  {
    this( GeometryFactory.createGM_Position( gmo.getAsArray() ), gmo.getCoordinateSystem() );
  }

  /**
   * constructor
   * 
   * @param gmo
   *          existing GM_Point
   * @param crs
   *          spatial reference system of the point
   */
  public GM_Point_Impl( final GM_Position gmo, final String crs )
  {
    super( crs );

    Assert.isNotNull( gmo );

    m_position = gmo;
  }

  /**
   * checks if this point is completly equal to the submitted geometry
   */
  @Override
  public boolean equals( final Object other )
  {
    if( other == this )
      return true;

    if( super.equals( other ) && (other instanceof GM_Point) )
    {
      final GM_Point p = (GM_Point) other;
      boolean flagEq = (Math.abs( getX() - p.getX() ) < GM_Position.MUTE) && (Math.abs( getY() - p.getY() ) < GM_Position.MUTE);
      if( getCoordinateDimension() == 3 )
      {
        final double z1 = getZ();
        final double z2 = p.getZ();

        if( Double.isNaN( z1 ) && Double.isNaN( z2 ) )
          flagEq = flagEq && true;
        else
          flagEq = flagEq && (Math.abs( z1 - z2 ) < GM_Position.MUTE);
      }
      return flagEq;
    }

    return false;
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
    return m_position.getCoordinateDimension();
  }

  /**
   * returns a shallow copy of the geometry.
   */
  @Override
  public Object clone( )
  {
    final String system = getCoordinateSystem();

    if( getDimension() == 3 )
      return new GM_Point_Impl( getX(), getY(), getZ(), system );

    return new GM_Point_Impl( getX(), getY(), system );
  }

  /**
   * @see org.kalypsodeegree_impl.model.geometry.GM_Object_Impl#isEmpty()
   */
  @Override
  public boolean isEmpty( )
  {
    return false;
  }

  /**
   * returns the x-value of this point
   */
  @Override
  public double getX( )
  {
    return m_position.getX();
  }

  /**
   * returns the y-value of this point
   */
  @Override
  public double getY( )
  {
    return m_position.getY();
  }

  /**
   * returns the y-value of this point
   */
  @Override
  public double getZ( )
  {
    return m_position.getZ();
  }

  /**
   * returns the x- and y-value of the point as a two dimensional array the first field contains the x- the second field
   * the y-value.
   */
  @Override
  public double[] getAsArray( )
  {
    return m_position.getAsArray();
  }

  /**
   * translate the point by the submitted values. the <code>dz</code>- value will be ignored.
   */
  @Override
  public void translate( final double[] d )
  {
    m_position.translate( d );
    invalidate();
  }

  @Override
  public GM_Position getPosition( )
  {
    return m_position;
  }

  /**
   * The Boolean valued operation "intersects" shall return TRUE if this GM_Object intersects another GM_Object. Within
   * a GM_Complex, the GM_Primitives do not intersect one another. In general, topologically structured data uses shared
   * geometric objects to capture intersection information.
   * <p>
   * </p>
   * dummy implementation
   */
  @Override
  public boolean intersects( final GM_Object gmo )
  {
    boolean inter = false;

    try
    {
      if( gmo instanceof GM_Point )
      {
        inter = LinearIntersects.intersects( (GM_Point) gmo, this );
      }
      else if( gmo instanceof GM_Curve )
      {
        inter = LinearIntersects.intersects( this, (GM_Curve) gmo );
      }
      else if( gmo instanceof GM_Surface )
      {
        inter = LinearIntersects.intersects( this, (GM_Surface< ? >) gmo );
      }
      else if( gmo instanceof GM_Aggregate )
      {
        inter = intersectsAggregate( (GM_Aggregate) gmo );
      }
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    return inter;
  }

  /**
   * the operations returns true if the submitted multi primitive intersects with the curve segment
   */
  private boolean intersectsAggregate( final GM_Aggregate mprim ) throws Exception
  {
    boolean inter = false;

    final int cnt = mprim.getSize();

    for( int i = 0; i < cnt; i++ )
    {
      if( intersects( mprim.getObjectAt( i ) ) )
      {
        inter = true;
        break;
      }
    }

    return inter;
  }

  /**
   * The Boolean valued operation "contains" shall return TRUE if this GM_Object contains another GM_Object.
   * <p>
   * </p>
   */
  @Override
  public boolean contains( final GM_Object gmo )
  {
    return equals( gmo );
    // TODO: check if this is correct in all cases
    // throw new UnsupportedOperationException( "the contains operation for points " + "isn't supported at the moment."
    // );
  }

  /**
   * @see org.kalypsodeegree_impl.model.geometry.GM_Object_Impl#calculateEnvelope()
   */
  @Override
  protected GM_Envelope calculateEnvelope( )
  {
    return GeometryFactory.createGM_Envelope( getPosition(), getPosition(), getCoordinateSystem() );
  }

  /**
   * @see org.kalypsodeegree_impl.model.geometry.GM_Object_Impl#calculateBoundary()
   */
  @Override
  protected GM_Boundary calculateBoundary( )
  {
    // TODO: implement: what is the boundary of a point?
    return GM_Constants.EMPTY_BOUNDARY;
  }

  /**
   * @see org.kalypsodeegree_impl.model.geometry.GM_Object_Impl#getCentroid()
   */
  @Override
  public final GM_Point getCentroid( )
  {
    return this;
  }

  /**
   * @see org.kalypsodeegree_impl.model.geometry.GM_Object_Impl#calculateCentroid()
   */
  @Override
  protected GM_Point calculateCentroid( )
  {
    // We implement getCentroid ourself's, so this should never be called
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString( )
  {
    String ret = "GM_Point: ";

    for( int i = 0; i < getCoordinateDimension(); i++ )
    {
      ret += (getAsArray()[i] + " ");
    }

    return ret;
  }

  /**
   * @see org.kalypsodeegree.model.geometry.GM_Object#transform(java.lang.String)
   */
  @Override
  public GM_Object transform( final String targetCRS ) throws Exception
  {
    /* If the target is the same coordinate system, do not transform. */
    final String sourceCRS = getCoordinateSystem();
    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( targetCRS ) )
      return this;

    IGeoTransformer geoTransformer = GeoTransformerFactory.getGeoTransformer( targetCRS );
    return geoTransformer.transform( this );
  }
}