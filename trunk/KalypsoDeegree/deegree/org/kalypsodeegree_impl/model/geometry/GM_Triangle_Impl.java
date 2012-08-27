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

import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Ring;
import org.kalypsodeegree.model.geometry.GM_SurfacePatch;
import org.kalypsodeegree.model.geometry.GM_Triangle;

/**
 * @author Gernot Belger
 */
public class GM_Triangle_Impl extends GM_Polygon_Impl implements GM_Triangle
{
  public GM_Triangle_Impl( final GM_Position pos1, final GM_Position pos2, final GM_Position pos3, final String crs ) throws GM_Exception
  {
    super( new GM_Position[] { pos1, pos2, pos3, pos1 }, null, crs );
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
    final double x = position.getX();
    final double y = position.getY();

    final GM_Position[] exteriorRing = getExteriorRing();
    final GM_Position c0 = exteriorRing[0];
    final GM_Position c1 = exteriorRing[1];
    final GM_Position c2 = exteriorRing[2];

    // FIXME: was cached before; check if this is needed; memory consumption!
    final Plane plane = new Plane();
    final Point3d p0 = new Point3d( c0.getX(), c0.getY(), c0.getZ() );
    final Point3d p1 = new Point3d( c1.getX(), c1.getY(), c1.getZ() );
    final Point3d p2 = new Point3d( c2.getX(), c2.getY(), c2.getZ() );
    plane.setPlane( p0, p1, p2 );

    return plane.z( x, y );
  }

  @Override
  public boolean contains( final GM_Position position )
  {
    // REMARK: directly computing 'isInside' for performance reasons. jts PointInRing is actually slower (because for
    // arbitrary polygons) and more memory consuming.

    final GM_Position[] exteriorRing = getExteriorRing();
    final GM_Position c0 = exteriorRing[0];
    final GM_Position c1 = exteriorRing[1];
    final GM_Position c2 = exteriorRing[2];

    final int a1 = orientation( c0, c1, position );
    final int a2 = orientation( c1, c2, position );
    final int a3 = orientation( c2, c0, position );

    return (a1 == a2) && (a2 == a3);
  }

  // FIXME: JTS!
  private static int orientation( final GM_Position pos1, final GM_Position pos2, final GM_Position pos3 )
  {
    final double s_a = signedArea( pos1, pos2, pos3 );
    return s_a > 0 ? 1 : (s_a < 0 ? -1 : 0);
  }

  // FIXME: JTS!
  private static double signedArea( final GM_Position a, final GM_Position b, final GM_Position c )
  {
    return ((c.getX() - a.getX()) * (b.getY() - a.getY()) - (b.getX() - a.getX()) * (c.getY() - a.getY())) / 2;
  }

  @Override
  public GM_SurfacePatch transform( final String targetCRS ) throws Exception
  {
    /* If the target is the same coordinate system, do not transform. */
    final String sourceCRS = getCoordinateSystem();
    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( targetCRS ) )
      return this;

    final GM_Ring exRing = GeometryFactory.createGM_Ring( getExteriorRing(), getCoordinateSystem() );
    final GM_Ring transExRing = (GM_Ring) exRing.transform( targetCRS );
    final GM_Position[] positions = transExRing.getPositions();
    return GeometryFactory.createGM_Triangle( positions[0], positions[1], positions[2], targetCRS );
  }

  /**
   * Returns a deep copy of the geometry.
   */
  @Override
  public Object clone( )
  {
    try
    {
      final GM_Position[] clonedExteriorRing = GeometryFactory.cloneGM_Position( getExteriorRing() );
      return new GM_Triangle_Impl( clonedExteriorRing[0], clonedExteriorRing[1], clonedExteriorRing[2], getCoordinateSystem() );
    }
    catch( final GM_Exception e )
    {
      e.printStackTrace();
    }

    throw new IllegalStateException();
  }

  @Override
  public int getOrientation( )
  {
    return orientation( getExteriorRing()[0], getExteriorRing()[1], getExteriorRing()[2] );
  }

  @Override
  public double getMinValue( )
  {
    final GM_Position[] ring = getExteriorRing();
    double min = ring[0].getZ();

    if( min > ring[1].getZ() )
      min = ring[1].getZ();

    if( min > ring[2].getZ() )
      min = ring[2].getZ();

    return min;
  }

  @Override
  public double getMaxValue( )
  {
    final GM_Position[] ring = getExteriorRing();
    double max = ring[0].getZ();

    if( max < ring[1].getZ() )
      max = ring[1].getZ();

    if( max < ring[2].getZ() )
      max = ring[2].getZ();

    return max;
  }
}