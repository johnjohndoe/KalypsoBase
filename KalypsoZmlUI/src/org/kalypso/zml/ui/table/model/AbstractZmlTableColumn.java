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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.schema.IndexColumnType;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableListener;
import org.kalypso.zml.ui.table.IZmlTableSelectionHandler;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractZmlTableColumn extends ZmlTableElement implements IZmlTableColumn
{
  private final TableViewerColumn m_column;

  private final BaseColumn m_type;

  /** visibility flag is used by hide columns command */
  private boolean m_visible = true;

  public AbstractZmlTableColumn( final IZmlTable table, final TableViewerColumn column, final BaseColumn type )
  {
    super( table );

    m_column = column;
    m_type = type;
  }

  @Override
  public boolean isIndexColumn( )
  {
    return m_type.getType() instanceof IndexColumnType;
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof IZmlTableColumn )
    {
      final IZmlTableColumn other = (IZmlTableColumn) obj;

      final EqualsBuilder builder = new EqualsBuilder();
      builder.append( getColumnType().getIdentifier(), other.getColumnType().getIdentifier() );

      return builder.isEquals();
    }

    return super.equals( obj );
  }

  @Override
  public int hashCode( )
  {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append( getClass().getName() );
    builder.append( getColumnType().getIdentifier() );

    return builder.toHashCode();
  }

  @Override
  public BaseColumn getColumnType( )
  {
    return m_type;
  }

  @Override
  public TableViewerColumn getTableViewerColumn( )
  {
    return m_column;
  }

  public TableColumn getTableColumn( )
  {
    return m_column.getColumn();
  }

  @Override
  public IZmlModelColumn getModelColumn( )
  {
    final IZmlModel model = getTable().getModel();

    return model.getColumn( m_type.getIdentifier() );
  }

  @Override
  public boolean isVisible( )
  {
    if( isIndexColumn() )
      return true;

    final IZmlModelColumn column = getModelColumn();
    if( Objects.isNull( column ) )
      return false;

    if( !m_visible )
      return false;

    return column.isActive();
  }

  @Override
  public IZmlTableCell[] getCells( )
  {
    final TableViewer viewer = (TableViewer) m_column.getViewer();
    final List<IZmlTableCell> cells = new ArrayList<IZmlTableCell>();

    final TableItem[] items = viewer.getTable().getItems();
    for( final TableItem item : items )
    {
      final Object data = item.getData();
      if( data instanceof IZmlModelRow )
      {
        final IZmlModelRow row = (IZmlModelRow) data;
        final ZmlTableRow zmlRow = new ZmlTableRow( getTable(), row );

        cells.add( new ZmlTableCell( zmlRow, this ) );
      }
    }

    return cells.toArray( new IZmlTableCell[] {} );
  }

  @Override
  public IZmlTableCell[] getSelectedCells( )
  {
    final List<IZmlTableCell> selected = new ArrayList<IZmlTableCell>();

    final IZmlTableSelectionHandler selection = getTable().getSelectionHandler();
    final IZmlTableRow[] rows = selection.getSelectedRows();
    for( final IZmlTableRow row : rows )
    {
      selected.add( row.getCell( this ) );
    }

    return selected.toArray( new IZmlTableCell[] {} );
  }

  @Override
  public IZmlTableCell findCell( final IZmlModelRow row )
  {
    return new ZmlTableCell( new ZmlTableRow( getTable(), row ), this );
  }

  @Override
  public void reset( )
  {
    m_type.reset();
  }

  @Override
  public void setVisible( final boolean visibility )
  {
    if( Objects.notEqual( m_visible, visibility ) )
    {
      m_visible = visibility;

      getTable().fireTableChanged( IZmlTableListener.TYPE_ACTIVE_RULE_CHANGED, getModelColumn() );
    }
  }
}
