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
import org.kalypso.transformation.transformer.GeoTransformerException;
import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.model.geometry.GM_Position;

/**
 * A sequence of decimals numbers which when written on a width are a sequence of coordinate positions. The width is
 * derived from the CRS or coordinate dimension of the container.
 * <p>
 * -----------------------------------------------------------------------
 * </p>
 *
 * @version
 * @author Andreas Poth
 *         <p>
 */
class GM_Position_Impl implements GM_Position, Serializable
{
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = -3780255674921824356L;

  private final double[] m_point;

  /**
   * constructor. initializes a point to the coordinate 0/0
   */
  private GM_Position_Impl( )
  {
    m_point = new double[] { 0, 0, 0 };
  }

  /**
   * constructor
   *
   * @param x
   *          x-value of the point
   * @param y
   *          y-value of the point
   */
  private GM_Position_Impl( final double x, final double y )
  {
    m_point = new double[] { x, y };
  }

  /**
   * constructor
   *
   * @param x
   *          x-value of the point
   * @param y
   *          y-value of the point
   * @param z
   *          z-value of the point
   */
  private GM_Position_Impl( final double x, final double y, final double z )
  {
    m_point = new double[] { x, y, z };
  }

  /**
   * Copies the content of the given array, does NOT keep a reference to it.
   */
  private GM_Position_Impl( final double[] coords )
  {
    Assert.isNotNull( coords );

    m_point = coords.clone();
  }

  /**
   * returns a deep copy of the geometry.
   */
  @Override
  public Object clone( )
  {
    return new GM_Position_Impl( m_point.clone() );
  }

  /**
   * returns the x-value of this point
   */
  @Override
  public double getX( )
  {
    return m_point[0];
  }

  /**
   * returns the y-value of this point
   */
  @Override
  public double getY( )
  {
    return m_point[1];
  }

  /**
   * returns the z-value of this point
   */
  @Override
  public double getZ( )
  {
    if( m_point.length > 2 )
      return m_point[2];

    return Double.NaN;
  }

  /**
   * @see org.kalypsodeegree.model.geometry.GM_Position#getCoordinateDimension()
   */
  @Override
  public short getCoordinateDimension( )
  {
    return (short) m_point.length;
  }

  /**
   * returns the position as a array the first field contains the x- the second field the y-value etc.
   */
  @Override
  public double[] getAsArray( )
  {
    // return (double[])point.clone();
    return m_point;
  }

  /**
   * translate the point by the submitted values. the <code>dz</code>- value will be ignored.
   */
  @Override
  public void translate( final double[] d )
  {
    if( d.length > m_point.length )
      throw new IllegalArgumentException();

    for( int i = 0; i < m_point.length; i++ )
      m_point[i] += d[i];
  }

  /**
   * compares if all field of other are equal to the corresponding fields of this position
   */
  @Override
  public boolean equals( final Object other )
  {
    if( other instanceof GM_Position )
      return equals( (GM_Position) other, false );

    return false;
  }

  @Override
  public int hashCode( )
  {
    int lIntHash = 17;
    lIntHash = 31 * lIntHash + (int) (Double.doubleToLongBits( getX() ) ^ Double.doubleToLongBits( getX() ) >>> 32);
    lIntHash = 31 * lIntHash + (int) (Double.doubleToLongBits( getY() ) ^ Double.doubleToLongBits( getY() ) >>> 32);
    final double lDoubleZ = getZ();
    if( lDoubleZ != Double.NaN )
      lIntHash = 31 * lIntHash + (int) (Double.doubleToLongBits( lDoubleZ ) ^ Double.doubleToLongBits( lDoubleZ ) >>> 32);
    return lIntHash;
  }

  /**
   * compares if all field of other are equal to the corresponding fields of this position
   */
  public boolean equals( final GM_Position other, final boolean exact )
  {
    final double[] other_ = other.getAsArray();

    if( other_.length != m_point.length )
      return false;

    // REMARK: still strange... depends on coordinate system as well. Map should not zoom deeper than this value...
    final double mute = exact ? Double.MIN_NORMAL : MUTE;

    for( int i = 0; i < m_point.length; i++ )
    {
      if( Math.abs( m_point[i] - other_[i] ) > mute )
        return false;
    }

    return true;
  }

  @Override
  public String toString( )
  {
    String ret = "GM_Position: ";

    for( final double element : m_point )
    {
      ret += element + " ";
    }

    return ret;
  }

  @Override
  public double getDistance( final GM_Position other )
  {
    final double dx = getX() - other.getX();
    final double dy = getY() - other.getY();
    final double d = dx * dx + dy * dy;

    return Math.sqrt( d );
  }

  @Override
  public GM_Position transform( final String sourceCRS, final String targetCRS ) throws GeoTransformerException
  {
    final IGeoTransformer geoTransformer = GeoTransformerFactory.getGeoTransformer( targetCRS );
    return geoTransformer.transform( this, sourceCRS );
  }
}