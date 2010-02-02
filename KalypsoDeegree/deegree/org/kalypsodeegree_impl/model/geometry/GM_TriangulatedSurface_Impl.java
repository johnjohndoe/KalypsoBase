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

import java.util.ArrayList;
import java.util.List;

import org.deegree.crs.transformations.CRSTransformation;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;

import com.vividsolutions.jts.geom.Envelope;

/**
 * @author Gernot Belger
 */
public class GM_TriangulatedSurface_Impl extends GM_PolyhedralSurface_Impl<GM_Triangle> implements GM_TriangulatedSurface
{
  public GM_TriangulatedSurface_Impl( final String crs ) throws GM_Exception
  {
    this( new ArrayList<GM_Triangle>(), crs );
  }

  public GM_TriangulatedSurface_Impl( final List<GM_Triangle> items, final String crs ) throws GM_Exception
  {
    super( items, crs );
  }

  /**
   * @see org.kalypsodeegree_impl.model.geometry.GM_Primitive_Impl#getAdapter(java.lang.Class)
   */
  @SuppressWarnings("unchecked")
  @Override
  public Object getAdapter( final Class adapter )
  {
    if( adapter == GM_Triangle[].class )
    {
      return toArray( new GM_Triangle[size()] );
    }
    return super.getAdapter( adapter );
  }

  /**
   * @see org.kalypsodeegree.model.geometry.GM_TriangulatedSurface#getValue(org.kalypsodeegree.model.geometry.GM_Position)
   */
  public double getValue( final GM_Point location )
  {
    // TODO: transform to my own crs

    final GM_Position position = location.getPosition();
    return getValue( position );
  }

  /**
   * @see org.kalypsodeegree.model.geometry.GM_TriangulatedSurface#getValue(org.kalypsodeegree.model.geometry.GM_Position)
   */
  @SuppressWarnings("unchecked")
  public double getValue( final GM_Position position )
  {
    final Envelope searchEnv = new Envelope( position.getX(), position.getX(), position.getY(), position.getY() );
    final List<GM_Triangle> query = m_index.query( searchEnv );
    for( final GM_Triangle triangle : query )
    {
      if( triangle.contains( position ) )
        return triangle.getValue( position );
    }

    return Double.NaN;
  }

  @SuppressWarnings("unchecked")
  public GM_Triangle getTriangle( final GM_Position position )
  {
    final Envelope searchEnv = new Envelope( position.getX(), position.getX(), position.getY(), position.getY() );
    final List<GM_Triangle> query = m_index.query( searchEnv );
    for( final GM_Triangle triangle : query )
    {
      if( triangle.contains( position ) )
        return triangle;
    }

    return null;
  }

  /**
   * Must override, else the wrong type is created.
   * 
   * @see org.kalypsodeegree.model.geometry.GM_Object#transform(org.deegree.crs.transformations.CRSTransformation,
   *      java.lang.String)
   */
  @Override
  public GM_Object transform( final CRSTransformation trans, final String targetOGCCS ) throws Exception
  {
    /* If the target is the same coordinate system, do not transform. */
    final String coordinateSystem = getCoordinateSystem();
    if( coordinateSystem == null || coordinateSystem.equalsIgnoreCase( targetOGCCS ) )
      return this;

    final int cnt = size();
    final GM_Triangle[] triangles = new GM_Triangle[cnt];
    for( int i = 0; i < cnt; i++ )
      triangles[i] = (GM_Triangle) get( i ).transform( trans, targetOGCCS );

    return GeometryFactory.createGM_TriangulatedSurface( triangles, targetOGCCS );
  }
}
