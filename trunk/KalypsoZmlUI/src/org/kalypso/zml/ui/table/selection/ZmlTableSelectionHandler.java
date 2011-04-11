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
package org.kalypso.zml.ui.table.selection;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
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
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.jface.viewers.table.Tables;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.ZmlModelRow;
import org.kalypso.zml.core.table.schema.DataColumnType;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableSelectionHandler;
import org.kalypso.zml.ui.table.ZmlTableComposite;
import org.kalypso.zml.ui.table.base.helper.ZmlTables;
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
public class ZmlTableSelectionHandler implements MouseMoveListener, Listener, IZmlTableSelectionHandler
{
  protected Point m_position;

  private final MenuManager m_contextMenuManager = new MenuManager();

  private final ZmlTableComposite m_table;

  private IExtendedZmlTableColumn m_lastColumn;

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
        // TODO: delegate to navigation strategy
        e.doit = false;
      }
    } );

    /**
     * context menu
     */
    m_contextMenuManager.setRemoveAllWhenShown( false );
    final Menu contextMenu = m_contextMenuManager.createContextMenu( m_table );
    m_table.setMenu( contextMenu );

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
    final Point controlPoint = table.toControl( eventPoint );

    IExtendedZmlTableColumn column = findColumn( controlPoint );
    if( Objects.isNull( column ) )
      column = findColumn( eventPoint );

    if( Objects.isNull( column ) )
      return;

    m_lastColumn = column;

    if( controlPoint.x > 0 ) // header
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

  private IExtendedZmlTableColumn findColumn( final Point point )
  {
    if( Objects.isNull( point ) )
      return null;

    final int columnIndex = findColumnIndex( point.x );
    if( columnIndex == -1 )
      return null;

    return (IExtendedZmlTableColumn) m_table.findColumn( columnIndex );
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

  @Override
  public IZmlTableColumn findActiveColumnByPosition( )
  {
    if( Objects.isNotNull( m_lastColumn ) )
      return m_lastColumn;

    final IExtendedZmlTableColumn found = findColumn( m_position );
    if( Objects.isNotNull( found ) )
      m_lastColumn = found;

    return m_lastColumn;
  }

  @Override
  public IZmlTableCell findActiveCellByPosition( )
  {
    final IZmlTableColumn column = findActiveColumnByPosition();

    final IZmlTableRow row = findActiveRowByPosition();
    if( Objects.isNull( column, row ) )
      return null;

    return new ZmlTableCell( row, column );
  }

  private ViewerCell findActiveViewerCell( )
  {
    if( Objects.isNull( m_position ) )
      return null;

    final ViewerCell viewerCell = m_table.getTableViewer().getCell( m_position );
    if( Objects.isNull( viewerCell ) )
      return null;

    return viewerCell;
  }

  @Override
  public IZmlTableRow findActiveRowByPosition( )
  {
    final ViewerCell cell = findActiveViewerCell();
    if( Objects.isNull( cell ) )
      return null;

    return ZmlTables.toTableRow( m_table, cell );
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
  public ViewerCell toViewerCell( final IZmlTableCell cell )
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
}
