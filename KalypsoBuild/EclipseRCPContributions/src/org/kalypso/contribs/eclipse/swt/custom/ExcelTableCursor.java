package org.kalypso.contribs.eclipse.swt.custom;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusListener;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * TODO: merge code with TableCursor-Copy
 * 
 * @author Gernot Belger
 */
public class ExcelTableCursor extends TableCursor
{
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
  private final MouseListener m_mouseListener = new MouseAdapter()
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

      final boolean allowUpDown = !(e.widget instanceof CCombo);
      final boolean allowLeftRight = !(e.widget instanceof Text);

      // handle cursor moving
      int dx = 0;
      int dy = 0;

      if( e.keyCode == SWT.ARROW_LEFT && allowLeftRight )
        dx = -1;
      else if( e.keyCode == SWT.ARROW_RIGHT && allowLeftRight )
        dx = 1;
      else if( e.keyCode == SWT.ARROW_UP && allowUpDown )
        dy = -1;
      else if( e.keyCode == SWT.ARROW_DOWN && allowUpDown )
        dy = 1;
      else if( e.keyCode == SWT.ESC )
      {
        // handle ESCAPE
        // final CellEditor cellEditor = getViewer().getCellEditors()[getColumn()];
        // cellEditor.performUndo();
      }
      /* On enter, stop editing and move either forwards or downwards */
      else if( e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR )
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
      if( (e.keyCode != SWT.CR || e.keyCode == SWT.KEYPAD_CR) && (e.getSource() instanceof CheckboxCellEditor) )
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
  final KeyListener m_keyListener = new KeyAdapter()
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
    }
  };

  private final ISelectionChangedListener m_tableSelectionListener = new ISelectionChangedListener()
  {
    @Override
    public void selectionChanged( final SelectionChangedEvent event )
    {
      handleTableSelectionChanged();
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
    @Override
    public void keyTraversed( final TraverseEvent e )
    {
      if( (e.detail == SWT.TRAVERSE_TAB_NEXT) || (e.detail == SWT.TRAVERSE_TAB_PREVIOUS) )
        e.doit = false;
    }
  };

  private final SelectionListener m_selectionListener = new SelectionListener()
  {
    /**
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    @Override
    public void widgetSelected( final SelectionEvent e )
    {
      handleWidgetSelected();
    }

    @Override
    public void widgetDefaultSelected( final SelectionEvent e )
    {
      startEditing( null );
    }
  };

  private final FocusListener m_tableFocusListener = new FocusAdapter()
  {
    @Override
    public void focusGained( final org.eclipse.swt.events.FocusEvent e )
    {
      tableFocusIn();
    }
  };

  private final MouseListener m_tableMouseListener = new MouseAdapter()
  {
    @Override
    public void mouseDown( final MouseEvent e )
    {
      tableMouseDown( e );
    }
  };

  private final Color m_cannotEditColor;

  private final Color m_canEditColor;

  private ADVANCE_MODE m_mode;

  private final TableViewer m_viewer;

  private final Color m_errorColor;

  private final boolean m_selectionFollowsCursor;

  public ExcelTableCursor( final TableViewer viewer, final int style, final ADVANCE_MODE mode, final boolean selectionFollowsCursor )
  {
    super( viewer.getTable(), style );

    m_viewer = viewer;
    m_mode = mode;
    m_selectionFollowsCursor = selectionFollowsCursor;

    m_cannotEditColor = viewer.getTable().getDisplay().getSystemColor( SWT.COLOR_GRAY );
    m_canEditColor = getBackground();
    m_errorColor = viewer.getControl().getDisplay().getSystemColor( SWT.COLOR_RED );

    // add keylistener to start editing on key pressed
    addKeyListener( m_keyListener );

    // change background color when cell is not editable
    addSelectionListener( m_selectionListener );

    final Table table = viewer.getTable();
    table.addKeyListener( m_tableKeyListener );
    table.addFocusListener( m_tableFocusListener );
    table.addMouseListener( m_tableMouseListener );

    viewer.addSelectionChangedListener( m_tableSelectionListener );

    // BUGFIX: always invalidate self if table was redrawn. Fixes: if a row was deleted/added,
    // the cursor showed still the old value
    // Is there a better idea than to use PaintListener? Also the cursor should keep its
    // relative place in the table
    table.addPaintListener( new PaintListener()
    {
      @Override
      public void paintControl( final PaintEvent e )
      {
        if( !ExcelTableCursor.this.isDisposed() )
          ExcelTableCursor.this.redraw();
      }
    } );

    addMouseListener( m_mouseListener );
  }

  protected void handleTableSelectionChanged( )
  {
    final Table table = m_viewer.getTable();
    final int selectionIndex = table.getSelectionIndex();
    final TableItem row = getRow();
    final TableItem selectedRow = selectionIndex == -1 ? null : table.getItem( selectionIndex );
    if( selectedRow != row )
    {
      if( selectedRow == null )
        setVisible( false );
      else
      {
        final int column = getColumn();
        setSelection( selectionIndex, column, false );
      }
    }
  }

  protected void handleWidgetSelected( )
  {
    if( DEBUG )
      System.out.println( "handleWidgetSelected" );

    final TableItem row = getRow();
    final int widgetCol = getColumn();

    // change background color when cell is not editable
    final boolean canModify = checkCanModify( row, widgetCol );
    setBackground( canModify ? getCanEditColor() : getCannotEditColor() );

    if( m_selectionFollowsCursor )
    {
      final Table table = (Table) getParent();

      final TableItem[] currentSelection = table.getSelection();
      if( currentSelection.length == 1 && currentSelection[0] == row )
        return;

      table.setSelection( new TableItem[] { row } );
      // TODO: sometimes the selection event already has been sent, so we now send it a second time.
      // TODO: as we are creating the event ourself, post selection does not work on this kind of tables. This is a
      // pity...
      table.notifyListeners( SWT.Selection, null );
    }
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
    if( DEBUG )
      System.out.println( "startEditing" );

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
    hookCellEditor( cellEditor );

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
    final Control control = cellEditor.getControl();
    if( keyEvent != null && control != null && !control.isDisposed() && control instanceof Button )
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

  /**
   * Hook listeners on current cell editor
   */
  void hookCellEditor( final CellEditor cellEditor )
  {
    cellEditor.addListener( new ValidateCellEditorListener( cellEditor, m_errorColor ) );
    cellEditor.addListener( new StopEditingCellEditorListener( cellEditor, this, m_viewer ) );

    // remove potential old listeners
    final Control control = cellEditor.getControl();
    if( (control != null) && !control.isDisposed() )
    {
      control.removeKeyListener( m_keyListenerOnCell );
      control.addKeyListener( m_keyListenerOnCell );
      control.removeTraverseListener( m_dontTraverseListener );
      control.addTraverseListener( m_dontTraverseListener );
    }
  }

  protected boolean checkCanModify( final TableItem row, final int column )
  {
    if( m_viewer == null )
      return false;

    final Object[] columnProperties = m_viewer.getColumnProperties();
    if( columnProperties == null )
      return false;

    final String property = columnProperties[column].toString();
    final ICellModifier modifier = m_viewer.getCellModifier();
    if( modifier == null )
      return false;

    if( row == null )
      return false;

    return modifier.canModify( row.getData(), property );
  }

  public void stopEditing( final Control cellEditorControl )
  {
    if( DEBUG )
      System.out.println( "stopEditing" );

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
    if( DEBUG )
      System.out.println( "advanceCursor" );

    if( dx == 0 && dy == 0 )
      return;

    stopEditing( control );

    // The stopEditing (above) probably triggered a ui-event that potentially removes the focus from the cursor
    // re-focus in an asynchronous display-event, forces the focus back to me
    getDisplay().asyncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        if( isDisposed() )
          return;

        final Table table = getViewer().getTable();
        final TableItem row2 = getRow();

        if( row2 == null )
          return;

        final int row = table.indexOf( row2 ) + dy;
        int col = getColumn() + dx;
        final int rowCount = table.getItemCount();
        final int columnCount = table.getColumnCount();

        if( (col >= 0) && (col < columnCount) && (row >= 0) && (row < rowCount) )
        {
          /*
           * Rather crude: advance further if the new column is not visible. Fixes the problem, that the first invisible
           * column breaks the tabbing.
           */
          final TableColumn column = getViewer().getTable().getColumn( col );
          final int width = column.getWidth();
          if( width == 0 && col < columnCount - 1 )
            col += 1;

          setSelection( row, col, true );
        }

        setFocus();

        // a bit wild... but still sometimes we lose the focus. So just do it once again...
        getDisplay().asyncExec( new Runnable()
        {
          @Override
          public void run( )
          {
            if( isDisposed() )
              return;

            setFocus();
          }
        } );
      }
    } );
  }

  void tableFocusIn( )
  {
    if( DEBUG )
      System.out.println( "tableFocusIn" );

    if( isDisposed() )
      return;
    if( isVisible() )
      setFocus();
  }

  void tableMouseDown( final MouseEvent event )
  {
    if( DEBUG )
      System.out.println( "tableMouseDown" );

    if( isDisposed() || !isVisible() )
      return;

    // FIXME: check, merged from Kalypso 2.2, but war originally a change in TableCursor
    /* Only change the selection on left clicks */
    // This is not yet perfect, but better than loose the selection when opening the context menu (right click)
    // However, at the moment the table moves the selection (if only one line is selected) but the cursor is not moved.
    if( event.button != 1 )
      return;

    final Point pt = new Point( event.x, event.y );
    // Find clicked row
    final int lineWidth = m_table.getLinesVisible() ? m_table.getGridLineWidth() : 0;
    TableItem item = m_table.getItem( pt );
    if( (m_table.getStyle() & SWT.FULL_SELECTION) != 0 )
    {
      if( item == null )
        return;
    }
    else
    {
      final int start = item != null ? m_table.indexOf( item ) : m_table.getTopIndex();
      final int end = m_table.getItemCount();
      final Rectangle clientRect = m_table.getClientArea();
      for( int i = start; i < end; i++ )
      {
        final TableItem nextItem = m_table.getItem( i );
        final Rectangle rect = nextItem.getBounds( 0 );
        if( pt.y >= rect.y && pt.y < rect.y + rect.height + lineWidth )
        {
          item = nextItem;
          break;
        }
        if( rect.y > clientRect.y + clientRect.height )
          return;
      }
      if( item == null )
        return;
    }

    // Find clicked column
    int column = -1;
    final int columnCount = m_table.getColumnCount();
    if( columnCount > 0 )
    {
      for( int i = 0; i < columnCount; i++ )
      {
        final Rectangle rect = item.getBounds( i );
        rect.width += lineWidth;
        rect.height += lineWidth;
        if( rect.contains( pt ) )
        {
          column = i;
          break;
        }
      }
      if( column == -1 )
        column = 0;
    }

    final TableColumn newColumn = column == -1 ? null : m_table.getColumn( column );

    final boolean cellEditorActive = m_viewer.isCellEditorActive();
    if( cellEditorActive )
    {
      setVisible( false );
      final CellEditor[] cellEditors = m_viewer.getCellEditors();
      hookCellEditor( cellEditors[column] );
      setRowColumn( item, newColumn, false );
    }
    else
    {
      setRowColumn( item, newColumn, true );
      setFocus();
    }

    return;
  }

  @Override
  public void setVisible( final boolean visible )
  {
    if( DEBUG )
      System.out.println( "setVisible: " + visible );

    checkWidget();
    if( visible )
    {
      // change background color when cell is not editable
      final boolean canModify = checkCanModify( getRow(), getColumn() );
      setBackground( canModify ? getCanEditColor() : getCannotEditColor() );
      resize();
    }

    super.setVisible( visible );
  }

}
