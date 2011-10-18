/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *     @author changed / updated by: Dirk Kuch
 *******************************************************************************/
package org.kalypso.zml.ui.table.focus.cursor;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.model.IZmlTableCell;
import org.kalypso.zml.ui.table.model.IZmlTableColumn;
import org.kalypso.zml.ui.table.model.ZmlTableCell;
import org.kalypso.zml.ui.table.model.ZmlTableRow;
import org.kalypso.zml.ui.table.provider.ZmlTabelCellPainter;
import org.kalypso.zml.ui.table.provider.strategy.ExtendedZmlTableColumn;

public class ZmlTableCursor extends AbstractZmlCellCursor
{
  private static final Color COLOR_BACKGROUND_SELECTION = new Color( null, new RGB( 0xBA, 0xFF, 0xEC ) );

  private static final Color COLOR_BACKGROUND_SELECTION_DISABLED = new Color( null, new RGB( 0xCC, 0xCC, 0xCC ) );

  private final IZmlTable m_table;

  public ZmlTableCursor( final IZmlTable table )
  {
    super( table.getViewer() );
    m_table = table;
  }

  @Override
  protected void paint( final Event event )
  {
    final ViewerCell cell = getFocusCell();
    if( Objects.isNull( cell ) )
      return;

    try
    {
      if( cell.getControl().isDisposed() )
        return;
      final ZmlTabelCellPainter renderer = new ZmlTabelCellPainter( findCell( cell ) );
      if( Objects.isNull( renderer ) )
        return;

      drawBackground( event );

      renderer.initGc( event );

      final Rectangle bounds = event.getBounds();
      apply( bounds, renderer.drawImage( event.gc, bounds ) );
      apply( bounds, renderer.drawText( event.gc, bounds ) );

      renderer.resetGc( event );

      event.detail &= ~SWT.SELECTED;
      event.detail &= ~SWT.FOREGROUND;

      setVisible( true );
    }
    catch( final Throwable t )
    {
      setVisible( false );
      if( t instanceof SWTException )
        return;

      t.printStackTrace();
    }
  }

  private void drawBackground( final Event event )
  {
    final Color foreground = event.gc.getForeground();
    final Color background = event.gc.getBackground();

    event.gc.setForeground( getDisplay().getSystemColor( SWT.COLOR_BLACK ) );
    event.gc.setBackground( getBackground() );

    event.gc.fillRectangle( event.x, event.y, event.width - 1, event.height - 1 );
    event.gc.drawRectangle( event.x, event.y, event.width - 1, event.height - 1 );
    event.gc.drawRectangle( event.x + 1, event.y + 1, event.width - 3, event.height - 3 );

    event.gc.setForeground( foreground );
    event.gc.setBackground( background );
  }

  private void apply( final Rectangle bounds, final Point extend )
  {
    bounds.width -= extend.x;
    bounds.x += extend.x;
    bounds.height = Math.max( bounds.height, extend.y );
  }

  private IZmlTableCell findCell( final ViewerCell cell )
  {
    final IZmlModelRow row = (IZmlModelRow) cell.getElement();
    final IZmlTableColumn[] columns = m_table.getColumns();
    final int index = cell.getVisualIndex() - 1; // table rendering offset ("windows layout bug")
    if( index < 0 )
      return null;

    final IZmlTableColumn column = columns[index];
    if( column instanceof ExtendedZmlTableColumn )
      if( !((ExtendedZmlTableColumn) column).isVisible() )
        return null;

    return new ZmlTableCell( new ZmlTableRow( m_table, row ), column );
  }

  @Override
  public Color getBackground( )
  {
    final IZmlTableCell cell = getFocusTableCell();
    final IZmlTableColumn column = cell.getColumn();
    final BaseColumn type = column.getColumnType();
    if( type.isEditable() )
      return COLOR_BACKGROUND_SELECTION;

    return COLOR_BACKGROUND_SELECTION_DISABLED;
  }

}
