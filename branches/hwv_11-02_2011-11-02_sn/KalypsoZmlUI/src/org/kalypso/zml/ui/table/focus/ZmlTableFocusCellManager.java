package org.kalypso.zml.ui.table.focus;

import org.eclipse.jface.viewers.CellNavigationStrategy;
import org.eclipse.jface.viewers.FocusCellHighlighter;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.ViewerCell;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.base.helper.ZmlTables;
import org.kalypso.zml.ui.table.model.IZmlTableCell;
import org.kalypso.zml.ui.table.model.IZmlTableColumn;
import org.kalypso.zml.ui.table.model.IZmlTableRow;
import org.kalypso.zml.ui.table.model.ZmlTableCell;

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

    final IZmlTableRow row = ZmlTables.toTableRow( m_table, cell );
    final IZmlTableColumn column = m_table.findColumn( cell.getColumnIndex() );
    if( Objects.isNull( row, column ) )
      return null;

    return new ZmlTableCell( row, column );
  }
}