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
  ZmlTableFocusCellManager( final TableViewer viewer, final FocusCellHighlighter focusDrawingDelegate, final CellNavigationStrategy navigationStrategy )
  {
    super( viewer, focusDrawingDelegate, navigationStrategy );
  }

  /**
   * @see org.eclipse.jface.viewers.TableViewerFocusCellManager#getFocusCell()
   */
  @Override
  public ViewerCell getFocusCell( )
  {
    // TODO Auto-generated method stub
    return super.getFocusCell();
  }

  public void setFocusCell2( final ViewerCell cell )
  {

  }
}