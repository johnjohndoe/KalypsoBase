/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.zml.ui.table.provider.rendering.cell;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.data.IZmlModelColumnDataHandler;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.model.cells.IZmlTableCell;
import org.kalypso.zml.ui.table.model.columns.IZmlTableColumn;
import org.kalypso.zml.ui.table.model.columns.IZmlTableValueColumn;
import org.kalypso.zml.ui.table.model.rows.IZmlTableRow;

/**
 * @author Dirk Kuch
 */
public class ZmlTableCellPaintListener implements Listener
{
  private final IZmlTable m_table;

  public ZmlTableCellPaintListener( final IZmlTable table )
  {
    m_table = table;
  }

  @Override
  public void handleEvent( final Event event )
  {
    switch( event.type )
    {
      case SWT.MeasureItem:
        doMeasureItem( event );
        break;
      case SWT.PaintItem:
        doPaintItem( event );
        break;
      case SWT.EraseItem:
        doPaintBackground( event );
        break;
    }
  }

  /**
   * @param event
   *          .width and .height are the column / cell default values
   */
  private void doMeasureItem( final Event event )
  {
    final IZmlTableCellPainter renderer = findCellPainter( event );
    if( Objects.isNull( renderer ) )
      return;

    if( !renderer.isVisble() )
      return;

    final Point extend = renderer.getExtend( event );
    if( Objects.isNull( extend ) )
      return;

    event.width = Math.max( event.width, extend.x );
    event.height = Math.max( event.height, extend.y + 2 );

    /**
     * purpose of clearing the SWT.FOREGROUND bit from the event's detail field. This indicates that the default drawing
     * of item foregrounds should not occur because the SWT.PaintItem listener will do this in full.
     */
    event.detail &= ~SWT.FOREGROUND;

// printDebug( renderer.getCell(), event, "doMeasureItem()" );
  }

// private void printDebug( final IZmlTableCell cell, final Event event, final String msg )
// {
//
// final int row = cell.getIndex();
// final IZmlTableColumn column = cell.getColumn();
// final String columnLabel = column.getColumnType().getLabel();
//
// KalypsoZmlUiDebug.DEBUG_TABLE.printf( "column %s;row %d;%s;width: %d;height: %d\n", columnLabel, row, msg,
// event.width, event.height );
// }

  public void doPaintBackground( final Event event )
  {
    if( (event.detail & SWT.SELECTED) != 0 )
      return; /* item selected */

    final IZmlTableCellPainter renderer = findCellPainter( event );
    if( Objects.isNull( renderer ) )
      return;

    renderer.initGc( event );

    if( (event.detail & SWT.SELECTED) == 0 )
      renderer.drawBackground( event );

    renderer.resetGc( event );

    event.detail &= ~SWT.BACKGROUND; // default cell background should not be drawn
// event.detail &= ~SWT.SELECTED; // default swt selection style should not be drawn
// event.detail &= ~SWT.HOT; // default swt mouse hover style (=hot) should not be draw

// printDebug( renderer.getCell(), event, "doPaintBackground()" );
  }

  public void doPaintItem( final Event event )
  {
    final IZmlTableCellPainter renderer = findCellPainter( event );
    if( Objects.isNull( renderer ) )
      return;

    renderer.initGc( event );

    final Rectangle bounds = event.getBounds();
    bounds.width = getTableColumnWidth( renderer );

    apply( bounds, renderer.drawImage( event.gc, bounds ) );
    apply( bounds, renderer.drawText( event.gc, bounds ) );

    renderer.resetGc( event );

    event.detail &= ~SWT.FOREGROUND;

// printDebug( renderer.getCell(), event, "doPaintItem()" );
  }

  private void apply( final Rectangle bounds, final Point extend )
  {
    bounds.width -= extend.x;
    bounds.x += extend.x;
    bounds.height = Math.max( bounds.height, extend.y );
  }

  private int getTableColumnWidth( final IZmlTableCellPainter renderer )
  {
    final IZmlTableCell cell = renderer.getCell();
    return cell.getColumn().getTableViewerColumn().getColumn().getWidth();
  }

  private IZmlTableCellPainter findCellPainter( final Event event )
  {
    final IZmlTableRow row = (IZmlTableRow) event.item.getData();
    final IZmlTableColumn[] columns = m_table.getModel().getColumns();
    final int index = event.index - 1; // table rendering offset ("windows layout bug")
    if( index < 0 )
      return null;

    if( columns.length <= index )
      return null;

    final IZmlTableColumn column = columns[index];
    if( !column.isVisible() )
      return null;

    if( column instanceof IZmlTableValueColumn )
    {
      final IZmlModelColumn modelColumn = column.getModelColumn();
      final IZmlModelColumnDataHandler dataHandler = modelColumn.getDataHandler();
      if( Objects.isNull( dataHandler ) )
        return null;

      final IObservation observation = dataHandler.getObservation();
      if( Objects.isNull( observation ) )
        return null;
    }

    return m_table.getCache().getPainter( row, column );
  }
}