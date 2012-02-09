package org.kalypso.zml.ui.table.focus;

import org.eclipse.jface.viewers.CellNavigationStrategy;
import org.eclipse.jface.viewers.FocusCellHighlighter;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.ViewerCell;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.model.cells.IZmlTableCell;
import org.kalypso.zml.ui.table.model.columns.IZmlTableColumn;
import org.kalypso.zml.ui.table.model.columns.ZmlTableColumns;
import org.kalypso.zml.ui.table.model.rows.IZmlTableValueRow;

/**
 * @author Dirk Kuch
 */
public final class ZmlTableFocusCellManager extends TableViewerFocusCellManager
{
  private final IZmlTable m_table;

  ZmlTableFocusCellManager( final IZmlTable table, final FocusCellHighlighter focusDrawingDelegate, final CellNavigationStrategy navigationStrategy )
  {
    super( table.getViewer(), focusDrawingDelegate, navigationStrategy );
    m_table = table;
  }

  public IZmlTableCell getFocusTableCell( )
  {
    final ViewerCell cell = getFocusCell();
    if( Objects.isNull( cell ) )
      return null;

    final IZmlTableValueRow row = ZmlTableColumns.toTableRow( cell );
    final IZmlTableColumn column = m_table.findColumn( cell.getColumnIndex() );
    if( Objects.isNull( row, column ) )
      return null;

    return row.getCell( column );
  }
}