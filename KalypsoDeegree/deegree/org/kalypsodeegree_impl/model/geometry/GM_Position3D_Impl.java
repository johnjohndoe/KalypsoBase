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

import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.model.geometry.GM_Position;

/**
 * 3D implementation of {@link GM_Position}; specially implemented for memory optimization<br>
 * <br>
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
class GM_Position3D_Impl implements GM_Position, Serializable
{
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = -3780255674921824356L;

  public static final double MUTE = 0.000001;

  private double m_x;

  private double m_y;

  private double m_z;

  /**
   * constructor. initializes a point to the coordinate 0/0
   */
  GM_Position3D_Impl( )
  {
    m_x = 0.0;
    m_y = 0.0;
    m_z = 0.0;
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
  GM_Position3D_Impl( final double x, final double y, final double z )
  {
    m_x = x;
    m_y = y;
    m_z = z;
  }

  /**
   * returns a deep copy of the geometry.
   */
  @Override
  public Object clone( )
  {
    return new GM_Position3D_Impl( m_x, m_y, m_z );
  }

  /**
   * returns the x-value of this point
   */
  @Override
  public double getX( )
  {
    return m_x;
  }

  /**
   * returns the y-value of this point
   */
  @Override
  public double getY( )
  {
    return m_y;
  }

  /**
   * returns the z-value of this point
   */
  @Override
  public double getZ( )
  {
    return m_z;
  }

  /**
   * @see org.kalypsodeegree.model.geometry.GM_Position#getCoordinateDimension()
   */
  @Override
  public short getCoordinateDimension( )
  {
    return 3;
  }

  /**
   * returns the position as a array the first field contains the x- the second field the y-value etc.
   */
  @Override
  public double[] getAsArray( )
  {
    return new double[] { m_x, m_y, m_z };
  }

  /**
   * translate the point by the submitted values. the <code>dz</code>- value will be ignored.
   */
  @Override
  public void translate( final double[] d )
  {
    if( d.length < 3 )
      throw new IllegalArgumentException( "3D position must be translated with 3d vector" );

    m_x += d[0];
    m_y += d[1];
    m_z += d[2];
  }

  /**
   * compares if all field of other are equal to the corresponding fields of this position
   */
  @Override
  public boolean equals( final Object other )
  {
    if( other instanceof GM_Position3D_Impl )
      return equals( (GM_Position3D_Impl) other, false );

    return false;
  }

  @Override
  public int hashCode( )
  {
    int lIntHash = 17;
    lIntHash = 31 * lIntHash + (int) (Double.doubleToLongBits( getX() ) ^ (Double.doubleToLongBits( getX() ) >>> 32));
    lIntHash = 31 * lIntHash + (int) (Double.doubleToLongBits( getY() ) ^ (Double.doubleToLongBits( getY() ) >>> 32));
    lIntHash = 31 * lIntHash + (int) (Double.doubleToLongBits( getZ() ) ^ (Double.doubleToLongBits( getZ() ) >>> 32));
    return lIntHash;
  }

  /**
   * compares if all field of other are equal to the corresponding fields of this position
   */
  public boolean equals( final GM_Position3D_Impl other, final boolean exact )
  {
    // REMARK: still strange... depends on coordinate system as well. Map should not zoom deeper than this value...
    final double mute = exact ? Double.MIN_NORMAL : MUTE;

    if( Math.abs( m_x - other.m_x ) > mute )
      return false;
    if( Math.abs( m_y - other.m_y ) > mute )
      return false;
    if( Math.abs( m_z - other.m_z ) > mute )
      return false;

    return true;
  }

  @Override
  public String toString( )
  {
    String ret = "GM_Position: ";

    ret += m_x + " ";
    ret += m_y + " ";
    ret += m_z;

    return ret;
  }

  /**
   * @see org.kalypsodeegree.model.geometry.GM_Position#getDistance(org.kalypsodeegree.model.geometry.GM_Position)
   */
  @Override
  public double getDistance( final GM_Position other )
  {
    final double dx = m_x - other.getX();
    final double dy = m_y - other.getY();
    final double d = dx * dx + dy * dy;

    return Math.sqrt( d );
  }

  /**
   * @see org.kalypsodeegree.model.geometry.GM_Position#transform(java.lang.String, java.lang.String)
   */
  @Override
  public GM_Position transform( final String sourceCRS, final String targetCRS ) throws Exception
  {
    IGeoTransformer geoTransformer = GeoTransformerFactory.getGeoTransformer( targetCRS );
    return geoTransformer.transform( this, sourceCRS );
  }
}