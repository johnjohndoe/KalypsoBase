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

import javax.vecmath.Point3d;

import org.apache.commons.lang3.ArrayUtils;
import org.kalypso.transformation.transformer.GeoTransformerException;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_AbstractSurfacePatch;
import org.kalypsodeegree.model.geometry.GM_Triangle;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Triangle;

/**
 * Improved (direct) triangle implementation that does not keep any state except its three vertices. <br/>
 * Everything else is calculated on the fly in order to minimize memory consumption.<br/>
 * This is usually ok (regarding performance), because the involved operations are fast enough for three points.
 *
 * @author Gernot Belger
 */
final class GM_Triangle_Impl implements GM_Triangle
{
  private final GM_Position m_p1;

  private final GM_Position m_p2;

  private final GM_Position m_p3;

  private final String m_srsName;

  public GM_Triangle_Impl( final GM_Position p1, final GM_Position p2, final GM_Position p3, final String srsName )
  {
    m_p1 = p1;
    m_p2 = p2;
    m_p3 = p3;
    m_srsName = srsName;
  }

  @Override
  public double getValue( final GM_Point location )
  {
    final GM_Position position = location.getPosition();
    // TODO: transform into own crs if necessary
    return getValue( position );
  }

  @Override
  public double getValue( final GM_Position position )
  {
    try
    {
      final double x = position.getX();
      final double y = position.getY();

      final Plane plane = createPlane();

      return plane.z( x, y );
    }
    catch( final ArithmeticException e )
    {
      return Double.NaN;
    }
  }

  private Plane createPlane( )
  {
    // REMARK: we do not cache the plane here, because
    // - it actually does not save that much time
    // - we have huge triagulated surfaces with millions of tirangles, so caching consumes too much memory.
    // TODO: replace the heavywheight plane class with a direct calculation of the plane equation
    final Plane plane = new Plane();

    final Point3d p0 = new Point3d( m_p1.getX(), m_p1.getY(), m_p1.getZ() );
    final Point3d p1 = new Point3d( m_p2.getX(), m_p2.getY(), m_p2.getZ() );
    final Point3d p2 = new Point3d( m_p3.getX(), m_p3.getY(), m_p3.getZ() );
    plane.setPlane( p0, p1, p2 );

    return plane;
  }

  @Override
  public boolean contains( final GM_Position position )
  {
    // REMARK: see below; but still problematic, because we create many objects here....
    final Coordinate[] ring = JTSAdapter.export( getExteriorRing() );

    final Coordinate pt = new Coordinate( position.getX(), position.getY() );
    return CGAlgorithms.isPointInRing( pt, ring );

//    REMARK: this does not work for points on the edge or vetice of th etriangle
//    final int a1 = orientation( m_p1, m_p2, position );
//    final int a2 = orientation( m_p2, m_p3, position );
//    final int a3 = orientation( m_p3, m_p1, position );
//    return (a1 == a2) && (a2 == a3);
  }

  private static int orientation( final GM_Position pos1, final GM_Position pos2, final GM_Position pos3 )
  {
    final double s_a = signedArea( pos1, pos2, pos3 );
    if( s_a > 0 )
      return 1;

    if( s_a < 0 )
      return -1;

    return 0;
  }

  private static double signedArea( final GM_Position a, final GM_Position b, final GM_Position c )
  {
    return ((c.getX() - a.getX()) * (b.getY() - a.getY()) - (b.getX() - a.getX()) * (c.getY() - a.getY())) / 2;
  }

  @Override
  public GM_AbstractSurfacePatch transform( final String targetCRS ) throws GeoTransformerException
  {
    /* If the target is the same coordinate system, do not transform. */
    final String sourceCRS = getCoordinateSystem();
    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( targetCRS ) )
      return this;

    final GM_Position t1 = m_p1.transform( sourceCRS, targetCRS );
    final GM_Position t2 = m_p2.transform( sourceCRS, targetCRS );
    final GM_Position t3 = m_p3.transform( sourceCRS, targetCRS );

    return GeometryFactory.createGM_Triangle( t1, t2, t3, targetCRS );
  }

  /**
   * Returns a deep copy of the geometry.
   */
  @Override
  public Object clone( )
  {
    final GM_Position c1 = (GM_Position) m_p1.clone();
    final GM_Position c2 = (GM_Position) m_p2.clone();
    final GM_Position c3 = (GM_Position) m_p3.clone();

    return GeometryFactory.createGM_Triangle( c1, c2, c3, getCoordinateSystem() );
  }

  @Override
  public int getOrientation( )
  {
    return orientation( m_p1, m_p2, m_p3 );
  }

  @Override
  public double getMinValue( )
  {
    double min = m_p1.getZ();

    if( min > m_p2.getZ() )
      min = m_p2.getZ();

    if( min > m_p3.getZ() )
      min = m_p3.getZ();

    return min;
  }

  @Override
  public double getMaxValue( )
  {
    double max = m_p1.getZ();

    if( max < m_p2.getZ() )
      max = m_p2.getZ();

    if( max < m_p3.getZ() )
      max = m_p3.getZ();

    return max;
  }

  @Override
  public GM_Position[] getExteriorRing( )
  {
    return new GM_Position[] { m_p1, m_p2, m_p3, m_p1 };
  }

  @Override
  public GM_Position[][] getInteriorRings( )
  {
    return null;
  }

  @Override
  public String getCoordinateSystem( )
  {
    return m_srsName;
  }

  @Override
  public boolean intersects( final GM_Object gmo )
  {
    try
    {
      final GM_PolygonPatch_Impl poly = new GM_PolygonPatch_Impl( getExteriorRing(), getInteriorRings(), m_srsName );
      return poly.intersects( gmo );
    }
    catch( final GM_Exception e )
    {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public boolean contains( final GM_Object gmo )
  {
    try
    {
      if( gmo instanceof GM_Point )
      {
        final GM_Point point = (GM_Point) gmo;
        final GM_Point transformedPoint = (GM_Point) point.transform( m_srsName );
        return contains( transformedPoint.getPosition() );
      }

      final GM_PolygonPatch_Impl poly = new GM_PolygonPatch_Impl( getExteriorRing(), getInteriorRings(), m_srsName );
      return poly.contains( gmo );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public GM_Point getCentroid( )
  {
    final Coordinate a = JTSAdapter.export( m_p1 );
    final Coordinate b = JTSAdapter.export( m_p2 );
    final Coordinate c = JTSAdapter.export( m_p3 );
    final Coordinate centroid = Triangle.centroid( a, b, c );

    final GM_Position pos2d = GeometryFactory.createGM_Position( centroid.x, centroid.y );
    final double centroidZ = getValue( pos2d );

    final GM_Position pos3d = GeometryFactory.createGM_Position( centroid.x, centroid.y, centroidZ );
    return GeometryFactory.createGM_Point( pos3d, m_srsName );
  }

  @Override
  public double getArea( )
  {
    final Coordinate a = JTSAdapter.export( m_p1 );
    final Coordinate b = JTSAdapter.export( m_p2 );
    final Coordinate c = JTSAdapter.export( m_p3 );

    return Triangle.area( a, b, c );
  }

  @Override
  public double getPerimeter( )
  {
    final double d1 = m_p1.getDistance( m_p2 );
    final double d2 = m_p2.getDistance( m_p3 );
    final double d3 = m_p3.getDistance( m_p1 );

    return d1 + d2 + d3;
  }

  @Override
  public GM_Envelope getEnvelope( )
  {
    final GM_Envelope env = GeometryFactory.createGM_Envelope( m_p1, m_p2, m_srsName );
    return env.getMerged( m_p3 );
  }

  @Override
  public void invalidate( )
  {
    // no ned for invalidatrion, we keep no state...
  }

  @Override
  public int hashCode( )
  {
    final Plane plane = createPlane();

    return plane.hashCode();
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( this == obj )
      return true;

    if( getClass() != obj.getClass() )
      return false;

    final GM_Triangle_Impl other = (GM_Triangle_Impl) obj;

    final GM_Position[] exteriorRing = this.getExteriorRing();

    for( final GM_Position pos : other.getExteriorRing() )
    {
      // REMARK: linear search in the array of 3 elements is faster than hashing!
      if( !ArrayUtils.contains( exteriorRing, pos ) )
      {
        return false;
      }
    }

    return true;
  }
}