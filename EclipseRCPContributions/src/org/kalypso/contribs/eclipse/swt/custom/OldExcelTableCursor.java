package org.kalypso.contribs.eclipse.swt.custom;

import static org.kalypso.contribs.eclipse.swt.custom.OldExcelTableCursor.ADVANCE_MODE.DOWN;
import static org.kalypso.contribs.eclipse.swt.custom.OldExcelTableCursor.ADVANCE_MODE.NONE;
import static org.kalypso.contribs.eclipse.swt.custom.OldExcelTableCursor.ADVANCE_MODE.RIGHT;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author gernot
 * 
 */
public class OldExcelTableCursor extends TableCursor
{
  public static enum ADVANCE_MODE {
    DOWN {
      @Override
      public String toString( )
      {
        return "nach &unten setzen";
      }
    },
    RIGHT {
      @Override
      public String toString( )
      {
        return "nach &rechts setzen";
      }
    },
    NONE {
      @Override
      public String toString( )
      {
        return "&nicht verändern";
      }
    };
  }

  private ADVANCE_MODE m_mode;

  private final TableViewer m_viewer;

  public OldExcelTableCursor( final TableViewer viewer, final int style,
      final ADVANCE_MODE mode )
  {
    super( viewer.getTable(), style );
    m_viewer = viewer;
    m_mode = mode;

    final Table table = viewer.getTable();

    addSelectionListener( new SelectionAdapter()
    {
      // when the TableEditor is over a cell, select the 
      // corresponding row in the table
      @Override
      public void widgetSelected( final SelectionEvent se )
      {
        table.setSelection( new TableItem[] { getRow() } );
      }

      // when the user hits "ENTER" in the TableCursor, pop up a
      // text editor so that they can change the text of the cell
      @Override
      public void widgetDefaultSelected( final SelectionEvent se )
      {
        startEditing( null );
      }
    } );

    // Hide the TableCursor when the user hits the "MOD1" or "MOD2" key.
    // This alows the user to select multiple items in the table.
    addKeyListener( new KeyAdapter()
    {
      @Override
      public void keyPressed( final KeyEvent e )
      {
        if( Character.getType( e.character ) == Character.DECIMAL_DIGIT_NUMBER )
          startEditing( e );
        else if( e.keyCode == SWT.F2 )
          startEditing( null );
        else if( e.keyCode == SWT.MOD1 || e.keyCode == SWT.MOD2
            || (e.stateMask & SWT.MOD1) != 0 || (e.stateMask & SWT.MOD2) != 0 )
          setVisible( false );
      }
    } );

    // Show the TableCursor when the user releases the "MOD2" or "MOD1" key.
    // This signals the end of the multiple selection task.
    table.addKeyListener( new KeyAdapter()
    {
      @Override
      public void keyReleased( KeyEvent e )
      {
        if( e.keyCode == SWT.MOD1 && (e.stateMask & SWT.MOD2) != 0 )
          return;
        if( e.keyCode == SWT.MOD2 && (e.stateMask & SWT.MOD1) != 0 )
          return;
        if( e.keyCode != SWT.MOD1 && (e.stateMask & SWT.MOD1) != 0 )
          return;
        if( e.keyCode != SWT.MOD2 && (e.stateMask & SWT.MOD2) != 0 )
          return;

        final TableItem[] selection = table.getSelection();
        final TableItem row = (selection.length == 0) ? table.getItem( table
            .getTopIndex() ) : selection[0];
        table.showItem( row );
        setSelection( row, 0 );
        setVisible( true );
        setFocus();
      }
    } );
  }

  public void setAdvanceMode( final ADVANCE_MODE mode )
  {
    m_mode = mode;
  }

  protected void startEditing( final KeyEvent ke )
  {
    final int column = getColumn();
    final CellEditor[] editors = m_viewer.getCellEditors();
    if( column > editors.length - 1 )
      return;
    
    final CellEditor cellEditor = editors[column];
    cellEditor.addListener( new ICellEditorListener()
    {
      public void applyEditorValue( )
      {
        onEditingStopped( cellEditor, this, true );
      }

      public void cancelEditor( )
      {
        onEditingStopped( cellEditor, this, false );
      }

      public void editorValueChanged( boolean oldValidState,
          boolean newValidState )
      {
        // 
      }
    } );

    setVisible( false );
    m_viewer.editElement( getRow().getData(), column );

    if( !m_viewer.isCellEditorActive() )
      onEditingStopped( null, null, false );
    else if( ke != null && cellEditor instanceof TextCellEditor )
    {
      cellEditor.setValue( "" + ke.character );
      cellEditor.performSelectAll();
      cellEditor.performCut();
      cellEditor.performPaste();
    }
  }

  protected void onEditingStopped( final CellEditor editor,
      final ICellEditorListener l, final boolean advance )
  {
    if( editor != null )
      editor.removeListener( l );

    if( advance )
      advanceCursor();

    setVisible( true );
  }

  private void advanceCursor( )
  {
    final Table table = (Table) getParent();
    int rowindex = table.indexOf( getRow() );
    int columnindex = getColumn();

    switch( m_mode )
    {
      case RIGHT:
        columnindex++;
        if( columnindex > table.getColumnCount() - 1 )
          columnindex = 0;
        else
          break;

      case DOWN:
        rowindex++;
        if( rowindex > table.getItemCount() - 1 )
          rowindex = 0;
        break;

      case NONE:
      default:
        break;
    }

    setSelection( rowindex, columnindex );
  }

}
