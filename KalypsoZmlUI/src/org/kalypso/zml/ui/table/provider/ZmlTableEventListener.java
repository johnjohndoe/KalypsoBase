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
package org.kalypso.zml.ui.table.provider;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.snippets.viewers.TableCursor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.ui.table.ZmlTableComposite;
import org.kalypso.zml.ui.table.menu.ZmlTableContextMenuProvider;
import org.kalypso.zml.ui.table.menu.ZmlTableHeaderContextMenuProvider;
import org.kalypso.zml.ui.table.model.IZmlTableCell;
import org.kalypso.zml.ui.table.model.IZmlTableColumn;
import org.kalypso.zml.ui.table.model.IZmlTableRow;
import org.kalypso.zml.ui.table.model.ZmlTableCell;
import org.kalypso.zml.ui.table.model.ZmlTableRow;
import org.kalypso.zml.ui.table.provider.strategy.ExtendedZmlTableColumn;

/**
 * handles mouse move and menu detect events (active selection of table cells, columns and rows and updating of the
 * table context menu)
 * 
 * @author Dirk Kuch
 */
public class ZmlTableEventListener implements MouseMoveListener, Listener
{
  Point m_position;

  private final MenuManager m_contextMenuManager = new MenuManager();

  private final ZmlTableComposite m_table;

  private final TableCursor m_cursor;

  public ZmlTableEventListener( final ZmlTableComposite table, final TableCursor cursor )
  {
    m_table = table;
    m_cursor = cursor;

    m_contextMenuManager.setRemoveAllWhenShown( false );
    final Menu contextMenu = m_contextMenuManager.createContextMenu( table );
    table.setMenu( contextMenu );
  }

  /**
   * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
   */
  @Override
  public void handleEvent( final Event event )
  {
    if( SWT.MenuDetect != event.type )
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

    final Rectangle clientArea = table.getClientArea();
    final boolean header = clientArea.y <= pt.y && pt.y < clientArea.y + table.getHeaderHeight();

    int columnIndex = findColumnIndex( pt.x );
    if( columnIndex == -1 )
      columnIndex = findColumnIndex( eventPoint.x );
    if( columnIndex == -1 )
      return;

    final ExtendedZmlTableColumn zmlColumn = (ExtendedZmlTableColumn) m_table.findColumn( columnIndex );

    if( header )
    {
      final ZmlTableHeaderContextMenuProvider menuProvider = new ZmlTableHeaderContextMenuProvider();
      menuProvider.fillMenu( zmlColumn, m_contextMenuManager );
      m_contextMenuManager.update( true );
    }
    else
    {
      final ZmlTableContextMenuProvider menuProvider = new ZmlTableContextMenuProvider();
      menuProvider.fillMenu( zmlColumn, m_contextMenuManager );
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

// if( m_position == null )
// return null;
//
// final TableViewer tableViewer = m_table.getTableViewer();
// if( tableViewer == null )
// return null;
//
// final ViewerCell cell = tableViewer.getCell( m_position );
//
// return cell;
  }

  public IZmlTableColumn findActiveColumn( )
  {
    final ViewerCell cell = getActiveViewerCell();
    if( cell == null )
    {
      return null;
    }

    return m_table.findColumn( cell.getColumnIndex() );
  }

  public IZmlTableRow findActiveRow( )
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

  public IZmlTableCell findActiveCell( )
  {
    final IZmlTableColumn column = findActiveColumn();
    final IZmlTableRow row = findActiveRow();
    if( column == null || row == null )
      return null;

    final ZmlTableCell cell = new ZmlTableCell( column, row );

    return cell;
  }

  public IZmlTableRow[] findSelectedRows( )
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

}
