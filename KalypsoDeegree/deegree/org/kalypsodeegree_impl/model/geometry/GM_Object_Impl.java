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

import org.eclipse.core.runtime.PlatformObject;
import org.kalypsodeegree.model.geometry.GM_Boundary;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.tools.Debug;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.TopologyException;

/**
 * Default implementation of the GM_Object interface from package deegree.model. The implementation is abstract because
 * only the management of the spatial reference system is unique for all geometries.
 * <p>
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$ $Date$
 */
public abstract class GM_Object_Impl extends PlatformObject implements GM_Object, Serializable
{
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 130728662284673112L;

  /** Placeholder if the boundary cannot be created */
  protected static final GM_Boundary EMPTY_BOUNDARY = new GM_CurveBoundary_Impl( null, null, null );

  /** Placeholder if the centroid cannot be created */
  protected static final GM_Point EMPTY_CENTROID = new GM_Point_Impl( Double.NaN, Double.NaN, null );

  /** Placeholder if the envelope cannot be created */
  protected static final GM_Envelope EMPTY_ENVELOPE = new GM_Envelope_Impl( Double.NaN, Double.NaN, Double.NaN, Double.NaN, null );

  private String m_crs = null;

  private GM_Boundary m_boundary = null;

  private GM_Envelope m_envelope = null;

  private GM_Point m_centroid = null;

  private boolean m_valid = false;

  /**
   * constructor that sets the spatial reference system
   * 
   * @param crs
   *          new spatial reference system
   */
  protected GM_Object_Impl( final String crs )
  {
    setCoordinateSystem( crs );
  }

  /**
   * returns the spatial reference system of a geometry
   */
  public String getCoordinateSystem( )
  {
    return m_crs;
  }

  /**
   * sets the spatial reference system
   * 
   * @param crs
   *          new spatial reference system
   */
  public void setCoordinateSystem( final String crs )
  {
    m_crs = crs;
  }

  /**
   * returns a deep copy of the geometry. this isn't realized at this level so a CloneNotSupportedException will be
   * thrown.
   */
  @Override
  public Object clone( ) throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException();
  }

  /**
   * returns true if no geometry values resp. points stored within the geometry.<br>
   * Currently not implemented, always returns true<br>
   * TODO: decide if it should be implemented or be thrown away
   */
  public boolean isEmpty( )
  {
    return true;
  }

  /**
   * returns the boundary of the surface as general boundary
   */
  public final GM_Boundary getBoundary( )
  {
    // only recalculate the boundary if null
    if( m_boundary == null )
    {
      try
      {
        m_boundary = calculateBoundary();
      }
      catch( final GM_Exception e )
      {
        // TODO: exception should be thrown by this method
        e.printStackTrace();
        m_boundary = EMPTY_BOUNDARY;
      }
    }

    // if boundary is empty, return null
    if( m_boundary == EMPTY_BOUNDARY )
      return null;

    return m_boundary;
  }

  /**
   * Calculate the boundary of this geometry.<br>
   * Must be overridden by implementors.<br>
   * Default implementation returns the invalid boundary.
   */
  abstract protected GM_Boundary calculateBoundary( ) throws GM_Exception;

  /**
   * dummy implementation of this method
   */
  public void translate( final double[] d )
  {
    invalidate();
  }

  /**
   * The operation "distance" shall return the distance between this GM_Object and another GM_Object. This distance is
   * defined to be the greatest lower bound of the set of distances between all pairs of points that include one each
   * from each of the two GM_Objects. A "distance" value shall be a positive number associated to distance units such as
   * meters or standard foot. If necessary, the second geometric object shall be transformed into the same coordinate
   * reference system as the first before the distance is calculated.
   * <p>
   * </p>
   * If the geometric objects overlap, or touch, then their distance apart shall be zero. Some current implementations
   * use a "negative" distance for such cases, but the approach is neither consistent between implementations, nor
   * theoretically viable.
   * <p>
   * </p>
   * dummy implementation
   */
  public double distance( final GM_Object gmo )
  {
    // ziemlicher hack, um die distance zu ermitteln, vermutlich sehr teuer (=langsam)

    try
    {
      final Geometry otherGmo = JTSAdapter.export( gmo );
      final Geometry thisGmo = JTSAdapter.export( this );

      return otherGmo.distance( thisGmo );
    }
    catch( final GM_Exception e )
    {
      e.printStackTrace();
    }

    return -9999;
  }

  /**
   * The operation "centroid" shall return the mathematical centroid for this GM_Object. The result is not guaranteed to
   * be on the object. For heterogeneous collections of primitives, the centroid only takes into account those of the
   * largest dimension. For example, when calculating the centroid of surfaces, an average is taken weighted by area.
   * Since curves have no area they do not contribute to the average. <br>
   * TODO: check this comment; this seems not be always implemented like described...
   */
  public GM_Point getCentroid( )
  {
    if( m_centroid == null )
    {
      // Only recalculate centroid if invalid (=null)
      try
      {
        m_centroid = calculateCentroid();
      }
      catch( final GM_Exception e )
      {
        e.printStackTrace();
        // TODO: we should throw this exception
        m_centroid = EMPTY_CENTROID;
      }
    }

    // if empty, just return null
    if( m_centroid == EMPTY_CENTROID )
      return null;

    return m_centroid;
  }

  protected abstract GM_Point calculateCentroid( ) throws GM_Exception;

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

  abstract protected GM_Envelope calculateEnvelope( ) throws GM_Exception;

  /**
   * The operation "convexHull" shall return a GM_Object that represents the convex hull of this GM_Object.
   * <p>
   * </p>
   * dummy implementation
   * 
   * @throws GM_Exception
   */
  public GM_Object getConvexHull( ) throws GM_Exception
  {
    // let JTS do this stuff (doemming)
    final Geometry geometry = JTSAdapter.export( this );
    final Geometry convexHull = geometry.convexHull();
    final GM_Object result = JTSAdapter.wrap( convexHull );
    ((GM_Object_Impl) result).setCoordinateSystem( getCoordinateSystem() );
    return result;
  }

  /**
   * The operation "buffer" shall return a GM_Object containing all points whose distance from this GM_Object is less
   * than or equal to the "distance" passed as a parameter. The GM_Object returned is in the same reference system as
   * this original GM_Object. The dimension of the returned GM_Object is normally the same as the coordinate dimension -
   * a collection of GM_Surfaces in 2D space and a collection of GM_Solids in 3D space, but this may be application
   * defined.
   * <p>
   * </p>
   * dummy implementation
   */
  public GM_Object getBuffer( final double distance )
  {
    try
    {
      final Geometry export = JTSAdapter.export( this );
      final Geometry poly = export.buffer( distance );
      return JTSAdapter.wrap( poly );
    }
    catch( final GM_Exception e )
    {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * The Boolean valued operation "contains" shall return TRUE if this GM_Object contains another GM_Object.
   * <p>
   * 
   * @param that
   *          the GM_Object to test (whether is is contained)
   * @return true if the given object is contained, else false
   */
  public boolean contains( final GM_Object that )
  {
    try
    {
      // let JTS do the hard work
      final Geometry jtsThis = JTSAdapter.export( this );
      final Geometry jtsThat = JTSAdapter.export( that );
      return jtsThis.contains( jtsThat );
    }
    catch( final GM_Exception e )
    {
      System.out.println( e );
      return false;
    }
  }

  /**
   * The Boolean valued operation "contains" shall return TRUE if this GM_Object contains a single point given by a
   * coordinate.
   * <p>
   * 
   * @param position
   *          GM_Position to test (whether is is contained)
   * @return true if the given object is contained, else false
   */
  public boolean contains( final GM_Position position )
  {
    return contains( new GM_Point_Impl( position, null ) );
  }

  /**
   * The Boolean valued operation "intersects" shall return TRUE if this GM_Object intersects another GM_Object. Within
   * a GM_Complex, the GM_Primitives do not intersect one another. In general, topologically structured data uses shared
   * geometric objects to capture intersection information.
   * 
   * @param that
   *          the GM_Object to intersect with
   * @return true if the objects intersects, else false
   */
  public boolean intersects( final GM_Object that )
  {
    try
    {
      // let JTS do the hard work
      final Geometry jtsThis = JTSAdapter.export( this );
      final Geometry jtsThat = JTSAdapter.export( that );
      return jtsThis.intersects( jtsThat );
    }
    catch( final GM_Exception e )
    {
      System.out.println( e );
      return false;
    }
  }

  /**
   * The "union" operation shall return the set theoretic union of this GM_Object and the passed GM_Object.
   * <p>
   * 
   * @param that
   *          the GM_Object to unify
   * @return intersection or null, if computation failed
   */
  public GM_Object union( final GM_Object that )
  {
    GM_Object union = null;

    try
    {
      // let JTS do the hard work
      final Geometry jtsThis = JTSAdapter.export( this );
      final Geometry jtsThat = JTSAdapter.export( that );
      final Geometry jtsUnion = jtsThis.union( jtsThat );

      if( !jtsUnion.isEmpty() )
      {
        union = JTSAdapter.wrap( jtsUnion );
        ((GM_Object_Impl) union).setCoordinateSystem( getCoordinateSystem() );
      }
    }
    catch( final GM_Exception e )
    {
      System.out.println( e );
    }
    return union;
  }

  /**
   * The "intersection" operation shall return the set theoretic intersection of this <tt>GM_Object</tt> and the passed
   * <tt>GM_Object</tt>.
   * <p>
   * 
   * @param that
   *          the GM_Object to intersect with
   * @return intersection or null, if it is empty (or computation failed)
   */
  public GM_Object intersection( final GM_Object that )
  {
    GM_Object intersection = null;

    try
    {
      // let JTS do the hard work
      final Geometry jtsThis = JTSAdapter.export( this );
      final Geometry jtsThat = JTSAdapter.export( that );
      final Geometry jtsIntersection = jtsThis.intersection( jtsThat );

      if( !jtsIntersection.isEmpty() )
      {
        intersection = JTSAdapter.wrap( jtsIntersection );
        ((GM_Object_Impl) intersection).setCoordinateSystem( getCoordinateSystem() );
      }
    }
    catch( final GM_Exception e )
    {
      System.out.println( e );
    }
    /* May be the case if there are two identical points in one object, return null in this case */
    catch( final IllegalArgumentException e )
    {
      System.out.println( e );
    }

    return intersection;
  }

  /**
   * The "difference" operation shall return the set theoretic difference of this GM_Object and the passed GM_Object.
   * <p>
   * 
   * @param that
   *          the GM_Object to calculate the difference with
   * @return difference or null, if it is empty (or computation failed)
   */
  public GM_Object difference( final GM_Object that )
  {
    GM_Object difference = null;

    try
    {
      // let JTS do the hard work
      final Geometry jtsThis = JTSAdapter.export( this );
      final Geometry jtsThat = JTSAdapter.export( that );

      final Geometry jtsDifference = jtsThis.difference( jtsThat );

      if( !jtsDifference.isEmpty() )
      {
        difference = JTSAdapter.wrap( jtsDifference );
        ((GM_Object_Impl) difference).setCoordinateSystem( getCoordinateSystem() );
      }
    }
    catch( final GM_Exception e )
    {
      System.out.println( e );
    }
    catch( final TopologyException e )
    {
      System.out.println( e );
    }

    return difference;
  }

  /**
   * Compares the GM_Object to be equal to another GM_Object.
   * <p>
   * 
   * @param that
   *          the GM_Object to test for equality
   * @return true if the objects are equal, else false
   */
  public @Override
  boolean equals( final Object that )
  {
    if( that == this )
      return true;

    if( that == null || !(that instanceof GM_Object_Impl) )
      return false;

    if( m_crs != null )
    {
      if( !m_crs.equals( ((GM_Object) that).getCoordinateSystem() ) )
        return false;
    }
    else
    {
      if( ((GM_Object) that).getCoordinateSystem() != null )
      {
        return false;
      }
    }

    try
    {
      // let JTS do the hard work
      final Geometry jtsThis = JTSAdapter.export( this );
      final Geometry jtsThat = JTSAdapter.export( (GM_Object) that );
      return jtsThis.equals( jtsThat );
    }
    catch( final GM_Exception e )
    {
      System.out.println( e );
      return false;
    }
  }

  /*
   * provide optimized proximity queries within for a distance . calvin added on 10/21/2003
   */
  public boolean isWithinDistance( final GM_Object that, final double distance )
  {
    if( that == null )
      return false;
    try
    {
      // let JTS do the hard work
      final Geometry jtsThis = JTSAdapter.export( this );
      final Geometry jtsThat = JTSAdapter.export( that );
      return jtsThis.isWithinDistance( jtsThat, distance );
    }
    catch( final GM_Exception e )
    {
      Debug.debugException( e, "" );
      return false;
    }

  }

  /**
   * returns true if the calculated parameters of the GM_Object are valid and false if they must be recalculated
   */
  protected boolean isValid( )
  {
    return m_valid;
  }

  @Override
  public String toString( )
  {
    String ret = null;
    ret = "CoordinateSystem = " + m_crs + "\n";
    ret += ("mute = " + GM_Position.MUTE + "\n");
    return ret;
  }

  /**
   * @see org.kalypsodeegree.model.geometry.GM_Object#invalidate()
   */
  public void invalidate( )
  {
    m_boundary = null;
    m_centroid = null;
    m_envelope = null;
    m_valid = false;
  }

  /**
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  @SuppressWarnings("unchecked")
  @Override
  public Object getAdapter( final Class adapter )
  {
    if( adapter == this.getClass() )
      return this;

    if( adapter == GM_Point.class )
      return getCentroid();

    return super.getAdapter( adapter );
  }
}