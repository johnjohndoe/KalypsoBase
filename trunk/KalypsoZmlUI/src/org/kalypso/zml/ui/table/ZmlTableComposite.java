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
package org.kalypso.zml.ui.table;

import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.contribs.eclipse.swt.layout.LayoutHelper;
import org.kalypso.zml.ui.table.provider.ZmlColumnRegistry;
import org.kalypso.zml.ui.table.provider.ZmlLabelProvider;
import org.kalypso.zml.ui.table.provider.ZmlTableContentProvider;
import org.kalypso.zml.ui.table.schema.ColumnType;
import org.kalypso.zml.ui.table.schema.ZmlTableType;
import org.kalypso.zml.ui.table.utils.ZmlTableHelper;

/**
 * @author Dirk Kuch
 */
public class ZmlTableComposite extends Composite
{
  private TableViewer m_tableViewer;

  private ZmlColumnRegistry m_registry;

  public ZmlTableComposite( final Composite parent, final ZmlTableType tableType )
  {
    super( parent, SWT.NULL );

    setLayout( LayoutHelper.createGridLayout() );

    if( tableType != null )
      setup( tableType );
  }

  private void setup( final ZmlTableType tableType )
  {
    m_registry = new ZmlColumnRegistry( tableType );
    m_tableViewer = new TableViewer( this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
    m_tableViewer.getTable().setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
    m_tableViewer.setContentProvider( new ZmlTableContentProvider( tableType ) );

    final TableViewerColumn indexColumn = new TableViewerColumn( m_tableViewer, SWT.LEFT );
    indexColumn.setLabelProvider( new ZmlLabelProvider( getIndexColumnType() ) );
    indexColumn.getColumn().setText( "blub" );
    indexColumn.getColumn().setWidth( 100 );

    final List<ColumnType> columns = tableType.getColumns().getColumn();
    for( final ColumnType column : columns )
    {
      addColumnViewer( column );
    }

    m_tableViewer.setInput( m_registry );
  }

  private ColumnType getIndexColumnType( )
  {
    final ColumnType type = new ColumnType();
    type.setId( IZmlTableColumn.ZML_TABLE_INDEX_ID );
    type.setIndexAxis( "date" );
    type.setValueAxis( "date" );
    type.setFormat( "dd.MM.yyyy HH:mm" );

    return type;
  }

  private TableViewerColumn addColumnViewer( final ColumnType type )
  {
    final TableViewerColumn column = new TableViewerColumn( m_tableViewer, ZmlTableHelper.toSWT( type.getAlignment() ) );
    column.setLabelProvider( new ZmlLabelProvider( type ) );
    column.getColumn().setText( "blub" );
    column.getColumn().setWidth( 100 );

    return column;
  }

  public void clean( )
  {
    m_registry.clean();
  }

  public void addColumn( final IZmlTableColumn column )
  {
    m_registry.addColumn( column );

    m_tableViewer.refresh();
  }

}
