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

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.kalypso.contribs.eclipse.swt.layout.LayoutHelper;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.ui.table.provider.ZmlColumnRegistry;
import org.kalypso.zml.ui.table.provider.ZmlLabelProvider;
import org.kalypso.zml.ui.table.provider.ZmlTableContentProvider;
import org.kalypso.zml.ui.table.schema.AbstractColumnType;
import org.kalypso.zml.ui.table.schema.IndexColumnType;
import org.kalypso.zml.ui.table.schema.ZmlTableType;
import org.kalypso.zml.ui.table.utils.ZmlTableHelper;

/**
 * @author Dirk Kuch
 */
public class ZmlTableComposite extends Composite
{
  private TableViewer m_tableViewer;

  private ZmlColumnRegistry m_registry;

  private final Map<Integer, AbstractColumnType> m_columnIndex = new HashMap<Integer, AbstractColumnType>();

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
    m_tableViewer.setContentProvider( new ZmlTableContentProvider( tableType ) );

    final List<AbstractColumnType> columns = tableType.getColumns().getColumn();
    for( final AbstractColumnType column : columns )
    {
      addColumnViewer( column );
    }

    m_tableViewer.setInput( m_registry );

    /** layout stuff */
    final Table table = m_tableViewer.getTable();
    table.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
    table.setHeaderVisible( true );

    /** TODO auto pack */
// final TableColumn[] tableColumns = table.getColumns();
// for( final TableColumn tableColumn : tableColumns )
// tableColumn.pack();
  }

  private TableViewerColumn addColumnViewer( final AbstractColumnType type )
  {
    final int index = m_tableViewer.getTable().getColumnCount();
    m_columnIndex.put( index, type );

    final TableViewerColumn column = new TableViewerColumn( m_tableViewer, ZmlTableHelper.toSWT( type.getAlignment() ) );
    column.setLabelProvider( new ZmlLabelProvider( type ) );
    column.getColumn().setText( type.getLabel() );

    final BigInteger width = type.getWidth();
    if( width != null )
      column.getColumn().setWidth( width.intValue() );

    return column;
  }

  public void clean( )
  {
    m_registry.clean();
  }

  public void addColumn( final IZmlTableColumn column )
  {
    m_registry.addColumn( column );

    refresh();
  }

  private void refresh( )
  {
    m_tableViewer.refresh();

    /** update header labels */
    final IZmlTableColumn[] dataColumns = m_registry.getColumns();

    final TableColumn[] tableColumns = m_tableViewer.getTable().getColumns();
    Assert.isTrue( tableColumns.length == m_columnIndex.size() );

    /*** FIXME *brrrrr* refactor */
    for( int i = 0; i < tableColumns.length; i++ )
    {
      final AbstractColumnType columnType = m_columnIndex.get( i );
      /** data of index column types are generated on-the-fly */
      if( columnType instanceof IndexColumnType )
        continue;

      final TableColumn tableColumn = tableColumns[i];

      final IZmlTableColumn zmlColumn = ZmlTableHelper.findZmlTableColumn( dataColumns, columnType.getId() );

      final IAxis[] axes = zmlColumn.getObsProvider().getObservation().getAxisList();
      final IAxis axis = AxisUtils.findAxis( axes, columnType.getId() );

      final String label = zmlColumn.getTitle( axis );
      tableColumn.setText( label );
    }
  }
}
