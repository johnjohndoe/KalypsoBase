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

import java.util.Iterator;

import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.transformation.transformer.GeoTransformerException;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.geometry.GM_AbstractSurfacePatch;
import org.kalypsodeegree.model.geometry.GM_Boundary;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree.model.geometry.GM_PolygonPatch;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Ring;
import org.kalypsodeegree.model.geometry.GM_SurfaceBoundary;
import org.kalypsodeegree.model.geometry.ISurfacePatchVisitor;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

/**
 * default implementation of the GM_Surface interface from package jago.model.
 * <p>
 * </p>
 * for simplicity of the implementation it is assumed that a surface is build from just one surface patch. this isn'GM_PolygonPatch
 * completly confrom to the ISO 19107 and the OGC GAIA specification but sufficient for most applications.
 * <p>
 * </p>
 * It will be extended to fullfill the complete specs as soon as possible.
 * <p>
 * -----------------------------------------------------------------------
 * </p>
 * 
 * @version 05.04.2002
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 */
final class GM_Polygon_Impl extends GM_AbstractSurface_Impl<GM_PolygonPatch> implements GM_Polygon
{
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = -2148069106391096842L;

  private final GM_PolygonPatch m_patch;

  /**
   * initializes the surface with default orientation submitting one surface patch.
   * 
   * @param surfacePatch
   *          patches of the surface.
   */
  public GM_Polygon_Impl( final GM_PolygonPatch surfacePatch ) throws GM_Exception
  {
    this( '+', surfacePatch );
  }

  /**
   * initializes the surface submitting the orientation and one surface patch.
   * 
   * @param surfacePatch
   *          patches of the surface.
   */
  public GM_Polygon_Impl( final char orientation, final GM_PolygonPatch surfacePatch ) throws GM_Exception
  {
    super( surfacePatch.getCoordinateSystem(), orientation );

// m_list = Collections.singletonList( surfacePatch );
    m_patch = surfacePatch;
  }

  /**
   * initializes the surface with default orientation submitting the surfaces boundary
   * 
   * @param boundary
   *          boundary of the surface
   */
  public GM_Polygon_Impl( final GM_SurfaceBoundary boundary ) throws GM_Exception
  {
    this( '+', boundary );
  }

  /**
   * initializes the surface submitting the orientation and the surfaces boundary.
   * 
   * @param boundary
   *          boundary of the surface
   */
  public GM_Polygon_Impl( final char orientation, final GM_SurfaceBoundary boundary ) throws GM_Exception
  {
    super( boundary.getCoordinateSystem(), orientation );

    m_patch = GeometryFactory.createGM_PolygonPatch( boundary.getExteriorRing(), boundary.getInteriorRings(), boundary.getCoordinateSystem() );
  }

  /**
   * calculates the boundary and area of the surface
   */
  @Override
  protected GM_Boundary calculateBoundary( ) throws GM_Exception
  {
    final GM_Ring ext = new GM_Ring_Impl( m_patch.getExteriorRing(), getCoordinateSystem() );
    final GM_Position[][] inn_ = m_patch.getInteriorRings();
    GM_Ring[] inn = null;

    if( inn_ != null )
    {
      inn = new GM_Ring_Impl[inn_.length];

      for( int i = 0; i < inn_.length; i++ )
      {
        inn[i] = new GM_Ring_Impl( inn_[i], getCoordinateSystem() );
      }
    }

    return new GM_SurfaceBoundary_Impl( ext, inn );
  }

  /**
   * @see org.kalypsodeegree_impl.model.geometry.GM_Object_Impl#calculateCentroid()
   */
  @Override
  protected GM_Point calculateCentroid( )
  {
    return GeometryUtilities.guessPointOnSurface( this, m_patch.getCentroid(), 3 );
  }

  /**
   * Optimization: we do not need to instantiate a new envelope, just get the one from the patch
   * 
   * @see org.kalypsodeegree_impl.model.geometry.GM_Object_Impl#getEnvelope()
   */
  @Override
  public GM_Envelope getEnvelope( )
  {
    return m_patch.getEnvelope();
  }

  /**
   * @see org.kalypsodeegree_impl.model.geometry.GM_Object_Impl#calculateEnvelope()
   */
  @Override
  protected GM_Envelope calculateEnvelope( )
  {
    // as we overwrite getEnvelope, this should never be called
    throw new UnsupportedOperationException();
  }

  /**
   * returns the length of all boundaries of the surface in a reference system appropriate for measuring distances.
   */
  @Override
  public double getPerimeter( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * The operation "area" shall return the area of this GM_GenericSurface. The area of a 2 dimensional geometric object
   * shall be a numeric measure of its surface area Since area is an accumulation (integral) of the product of two
   * distances, its return value shall be in a unit of measure appropriate for measuring distances squared.
   */
  @Override
  public double getArea( )
  {
    return m_patch.getArea();
  }

  /**
   * returns the boundary of the surface as surface boundary
   */
  @Override
  public GM_SurfaceBoundary getSurfaceBoundary( )
  {
    return (GM_SurfaceBoundary)getBoundary();
  }

  /**
   * returns the number of patches building the surface
   */
  public int getNumberOfSurfacePatches( )
  {
    return 1;
  }

  /**
   * returns the surface patch at the submitted index
   */
  @Override
  public GM_PolygonPatch getSurfacePatch( )
  {
    return m_patch;
  }

  /**
   * checks if this surface is completly equal to the submitted geometry
   * 
   * @param other
   *          object to compare to
   */
  @Override
  public boolean equals( final Object other )
  {
    if( !super.equals( other ) )
      return false;

    if( !(other instanceof GM_Polygon_Impl) )
      return false;

    if( !ObjectUtils.equals( getEnvelope(), ((GM_Object)other).getEnvelope() ) )
      return false;

    return ObjectUtils.equals( m_patch, m_patch );
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
    return 2;
  }

  /**
   * The operation "coordinateDimension" shall return the dimension of the coordinates that define this GM_Object, which
   * must be the same as the coordinate dimension of the coordinate reference system for this GM_Object.
   */
  @Override
  public int getCoordinateDimension( )
  {
    return m_patch.getExteriorRing()[0].getCoordinateDimension();
  }

  /**
   * returns a shallow copy of the geometry
   */
  @Override
  public Object clone( ) throws CloneNotSupportedException
  {
    try
    {
      final GM_PolygonPatch myPatch = (GM_PolygonPatch)m_patch.clone();

      return new GM_Polygon_Impl( getOrientation(), myPatch );
    }
    catch( final GM_Exception e )
    {
      e.printStackTrace();
    }

    throw new IllegalStateException();
  }

  /**
   * translate each point of the surface with the values of the submitted double array.
   */
  @Override
  public void translate( final double[] d )
  {
    final GM_Position[] ext = m_patch.getExteriorRing();
    final GM_Position[][] inn = m_patch.getInteriorRings();

    for( final GM_Position element : ext )
    {
      element.translate( d );
    }

    if( inn != null )
    {
      for( final GM_Position[] element : inn )
      {
        for( final GM_Position element2 : element )
        {
          element2.translate( d );
        }
      }
    }
    invalidate();
  }

  /**
   * The boolean valued operation "intersects" shall return TRUE if this <tt>GM_Surface_Impl</tt> intersects with the
   * given <tt>GM_Object</GM_PolygonPatch>.
   * Within a <tt>GM_Complex</tt>, the <tt>GM_Primitives</tt> do not intersect one another. In general, topologically
   * structured data uses shared geometric objects to capture intersection information.
   * 
   * @param gmo
   *          the <tt>GM_Object</tt> to test for intersection
   * @return true if the <tt>GM_Object</tt> intersects with this
   */
  @Override
  public boolean intersects( final GM_Object gmo )
  {
    return m_patch.contains( gmo ) || getBoundary().intersects( gmo );
  }

  /**
   * The Boolean valued operation "contains" shall return TRUE if this GM_Object contains a single point given by a
   * coordinate.
   */
  @Override
  public boolean contains( final GM_Position position )
  {
    return getBoundary().contains( position );
  }

  /**
   * The Boolean valued operation "contains" shall return TRUE if this GM_Object contains another GM_Object.
   */
  @Override
  public boolean contains( final GM_Object gmo )
  {
    return getBoundary().contains( gmo );
  }

  /**
   *
   */
  @Override
  public String toString( )
  {
    String ret = getClass().getName() + ":\n";

    ret += "envelope = " + getEnvelope() + "\n";
    ret += " CRS: " + getCoordinateSystem() + "\n";
    ret += "patch = " + m_patch + "\n";

    return ret;
  }

  @Override
  public void invalidate( )
  {
    if( m_patch != null )
      m_patch.invalidate();

    super.invalidate();
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    if( adapter == GM_AbstractSurfacePatch.class )
      return m_patch;

    if( adapter == GM_AbstractSurfacePatch[].class )
      return new GM_AbstractSurfacePatch[] { m_patch };

    if( adapter == GM_Curve.class )
    {
      final GM_AbstractSurfacePatch surfacePatchAt = m_patch;
      final GM_Position[] exteriorRing = surfacePatchAt.getExteriorRing();
      try
      {
        return GeometryFactory.createGM_Curve( exteriorRing, getCoordinateSystem() );
      }
      catch( final GM_Exception e )
      {
        final IStatus status = StatusUtilities.statusFromThrowable( e );
        KalypsoDeegreePlugin.getDefault().getLog().log( status );
        return null;
      }
    }

    return super.getAdapter( adapter );
  }

  @Override
  @Deprecated
  public GM_PolygonPatch get( final int index )
  {
    return getSurfacePatch();
  }

  @Override
  @Deprecated
  @SuppressWarnings( "unchecked" )
  public Iterator<GM_PolygonPatch> iterator( )
  {
    return new SingletonIterator( m_patch );
  }

  @Override
  @Deprecated
  public int size( )
  {
    return 1;
  }

  @Override
  public int hashCode( )
  {
    return m_patch.hashCode();
  }

  @Override
  public void acceptSurfacePatches( final GM_Envelope envToVisit, final ISurfacePatchVisitor<GM_PolygonPatch> visitor, final IProgressMonitor monitor ) throws CoreException
  {
    monitor.beginTask( StringUtils.EMPTY, 1 );

    visitor.visit( m_patch );

    ProgressUtilities.done( monitor );
  }

  @Override
  public GM_Object transform( final String targetCRS ) throws GeoTransformerException
  {
    try
    {
      /* If the target is the same coordinate system, do not transform. */
      final String sourceCRS = getCoordinateSystem();
      if( sourceCRS == null || sourceCRS.equalsIgnoreCase( targetCRS ) )
        return this;

      final GM_PolygonPatch patch = (GM_PolygonPatch)m_patch.transform( targetCRS );

      return new GM_Polygon_Impl( getOrientation(), patch );
    }
    catch( final GM_Exception e )
    {
      throw new GeoTransformerException( e );
    }
  }

  @Override
  public void setCoordinateSystem( final String crs )
  {
    super.setCoordinateSystem( crs );

    if( m_patch instanceof GM_AbstractSurfacePatch_Impl )
      ((GM_AbstractSurfacePatch_Impl)m_patch).setCoordinateSystem( crs );
  }

}