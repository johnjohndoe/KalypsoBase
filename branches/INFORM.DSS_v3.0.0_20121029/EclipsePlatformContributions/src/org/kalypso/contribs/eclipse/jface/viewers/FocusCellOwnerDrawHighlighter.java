/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */

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
  private ViewerCell m_oldCell;

  /**
   * @param viewer
   *          the viewer
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
      @Override
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

  /**
   * @see org.eclipse.jface.viewers.FocusCellHighlighter#focusCellChanged(org.eclipse.jface.viewers.ViewerCell,
   *      org.eclipse.jface.viewers.ViewerCell)
   */
  @Override
  protected void focusCellChanged( final ViewerCell cell, final ViewerCell oldCell2 )
  {
    super.focusCellChanged( cell, m_oldCell );

    // Redraw new area
    if( cell != null )
    {
      final Rectangle rect = cell.getBounds();
      final int x = cell.getColumnIndex() == 0 ? 0 : rect.x;
      final int width = cell.getColumnIndex() == 0 ? rect.x + rect.width : rect.width;
      // 1 is a fix for Linux-GTK
      cell.getControl().redraw( x, rect.y - 1, width, rect.height + 1, true );
    }

    if( m_oldCell != null )
    {
      final Rectangle rect = m_oldCell.getBounds();
      final int x = m_oldCell.getColumnIndex() == 0 ? 0 : rect.x;
      final int width = m_oldCell.getColumnIndex() == 0 ? rect.x + rect.width : rect.width;
      // 1 is a fix for Linux-GTK
      m_oldCell.getControl().redraw( x, rect.y - 1, width, rect.height + 1, true );
    }

    m_oldCell = cell;
  }
}
