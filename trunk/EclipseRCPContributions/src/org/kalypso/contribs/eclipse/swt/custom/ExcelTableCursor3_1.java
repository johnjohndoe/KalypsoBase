package org.kalypso.contribs.eclipse.swt.custom;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * TODO: merge with other ExcelLikeTablecursor
 * 
 * @author belger
 */
public class ExcelTableCursor3_1 extends TableCursor
{
  // it is difficult to debug thinks like event
  // in eclipse debugmode, so here some printouts
  // can be enabled
  private final boolean DEBUG = false;

  public static enum ADVANCE_MODE
  {
    DOWN
    {
      public String toString( )
      {
        return "nach &unten setzen";
      }
    },
    RIGHT
    {
      public String toString( )
      {
        return "nach &rechts setzen";
      }
    },
    NONE
    {
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
    public void mouseDown( final MouseEvent e )
    {
      // force default selection (e.g. checkbox gets toggled)
      if( e.button == 1 )
        startEditing( null );
    }
  };

  /**
   * handle stop editing to continue navigating with cursor on key events
   */
  final ICellEditorListener m_cellEditorListener = new ICellEditorListener()
  {
    // after editing set tablecursor visible
    // and give it the focus to continue navigating (e.g. CR-Up, RC-Down)
    public void applyEditorValue( )
    {
      // leaf cell
      stopEditing();
    }

    public void cancelEditor( )
    {
      // leaf cell
      stopEditing();
    }

    public void editorValueChanged( boolean oldValidState, boolean newValidState )
    {
      // nothing (maybe change color of something ?)
    }
  };

  /**
   * keylistener while editing a cell <br>
   * handle cursor moving
   */
  final KeyListener m_keyListenerOnCell = new KeyAdapter()
  {
    public void keyPressed( KeyEvent e )
    {
      // handle cursor moving
      int dx = 0, dy = 0;
      if( e.keyCode == SWT.ARROW_LEFT )
        dx = -1;
      else if( e.keyCode == SWT.ARROW_RIGHT )
        dx = 1;
      else if( e.keyCode == SWT.ARROW_UP )
        dy = -1;
      else if( e.keyCode == SWT.ARROW_DOWN )
        dy = 1;
      else if( e.keyCode == SWT.ESC )
      {
        // handle ESCAPE
        final CellEditor cellEditor = getViewer().getCellEditors()[getColumn()];
        cellEditor.performUndo();
      }
      else if( e.keyCode == SWT.CR )
      {
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
      }

      if( dx != 0 || dy != 0 )
      {
        final int col = getColumn() + dx;
        final Table table2 = getViewer().getTable();
        TableItem row2 = getRow();
        final int row = table2.indexOf( row2 ) + dy;

        if( col >= 0 && col < table2.getColumnCount() && row >= 0 && row < table2.getItemCount() )
        {
          setSelection( row, col );
          setVisible( true );
          setFocus();
          // leaf cell
          ((Control)e.getSource()).removeKeyListener( m_keyListenerOnCell );
        }
      }
      if( e.keyCode != SWT.CR && e.getSource() instanceof CheckboxCellEditor )
      {
        // toggle checkbox
        CheckboxCellEditor ce = (CheckboxCellEditor)e.getSource();
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
   * handle CTRL and SHIFT keys
   */
  final KeyListener m_keyListenerOnTableCursor = new KeyAdapter()
  {
    public void keyPressed( final KeyEvent e )
    {
      if( e.keyCode == SWT.CTRL || (e.stateMask & SWT.CONTROL) != 0 )
      {
        setVisible( false );
        return;
      }

      if( e.keyCode == SWT.SHIFT || (e.stateMask & SWT.SHIFT) != 0 )
      {
        setVisible( false );
        return;
      }

      // handle F2 to start editing
      if( e.keyCode == SWT.F2
          || (" -+,.;:öäüÖÄÜ´ß?`=!\"§$%&\\/()={}^°_#'<>|€µ".indexOf( e.character ) >= 0
              || (e.character >= '0' && e.character <= 'z') || (e.character >= 'A' && e.character <= 'Z')) )
      {
        startEditing( e );
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
    public void keyReleased( final KeyEvent e )
    {
      if( e.keyCode == SWT.CONTROL && (e.stateMask & SWT.SHIFT) != 0 )
        return;
      if( e.keyCode == SWT.SHIFT && (e.stateMask & SWT.CONTROL) != 0 )
        return;
      if( e.keyCode != SWT.CONTROL && (e.stateMask & SWT.CONTROL) != 0 )
        return;
      if( e.keyCode != SWT.SHIFT && (e.stateMask & SWT.SHIFT) != 0 )
        return;
      setVisible( true );
      setFocus();
    }
  };

  private final Color m_cannotEditColor;

  private final Color m_canEditColor;

  private ADVANCE_MODE m_mode;

  private final TableViewer m_viewer;

  public ExcelTableCursor3_1( final TableViewer viewer, final int style, final ADVANCE_MODE mode,
      final boolean selectionFollowsCursor )
  {
    super( viewer.getTable(), style );

    m_viewer = viewer;
    m_mode = mode;

    m_cannotEditColor = viewer.getTable().getDisplay().getSystemColor( SWT.COLOR_GRAY );
    m_canEditColor = getBackground();

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
          ((Table)getParent()).setSelection( new TableItem[]
          { row } );
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
    cellEditor.removeListener( m_cellEditorListener );
    cellEditor.addListener( m_cellEditorListener );

    // remove potential old listener
    final Control control = cellEditor.getControl();
    if( control != null && !control.isDisposed() )
    {
      control.removeKeyListener( m_keyListenerOnCell );
      if( keyEvent != null && keyEvent.keyCode != SWT.F2 )
        control.addKeyListener( m_keyListenerOnCell );
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
    if( keyEvent != null && control != null && !control.isDisposed() && control instanceof Button )
    {
      Button button = (Button)control;
      button.setSelection( !button.getSelection() );
    }
    // 
    if( control instanceof Text )
    {
      final Text text = (Text)control;
      if( keyEvent != null && keyEvent.keyCode != SWT.F2 )
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

  public void stopEditing( )
  {
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
}
