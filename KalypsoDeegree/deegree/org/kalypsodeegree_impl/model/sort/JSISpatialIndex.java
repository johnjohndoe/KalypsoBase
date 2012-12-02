/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypsodeegree_impl.model.sort;

import gnu.trove.TIntProcedure;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.kalypso.commons.java.lang.MathUtils;
import org.kalypsodeegree.graphics.transformation.GeoTransform;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.ItemVisitor;

/**
 * {@link SpatialIndexExt} implementation based on the JSI library.
 * 
 * @author Gernot Belger
 */
public class JSISpatialIndex implements SpatialIndexExt
{
  private final List<Object> m_items = new ArrayList<>();

  private final SpatialIndex m_index = new RTree();

  public JSISpatialIndex( )
  {
    m_index.init( null );
  }

  @Override
  public void insert( final Envelope itemEnv, final Object item )
  {
    final int index = m_items.size();
    m_items.add( item );

    if( itemEnv != null )
    {
      final Rectangle rectangle = toRectangle( itemEnv );

      m_index.add( rectangle, index );
    }
  }

  static Rectangle toRectangle( final Envelope itemEnv )
  {
    if( itemEnv == null )
      return null;

    final float x1 = MathUtils.floorFloat( itemEnv.getMinX() );
    final float y1 = MathUtils.floorFloat( itemEnv.getMinY() );
    final float x2 = MathUtils.ceilFloat( itemEnv.getMaxX() );
    final float y2 = MathUtils.ceilFloat( itemEnv.getMaxY() );
    return new Rectangle( x1, y1, x2, y2 );
  }

  @Override
  public List< ? > query( final Envelope searchEnv )
  {
    if( searchEnv == null )
      return Collections.unmodifiableList( m_items );

    final Rectangle searchBox = toRectangle( searchEnv );

    final List<Object> result = new LinkedList<>();

    final List<Object> items = m_items;
    final TIntProcedure ip = new TIntProcedure()
    {
      @Override
      public boolean execute( final int index )
      {
        result.add( items.get( index ) );
        return true;
      }
    };

    m_index.intersects( searchBox, ip );

    return result;
  }

  @Override
  public void query( final Envelope searchEnv, final ItemVisitor visitor )
  {
    final Rectangle searchBox = toRectangle( searchEnv );

    final List<Object> items = m_items;
    final TIntProcedure ip = new TIntProcedure()
    {
      @Override
      public boolean execute( final int index )
      {
        final Object item = items.get( index );
        visitor.visitItem( item );
        return true;
      }
    };

    m_index.intersects( searchBox, ip );
  }

  @Override
  public boolean remove( final Envelope itemEnv, final Object item )
  {
    final Rectangle bounds = toRectangle( itemEnv );

    if( bounds != null )
    {
      final int index = findItem( bounds, item );
      if( index == -1 )
        return false;

      m_items.remove( bounds );
      return m_index.delete( bounds, index );
    }

    /* Without bounds, we can only remove from our list here */
    return m_items.remove( bounds );
  }

  @Override
  public int size( )
  {
    return m_index.size();
  }

  @Override
  public void paint( final Graphics g, final GeoTransform geoTransform )
  {
    // don't known how to paint the index
  }

  @Override
  public Envelope getBoundingBox( )
  {
    final Rectangle bounds = m_index.getBounds();
    if( bounds == null )
      return null;

    final float x1 = bounds.minX;
    final float y1 = bounds.minY;
    final float x2 = bounds.maxX;
    final float y2 = bounds.maxY;

    return new Envelope( x1, x2, y1, y2 );
  }

  @Override
  public boolean contains( final Envelope itemEnv, final Object item )
  {
    if( itemEnv == null )
      return m_items.contains( item );

    final Rectangle rectangle = toRectangle( itemEnv );

    final int index = findItem( rectangle, item );
    return index != -1;
  }

  private int findItem( final Rectangle bounds, final Object item )
  {
    final List<Object> items = m_items;

    final int[] result = new int[] { -1 };

    final TIntProcedure ip = new TIntProcedure()
    {
      @Override
      public boolean execute( final int index )
      {
        final Object element = items.get( index );
        if( element == item )
        {
          result[0] = index;
          return false;
        }

        return true;
      }
    };

    m_index.contains( bounds, ip );

    return result[0];
  }
}