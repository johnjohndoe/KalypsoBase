package org.kalypso.zml.ui.table.focus;

import jregex.Pattern;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Text;
import org.kalypso.zml.ui.table.model.cells.IZmlTableValueCell;
import org.kalypso.zml.ui.table.model.columns.IZmlTableValueColumn;

/**
 * @author Dirk Kuch
 */
final class ZmlTableEditorActivationStrategy extends ColumnViewerEditorActivationStrategy
{
  protected final ZmlTableFocusCellManager m_cellManager;

  public ZmlTableEditorActivationStrategy( final TableViewer viewer, final ZmlTableFocusCellManager cellManager )
  {
    super( viewer );
    m_cellManager = cellManager;

    hookListener( viewer );
  }

  private void hookListener( final ColumnViewer viewer )
  {
    final Pattern pattern = new Pattern( "[\\w\\d]" ); // $NON-NLS-1$
    viewer.getControl().addKeyListener( new KeyAdapter()
    {
      @Override
      public void keyPressed( final KeyEvent e )
      {
        final char character = e.character;

        if( org.kalypso.commons.java.lang.Objects.isNotNull( character ) && pattern.matches( String.valueOf( character ) ) )
        {
          final IZmlTableValueCell cell = (IZmlTableValueCell) m_cellManager.getFocusTableCell();
          if( org.kalypso.commons.java.lang.Objects.isNull( cell ) )
            return;

          startEditing( cell, character );
        }
      }

      private void startEditing( final IZmlTableValueCell cell, final char character )
      {
        viewer.editElement( cell.getRow().getModelRow(), cell.findIndex() );

        final IZmlTableValueColumn column = cell.getColumn();
        final ZmlTableEditingSupport support = column.getEditingSupport();
        if( support != null )
        {
          final TextCellEditor editor = support.getCellEditor();
          ((Text) editor.getControl()).insert( String.valueOf( character ) );
        }
      }
    } );
  }

  @Override
  protected boolean isEditorActivationEvent( final ColumnViewerEditorActivationEvent event )
  {
    if( ColumnViewerEditorActivationEvent.TRAVERSAL == event.eventType )
      return true;
    else if( ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION == event.eventType )
      return true;
    else if( ColumnViewerEditorActivationEvent.KEY_PRESSED == event.eventType )
    {
      if( SWT.CR == event.keyCode || SWT.F2 == event.keyCode )
        return true;
    }
    else if( ColumnViewerEditorActivationEvent.PROGRAMMATIC == event.eventType )
      return true;

    return false;
  }
}