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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.jface.viewers.table.Tables;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.ZmlModelRow;
import org.kalypso.zml.core.table.schema.DataColumnType;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableListener;
import org.kalypso.zml.ui.table.IZmlTableSelectionHandler;
import org.kalypso.zml.ui.table.ZmlTableComposite;
import org.kalypso.zml.ui.table.cursor.update.ZmlSelectionUpdater;
import org.kalypso.zml.ui.table.menu.ZmlTableContextMenuProvider;
import org.kalypso.zml.ui.table.menu.ZmlTableHeaderContextMenuProvider;
import org.kalypso.zml.ui.table.model.IZmlTableCell;
import org.kalypso.zml.ui.table.model.IZmlTableColumn;
import org.kalypso.zml.ui.table.model.IZmlTableRow;
import org.kalypso.zml.ui.table.model.ZmlTableCell;
import org.kalypso.zml.ui.table.model.ZmlTableRow;
import org.kalypso.zml.ui.table.provider.strategy.IExtendedZmlTableColumn;

/**
 * handles mouse move and menu detect events (active selection of table cells, columns and rows and updating of the
 * table context menu)
 * 
 * @author Dirk Kuch
 */
public class ZmlTableSelectionHandler implements MouseMoveListener, Listener, IZmlTableSelectionHandler, IZmlTableListener
{
  protected Point m_position;

  private final MenuManager m_contextMenuManager = new MenuManager();

  private final ZmlTableComposite m_table;

  protected ZmlTableCursor m_cursor;

  private ZmlTableFocusCellManager m_cellManager;

  public ZmlTableSelectionHandler( final ZmlTableComposite table )
  {
    m_table = table;

    init();
  }

  private void init( )
  {
    final TableViewer viewer = m_table.getTableViewer();

    viewer.getTable().addMouseMoveListener( this );
    viewer.getTable().addListener( SWT.MenuDetect, this );
    viewer.getTable().addListener( SWT.MouseDown, this );

    viewer.getTable().addTraverseListener( new TraverseListener()
    {
      @Override
      public void keyTraversed( final TraverseEvent e )
      {
        e.doit = false;
        // TODO: delegate to navigation strategy
      }
    } );

    /**
     * context menu
     */
    m_contextMenuManager.setRemoveAllWhenShown( false );
    final Menu contextMenu = m_contextMenuManager.createContextMenu( m_table );
    m_table.setMenu( contextMenu );

    m_cursor = new ZmlTableCursor( m_table );
    final ZmlCursorCellHighlighter highlighter = new ZmlCursorCellHighlighter( viewer, m_cursor );

    final ZmlCellNavigationStrategy navigationStrategy = new ZmlCellNavigationStrategy();
    m_cellManager = new ZmlTableFocusCellManager( viewer, highlighter, navigationStrategy );

    m_cursor.setCellManager( m_cellManager );

    final ColumnViewerEditorActivationStrategy activationSupport = new ZmlTableEditorActivationStrategy( this, viewer );

    TableViewerEditor.create( viewer, m_cellManager, activationSupport, ColumnViewerEditor.KEYBOARD_ACTIVATION );

    /* selection change listener -> updates selection in chart composite */
    viewer.addSelectionChangedListener( new UpdateChartSelectionListener( this ) );
  }

  /**
   * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
   */
  @Override
  public void handleEvent( final Event event )
  {
    if( SWT.MenuDetect != event.type && SWT.MouseDown != event.type )
      return;

    if( SWT.MouseDown == event.type )
      if( event.button != 3 )
        return;

    handleMenuDetect( event );

    final Menu menu = m_contextMenuManager.getMenu();
    menu.setVisible( true );
  }

  protected void handleMenuDetect( final Event event )
  {
    m_contextMenuManager.removeAll();

    final Table table = m_table.getTableViewer().getTable();
    if( table.isDisposed() )
      return;

    final Point eventPoint = new Point( event.x, event.y );
    final Point pt = table.toControl( eventPoint );
    final boolean header = pt.x > 0;

    int columnIndex = findColumnIndex( pt.x );
    if( columnIndex == -1 )
      columnIndex = findColumnIndex( eventPoint.x );
    if( columnIndex == -1 )
      return;

    final IExtendedZmlTableColumn column = (IExtendedZmlTableColumn) m_table.findColumn( columnIndex );

    if( header )
    {
      final ZmlTableHeaderContextMenuProvider menuProvider = new ZmlTableHeaderContextMenuProvider();
      menuProvider.fillMenu( column, m_contextMenuManager );
      m_contextMenuManager.update( true );
    }
    else
    {
      final ZmlTableContextMenuProvider menuProvider = new ZmlTableContextMenuProvider();
      menuProvider.fillMenu( column, m_contextMenuManager );
      m_contextMenuManager.update( true );
    }
  }

  private int findColumnIndex( final int x )
  {
    final Table table = m_table.getTableViewer().getTable();
    final TableColumn[] columns = table.getColumns();
    final int[] columnOrder = table.getColumnOrder();

    if( x < 0 )
      return -1;

    int currentWidth = 0;
    for( final int index : columnOrder )
    {
      final TableColumn column = columns[index];
      if( x < currentWidth + column.getWidth() )
        return index;

      currentWidth += column.getWidth();
    }

    return -1;
  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseMove( final MouseEvent e )
  {
    m_position = new Point( e.x, e.y );
  }

  public ViewerCell getActiveViewerCell( )
  {
    return m_cursor.getFocusCell();
  }

  @Override
  public IZmlTableColumn getSetActiveColumn( )
  {
    final ZmlSelectionUpdater updater = new ZmlSelectionUpdater( m_table, m_position );
    updater.run();

    return getActiveColumn();
  }

  @Override
  public IZmlTableColumn getActiveColumn( )
  {
    final ViewerCell cell = getActiveViewerCell();
    if( Objects.isNull( cell ) )
    {
      return null;
    }

    return m_table.findColumn( cell.getColumnIndex() );
  }

  @Override
  public IZmlTableRow getActiveRow( )
  {
    final ViewerCell cell = getActiveViewerCell();
    if( cell == null )
      return null;

    final Object element = cell.getElement();
    if( element instanceof IZmlModelRow )
    {
      final IZmlModelRow row = (IZmlModelRow) element;

      return new ZmlTableRow( m_table, row );
    }

    return null;
  }

  @Override
  public IZmlTableCell getActiveCell( )
  {
    final IZmlTableColumn column = getActiveColumn();
    final IZmlTableRow row = getActiveRow();
    if( column == null || row == null )
      return null;

    final ZmlTableCell cell = new ZmlTableCell( column, row );

    return cell;
  }

  @Override
  public IZmlTableRow[] getSelectedRows( )
  {
    final TableViewer viewer = m_table.getTableViewer();
    final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();

    final List<IZmlTableRow> rows = new ArrayList<IZmlTableRow>();

    final Object[] elements = selection.toArray();
    for( final Object element : elements )
    {
      if( element instanceof IZmlModelRow )
      {
        rows.add( new ZmlTableRow( m_table, (IZmlModelRow) element ) );
      }
    }

    return rows.toArray( new IZmlTableRow[] {} );
  }

  public void setFocusCell( final Date index, final IZmlTableColumn column )
  {
    final TableViewer viewer = m_table.getTableViewer();
    final Table table = viewer.getTable();
    final TableItem[] items = table.getItems();

    for( final TableItem item : items )
    {
      final IZmlModelRow row = (IZmlModelRow) item.getData();
      if( row.getIndexValue().equals( index ) )
      {
        final Rectangle bounds = item.getBounds();

        final ViewerCell cell = findCell( column, bounds.y );
        m_cellManager.setFocusCell2( cell );

        return;
      }
    }
  }

  private ViewerCell findCell( final IZmlTableColumn column, final int y )
  {
    final IZmlTable table = column.getTable();
    final TableViewer viewer = table.getTableViewer();

    /** focus on the same row and column of the old table cell */
    final int ptrX = Tables.getX( viewer.getTable(), column.getTableViewerColumn().getColumn() );
    final ViewerCell cell = viewer.getCell( new Point( ptrX, y ) );
    if( Objects.isNotNull( cell ) )
      return cell;

    /** if not, focus on the same row */
    final IZmlTableColumn[] columns = table.getColumns();
    for( final IZmlTableColumn col : columns )
    {
      final BaseColumn type = col.getColumnType();
      if( !(type.getType() instanceof DataColumnType) )
        continue;

      final int x = Tables.getX( viewer.getTable(), col.getTableViewerColumn().getColumn() );
      final ViewerCell c = viewer.getCell( new Point( x, y ) );
      if( Objects.isNotNull( c ) )
        return c;
    }

    /** hmmm, try to focus on the same row index value */
    return viewer.getCell( new Point( 1, y ) );
  }

  /**
   * @see org.kalypso.zml.ui.table.provider.IZmlTableSelectionHandler#findViewerCell(org.kalypso.zml.ui.table.model.IZmlTableCell)
   */
  @Override
  public ViewerCell findViewerCell( final IZmlTableCell cell )
  {
    final IZmlTable zml = cell.getTable();
    final Table table = zml.getTableViewer().getTable();
    final TableItem[] items = table.getItems();
    for( final TableItem item : items )
    {
      final ZmlModelRow row = (ZmlModelRow) item.getData();

      if( row.equals( cell.getRow().getModelRow() ) )
      {
        final Rectangle bounds = item.getBounds();

        return findCell( cell.getColumn(), bounds.y + 1 );
      }
    }

    return null;
  }

  /**
   * @see org.kalypso.zml.ui.table.provider.IZmlTableSelectionHandler#setFocusCell(org.eclipse.jface.viewers.ViewerCell)
   */
  @Override
  public void setFocusCell( final ViewerCell cell )
  {
    m_cellManager.setFocusCell2( cell );
  }

  /**
   * @see org.kalypso.zml.ui.table.IZmlTableListener#eventTableChanged()
   */
  @Override
  public void eventTableChanged( )
  {
    new UIJob( "" )
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        if( m_cursor != null && !m_cursor.isDisposed() )
          m_cursor.redraw();

        return Status.OK_STATUS;
      }
    }.schedule();
  }

}
