/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.zml.ui.table.cursor;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableSelectionHandler;
import org.kalypso.zml.ui.table.model.IZmlTableCell;
import org.kalypso.zml.ui.table.model.IZmlTableColumn;
import org.kalypso.zml.ui.table.model.IZmlTableRow;

/**
 * @author Dirk Kuch
 */
public class ZmlSelectionUpdater implements Runnable
{
  private final IZmlTable m_table;

  public ZmlSelectionUpdater( final IZmlTable table )
  {
    m_table = table;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run( )
  {
    final IZmlTableSelectionHandler handler = m_table.getSelectionHandler();
    final IZmlTableRow[] selectedRows = handler.getSelectedRows();
    if( ArrayUtils.isEmpty( selectedRows ) )
      return;

    final IZmlTableCell cell = handler.getActiveCell();
    if( Objects.isNull( cell ) )
      setInitialSelection( handler, selectedRows[0] );
    else if( !ArrayUtils.contains( selectedRows, cell.getRow() ) )
      updateSelection( handler, selectedRows[0], cell );
  }

  private void updateSelection( final IZmlTableSelectionHandler handler, final IZmlTableRow row, final IZmlTableCell cell )
  {
    final IZmlTableCell selectedCell = row.getCell( cell.getColumn() );
    handler.setFocusCell( selectedCell.getViewerCell() );
  }

  private void setInitialSelection( final IZmlTableSelectionHandler handler, final IZmlTableRow row )
  {
    final IZmlTableColumn[] columns = row.getColumns();
    if( ArrayUtils.isEmpty( columns ) )
      return;

    IZmlTableCell cell;
    if( columns.length > 1 )
      cell = row.getCell( columns[1] );
    else
      cell = row.getCell( columns[0] );

    handler.setFocusCell( cell.getViewerCell() );
  }

}
