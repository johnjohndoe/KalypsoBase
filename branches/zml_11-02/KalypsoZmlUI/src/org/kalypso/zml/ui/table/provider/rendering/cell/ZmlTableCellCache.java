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
package org.kalypso.zml.ui.table.provider.rendering.cell;

import org.kalypso.zml.ui.table.model.columns.IZmlTableColumn;
import org.kalypso.zml.ui.table.model.rows.IZmlTableRow;

/**
 * @author Dirk Kuch
 */
public class ZmlTableCellCache
{
  @Deprecated
  public ZmlTableCellCache( )
  {
  }

  public IZmlTableCellPainter getPainter( final IZmlTableRow row, final IZmlTableColumn column )
  {
    if( !column.isVisible() )
      return null;

    synchronized( this )
    {
      // FIXME do some caching
//
// final IZmlTableCell cell = row.getCell( column );
// if( cell == null )
// return null;
// else if( cell instanceof IZmlTableHeaderCell )
// return new ZmlTableHeaderCellPainter( (IZmlTableHeaderCell) cell );
// else if( cell instanceof IZmlTableIndexCell )
// return new ZmlTableIndexCellPainter( (IZmlTableIndexCell) cell );
// else if( cell instanceof IZmlTableValueCell )
// return new ZmlTableValueCellPainter( (IZmlTableValueCell) cell );

      throw new UnsupportedOperationException();
    }
  }

  public synchronized void clear( )
  {
  }

}
