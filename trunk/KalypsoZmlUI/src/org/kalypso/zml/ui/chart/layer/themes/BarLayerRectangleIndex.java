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
package org.kalypso.zml.ui.chart.layer.themes;

import gnu.trove.TIntProcedure;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;

/**
 * @author Gernot Belger
 */
public class BarLayerRectangleIndex
{
  private final SpatialIndex m_index = new RTree();

  public BarLayerRectangleIndex( )
  {
    m_index.init( null );
  }

  public void addElement( final Rectangle rect, final int id )
  {
    final com.infomatiq.jsi.Rectangle jsiRect = new com.infomatiq.jsi.Rectangle( rect.x, rect.y, rect.x + rect.width, rect.y + rect.height );
    m_index.add( jsiRect, id );
  }

  public Pair<Rectangle, Integer> findElement( final Point pos )
  {
    final com.infomatiq.jsi.Point searchPoint = new com.infomatiq.jsi.Point( pos.x, pos.y );

    // Everything is in pixels here, so we directly use 5px
    final float snapDist = 5.0f;

    final int[] result = new int[] { -1 };
    final TIntProcedure v = new TIntProcedure()
    {
      @Override
      public boolean execute( final int index )
      {
        result[0] = index;
        return false;
      }
    };

    m_index.nearest( searchPoint, v, snapDist );

    if( result[0] == -1 )
      return null;

    // TODO: where to get rectangle from?
    return Pair.of( null, result[0] );
  }
}