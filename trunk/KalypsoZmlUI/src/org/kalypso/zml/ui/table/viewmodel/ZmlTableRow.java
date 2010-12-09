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
package org.kalypso.zml.ui.table.viewmodel;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.ui.table.IZmlTable;

/**
 * @author Dirk Kuch
 */
public class ZmlTableRow extends ZmlTableElement implements IZmlTableRow
{
  private final IZmlModelRow m_row;

  public ZmlTableRow( final IZmlTable table, final IZmlModelRow row )
  {
    super( table );

    m_row = row;
  }

  /**
   * @see org.kalypso.zml.ui.table.viewmodel.IZmlTableRow#getValueReference(org.kalypso.zml.ui.table.viewmodel.IZmlTableColumn)
   */
  @Override
  public IZmlValueReference getValueReference( final IZmlTableColumn column )
  {
    return m_row.get( column.getModelColumn() );
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof IZmlTableRow )
    {
      final IZmlTableRow other = (IZmlTableRow) obj;

      if( getIndex() != other.getIndex() )
        return false;

      final IZmlTableColumn[] columns = getColumns();
      final IZmlTableColumn[] otherColumns = other.getColumns();

      if( columns.length != otherColumns.length )
        return false;

      final EqualsBuilder builder = new EqualsBuilder();
      for( int i = 0; i < columns.length; i++ )
      {
        final IZmlTableColumn column = columns[i];
        final IZmlTableColumn otherColumn = otherColumns[i];

        builder.append( column.getColumnType().getIdentifier(), otherColumn.getColumnType().getIdentifier() );
      }

      return builder.isEquals();
    }

    return super.equals( obj );
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode( )
  {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append( getIndex() );

    final IZmlTableColumn[] columns = getColumns();
    for( final IZmlTableColumn column : columns )
    {
      builder.append( column.getColumnType().getIdentifier() );
    }

    return builder.toHashCode();
  }

  /**
   * @see org.kalypso.zml.ui.table.viewmodel.IZmlTableRow#getValueReference(org.kalypso.zml.ui.table.viewmodel.IZmlTableColumn)
   */
  @Override
  public IZmlTableCell getCell( final IZmlTableColumn column )
  {
    return new ZmlTableCell( column, this );
  }

  /**
   * @see org.kalypso.zml.ui.table.viewmodel.IZmlTableRow#getColumns()
   */
  @Override
  public IZmlTableColumn[] getColumns( )
  {
    return getTable().getColumns();
  }

  /**
   * @see org.kalypso.zml.ui.table.viewmodel.IZmlTableRow#getIndex()
   */
  @Override
  public int getIndex( )
  {
    final TableViewer viewer = getTable().getTableViewer();
    final Table table = viewer.getTable();
    final TableItem[] items = table.getItems();
    for( final TableItem item : items )
    {
      final IZmlModelRow row = (IZmlModelRow) item.getData();

      if( row == m_row )
        return ArrayUtils.indexOf( items, item );
    }

    return -1;
  }
}
