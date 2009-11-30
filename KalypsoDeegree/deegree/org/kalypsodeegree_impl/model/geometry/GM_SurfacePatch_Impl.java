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
import java.util.Arrays;

import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_GenericSurface;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_SurfaceInterpolation;
import org.kalypsodeegree.model.geometry.GM_SurfacePatch;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

/**
 * default implementation of the GM_SurfacePatch interface from package jago.model. the class is abstract because it
 * should be specialized by derived classes <code>GM_Polygon</code> for example
 * ------------------------------------------------------------
 * 
 * @version 11.6.2001
 * @author Andreas Poth
 */
abstract class GM_SurfacePatch_Impl implements GM_SurfacePatch, GM_GenericSurface, Serializable
{
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 7641735268892225180L;

  static GM_SurfaceInterpolation INTERPOLATION_NONE = new GM_SurfaceInterpolation_Impl();

  /** Placeholder if the centroid cannot be created */
  protected static final GM_Point EMPTY_CENTROID = new GM_Point_Impl( Double.NaN, Double.NaN, null );

  /** Placeholder if the envelope cannot be created */
  protected static final GM_Envelope EMPTY_ENVELOPE = new GM_Envelope_Impl( Double.NaN, Double.NaN, Double.NaN, Double.NaN, null );

  private String m_crs = null;

  private GM_Envelope m_envelope = null;

  private GM_Point m_centroid = null;

  // Not used anywhere; so for the moment we can just return a constant...
// protected GM_SurfaceInterpolation m_interpolation = null;

  private GM_Position[] m_exteriorRing = null;

  private GM_Position[][] m_interiorRings = null;

  private double m_area = Double.NaN;

  /**
   * Creates a new GM_SurfacePatch_Impl object.
   * 
   * @param interpolation
   * @param exteriorRing
   * @param interiorRings
   * @param crs
   * @throws GM_Exception
   */
  protected GM_SurfacePatch_Impl( final GM_Position[] exteriorRing, final GM_Position[][] interiorRings, final String crs ) throws GM_Exception
  {
    m_crs = crs;

    // REMARK: we need at least 4 points, as the first and last must be identical, else we do not get a non-corrupt
    // surface
    if( exteriorRing == null || exteriorRing.length < 4 )
    {
      throw new GM_Exception( "The exterior ring doesn't contains enough point!" );
    }

    // check, if the exteriorRing of the polygon is closed
    // and if the interiorRings (if !=null) are closed
    if( !exteriorRing[0].equals( exteriorRing[exteriorRing.length - 1] ) )
    {
      System.out.println( exteriorRing[0] );
      System.out.println( exteriorRing[exteriorRing.length - 1] );
      throw new GM_Exception( "The exterior ring isn't closed!" );
    }

    if( interiorRings != null )
    {
      for( int i = 0; i < interiorRings.length; i++ )
      {
        if( !interiorRings[i][0].equals( interiorRings[i][interiorRings[i].length - 1] ) )
        {
          throw new GM_Exception( "The interior ring " + i + " isn't closed!" );
        }
      }
    }

    m_exteriorRing = exteriorRing;
    // Memory Optimize: do not remember empty interior rings, we have to check for null anyway
    m_interiorRings = interiorRings == null || interiorRings.length == 0 ? null : interiorRings;
  }

  /**
   * returns the bounding box / envelope of a geometry
   */
  public GM_Envelope getEnvelope( )
  {
    if( m_envelope == null )
    {
      // Only recalculate envelope if invalid (=null)
      try
      {
        m_envelope = calculateEnvelope();
      }
      catch( final GM_Exception e )
      {
        e.printStackTrace();
        // TODO: we should throw this exception
        m_envelope = EMPTY_ENVELOPE;
      }
    }

    // if empty, just return null
    if( m_envelope == EMPTY_ENVELOPE )
      return null;

    return m_envelope;
  }

  @SuppressWarnings("unused")
  protected GM_Envelope calculateEnvelope( ) throws GM_Exception
  {
    return GeometryUtilities.envelopeFromRing( m_exteriorRing, getCoordinateSystem() );
  }
  /**
   * returns a reference to the exterior ring of the surface
   */
  public GM_Position[] getExteriorRing( )
  {
    return m_exteriorRing;
  }

  /**
   * returns a reference to the interior rings of the surface
   */
  public GM_Position[][] getInteriorRings( )
  {
    return m_interiorRings;
  }

  /**
   * returns the length of all boundaries of the surface in a reference system appropriate for measuring distances.
   */
  public double getPerimeter( )
  {
    return -1;
  }

  /**
   * returns the coordinate system of the surface patch
   */
  public String getCoordinateSystem( )
  {
    return m_crs;
  }

  /**
   * This function sets the coordinate system.
   * 
   * @param crs
   *          The coordinate system.
   */
  public void setCoordinateSystem( final String crs )
  {
    m_crs = crs;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object other )
  {
    if( (other == null) || !(other instanceof GM_SurfacePatch_Impl) )
    {
      return false;
    }

    // Assuming envelope cannot be null (always calculated)
    if( !m_envelope.equals( ((GM_SurfacePatch) other).getEnvelope() ) )
    {
      return false;
    }

    // Assuming exteriorRing cannot be null (checked by Constructor)
    if( !Arrays.equals( m_exteriorRing, ((GM_SurfacePatch) other).getExteriorRing() ) )
    {
      return false;
    }

    // Assuming either can have interiorRings set to null (not checked
    // by Constructor)
    if( m_interiorRings != null )
    {
      if( ((GM_SurfacePatch) other).getInteriorRings() == null )
      {
        return false;
      }

      if( m_interiorRings.length != ((GM_SurfacePatch) other).getInteriorRings().length )
      {
        return false;
      }

      for( int i = 0; i < m_interiorRings.length; i++ )
      {
        if( !Arrays.equals( m_interiorRings[i], ((GM_SurfacePatch) other).getInteriorRings()[i] ) )
        {
          return false;
        }
      }
    }
    else
    {
      if( ((GM_SurfacePatch) other).getInteriorRings() != null )
      {
        return false;
      }
    }

    return true;
  }

  public GM_Point getCentroid( )
  {
    if( m_centroid == null )
    {
      // Only recalculate centroid if invalid (=null)
      m_centroid = calculateCentroidArea();
    }

    // if empty, just return null
    if( m_centroid == EMPTY_CENTROID )
      return null;

    return m_centroid;
  }

  /**
   * The operation "area" shall return the area of this GM_GenericSurface. The area of a 2 dimensional geometric object
   * shall be a numeric measure of its surface area Since area is an accumulation (integral) of the product of two
   * distances, its return value shall be in a unit of measure appropriate for measuring distances squared.
   */
  public double getArea( )
  {
    // TODO: Still a bit hacky: centroid and area are calculated at the same moment;
    // we just make sure that the centroid is recalculated by calling getCentroid
    getCentroid();
    return m_area;
  }

  /**
   * calculates the centroid and area of the surface patch. this method is only valid for the two-dimensional case.
   */
  protected GM_Point calculateCentroidArea( )
  {
    final GM_Position centroid_ = GeometryUtilities.centroidFromRing( m_exteriorRing );
    double varea = calculateArea( m_exteriorRing );

    double x = centroid_.getX();
    double y = centroid_.getY();

    x *= varea;
    y *= varea;

    if( m_interiorRings != null )
    {
      for( final GM_Position[] element : m_interiorRings )
      {
        final double dum = -1 * calculateArea( element );
        final GM_Position temp = GeometryUtilities.centroidFromRing( element );
        x += (temp.getX() * dum);
        y += (temp.getY() * dum);
        varea += dum;
      }
    }

    m_area = varea;
    return new GM_Point_Impl( x / varea, y / varea, m_crs );
  }

  /**
   * calculates the area of the surface patch
   * <p>
   * </p>
   * taken from gems iv (modified)
   * <p>
   * </p>
   * this method is only valid for the two-dimensional case.
   */
  private double calculateArea( final GM_Position[] point )
  {
    int i;
    int j;
    double ai;
    double atmp = 0;

    for( i = point.length - 1, j = 0; j < point.length; i = j, j++ )
    {
      final double xi = point[i].getX() - point[0].getX();
      final double yi = point[i].getY() - point[0].getY();
      final double xj = point[j].getX() - point[0].getX();
      final double yj = point[j].getY() - point[0].getY();
      ai = (xi * yj) - (xj * yi);
      atmp += ai;
    }

    return Math.abs( atmp / 2 );
  }

  @Override
  public String toString( )
  {
    String ret = "GM_SurfacePatch: ";
    ret = "interpolation = " + INTERPOLATION_NONE + "\n";
    ret += "exteriorRing = \n";

    for( final GM_Position element : m_exteriorRing )
      ret += (element + "\n");

    ret += ("interiorRings = " + m_interiorRings + "\n");
    ret += ("envelope = " + m_envelope + "\n");
    return ret;
  }

  public void invalidate( )
  {
    m_envelope = null;
    m_centroid = null;
    m_area = Double.NaN;
  }

  /**
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone( ) throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException();
  }
}