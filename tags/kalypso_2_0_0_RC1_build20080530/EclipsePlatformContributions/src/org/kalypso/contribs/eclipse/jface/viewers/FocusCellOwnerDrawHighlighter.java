/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * 												 - fix for bug 183850, 182652
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.kalypso.contribs.eclipse.jface.viewers;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.FocusCellHighlighter;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * @since 3.3
 */
public class FocusCellOwnerDrawHighlighter extends FocusCellHighlighter
{
  private ViewerCell oldCell;

  /**
   * @param viewer
   *            the viewer
   */
  public FocusCellOwnerDrawHighlighter( final ColumnViewer viewer )
  {
    super( viewer );
    hookListener( viewer );
  }

  protected void markFocusedCell( final Event event )
  {
    final GC gc = event.gc;

    final Color background = event.item.getDisplay().getSystemColor( SWT.COLOR_LIST_SELECTION );
    final Color foreground = event.item.getDisplay().getSystemColor( SWT.COLOR_LIST_SELECTION_TEXT );

    gc.setBackground( background );
    gc.setForeground( foreground );
    gc.fillRectangle( event.getBounds() );

    // This is a workaround for an SWT-Bug on WinXP bug 169517
    final Rectangle bounds = event.getBounds();
    gc.drawText( " ", bounds.x, bounds.y, false ); //$NON-NLS-1$

    gc.drawFocus( bounds.x, bounds.y, bounds.width, bounds.height );
    event.detail &= ~SWT.SELECTED;
  }

  private void hookListener( final ColumnViewer viewer )
  {

    final Listener listener = new Listener()
    {
      public void handleEvent( final Event event )
      {
        if( (event.detail & SWT.SELECTED) > 0 )
        {
          final ViewerCell focusCell = getFocusCell();
          final Widget focusItem = focusCell == null ? null : focusCell.getItem();

          final Widget currentItem = event.item;
          if( focusItem != null && focusItem.equals( currentItem ) && focusCell.getColumnIndex() == event.index )
          {
            markFocusedCell( event );
          }
        }
      }

    };
    viewer.getControl().addListener( SWT.EraseItem, listener );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.FocusCellHighlighter#focusCellChanged(org.eclipse.jface.viewers.ViewerCell)
   */
  @Override
  protected void focusCellChanged( final ViewerCell cell )
  {
    super.focusCellChanged( cell );

    // Redraw new area
    if( cell != null )
    {
      final Rectangle rect = cell.getBounds();
      final int x = cell.getColumnIndex() == 0 ? 0 : rect.x;
      final int width = cell.getColumnIndex() == 0 ? rect.x + rect.width : rect.width;
      // 1 is a fix for Linux-GTK
      cell.getControl().redraw( x, rect.y - 1, width, rect.height + 1, true );
    }

    if( oldCell != null )
    {
      final Rectangle rect = oldCell.getBounds();
      final int x = oldCell.getColumnIndex() == 0 ? 0 : rect.x;
      final int width = oldCell.getColumnIndex() == 0 ? rect.x + rect.width : rect.width;
      // 1 is a fix for Linux-GTK
      oldCell.getControl().redraw( x, rect.y - 1, width, rect.height + 1, true );
    }

    this.oldCell = cell;
  }
}
