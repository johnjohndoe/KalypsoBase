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
package org.kalypso.zml.ui.table.model.columns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableComposite;
import org.kalypso.zml.ui.table.IZmlTableSelectionHandler;
import org.kalypso.zml.ui.table.model.cells.IZmlTableCell;
import org.kalypso.zml.ui.table.model.rows.IZmlTableRow;
import org.kalypso.zml.ui.table.model.rows.IZmlTableValueRow;
import org.kalypso.zml.ui.table.provider.RuleMapper;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractZmlTableColumn implements IZmlTableColumn
{
  private final BaseColumn m_type;

  private final Map<IZmlTable, TableViewerColumn> m_columns = new HashMap<IZmlTable, TableViewerColumn>();

  private final IZmlTableComposite m_table;

  private final RuleMapper m_mapper;

  public AbstractZmlTableColumn( final IZmlTableComposite table, final BaseColumn type )
  {
    m_table = table;
    m_type = type;

    m_mapper = new RuleMapper( table, type );
  }

  protected RuleMapper getMapper( )
  {
    return m_mapper;
  }

  @Override
  public IZmlTableComposite getTable( )
  {
    return m_table;
  }

  public void addColumn( final IZmlTable table, final TableViewerColumn column )
  {
    m_columns.put( table, column );
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
  public TableViewerColumn getTableViewerColumn( final IZmlTable table )
  {
    return m_columns.get( table );
  }

  public TableColumn getTableColumn( final IZmlTable table )
  {
    final TableViewerColumn column = getTableViewerColumn( table );
    if( Objects.isNotNull( column ) )
      return column.getColumn();

    return null;
  }

  @Override
  public IZmlModelColumn getModelColumn( )
  {
    final IZmlModel model = getTable().getModel();

    return model.getColumn( m_type.getIdentifier() );
  }

  @Override
  public IZmlTableCell[] getCells( final IZmlTable table )
  {
    final TableViewer viewer = table.getViewer();
    final List<IZmlTableCell> cells = new ArrayList<IZmlTableCell>();

    final TableItem[] items = viewer.getTable().getItems();
    for( final TableItem item : items )
    {
      final Object data = item.getData();
      if( data instanceof IZmlTableRow )
      {
        final IZmlTableRow row = (IZmlTableRow) data;
        cells.add( row.getCell( this ) );
      }
    }

    return cells.toArray( new IZmlTableCell[] {} );
  }

  @Override
  public IZmlTableCell[] getSelectedCells( final IZmlTable table )
  {
    final List<IZmlTableCell> selected = new ArrayList<IZmlTableCell>();

    final IZmlTableSelectionHandler selection = table.getSelectionHandler();
    final IZmlTableValueRow[] rows = selection.getSelectedRows();
    for( final IZmlTableValueRow row : rows )
    {
      selected.add( row.getCell( this ) );
    }

    return selected.toArray( new IZmlTableCell[] {} );
  }

  @Override
  public void reset( )
  {
    getMapper().reset();
    m_type.reset();
  }
}
