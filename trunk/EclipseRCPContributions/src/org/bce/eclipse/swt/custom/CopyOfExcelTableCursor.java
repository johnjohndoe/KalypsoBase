package org.bce.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * @author gernot
 * 
 */
public class CopyOfExcelTableCursor extends TableCursor
{
  public static enum ADVANCE_MODE {
    DOWN {
      public String toString( )
      {
        return "nach &unten setzen";
      };
    },
    RIGHT {
      public String toString( )
      {
        return "nach &rechts setzen";
      };
    },
    NONE {
      public String toString( )
      {
        return "&nicht verändern";
      };
    };
  }

  private final ControlEditor m_editor = new ControlEditor( this );

  private ADVANCE_MODE m_mode;

  public CopyOfExcelTableCursor( final Table table, final int style,
      final ADVANCE_MODE mode )
  {
    super( table, style );

    m_mode = mode;

    m_editor.grabHorizontal = true;
    m_editor.grabVertical = true;

    addSelectionListener( new SelectionAdapter()
    {
      // when the TableEditor is over a cell, select the corresponding row in
      // the table
      public void widgetSelected( final SelectionEvent se )
      {
        table.setSelection( new TableItem[] { getRow() } );
      }

      // when the user hits "ENTER" in the TableCursor, pop up a text editor so
      // that
      // they can change the text of the cell
      public void widgetDefaultSelected( final SelectionEvent se )
      {
        startEditing( null );
      }
    } );

    // Hide the TableCursor when the user hits the "MOD1" or "MOD2" key.
    // This alows the user to select multiple items in the table.
    addKeyListener( new KeyAdapter()
    {
      public void keyPressed( final KeyEvent e )
      {
        if( Character.getType( e.character ) == Character.DECIMAL_DIGIT_NUMBER )
          startEditing( e.character );
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

  protected void startEditing( final Character c )
  {
    final Text text = new Text( CopyOfExcelTableCursor.this, SWT.NONE );
    text.setText( getRow().getText( getColumn() ) );

    text.addKeyListener( new KeyAdapter()
    {
      public void keyPressed( final KeyEvent ke )
      {
        // close the text editor and copy the data over
        // when the user hits "ENTER"
        if( ke.character == SWT.CR || ke.character == SWT.TAB )
          stopEditing( text );

        // close the text editor when the user hits "ESC"
        if( ke.character == SWT.ESC )
          cancelEditing( text );
      }
    } );
    m_editor.setEditor( text );
    text.setFocus();

    if( c != null )
    {
      text.insert( "" + c );
      // text.setText( "" + c );
    }
  }

  protected void stopEditing( final Text editor )
  {
    getRow().setText( getColumn(), editor.getText() );
    cancelEditing( editor );

    advanceCursor();
  }

  protected void cancelEditing( final Control editor )
  {
    editor.dispose();
    setFocus();
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
