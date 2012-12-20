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
package de.openali.odysseus.chart.ext.base.layer;

import gnu.trove.TIntProcedure;

import org.eclipse.swt.graphics.Rectangle;

import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;

/**
 * Index that checks for collisions between painted markers.
 *
 * @author Gernot Belger
 */
public class CollisionIndex
{
  private final SpatialIndex m_index = new RTree();

  public CollisionIndex( )
  {
    m_index.init( null );
  }

  /**
   * Check if an collision occurs, and adds a new element to the index if no collision has occured.
   *
   * @return <code>true</code> if the element was added and no collision occured.
   */
  public boolean addAndCheck( final Rectangle rect, final int id )
  {
    final com.infomatiq.jsi.Rectangle indexRect = new com.infomatiq.jsi.Rectangle( rect.x, rect.y, rect.x + rect.width, rect.y + rect.height );

    if( checkCollission( indexRect ) )
      return false;

    m_index.add( indexRect, id );
    return true;
  }

  private boolean checkCollission( final com.infomatiq.jsi.Rectangle indexRect )
  {
    final boolean[] result = new boolean[] { false };

    final TIntProcedure ip = new TIntProcedure()
    {
      @Override
      public boolean execute( final int id )
      {
        result[0] = true;
        return false;
      }
    };

    m_index.intersects( indexRect, ip );

    return result[0];
  }
}