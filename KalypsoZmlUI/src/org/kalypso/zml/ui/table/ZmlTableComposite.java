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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.contribs.eclipse.swt.layout.LayoutHelper;
import org.kalypso.zml.ui.table.binding.BaseColumn;
import org.kalypso.zml.ui.table.menu.ZmlTableContextMenuListener;
import org.kalypso.zml.ui.table.model.IZmlColumnModel;
import org.kalypso.zml.ui.table.model.ZmlTableColumn;
import org.kalypso.zml.ui.table.model.ZmlTableRow;
import org.kalypso.zml.ui.table.model.references.IZmlValueReference;
import org.kalypso.zml.ui.table.provider.IZmlColumnModelListener;
import org.kalypso.zml.ui.table.provider.ZmlLabelProvider;
import org.kalypso.zml.ui.table.provider.ZmlTableContentProvider;
import org.kalypso.zml.ui.table.schema.AbstractColumnType;
import org.kalypso.zml.ui.table.schema.DataColumnType;
import org.kalypso.zml.ui.table.schema.ZmlTableType;
import org.kalypso.zml.ui.table.utils.TableTypeHelper;
import org.kalypso.zml.ui.table.utils.ZmlTableMouseMoveListener;

/**
 * @author Dirk Kuch
 */
public class ZmlTableComposite extends Composite implements IZmlColumnModelListener, IZmlTableComposite
{
  private TableViewer m_tableViewer;

  private final Map<Integer, BaseColumn> m_columnIndex = new HashMap<Integer, BaseColumn>();

  private final IZmlColumnModel m_model;

  private ZmlTableMouseMoveListener m_mouseMoveListener;

  public ZmlTableComposite( final IZmlColumnModel model, final Composite parent )
  {
    super( parent, SWT.NULL );
    m_model = model;

    setLayout( LayoutHelper.createGridLayout() );

    setup();

    model.addListener( this );
  }

  private void setup( )
  {
    final ZmlTableType tableType = m_model.getTableType();

    m_tableViewer = new TableViewer( this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION );
    m_tableViewer.getTable().setLinesVisible( true );

    m_mouseMoveListener = new ZmlTableMouseMoveListener( this );
    m_tableViewer.getTable().addMouseMoveListener( m_mouseMoveListener );

    m_tableViewer.addSelectionChangedListener( new ZmlTableContextMenuListener( this ) );

    /* excel table cursor */
// new ExcelTableCursor( m_tableViewer, SWT.BORDER_DASH, ADVANCE_MODE.DOWN, true );

    m_tableViewer.setContentProvider( new ZmlTableContentProvider( m_model ) );

    final List<AbstractColumnType> columns = tableType.getColumns().getColumn();
    for( final AbstractColumnType column : columns )
    {
      buildColumnViewer( new BaseColumn( column ) );
    }

    m_tableViewer.setInput( m_model );

    /** layout stuff */
    final Table table = m_tableViewer.getTable();
    table.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
    table.setHeaderVisible( true );

// final TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager( m_tableViewer, new
// FocusCellOwnerDrawHighlighter( m_tableViewer ) );
// final ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy( m_tableViewer )
// {
// @Override
// protected boolean isEditorActivationEvent( final ColumnViewerEditorActivationEvent event )
// {
// return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL || event.eventType ==
// ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
// || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR) || event.eventType
// == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
// }
// };
//
// TableViewerEditor.create( m_tableViewer, focusCellManager, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL |
// ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
// | ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION );

  }

  private TableViewerColumn buildColumnViewer( final BaseColumn type )
  {
    final int index = m_tableViewer.getTable().getColumnCount();
    m_columnIndex.put( index, type );

    final TableViewerColumn column = new TableViewerColumn( m_tableViewer, TableTypeHelper.toSWT( type.getAlignment() ) );
    column.setLabelProvider( new ZmlLabelProvider( type ) );
    column.getColumn().setText( type.getLabel() );

    final Integer width = type.getWidth();
    if( width != null )
      column.getColumn().setWidth( width.intValue() );

    if( width == null && type.isAutopack() )
      column.getColumn().pack();

    /** edit support */
    if( type.getType() instanceof DataColumnType && type.isEditable() )
    {
      column.setEditingSupport( new ZmlEditingSupport( type, column ) );
    }

    return column;
  }

  protected void refresh( )
  {
    if( m_tableViewer.getTable().isDisposed() )
      return;

    m_tableViewer.refresh();

    /** update header labels */
    final TableColumn[] tableColumns = m_tableViewer.getTable().getColumns();
    Assert.isTrue( tableColumns.length == m_columnIndex.size() );

    for( int i = 0; i < tableColumns.length; i++ )
    {
      final BaseColumn baseColumn = m_columnIndex.get( i );
      final TableColumn tableColumn = tableColumns[i];

      /** only update headers of data column types */
      if( baseColumn.getType() instanceof DataColumnType )
      {
        final ZmlTableColumn column = m_model.getColumn( baseColumn.getIdentifier() );
        if( column == null )
        {
          tableColumn.setWidth( 0 );
        }
        else
        {
          pack( tableColumn, baseColumn );
        }
      }
      else
      {
        pack( tableColumn, baseColumn );
      }

      tableColumn.setText( baseColumn.getLabel() );
    }
  }

  private void pack( final TableColumn table, final BaseColumn base )
  {
    if( base.isAutopack() )
      table.pack();

    final Integer width = base.getWidth();
    if( width == null )
      table.pack();
    else
      table.setWidth( width );
  }

  /**
   * @see org.kalypso.zml.ui.table.provider.IZmlColumnModelListener#modelChanged()
   */
  @Override
  public void modelChanged( )
  {
    new UIJob( "" )
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        refresh();

        return Status.OK_STATUS;
      }
    }.schedule();
  }

  public void duplicateColumn( final String identifier, final String newIdentifier )
  {
    // column already exists?
    final Collection<BaseColumn> columns = m_columnIndex.values();
    for( final BaseColumn column : columns )
    {
      if( column.getIdentifier().equals( newIdentifier ) )
        return;
    }

    final AbstractColumnType base = TableTypeHelper.finColumn( m_model.getTableType(), identifier );
    final AbstractColumnType clone = TableTypeHelper.cloneColumn( base );
    clone.setId( newIdentifier );

    buildColumnViewer( new BaseColumn( clone ) );
  }

  /**
   * @see org.kalypso.zml.ui.table.IZmlTableComposite#getColumn(int)
   */
  @Override
  public BaseColumn getColumn( final int columnIndex )
  {
    return m_columnIndex.get( columnIndex );
  }

  public TableViewer getTableViewer( )
  {
    return m_tableViewer;
  }

  /**
   * @see org.kalypso.zml.ui.table.IZmlTableComposite#getActiveCell()
   */
  @Override
  public IZmlValueReference getActiveCell( )
  {
    return m_mouseMoveListener.findActiveCell();
  }

  /**
   * @see org.kalypso.zml.ui.table.IZmlTableComposite#getActiveColumn()
   */
  @Override
  public BaseColumn getActiveColumn( )
  {
    return m_mouseMoveListener.findActiveColumn();
  }

  /**
   * @see org.kalypso.zml.ui.table.IZmlTableComposite#getActiveRow()
   */
  @Override
  public ZmlTableRow getActiveRow( )
  {
    return m_mouseMoveListener.findActiveRow();
  }

}
