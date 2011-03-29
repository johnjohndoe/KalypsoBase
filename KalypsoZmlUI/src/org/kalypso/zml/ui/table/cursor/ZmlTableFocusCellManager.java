package org.kalypso.zml.ui.table.cursor;

import org.eclipse.jface.viewers.CellNavigationStrategy;
import org.eclipse.jface.viewers.FocusCellHighlighter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.ViewerCell;

/**
 * @author kuch
 */
final class ZmlTableFocusCellManager extends TableViewerFocusCellManager
{
  private final ZmlTableCursor m_cursor;

  ZmlTableFocusCellManager( final TableViewer viewer, final FocusCellHighlighter focusDrawingDelegate, final CellNavigationStrategy navigationStrategy, final ZmlTableCursor cursor )
  {
    super( viewer, focusDrawingDelegate, navigationStrategy );
    m_cursor = cursor;
  }

  @Override
  public ViewerCell getFocusCell( )
  {
    /**
     * FIXME / TODO - SWTFocusCellManager defines it's own focusCell - no chance to change / overwrite / update this
     * cell from the here!
     */
    // super.getFocusCell();
    return m_cursor.getFocusCell();
  }
}