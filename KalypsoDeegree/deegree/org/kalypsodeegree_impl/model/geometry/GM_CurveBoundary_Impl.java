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

import org.kalypso.transformation.transformer.GeoTransformerException;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_CurveBoundary;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_MultiPrimitive;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Polygon;

/**
 * default implementation of the GM_CurveBoundary interface from package jago.model.
 * <p>
 * ------------------------------------------------------------
 * </p>
 *
 * @version 10.6.2001
 * @author Andreas Poth
 */
class GM_CurveBoundary_Impl extends GM_PrimitiveBoundary_Impl implements GM_CurveBoundary
{
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 4226497939552424434L;

  private GM_Position m_ep = null;

  private GM_Position m_sp = null;

  /**
   * constructor of curve_boundary with CS_CoordinateSystem and startpoint and endpoint
   */
  public GM_CurveBoundary_Impl( final String crs, final GM_Position sp, final GM_Position ep )
  {
    super( crs );

    m_sp = sp;
    m_ep = ep;
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
    return getStartPoint().getCoordinateDimension();
  }

  /**
   * returns a shallow copy of the geometry
   */
  @Override
  public Object clone( )
  {
    // kuch
    final String system = getCoordinateSystem();

    final GM_Position[] sp = GeometryFactory.cloneGM_Position( new GM_Position[] { m_sp } );
    final GM_Position[] ep = GeometryFactory.cloneGM_Position( new GM_Position[] { m_ep } );

    return new GM_CurveBoundary_Impl( system, sp[0], ep[0] );
  }

  /**
   * returns the StartPoint of the boundary
   */
  @Override
  public GM_Position getStartPoint( )
  {
    return m_sp;
  }

  /**
   * returns the EndPoint of the boundary
   */
  @Override
  public GM_Position getEndPoint( )
  {
    return m_ep;
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
    if( !super.equals( other ) || !(other instanceof GM_CurveBoundary_Impl) )
    {
      return false;
    }

    if( !m_ep.equals( ((GM_CurveBoundary) other).getEndPoint() ) || !m_sp.equals( ((GM_CurveBoundary) other).getStartPoint() ) )
    {
      return false;
    }

    return true;
  }

  /**
   * The Boolean valued operation "intersects" shall return TRUE if this GM_Object intersects another GM_Object. Within
   * a GM_Complex, the GM_Primitives do not intersect one another. In general, topologically structured data uses shared
   * geometric objects to capture intersection information.
   */
  @Override
  public boolean intersects( final GM_Object gmo )
  {
    boolean inter = false;
    final GM_Point p1 = new GM_Point_Impl( m_sp, getCoordinateSystem() );
    final GM_Point p2 = new GM_Point_Impl( m_ep, getCoordinateSystem() );

    try
    {
      if( gmo instanceof GM_Point )
      {
        inter = LinearIntersects.intersects( p1, (GM_Point) gmo );

        if( !inter )
        {
          inter = LinearIntersects.intersects( p2, (GM_Point) gmo );
        }
      }
      else if( gmo instanceof GM_Curve )
      {
        inter = LinearIntersects.intersects( p1, (GM_Curve) gmo );

        if( !inter )
        {
          inter = LinearIntersects.intersects( p2, (GM_Curve) gmo );
        }
      }
      else if( gmo instanceof GM_Polygon )
      {
        inter = LinearIntersects.intersects( p1, (GM_Polygon) gmo );

        if( !inter )
        {
          inter = LinearIntersects.intersects( p2, (GM_Polygon) gmo );
        }
      }
      else if( gmo instanceof GM_MultiPrimitive )
      {
        inter = intersectsMultiPrimitive( (GM_MultiPrimitive) gmo );
      }
    }
    catch( final Exception e )
    {
    }

    return inter;
  }

  /**
   * the operations returns true if the submitted multi primitive intersects with the curve segment
   */
  private boolean intersectsMultiPrimitive( final GM_MultiPrimitive mprim ) throws Exception
  {
    boolean inter = false;

    final int cnt = mprim.getSize();

    for( int i = 0; i < cnt; i++ )
    {
      if( intersects( mprim.getPrimitiveAt( i ) ) )
      {
        inter = true;
        break;
      }
    }

    return inter;
  }

  @Override
  protected GM_Point calculateCentroid( )
  {
    // TODO: implement
    return GM_Constants.EMPTY_CENTROID;
  }

  /**
   * calculates the envelope of the curve boundary
   */
  @Override
  protected GM_Envelope calculateEnvelope( )
  {
    return GeometryFactory.createGM_Envelope( m_sp, m_ep, getCoordinateSystem() );
  }

  @Override
  public String toString( )
  {
    return "point1: [" + m_sp + "] - point2: [" + m_ep + "]";
  }

  @Override
  public GM_Object transform( final String targetCRS ) throws GeoTransformerException
  {
    /* If the target is the same coordinate system, do not transform. */
    final String sourceCRS = getCoordinateSystem();
    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( targetCRS ) )
      return this;

    final GM_Position transStartPos = getStartPoint().transform( sourceCRS, targetCRS );
    final GM_Position transEndPos = getEndPoint().transform( sourceCRS, targetCRS );

    return new GM_CurveBoundary_Impl( targetCRS, transStartPos, transEndPos );
  }
}