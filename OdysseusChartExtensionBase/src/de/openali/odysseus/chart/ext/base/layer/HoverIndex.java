/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;

import de.openali.odysseus.chart.framework.model.layer.EditInfo;

/**
 * @author Gernot Belger
 */
public class HoverIndex
{
  private final SpatialIndex m_index = new RTree();

  private final List<EditInfo> m_elements = new ArrayList<>();

  public HoverIndex( )
  {
    m_index.init( null );
  }

  public synchronized void addElement( final Rectangle rect, final EditInfo info )
  {
    final int id = m_elements.size();

    m_elements.add( info );

    final com.infomatiq.jsi.Rectangle jsiRect = new com.infomatiq.jsi.Rectangle( rect.x, rect.y, rect.x + rect.width, rect.y + rect.height );
    m_index.add( jsiRect, id );
  }

  public synchronized EditInfo findElement( final Point pos )
  {
    final com.infomatiq.jsi.Point searchPoint = new com.infomatiq.jsi.Point( pos.x, pos.y );

    final List<EditInfo> result = new ArrayList<>( 1 );
    result.add( null );

    final List<EditInfo> elements = m_elements;

    final TIntProcedure receiver = new TIntProcedure()
    {
      @Override
      public boolean execute( final int index )
      {
        final EditInfo element = elements.get( index );
        result.set( 0, element );
        return false;
      }
    };

    // REMARK: use snap distance 0, we assume that the given rectangle was already including a 'snap'
    m_index.nearest( searchPoint, receiver, 0f );

    final EditInfo info = result.get( 0 );
    if( info == null )
      return null;

    /* Position was not set during paint... */
    return new EditInfo( info.getLayer(), info.getHoverFigure(), info.getEditFigure(), info.getData(), info.getText(), pos );
  }
}