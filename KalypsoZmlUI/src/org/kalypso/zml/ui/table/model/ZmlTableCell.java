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
package org.kalypso.zml.ui.table.model;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableSelectionHandler;

import com.google.common.base.Objects;

/**
 * @author Dirk Kuch
 */
public class ZmlTableCell extends ZmlTableElement implements IZmlTableCell
{
  private final IZmlTableColumn m_column;

  private final IZmlTableRow m_row;

  public ZmlTableCell( final IZmlTableRow row, final IZmlTableColumn column )
  {
    super( column.getTable() );

    m_column = column;
    m_row = row;
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof ZmlTableCell )
    {
      final ZmlTableCell other = (ZmlTableCell) obj;
      return getValueReference() == other.getValueReference();
    }

    return super.equals( obj );
  }

  @Override
  public int hashCode( )
  {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append( getValueReference() );

    return builder.toHashCode();
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
  public IZmlValueReference getValueReference( )
  {
    return m_row.getValueReference( m_column );
  }

  @Override
  public int getIndex( )
  {
    return m_row.getIndex();
  }

  @Override
  public IZmlTableCell findPreviousCell( )
  {
    final int index = getIndex();
    if( index <= 0 )
      return null;

    final IZmlTable table = getTable();
    final IZmlTableRow previousRow = table.getRow( index - 1 );

    return new ZmlTableCell( previousRow, m_column );
  }

  @Override
  public IZmlTableCell findNextCell( )
  {
    final IZmlTable table = getTable();

    final int index = getIndex();
    if( table.getRows().length <= index )
      return null;

    final IZmlTableRow nextRow = table.getRow( index + 1 );

    return new ZmlTableCell( nextRow, m_column );
  }

  @Override
  public int findIndex( )
  {
    final TableColumn base = getColumn().getTableViewerColumn().getColumn();

    final TableViewer tableViewer = getTable().getViewer();
    final Table table = tableViewer.getTable();
    final TableColumn[] columns = table.getColumns();
    for( final TableColumn col : columns )
    {
      if( Objects.equal( base, col ) )
      {
        return ArrayUtils.indexOf( columns, col );
      }
    }

    return 2;
  }

  @Override
  public ViewerCell getViewerCell( )
  {
    final IZmlTable table = m_column.getTable();
    final IZmlTableSelectionHandler handler = table.getSelectionHandler();

    return handler.toViewerCell( this );
  }
}
