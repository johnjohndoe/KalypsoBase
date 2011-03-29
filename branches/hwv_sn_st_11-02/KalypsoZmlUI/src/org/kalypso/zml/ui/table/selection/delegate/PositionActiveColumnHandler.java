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
package org.kalypso.zml.ui.table.selection.delegate;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Point;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.ui.table.ZmlTableComposite;
import org.kalypso.zml.ui.table.base.helper.ZmlTables;
import org.kalypso.zml.ui.table.model.IZmlTableCell;
import org.kalypso.zml.ui.table.model.IZmlTableColumn;
import org.kalypso.zml.ui.table.model.IZmlTableRow;
import org.kalypso.zml.ui.table.model.ZmlTableCell;

/**
 * @author Dirk Kuch
 */
public class PositionActiveColumnHandler
{
  private final ZmlTableComposite m_table;

  private final Point m_position;

  public PositionActiveColumnHandler( final ZmlTableComposite table, final Point position )
  {
    m_table = table;
    m_position = position;
  }

  public IZmlTableColumn findActiveColumn( )
  {
    final ViewerCell cell = findActiveViewerCell();
    if( Objects.isNull( cell ) )
      return null;

    return m_table.findColumn( cell.getColumnIndex() );
  }

  public ViewerCell findActiveViewerCell( )
  {
    if( Objects.isNull( m_position ) )
      return null;

    final ViewerCell viewerCell = m_table.getTableViewer().getCell( m_position );
    if( Objects.isNull( viewerCell ) )
      return null;

    return null;
  }

  public IZmlTableCell findActiveCell( )
  {
    final IZmlTableColumn column = findActiveColumn();

    final IZmlTableRow row = findActiveRow();
    if( column == null || row == null )
      return null;

    final ZmlTableCell cell = new ZmlTableCell( row, column );

    return cell;
  }

  public IZmlTableRow findActiveRow( )
  {
    final ViewerCell cell = findActiveViewerCell();
    if( cell == null )
      return null;

    return ZmlTables.toTableRow( m_table, cell );

  }

}
