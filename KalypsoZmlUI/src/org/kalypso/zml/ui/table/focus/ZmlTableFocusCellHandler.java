/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.zml.ui.table.focus;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableListener;
import org.kalypso.zml.ui.table.focus.cursor.ITableCursor;
import org.kalypso.zml.ui.table.focus.cursor.ZmlCellNavigationStrategy;
import org.kalypso.zml.ui.table.focus.cursor.ZmlCursorCellHighlighter;
import org.kalypso.zml.ui.table.focus.cursor.ZmlTableCursor;
import org.kalypso.zml.ui.table.model.cells.IZmlTableCell;

/**
 * @author Dirk Kuch
 */
public class ZmlTableFocusCellHandler implements IZmlTableListener, IZmlTableFocusHandler
{
  private final IZmlTable m_table;

  private ZmlTableFocusCellManager m_cellManager;

  protected ZmlTableCursor m_cursor;

  public ZmlTableFocusCellHandler( final IZmlTable table )
  {
    m_table = table;

    init();
  }

  private void init( )
  {
    final TableViewer viewer = m_table.getViewer();
    m_cursor = new ZmlTableCursor( m_table );

    final ZmlCursorCellHighlighter highlighter = new ZmlCursorCellHighlighter( viewer, m_cursor );
    final ZmlCellNavigationStrategy navigationStrategy = new ZmlCellNavigationStrategy();

    m_cellManager = new ZmlTableFocusCellManager( m_table, highlighter, navigationStrategy );
    m_cursor.setCellManager( m_cellManager );

    final ColumnViewerEditorActivationStrategy activationSupport = new ZmlTableEditorActivationStrategy( viewer, m_cellManager );

    TableViewerEditor.create( viewer, m_cellManager, activationSupport, ColumnViewerEditor.KEYBOARD_ACTIVATION );
  }

  @Override
  public void eventTableChanged( final String type, final IZmlModelColumn... columns )
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

  @Override
  public ViewerCell getFocusCell( )
  {
    return m_cellManager.getFocusCell();
  }

  /**
   * @see org.kalypso.zml.ui.table.focus.IZmlTableFocusHandler#getFocusTableCell()
   */
  @Override
  public IZmlTableCell getFocusTableCell( )
  {
    return m_cellManager.getFocusTableCell();
  }

  @Override
  public ITableCursor getCursor( )
  {
    return m_cursor;
  }

}
