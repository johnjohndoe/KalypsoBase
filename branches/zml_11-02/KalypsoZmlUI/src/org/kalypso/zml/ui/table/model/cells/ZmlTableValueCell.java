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
package org.kalypso.zml.ui.table.model.cells;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.model.IZmlTableModel;
import org.kalypso.zml.ui.table.model.columns.IZmlTableColumn;
import org.kalypso.zml.ui.table.model.columns.IZmlTableValueColumn;
import org.kalypso.zml.ui.table.model.rows.IZmlTableHeaderRow;
import org.kalypso.zml.ui.table.model.rows.IZmlTableRow;
import org.kalypso.zml.ui.table.model.rows.IZmlTableValueRow;

/**
 * @author Dirk Kuch
 */
public class ZmlTableValueCell extends AbstractZmlTableCell implements IZmlTableValueCell
{

  protected ZmlTableValueCell( final IZmlTable table, final IZmlTableRow row, final IZmlTableColumn column )
  {
    super( table, row, column );
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof IZmlTableValueCell )
    {
      final IZmlTableValueCell other = (IZmlTableValueCell) obj;
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
  public IZmlModelValueCell getValueReference( )
  {
    return (IZmlModelValueCell) getRow().getModelCell( getColumn() );
  }

  @Override
  public IZmlTableValueColumn getColumn( )
  {
    return (IZmlTableValueColumn) super.getColumn();
  }

  @Override
  public IZmlTableValueRow getRow( )
  {
    return (IZmlTableValueRow) super.getRow();
  }

  @Override
  public IZmlTableValueCell findPreviousCell( )
  {
    final int index = getIndex();
    if( index <= 0 )
      return null;

    final IZmlTableModel model = getModel();
    final IZmlTableRow previousRow = model.getRow( index - 1 );
    if( previousRow instanceof IZmlTableHeaderRow )
      return null;

    return (IZmlTableValueCell) previousRow.getCell( getColumn() );
  }

  @Override
  public IZmlTableValueCell findNextCell( )
  {
    final IZmlTableModel model = getModel();

    final int index = getIndex();
    final IZmlTableRow[] rows = model.getRows();

    if( rows.length <= index )
      return null;

    final IZmlTableRow nextRow = model.getRow( index + 1 );
    if( nextRow == null )
      return null;
    else if( nextRow instanceof IZmlTableHeaderRow )
      return null;

    return (IZmlTableValueCell) nextRow.getCell( getColumn() );
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
}
