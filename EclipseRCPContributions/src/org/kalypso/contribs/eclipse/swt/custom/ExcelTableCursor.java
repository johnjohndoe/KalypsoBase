package org.kalypso.contribs.eclipse.swt.custom;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * TODO: merge code with TableCursor-Copy
 * 
 * @author Gernot Belger
 */
public class ExcelTableCursor extends TableCursor
{
  // it is difficult to debug thinks like event
  // in eclipse debugmode, so here some printouts
  // can be enabled
  private final boolean DEBUG = false;

  public static enum ADVANCE_MODE
  {
    DOWN
    {
      @Override
      public String toString( )
      {
        return "nach &unten setzen";
      }
    },
    RIGHT
    {
      @Override
      public String toString( )
      {
        return "nach &rechts setzen";
      }
    },
    NONE
    {
      @Override
      public String toString( )
      {
        return "&nicht verändern";
      }
    };
  }

  /**
   * allow editing with interactive mouse (e.g. toggle checkbox)
   */
  private final MouseListener m_cellEditorMouseListener = new MouseAdapter()
  {
    /**
     * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
     */
    @Override
    public void mouseDown( final MouseEvent e )
    {
      // force default selection (e.g. checkbox gets toggled)
      if( e.button == 1 )
        startEditing( null );
    }
  };

  /**
   * keylistener while editing a cell <br>
   * handle cursor moving
   */
  final KeyListener m_keyListenerOnCell = new KeyAdapter()
  {
    @Override
    public void keyPressed( final KeyEvent e )
    {
      if( DEBUG )
        System.out.println( "Key pressed: " + e.character );

      // handle cursor moving
      int dx = 0, dy = 0;

      /*
       * If the user presses an arrow key, stop editing and move in this direction.<p>BUGFIX: don't do this for
       * left/right because than we cannot move within the cell.
       */
      // if( e.keyCode == SWT.ARROW_LEFT )
      // dx = -1;
      // else if( e.keyCode == SWT.ARROW_RIGHT )
      // dx = 1;
      // else
      if( e.keyCode == SWT.ARROW_UP )
        dy = -1;
      else if( e.keyCode == SWT.ARROW_DOWN )
        dy = 1;
      else if( e.keyCode == SWT.ESC )
      {
        // handle ESCAPE
        // final CellEditor cellEditor = getViewer().getCellEditors()[getColumn()];
        // cellEditor.performUndo();
      }
      /* On enter, stop editing and move either forwards or downwards */
      else if( e.keyCode == SWT.CR )
        switch( getAdvanceMode() )
        {
          case RIGHT:
            dx = 1;
            break;
          case DOWN:
            dy = 1;
            break;

          case NONE:
          default:
            break;
        }
      else if( e.keyCode == SWT.TAB )
      {
        final Table table = getViewer().getTable();
        final TableItem row2 = getRow();

        final int row = table.indexOf( row2 );
        final int col = getColumn();
        final int rowCount = table.getItemCount();
        final int columnCount = table.getColumnCount();

        // Advance cursor: always go to the left, go to first item of row at the end of row
        // go to first item at end of table
        if( col == columnCount - 1 )
        {
          dx = 1 - columnCount;

          if( row == rowCount - 1 )
            dy = 1 - rowCount;
          else
            dy = 1;
        }
        else
        {
          dx = 1;
          dy = 0;
        }
      }

      advanceCursor( (Control) e.getSource(), dx, dy );

      /*
       * Special case: checkbox: always toggle? TODO: shouldn't handle this the CheckboxCellEditor??
       */
      if( (e.keyCode != SWT.CR) && (e.getSource() instanceof CheckboxCellEditor) )
      {
        // toggle checkbox
        final CheckboxCellEditor ce = (CheckboxCellEditor) e.getSource();
        if( Boolean.TRUE.equals( ce.getValue() ) )
          ce.setValue( Boolean.FALSE );
        else
          ce.setValue( Boolean.TRUE );
      }
    }
  };

  /**
   * keylistener on table <br>
   * handle start editing on pressed key <br>
   * handle CTRL and SHIFT and TAB keys
   */
  final KeyListener m_keyListenerOnTableCursor = new KeyAdapter()
  {
    @Override
    public void keyPressed( final KeyEvent e )
    {
      if( (e.keyCode == SWT.CTRL) || ((e.stateMask & SWT.CONTROL) != 0) )
      {
        setVisible( false );
        return;
      }

      if( (e.keyCode == SWT.SHIFT) || ((e.stateMask & SWT.SHIFT) != 0) )
      {
        setVisible( false );
        return;
      }

      // handle F2 to start editing
      if( (e.keyCode == SWT.F2)
          || ((" -+,.;:öäüÖÄÜ´ß?`=!\"§$%&\\/()={}^°_#'<>|€µ".indexOf( e.character ) >= 0) || ((e.character >= '0') && (e.character <= 'z')) || ((e.character >= 'A') && (e.character <= 'Z'))) )
      {
        startEditing( e );
        return;
      }

      if( e.keyCode == SWT.DEL )
      {
        // TODO: empty current cell
      }

      if( e.keyCode == SWT.TAB )
      {
        final Table table = getViewer().getTable();
        final TableItem row2 = getRow();

        final int row = table.indexOf( row2 );
        final int col = getColumn();
        final int rowCount = table.getItemCount();
        final int columnCount = table.getColumnCount();

        final int dx;
        final int dy;

        // Advance cursor: always go to the left, go to first item of row at the end of row
        // go to first item at end of table
        if( col == columnCount - 1 )
        {
          dx = 1 - columnCount;

          if( row == rowCount - 1 )
            dy = 1 - rowCount;
          else
            dy = 1;
        }
        else
        {
          dx = 1;
          dy = 0;
        }

        advanceCursor( null, dx, dy );
        return;
      }

      setVisible( true );
      setFocus();
    }
  };

  /**
   * handle activation of cursor after multiselection
   */
  private final KeyListener m_tableKeyListener = new KeyAdapter()
  {
    @Override
    public void keyReleased( final KeyEvent e )
    {
      if( (e.keyCode == SWT.CONTROL) && ((e.stateMask & SWT.SHIFT) != 0) )
        return;
      if( (e.keyCode == SWT.SHIFT) && ((e.stateMask & SWT.CONTROL) != 0) )
        return;
      if( (e.keyCode != SWT.CONTROL) && ((e.stateMask & SWT.CONTROL) != 0) )
        return;
      if( (e.keyCode != SWT.SHIFT) && ((e.stateMask & SWT.SHIFT) != 0) )
        return;
      setVisible( true );
      setFocus();
    }
  };

  private final TraverseListener m_dontTraverseListener = new TraverseListener()
  {
    public void keyTraversed( final TraverseEvent e )
    {
      if( (e.detail == SWT.TRAVERSE_TAB_NEXT) || (e.detail == SWT.TRAVERSE_TAB_PREVIOUS) )
        e.doit = false;
    }
  };

  private final Color m_cannotEditColor;

  private final Color m_canEditColor;

  private ADVANCE_MODE m_mode;

  private final TableViewer m_viewer;

  private final Color m_errorColor;

  public ExcelTableCursor( final TableViewer viewer, final int style, final ADVANCE_MODE mode, final boolean selectionFollowsCursor )
  {
    super( viewer.getTable(), style );

    m_viewer = viewer;
    m_mode = mode;

    m_cannotEditColor = viewer.getTable().getDisplay().getSystemColor( SWT.COLOR_GRAY );
    m_canEditColor = getBackground();
    m_errorColor = viewer.getControl().getDisplay().getSystemColor( SWT.COLOR_RED );

    // add keylistener to start editing on key pressed
    addKeyListener( m_keyListenerOnTableCursor );

    // change background color when cell is not editable
    addSelectionListener(
    /**
     * handle background color and default editing
     */
    new SelectionListener()
    {
      /**
       * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      public void widgetSelected( final SelectionEvent e )
      {
        if( DEBUG )
          System.out.println( "widgetSelected" );

        final TableItem row = getRow();
        final int widgetCol = getColumn();
        // change background color when cell is not editable
        final boolean canModify = checkCanModify( row, widgetCol );
        setBackground( canModify ? getCanEditColor() : getCannotEditColor() );

        if( selectionFollowsCursor )
          ((Table) getParent()).setSelection( new TableItem[] { row } );
      }

      /**
       * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
       */
      public void widgetDefaultSelected( final SelectionEvent e )
      {
        startEditing( null );
      }
    } );

    final Table table = viewer.getTable();
    table.addKeyListener( m_tableKeyListener );

    // BUGFIX: always invalidate self if table was redrawn. Fixes: if a row was deleted/added,
    // the cursor showed still the old value
    // Is there a better idea than to use PaintListener? Also the cursor should keep its
    // relative place in the table
    table.addPaintListener( new PaintListener()
    {
      public void paintControl( final PaintEvent e )
      {
        if( !ExcelTableCursor.this.isDisposed() )
          ExcelTableCursor.this.redraw();
      }
    } );

    addMouseListener( m_cellEditorMouseListener );
  }

  protected TableViewer getViewer( )
  {
    return m_viewer;
  }

  protected boolean getDebug( )
  {
    return DEBUG;
  }

  public void setAdvanceMode( final ADVANCE_MODE mode )
  {
    m_mode = mode;
  }

  public ADVANCE_MODE getAdvanceMode( )
  {
    return m_mode;
  }

  protected void startEditing( final KeyEvent keyEvent )
  {
    final int column = getColumn();
    final TableItem tableRow = getRow();
    if( tableRow == null )
      return;

    // get current value of the cell
    final Object element = tableRow.getData();

    // do nothing if cell is not editable
    if( !checkCanModify( tableRow, column ) )
      return;

    // tablecursor should be invisible while editing the cell
    setVisible( false );
    // add the editorListener to the celleditor in order to refocus the
    // tablecursor
    final CellEditor cellEditor = m_viewer.getCellEditors()[column];

    cellEditor.addListener( new ValidateCellEditorListener( cellEditor, m_errorColor ) );
    cellEditor.addListener( new StopEditingCellEditorListener( cellEditor, this, m_viewer ) );

    // remove potential old listener
    final Control control = cellEditor.getControl();
    if( (control != null) && !control.isDisposed() )
    {
      control.removeKeyListener( m_keyListenerOnCell );
      control.addKeyListener( m_keyListenerOnCell );
      control.removeTraverseListener( m_dontTraverseListener );
      control.addTraverseListener( m_dontTraverseListener );
    }
    m_viewer.editElement( element, column );

    // eigentlich würde ich gerne direkt den event weiterschicken, das klappt
    // aber nicht
    // ??
    // final Event event = new Event();
    // event.type = SWT.KeyDown;
    // event.character = keyEvent.character;
    // event.keyCode = keyEvent.keyCode;
    // // wäre schön, jetzt ein KeyPressed abzusetzen
    // editorControl.notifyListeners( SWT.KeyDown, event );
    // ??

    // do not loose pressed character
    //
    if( (keyEvent != null) && (control != null) && !control.isDisposed() && (control instanceof Button) )
    {
      final Button button = (Button) control;
      button.setSelection( !button.getSelection() );
    }
    //
    if( control instanceof Text )
    {
      final Text text = (Text) control;
      if( (keyEvent != null) && (keyEvent.keyCode != SWT.F2) )
        text.insert( "" + keyEvent.character );
    }

  }

  protected boolean checkCanModify( final TableItem row, final int column )
  {
    if( m_viewer == null )
      return false;

    final String property = m_viewer.getColumnProperties()[column].toString();
    final ICellModifier modifier = m_viewer.getCellModifier();
    if( modifier == null )
      return false;

    return modifier.canModify( row.getData(), property );
  }

  public void stopEditing( final Control cellEditorControl )
  {
    // leaf cell
    if( cellEditorControl != null )
    {
      cellEditorControl.removeKeyListener( m_keyListenerOnCell );
      cellEditorControl.removeTraverseListener( m_dontTraverseListener );
    }

    setVisible( true );

    setFocus();
  }

  /**
   * @return Returns the canEditColor.
   */
  protected Color getCanEditColor( )
  {
    return m_canEditColor;
  }

  /**
   * @return Returns the cannotEditColor.
   */
  protected Color getCannotEditColor( )
  {
    return m_cannotEditColor;
  }

  /**
   * Advances the cursor position by the given delta.
   * 
   * @param control
   *          If not null, the keyListener will be removed from this control, used by the key-listener itself.
   */
  protected void advanceCursor( final Control control, final int dx, final int dy )
  {
    if( (dx != 0) || (dy != 0) )
    {
      stopEditing( control );

      final Table table = getViewer().getTable();
      final TableItem row2 = getRow();

      if( row2 == null )
        return;

      final int row = table.indexOf( row2 ) + dy;
      final int col = getColumn() + dx;
      final int rowCount = table.getItemCount();
      final int columnCount = table.getColumnCount();

      if( (col >= 0) && (col < columnCount) && (row >= 0) && (row < rowCount) )
        setSelection( row, col, true );
    }
  }
}
