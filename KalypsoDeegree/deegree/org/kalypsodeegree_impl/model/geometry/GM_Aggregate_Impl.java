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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.kalypsodeegree.model.geometry.GM_Aggregate;
import org.kalypsodeegree.model.geometry.GM_Boundary;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.tools.Debug;

/**
 * default implementierung of the GM_Aggregate interface ------------------------------------------------------------
 * 
 * @version 8.6.2001
 * @author Andreas Poth href="mailto:poth@lat-lon.de"
 */
abstract class GM_Aggregate_Impl extends GM_Object_Impl implements GM_Aggregate, Serializable
{
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 1161164609227432958L;

  protected List<GM_Object> m_aggregate;

  /**
   * Creates a new GM_Aggregate_Impl object.
   * 
   * @param crs
   */
  public GM_Aggregate_Impl( final String crs )
  {
    super( crs );

    m_aggregate = new ArrayList<GM_Object>();
  }

  /**
   * Creates a new GM_Aggregate_Impl object.
   * 
   * @param crs
   */
  public GM_Aggregate_Impl( final GM_Object[] children, final String crs )
  {
    super( crs );

    m_aggregate = new ArrayList<GM_Object>( children.length );
    for( final GM_Object gmObject : children )
      m_aggregate.add( gmObject );
  }

  /**
   * returns the number of GM_Object within the aggregation
   */
  @Override
  public int getSize( )
  {
    return m_aggregate.size();
  }

  /**
   * merges this aggregation with another one
   * 
   * @exception GM_Exception
   *              a GM_Exception will be thrown if the submitted isn't the same type as the recieving one.
   */
  @Override
  public void merge( final GM_Aggregate aggregate ) throws GM_Exception
  {
    if( !this.getClass().getName().equals( aggregate.getClass().getName() ) )
    {
      throw new GM_Exception( "Aggregations are not of the same type!" );
    }

    for( int i = 0; i < this.getSize(); i++ )
    {
      this.add( aggregate.getObjectAt( i ) );
    }

    invalidate();
  }

  /**
   * adds an GM_Object to the aggregation
   */
  @Override
  public void add( final GM_Object gmo )
  {
    m_aggregate.add( gmo );

    invalidate();
  }

  /**
   * inserts a GM_Object in the aggregation. all elements with an index equal or larger index will be moved. if index is
   * larger then getSize() - 1 or smaller then 0 or gmo equals null an exception will be thrown.
   * 
   * @param gmo
   *          GM_Object to insert.
   * @param index
   *          position where to insert the new GM_Object
   */
  @Override
  public void insertObjectAt( final GM_Object gmo, final int index ) throws GM_Exception
  {
    if( (index < 0) || (index > this.getSize() - 1) )
    {
      throw new GM_Exception( "invalid index/position: " + index + " to insert a geometry!" );
    }

    if( gmo == null )
    {
      throw new GM_Exception( "gmo == null. it isn't possible to insert a value" + " that equals null!" );
    }

    m_aggregate.add( index, gmo );

    invalidate();
  }

  /**
   * sets the submitted GM_Object at the submitted index. the element at the position <code>index</code> will be
   * removed. if index is larger then getSize() - 1 or smaller then 0 or gmo equals null an exception will be thrown.
   * 
   * @param gmo
   *          GM_Object to set.
   * @param index
   *          position where to set the new GM_Object
   */
  @Override
  public void setObjectAt( final GM_Object gmo, final int index ) throws GM_Exception
  {
    if( index < 0 )
    {
      throw new GM_Exception( "invalid index/position: " + index + " to set a geometry!" );
    }

    if( gmo == null )
    {
      throw new GM_Exception( "gmo == null. it isn't possible to set a value" + " that equals null!" );
    }

   ((ArrayList<GM_Object>) m_aggregate).ensureCapacity( index + 1 );

   ((ArrayList<GM_Object>) m_aggregate).add( index, gmo );

    invalidate();
  }

  /**
   * removes the submitted GM_Object from the aggregation
   * 
   * @return the removed GM_Object
   */
  @Override
  public GM_Object removeObject( final GM_Object gmo )
  {
    if( gmo == null )
    {
      return null;
    }

    final int i = m_aggregate.indexOf( gmo );

    GM_Object gmo_ = null;

    try
    {
      gmo_ = removeObjectAt( i );
    }
    catch( final GM_Exception e )
    {
      Debug.debugException( e, "" );
    }

    invalidate();

    return gmo_;
  }

  /**
   * removes the GM_Object at the submitted index from the aggregation. if index is larger then getSize() - 1 or smaller
   * then 0 an exception will be thrown.
   * 
   * @return the removed GM_Object
   */
  @Override
  public GM_Object removeObjectAt( final int index ) throws GM_Exception
  {
    if( index < 0 )
    {
      return null;
    }

    if( index > (this.getSize() - 1) )
    {
      throw new GM_Exception( "invalid index/position: " + index + " to remove a geometry!" );
    }

    final GM_Object gmo = m_aggregate.remove( index );

    invalidate();

    return gmo;
  }

  /**
   * removes all GM_Object from the aggregation.
   */
  @Override
  public void removeAll( )
  {
    m_aggregate.clear();
    invalidate();
  }

  /**
   * returns the GM_Object at the submitted index. if index is larger then getSize() - 1 or smaller then 0 an exception
   * will be thrown.
   */
  @Override
  public GM_Object getObjectAt( final int index )
  {
    return m_aggregate.get( index );
  }

  /**
   * returns all GM_Objects as array
   */
  @Override
  public GM_Object[] getAll( )
  {
    final GM_Object[] gmos = new GM_Object[this.getSize()];

    return m_aggregate.toArray( gmos );
  }

  /**
   * returns true if the submitted GM_Object is within the aggregation
   */
  @Override
  public boolean isMember( final GM_Object gmo )
  {
    return m_aggregate.contains( gmo );
  }

  /**
   * returns the aggregation as an iterator
   */
  @Override
  public Iterator<GM_Object> getIterator( )
  {
    return m_aggregate.iterator();
  }

  /**
   * returns true if no geometry stored within the collection.
   */
  @Override
  public boolean isEmpty( )
  {
    return (getSize() == 0);
  }

  /**
   * sets the spatial reference system
   * 
   * @param crs
   *          new spatial reference system
   */
  @Override
  public void setCoordinateSystem( final String crs )
  {
    super.setCoordinateSystem( crs );

    if( m_aggregate != null )
    {
      for( int i = 0; i < m_aggregate.size(); i++ )
      {
        ((GM_Object_Impl) getObjectAt( i )).setCoordinateSystem( crs );
      }
      invalidate();
    }
  }

  /**
   * translate the point by the submitted values. the <code>dz</code>- value will be ignored.
   */
  @Override
  public void translate( final double[] d )
  {
    try
    {
      for( int i = 0; i < getSize(); i++ )
      {
        final GM_Object gmo = getObjectAt( i );
        gmo.translate( d );
      }
    }
    catch( final Exception e )
    {
      Debug.debugException( e, "" );
    }
    invalidate();
  }

  @Override
  public boolean equals( final Object other )
  {
    if( other == this )
      return true;

    if( !super.equals( other ) || !(other instanceof GM_Aggregate_Impl) )
      return false;

    // envelope was not valid
    if( !ObjectUtils.equals( getEnvelope(), ((GM_Object) other).getEnvelope() ) )
      return false;

    if( getSize() != ((GM_Aggregate) other).getSize() )
      return false;

    try
    {
      for( int i = 0; i < getSize(); i++ )
      {
        final Object o1 = getObjectAt( i );
        final Object o2 = ((GM_Aggregate) other).getObjectAt( i );

        if( !o1.equals( o2 ) )
        {
          return false;
        }
      }
    }
    catch( final Exception ex )
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

    try
    {
      for( int i = 0; i < m_aggregate.size(); i++ )
      {
        if( this.getObjectAt( i ).intersects( gmo ) )
        {
          inter = true;
          break;
        }
      }
    }
    catch( final Exception e )
    {
    }

    return inter;
  }

  @Override
  public String toString( )
  {
    String ret = null;
    ret = "aggregate = " + m_aggregate + "\n";
    ret += ("envelope = " + getEnvelope() + "\n");
    return ret;
  }

  /**
   * @see org.kalypsodeegree_impl.model.geometry.GM_Object_Impl#invalidate()
   */
  @Override
  public void invalidate( )
  {
    for( final GM_Object gmobj : m_aggregate )
      gmobj.invalidate();

    super.invalidate();
  }

  /**
   * @see org.kalypsodeegree_impl.model.geometry.GM_Object_Impl#getAdapter(java.lang.Class)
   */
  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class adapter )
  {
    /* An array of GM_xxx adapts to the array of its adapters. */
    final Class< ? > componentType = adapter.getComponentType();
    if( componentType != null && GM_Object.class.isAssignableFrom( componentType ) )
    {
      final List<GM_Object> adaptedObjects = new ArrayList<GM_Object>();

      for( final GM_Object objectToAdapt : m_aggregate )
      {
        final GM_Object adaptedObject = (GM_Object) objectToAdapt.getAdapter( componentType );
        if( adaptedObject != null && componentType.isAssignableFrom( adaptedObject.getClass() ) )
          adaptedObjects.add( adaptedObject );
      }

      final Object adaptedArray = Array.newInstance( componentType, adaptedObjects.size() );
      return adaptedObjects.toArray( (GM_Object[]) adaptedArray );
    }

    return super.getAdapter( adapter );
  }

  /**
   * @see org.kalypsodeegree_impl.model.geometry.GM_Object_Impl#calculateBoundary()
   */
  @Override
  protected GM_Boundary calculateBoundary( )
  {
    // TODO: implement: what is the boundary of a GM_Aggregate?
    return GM_Constants.EMPTY_BOUNDARY;
  }

  /**
   * calculates the bounding box / envelope of the aggregation
   */
  @Override
  protected GM_Envelope calculateEnvelope( )
  {
    if( getSize() == 0 )
      return GM_Constants.EMPTY_ENVELOPE;

    final GM_Envelope bb = getObjectAt( 0 ).getEnvelope();

    double minX = bb.getMinX();
    double minY = bb.getMinY();
    double maxX = bb.getMaxX();
    double maxY = bb.getMaxY();

    final int size = getSize();
    for( int i = 1; i < size; i++ )
    {
      final GM_Object object = getObjectAt( i );
      final GM_Envelope envelope = object.getEnvelope();

      minX = Math.min( minX, envelope.getMinX() );
      minY = Math.min( minY, envelope.getMinY() );
      maxX = Math.max( maxX, envelope.getMaxX() );
      maxY = Math.max( maxY, envelope.getMaxY() );
    }

    return GeometryFactory.createGM_Envelope( minX, minY, maxX, maxY, getCoordinateSystem() );
  }

}