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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.kalypso.transformation.transformer.GeoTransformerException;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Ring;
import org.kalypsodeegree.model.geometry.GM_SurfaceBoundary;

/**
 * default implementation of the GM_SurfaceBoundary interface.
 * ------------------------------------------------------------
 *
 * @version 11.6.2001
 * @author Andreas Poth href="mailto:poth@lat-lon.de"
 */
class GM_SurfaceBoundary_Impl extends GM_PrimitiveBoundary_Impl implements GM_SurfaceBoundary
{
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 1399131144729310956L;

  public GM_Ring m_exterior = null;

  public GM_Ring[] m_interior = null;

  /**
   * constructor
   */
  public GM_SurfaceBoundary_Impl( final GM_Ring exterior, final GM_Ring[] interior )
  {
    super( exterior.getCoordinateSystem() );
    m_exterior = exterior;
    m_interior = interior;
  }

  /**
   * gets the exterior ring
   */
  @Override
  public GM_Ring getExteriorRing( )
  {
    return m_exterior;
  }

  /**
   * gets the interior ring(s)
   */
  @Override
  public GM_Ring[] getInteriorRings( )
  {
    return m_interior;
  }

  /**
   * checks if this curve is completly equal to the submitted geometry
   *
   * @param other
   *          object to compare to
   */
  @Override
  public boolean equals( final Object other )
  {
    if( !super.equals( other ) || !(other instanceof GM_SurfaceBoundary_Impl) )
    {
      return false;
    }

    if( !m_exterior.equals( ((GM_SurfaceBoundary) other).getExteriorRing() ) )
    {
      return false;
    }

    if( m_interior != null )
    {
      final GM_Ring[] r1 = getInteriorRings();
      final GM_Ring[] r2 = ((GM_SurfaceBoundary) other).getInteriorRings();

      if( !Arrays.equals( r1, r2 ) )
      {
        return false;
      }
    }
    else
    {
      if( ((GM_SurfaceBoundary) other).getInteriorRings() != null )
      {
        return false;
      }
    }

    return true;
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
    return 1;
  }

  /**
   * The operation "coordinateDimension" shall return the dimension of the coordinates that define this GM_Object, which
   * must be the same as the coordinate dimension of the coordinate reference system for this GM_Object.
   */
  @Override
  public int getCoordinateDimension( )
  {
    return m_exterior.getPositions()[0].getCoordinateDimension();
  }

  /**
   * returns a copy of the geometry
   */
  @Override
  public Object clone( ) throws CloneNotSupportedException
  {
    // kuch
    final GM_Ring myExteriorRing = (GM_Ring) getExteriorRing().clone();

    final GM_Ring[] interiorRings = getInteriorRings();
    final List<GM_Ring> myInteriorRings = new LinkedList<>();
    for( final GM_Ring ring : interiorRings )
    {
      myInteriorRings.add( (GM_Ring) ring.clone() );
    }

    return new GM_SurfaceBoundary_Impl( myExteriorRing, myInteriorRings.toArray( new GM_Ring[] {} ) );
  }

  /**
   * The Boolean valued operation "intersects" shall return TRUE if this GM_Object intersects another GM_Object. Within
   * a GM_Complex, the GM_Primitives do not intersect one another. In general, topologically structured data uses shared
   * geometric objects to capture intersection information.
   */
  @Override
  public boolean intersects( final GM_Object gmo )
  {
    boolean inter = m_exterior.intersects( gmo );

    if( !inter )
    {
      if( m_interior != null )
      {
        for( final GM_Ring element : m_interior )
        {
          if( element.intersects( gmo ) )
          {
            inter = true;
            break;
          }
        }
      }
    }

    return inter;
  }

  /**
   * The Boolean valued operation "contains" shall return TRUE if this GM_Object contains another GM_Object. <br/>
   * At the moment the operation just works with point geometries
   */
  @Override
  public boolean contains( final GM_Object gmo )
  {
    if( !m_exterior.contains( gmo ) )
      return false;

    if( m_interior == null )
      return true;

    for( final GM_Ring element : m_interior )
    {
      if( element.contains( gmo ) )
        return false;
    }

    return true;
  }

  /**
   * The Boolean valued operation "contains" shall return TRUE if this GM_Object contains a single point given by a
   * coordinate.
   */
  @Override
  public boolean contains( final GM_Position position )
  {
    return contains( new GM_Point_Impl( position, getCoordinateSystem() ) );
  }

  /**
   * calculates the envelope of the surface boundary
   */
  @Override
  protected GM_Envelope calculateEnvelope( )
  {
    return m_exterior.getEnvelope();
  }

  /**
   * calculates the centroid of the surface boundary
   */
  @Override
  protected GM_Point calculateCentroid( ) throws GM_Exception
  {
    final double[] cen = m_exterior.getCentroid().getAsArray().clone();
    double cnt = m_exterior.getAsCurveSegment().getNumberOfPoints();

    for( int i = 0; i < cen.length; i++ )
    {
      cen[i] *= cnt;
    }

    if( m_interior != null )
    {
      for( final GM_Ring element : m_interior )
      {
        final double[] pos = element.getCentroid().getAsArray();
        cnt += element.getAsCurveSegment().getNumberOfPoints();

        for( int j = 0; j < pos.length; j++ )
        {
          cen[j] += pos[j] * element.getAsCurveSegment().getNumberOfPoints();
        }
      }
    }

    for( int j = 0; j < cen.length; j++ )
    {
      cen[j] /= cnt;
    }

    return new GM_Point_Impl( GeometryFactory.createGM_Position( cen ), getCoordinateSystem() );
  }

  @Override
  public String toString( )
  {
    String ret = null;
    ret = "interior = " + m_interior + "\n";
    ret += "exterior = " + m_exterior + "\n";
    return ret;
  }

  @Override
  public void invalidate( )
  {
    m_exterior.invalidate();
    for( final GM_Object gmobj : m_interior )
    {
      gmobj.invalidate();
    }
  }

  @Override
  public GM_Object transform( final String targetCRS ) throws GeoTransformerException
  {
    /* If the target is the same coordinate system, do not transform. */
    final String sourceCRS = getCoordinateSystem();
    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( targetCRS ) )
      return this;

    /* exterior ring */
    final GM_Ring ex = getExteriorRing();
    final GM_Ring transEx = (GM_Ring) ex.transform( targetCRS );

    /* interior rings */
    final GM_Ring[] in = getInteriorRings();
    final GM_Ring[] transIn = new GM_Ring[in.length];

    for( int j = 0; j < in.length; j++ )
      transIn[j] = (GM_Ring) in[j].transform( targetCRS );

    return new GM_SurfaceBoundary_Impl( transEx, transIn );
  }
}