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
package org.kalypso.zml.ui.table.model.cells;

import org.eclipse.jface.viewers.ViewerCell;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.schema.AbstractColumnType;
import org.kalypso.zml.core.table.schema.DataColumnType;
import org.kalypso.zml.core.table.schema.IndexColumnType;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableSelectionHandler;
import org.kalypso.zml.ui.table.model.AbstractZmlTableElement;
import org.kalypso.zml.ui.table.model.columns.IZmlTableColumn;
import org.kalypso.zml.ui.table.model.rows.IZmlTableHeaderRow;
import org.kalypso.zml.ui.table.model.rows.IZmlTableRow;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractZmlTableCell extends AbstractZmlTableElement implements IZmlTableCell
{
  private final IZmlTableColumn m_column;

  private final IZmlTableRow m_row;

  protected AbstractZmlTableCell( final IZmlTableRow row, final IZmlTableColumn column )
  {
    super( column.getTable() );

    m_column = column;
    m_row = row;
  }

  @Override
  public IZmlTableColumn getColumn( )
  {
    return m_column;
  }

  @Override
  public IZmlTableRow getRow( )
  {
    return m_row;
  }

  @Override
  public int getIndex( )
  {
    return m_row.getIndex();
  }

  @Override
  public ViewerCell getViewerCell( )
  {
    final IZmlTable table = m_column.getTable();
    final IZmlTableSelectionHandler handler = table.getSelectionHandler();

    return handler.toViewerCell( this );
  }

  public static IZmlTableCell create( final IZmlTableRow row, final IZmlTableColumn column )
  {
    if( row instanceof IZmlTableHeaderRow )
      return new ZmlTableHeaderCell( row, column );

    final BaseColumn type = column.getColumnType();
    final AbstractColumnType baseType = type.getType();

    if( baseType instanceof IndexColumnType )
      return new ZmlTableIndexCell( row, column );
    else if( baseType instanceof DataColumnType )
      return new ZmlTableValueCell( row, column );

    throw new UnsupportedOperationException();
  }
}
