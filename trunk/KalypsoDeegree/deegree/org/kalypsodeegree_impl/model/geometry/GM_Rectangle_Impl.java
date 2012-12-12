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

import org.apache.commons.lang3.ArrayUtils;
import org.kalypso.transformation.transformer.GeoTransformerException;
import org.kalypsodeegree.model.geometry.GM_AbstractSurfacePatch;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Rectangle;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.VectorMath;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author Stefan Kurzbach
 */
final class GM_Rectangle_Impl implements GM_Rectangle
{
  private final GM_Position m_p1;

  private final GM_Position m_p2;

  private final GM_Position m_p3;

  private final GM_Position m_p4;

  private final String m_srsName;

  public GM_Rectangle_Impl( final GM_Position p1, final GM_Position p2, final GM_Position p3, final GM_Position p4, final String srsName )
  {
    m_p1 = p1;
    m_p2 = p2;
    m_p3 = p3;
    m_p4 = p4;
    m_srsName = srsName;
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
    final GM_Position t4 = m_p4.transform( sourceCRS, targetCRS );

    return GeometryFactory.createGM_Rectangle( t1, t2, t3, t4, targetCRS );
  }

  /**
   * Returns a deep copy of the geometry.
   */
  @Override
  public Object clone( )
  {
    final GM_Position c1 = (GM_Position)m_p1.clone();
    final GM_Position c2 = (GM_Position)m_p2.clone();
    final GM_Position c3 = (GM_Position)m_p3.clone();
    final GM_Position c4 = (GM_Position)m_p4.clone();

    return GeometryFactory.createGM_Rectangle( c1, c2, c3, c4, getCoordinateSystem() );
  }

  @Override
  public GM_Position[] getExteriorRing( )
  {
    return new GM_Position[] { m_p1, m_p2, m_p3, m_p4, m_p1 };
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
        final GM_Point point = (GM_Point)gmo;
        final GM_Point transformedPoint = (GM_Point)point.transform( m_srsName );
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

  private boolean contains( final GM_Position position )
  {
    final Coordinate[] ring = JTSAdapter.export( getExteriorRing() );
    final Coordinate pt = new Coordinate( position.getX(), position.getY() );
    return CGAlgorithms.isPointInRing( pt, ring );
  }

  @Override
  public GM_Point getCentroid( )
  {
    double x = (m_p1.getX() + m_p2.getX() + m_p3.getX() + m_p4.getX()) / 4;
    double y = (m_p1.getY() + m_p2.getY() + m_p3.getY() + m_p4.getY()) / 4;

    final GM_Position pos2d = GeometryFactory.createGM_Position( x, y );
    return GeometryFactory.createGM_Point( pos2d, m_srsName );
  }

  @Override
  public double getArea( )
  {
    // untested, calculates the cross product of the diagonal vectors (p X v)
    // area is |p X v|/2
    final Coordinate p = new Coordinate( m_p1.getX() - m_p3.getX(), m_p1.getY() - m_p3.getY() );
    final Coordinate v = new Coordinate( m_p2.getX() - m_p4.getX(), m_p2.getY() - m_p4.getY() );
    final Coordinate pxv = VectorMath.crossProduct( p, v );
    return Math.sqrt( pxv.x * pxv.x + pxv.y * pxv.y ) / 2.0;
  }

  @Override
  public double getPerimeter( )
  {
    final double d1 = m_p1.getDistance( m_p2 );
    final double d2 = m_p2.getDistance( m_p3 );
    final double d3 = m_p3.getDistance( m_p4 );
    final double d4 = m_p4.getDistance( m_p1 );

    return d1 + d2 + d3 + d4;
  }

  @Override
  public GM_Envelope getEnvelope( )
  {
    // there might be a more efficient way
    final GM_Envelope env = GeometryFactory.createGM_Envelope( m_p1, m_p2, m_srsName );
    return env.getMerged( m_p3 ).getMerged( m_p4 );
  }

  @Override
  public void invalidate( )
  {
    // no ned for invalidatrion, we keep no state...
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( this == obj )
      return true;

    if( getClass() != obj.getClass() )
      return false;

    final GM_Rectangle_Impl other = (GM_Rectangle_Impl)obj;

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