/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*****************************************
 * Copy/Paste of the original package org.eclipse.swt.custom.TableCursor in order to make it better. 
 */
package org.kalypso.contribs.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;

/**
 * A TableCursor provides a way for the user to navigate around a Table using the keyboard. It also provides a mechanism
 * for selecting an individual cell in a table.
 * <p>
 * Here is an example of using a TableCursor to navigate to a cell and then edit it. <code><pre>
 * public static void main( String[] args )
 * {
 *   Display display = new Display();
 *   Shell shell = new Shell( display );
 *   shell.setLayout( new GridLayout() );
 * 
 *   // create a a table with 3 columns and fill with data
 *   final Table table = new Table( shell, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION );
 *   table.setLayoutData( new GridData( GridData.FILL_BOTH ) );
 *   TableColumn column1 = new TableColumn( table, SWT.NONE );
 *   TableColumn column2 = new TableColumn( table, SWT.NONE );
 *   TableColumn column3 = new TableColumn( table, SWT.NONE );
 *   for( int i = 0; i &lt; 100; i++ )
 *   {
 *     TableItem item = new TableItem( table, SWT.NONE );
 *     item.setText( new String[] { &quot;cell &quot; + i + &quot; 0&quot;, &quot;cell &quot; + i + &quot; 1&quot;, &quot;cell &quot; + i + &quot; 2&quot; } );
 *   }
 *   column1.pack();
 *   column2.pack();
 *   column3.pack();
 * 
 *   // create a TableCursor to navigate around the table
 *   final TableCursor cursor = new TableCursor( table, SWT.NONE );
 *   // create an editor to edit the cell when the user hits &quot;ENTER&quot; 
 *   // while over a cell in the table
 *   final ControlEditor editor = new ControlEditor( cursor );
 *   editor.grabHorizontal = true;
 *   editor.grabVertical = true;
 * 
 *   cursor.addSelectionListener( new SelectionAdapter()
 *   {
 *     // when the TableEditor is over a cell, select the corresponding row in 
 *     // the table
 *     public void widgetSelected( SelectionEvent e )
 *     {
 *       table.setSelection( new TableItem[] { cursor.getRow() } );
 *     }
 * 
 *     // when the user hits &quot;ENTER&quot; in the TableCursor, pop up a text editor so that 
 *     // they can change the text of the cell
 *     public void widgetDefaultSelected( SelectionEvent e )
 *     {
 *       final Text text = new Text( cursor, SWT.NONE );
 *       TableItem row = cursor.getRow();
 *       int column = cursor.getColumn();
 *       text.setText( row.getText( column ) );
 *       text.addKeyListener( new KeyAdapter()
 *       {
 *         public void keyPressed( KeyEvent e )
 *         {
 *           // close the text editor and copy the data over 
 *           // when the user hits &quot;ENTER&quot;
 *           if( e.character == SWT.CR )
 *           {
 *             TableItem row = cursor.getRow();
 *             int column = cursor.getColumn();
 *             row.setText( column, text.getText() );
 *             text.dispose();
 *           }
 *           // close the text editor when the user hits &quot;ESC&quot;
 *           if( e.character == SWT.ESC )
 *           {
 *             text.dispose();
 *           }
 *         }
 *       } );
 *       editor.setEditor( text );
 *       text.setFocus();
 *     }
 *   } );
 *   // Hide the TableCursor when the user hits the &quot;MOD1&quot; or &quot;MOD2&quot; key.
 *   // This alows the user to select multiple items in the table.
 *   cursor.addKeyListener( new KeyAdapter()
 *   {
 *     public void keyPressed( KeyEvent e )
 *     {
 *       if( e.keyCode == SWT.MOD1 || e.keyCode == SWT.MOD2 || (e.stateMask &amp; SWT.MOD1) != 0 || (e.stateMask &amp; SWT.MOD2) != 0 )
 *       {
 *         cursor.setVisible( false );
 *       }
 *     }
 *   } );
 *   // Show the TableCursor when the user releases the &quot;MOD2&quot; or &quot;MOD1&quot; key.
 *   // This signals the end of the multiple selection task.
 *   table.addKeyListener( new KeyAdapter()
 *   {
 *     public void keyReleased( KeyEvent e )
 *     {
 *       if( e.keyCode == SWT.MOD1 &amp;&amp; (e.stateMask &amp; SWT.MOD2) != 0 )
 *         return;
 *       if( e.keyCode == SWT.MOD2 &amp;&amp; (e.stateMask &amp; SWT.MOD1) != 0 )
 *         return;
 *       if( e.keyCode != SWT.MOD1 &amp;&amp; (e.stateMask &amp; SWT.MOD1) != 0 )
 *         return;
 *       if( e.keyCode != SWT.MOD2 &amp;&amp; (e.stateMask &amp; SWT.MOD2) != 0 )
 *         return;
 * 
 *       TableItem[] selection = table.getSelection();
 *       TableItem row = (selection.length == 0) ? table.getItem( table.getTopIndex() ) : selection[0];
 *       table.showItem( row );
 *       cursor.setSelection( row, 0 );
 *       cursor.setVisible( true );
 *       cursor.setFocus();
 *     }
 *   } );
 * 
 *   shell.open();
 *   while( !shell.isDisposed() )
 *   {
 *     if( !display.readAndDispatch() )
 *       display.sleep();
 *   }
 *   display.dispose();
 * }
 * </pre></code>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BORDER</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection, DefaultSelection</dd>
 * </dl>
 * 
 * @since 2.0
 */
public class TableCursor extends Canvas
{
  // By default, invert the list selection colors
  static final int BACKGROUND = SWT.COLOR_LIST_SELECTION_TEXT;

  static final int FOREGROUND = SWT.COLOR_LIST_SELECTION;

  Table m_table;

  TableItem m_row = null;

  TableColumn m_column = null;

  Listener m_tableListener, m_resizeListener, m_disposeItemListener, m_disposeColumnListener;

  /**
   * Constructs a new instance of this class given its parent table and a style value describing its behavior and
   * appearance.
   * <p>
   * The style value is either one of the style constants defined in class <code>SWT</code> which is applicable to
   * instances of this class, or must be built by <em>bitwise OR</em>'ing together (that is, using the
   * <code>int</code> "|" operator) two or more of those <code>SWT</code> style constants. The class description
   * lists the style constants that are applicable to the class. Style bits are also inherited from superclasses.
   * </p>
   * 
   * @param parent
   *          a Table control which will be the parent of the new instance (cannot be null)
   * @param style
   *          the style of control to construct
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   *              <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   *              </ul>
   * @see SWT#BORDER
   * @see Widget#checkSubclass()
   * @see Widget#getStyle()
   */
  public TableCursor( Table parent, int style )
  {
    super( parent, style );
    m_table = parent;
    setBackground( null );
    setForeground( null );

    Listener listener = new Listener()
    {
      public void handleEvent( Event event )
      {
        switch( event.type )
        {
          case SWT.Dispose:
            disposeInternal();
            break;
          case SWT.FocusIn:
          case SWT.FocusOut:
            redraw();
            break;
          case SWT.KeyDown:
            keyDown( event );
            break;
          case SWT.Paint:
            paint( event );
            break;
          case SWT.Traverse:
            traverse( event );
            break;
        }
      }
    };
    int[] events = new int[] { SWT.Dispose, SWT.FocusIn, SWT.FocusOut, SWT.KeyDown, SWT.Paint, SWT.Traverse };
    for( int i = 0; i < events.length; i++ )
    {
      addListener( events[i], listener );
    }

    m_tableListener = new Listener()
    {
      public void handleEvent( Event event )
      {
        switch( event.type )
        {
          case SWT.MouseDown:
            tableMouseDown( event );
            break;
          case SWT.FocusIn:
            tableFocusIn();
            break;
        }
      }
    };
    m_table.addListener( SWT.FocusIn, m_tableListener );
    m_table.addListener( SWT.MouseDown, m_tableListener );

    m_disposeItemListener = new Listener()
    {
      public void handleEvent( Event event )
      {
        m_row = null;
        m_column = null;
        resize();
      }
    };
    m_disposeColumnListener = new Listener()
    {
      public void handleEvent( Event event )
      {
        m_row = null;
        m_column = null;
        resize();
      }
    };
    m_resizeListener = new Listener()
    {
      public void handleEvent( Event event )
      {
        resize();
      }
    };
    ScrollBar hBar = m_table.getHorizontalBar();
    if( hBar != null )
    {
      hBar.addListener( SWT.Selection, m_resizeListener );
    }
    ScrollBar vBar = m_table.getVerticalBar();
    if( vBar != null )
    {
      vBar.addListener( SWT.Selection, m_resizeListener );
    }
  }

  /**
   * Adds the listener to the collection of listeners who will be notified when the receiver's selection changes, by
   * sending it one of the messages defined in the <code>SelectionListener</code> interface.
   * <p>
   * When <code>widgetSelected</code> is called, the item field of the event object is valid. If the reciever has
   * <code>SWT.CHECK</code> style set and the check selection changes, the event object detail field contains the
   * value <code>SWT.CHECK</code>. <code>widgetDefaultSelected</code> is typically called when an item is
   * double-clicked.
   * </p>
   * 
   * @param listener
   *          the listener which should be notified
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *              </ul>
   * @see SelectionListener
   * @see SelectionEvent
   * @see #removeSelectionListener(SelectionListener)
   */
  public void addSelectionListener( SelectionListener listener )
  {
    checkWidget();
    if( listener == null )
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.Selection, typedListener );
    addListener( SWT.DefaultSelection, typedListener );
  }

  void disposeInternal( )
  {
    m_table.removeListener( SWT.FocusIn, m_tableListener );
    m_table.removeListener( SWT.MouseDown, m_tableListener );
    if( m_column != null )
    {
      m_column.removeListener( SWT.Dispose, m_disposeColumnListener );
      m_column.removeListener( SWT.Move, m_resizeListener );
      m_column.removeListener( SWT.Resize, m_resizeListener );
      m_column = null;
    }
    if( m_row != null )
    {
      m_row.removeListener( SWT.Dispose, m_disposeItemListener );
      m_row = null;
    }
    ScrollBar hBar = m_table.getHorizontalBar();
    if( hBar != null )
    {
      hBar.removeListener( SWT.Selection, m_resizeListener );
    }
    ScrollBar vBar = m_table.getVerticalBar();
    if( vBar != null )
    {
      vBar.removeListener( SWT.Selection, m_resizeListener );
    }
  }

  void keyDown( Event event )
  {
    if( m_row == null )
      return;
    switch( event.character )
    {
      case SWT.CR:
        notifyListeners( SWT.DefaultSelection, new Event() );
        return;
    }
    int rowIndex = m_table.indexOf( m_row );
    int columnIndex = m_column == null ? 0 : m_table.indexOf( m_column );
    switch( event.keyCode )
    {
      case SWT.ARROW_UP:
        setRowColumn( Math.max( 0, rowIndex - 1 ), columnIndex, true );
        break;
      case SWT.ARROW_DOWN:
        setRowColumn( Math.min( rowIndex + 1, m_table.getItemCount() - 1 ), columnIndex, true );
        break;
      case SWT.ARROW_LEFT:
      case SWT.ARROW_RIGHT:
      {
        int columnCount = m_table.getColumnCount();
        if( columnCount == 0 )
          break;
        int[] order = m_table.getColumnOrder();
        int index = 0;
        while( index < order.length )
        {
          if( order[index] == columnIndex )
            break;
          index++;
        }
        if( index == order.length )
          index = 0;
        int leadKey = (getStyle() & SWT.RIGHT_TO_LEFT) != 0 ? SWT.ARROW_RIGHT : SWT.ARROW_LEFT;
        if( event.keyCode == leadKey )
        {
          setRowColumn( rowIndex, order[Math.max( 0, index - 1 )], true );
        }
        else
        {
          setRowColumn( rowIndex, order[Math.min( columnCount - 1, index + 1 )], true );
        }
        break;
      }
      case SWT.HOME:
        setRowColumn( 0, columnIndex, true );
        break;
      case SWT.END:
      {
        int i = m_table.getItemCount() - 1;
        setRowColumn( i, columnIndex, true );
        break;
      }
      case SWT.PAGE_UP:
      {
        int index = m_table.getTopIndex();
        if( index == rowIndex )
        {
          Rectangle rect = m_table.getClientArea();
          TableItem item = m_table.getItem( index );
          Rectangle itemRect = item.getBounds( 0 );
          rect.height -= itemRect.y;
          int height = m_table.getItemHeight();
          int page = Math.max( 1, rect.height / height );
          index = Math.max( 0, index - page + 1 );
        }
        setRowColumn( index, columnIndex, true );
        break;
      }
      case SWT.PAGE_DOWN:
      {
        int index = m_table.getTopIndex();
        Rectangle rect = m_table.getClientArea();
        TableItem item = m_table.getItem( index );
        Rectangle itemRect = item.getBounds( 0 );
        rect.height -= itemRect.y;
        int height = m_table.getItemHeight();
        int page = Math.max( 1, rect.height / height );
        int end = m_table.getItemCount() - 1;
        index = Math.min( end, index + page - 1 );
        if( index == rowIndex )
        {
          index = Math.min( end, index + page - 1 );
        }
        setRowColumn( index, columnIndex, true );
        break;
      }
    }
  }

  void paint( Event event )
  {
    if( m_row == null )
      return;
    int columnIndex = m_column == null ? 0 : m_table.indexOf( m_column );
    GC gc = event.gc;
    Display display = getDisplay();
    gc.setBackground( getBackground() );
    gc.setForeground( getForeground() );
    gc.fillRectangle( event.x, event.y, event.width, event.height );
    int x = 0;
    Point size = getSize();
    Image image = m_row.getImage( columnIndex );
    if( image != null )
    {
      Rectangle imageSize = image.getBounds();
      int imageY = (size.y - imageSize.height) / 2;
      gc.drawImage( image, x, imageY );
      x += imageSize.width;
    }
    String text = m_row.getText( columnIndex );
    if( text != "" ) { //$NON-NLS-1$
      Rectangle bounds = m_row.getBounds( columnIndex );
      Point extent = gc.stringExtent( text );
      // Temporary code - need a better way to determine table trim
      String platform = SWT.getPlatform();
      if( "win32".equals( platform ) ) { //$NON-NLS-1$
        if( m_table.getColumnCount() == 0 || columnIndex == 0 )
        {
          x += 2;
        }
        else
        {
          int alignmnent = m_column.getAlignment();
          switch( alignmnent )
          {
            case SWT.LEFT:
              x += 6;
              break;
            case SWT.RIGHT:
              x = bounds.width - extent.x - 6;
              break;
            case SWT.CENTER:
              x += (bounds.width - x - extent.x) / 2;
              break;
          }
        }
      }
      else
      {
        if( m_table.getColumnCount() == 0 )
        {
          x += 5;
        }
        else
        {
          int alignmnent = m_column.getAlignment();
          switch( alignmnent )
          {
            case SWT.LEFT:
              x += 5;
              break;
            case SWT.RIGHT:
              x = bounds.width - extent.x - 2;
              break;
            case SWT.CENTER:
              x += (bounds.width - x - extent.x) / 2 + 2;
              break;
          }
        }
      }
      int textY = (size.y - extent.y) / 2;
      gc.drawString( text, x, textY );
    }
    if( isFocusControl() )
    {
      gc.setBackground( display.getSystemColor( SWT.COLOR_BLACK ) );
      gc.setForeground( display.getSystemColor( SWT.COLOR_WHITE ) );
      gc.drawFocus( 0, 0, size.x, size.y );
    }
  }

  void tableFocusIn( )
  {
    if( isDisposed() )
      return;
    if( isVisible() )
      setFocus();
  }

  void tableMouseDown( Event event )
  {
    if( isDisposed() || !isVisible() )
      return;
    Point pt = new Point( event.x, event.y );
    Rectangle clientRect = m_table.getClientArea();
    int columnCount = m_table.getColumnCount();
    int maxColumnIndex = columnCount == 0 ? 0 : columnCount - 1;
    int start = m_table.getTopIndex();
    int end = m_table.getItemCount();
    for( int i = start; i < end; i++ )
    {
      TableItem item = m_table.getItem( i );
      for( int j = 0; j <= maxColumnIndex; j++ )
      {
        Rectangle rect = item.getBounds( j );
        if( rect.y > clientRect.y + clientRect.height )
          return;
        if( rect.contains( pt ) )
        {
          setRowColumn( i, j, true );
          setFocus();
          return;
        }
      }
    }
  }

  void traverse( Event event )
  {
    switch( event.detail )
    {
      case SWT.TRAVERSE_ARROW_NEXT:
      case SWT.TRAVERSE_ARROW_PREVIOUS:
      case SWT.TRAVERSE_TAB_NEXT:
      case SWT.TRAVERSE_TAB_PREVIOUS:
      case SWT.TRAVERSE_RETURN:
        event.doit = false;
        return;
    }
    event.doit = true;
  }

  void setRowColumn( int row, int column, boolean notify )
  {
    TableItem item = row == -1 ? null : m_table.getItem( row );
    TableColumn col = column == -1 || m_table.getColumnCount() == 0 ? null : m_table.getColumn( column );
    setRowColumn( item, col, notify );
  }

  void setRowColumn( TableItem row, TableColumn column, boolean notify )
  {
    if( this.m_row == row && this.m_column == column )
    {
      return;
    }
    if( this.m_row != null && this.m_row != row )
    {
      this.m_row.removeListener( SWT.Dispose, m_disposeItemListener );
      this.m_row = null;
    }
    if( this.m_column != null && this.m_column != column )
    {
      this.m_column.removeListener( SWT.Dispose, m_disposeColumnListener );
      this.m_column.removeListener( SWT.Move, m_resizeListener );
      this.m_column.removeListener( SWT.Resize, m_resizeListener );
      this.m_column = null;
    }
    if( row != null )
    {
      if( this.m_row != row )
      {
        this.m_row = row;
        row.addListener( SWT.Dispose, m_disposeItemListener );
        m_table.showItem( row );
      }
      if( this.m_column != column && column != null )
      {
        this.m_column = column;
        column.addListener( SWT.Dispose, m_disposeColumnListener );
        column.addListener( SWT.Move, m_resizeListener );
        column.addListener( SWT.Resize, m_resizeListener );
        m_table.showColumn( column );
      }
      int columnIndex = column == null ? 0 : m_table.indexOf( column );
      setBounds( row.getBounds( columnIndex ) );
      redraw();
      if( notify )
      {
        notifyListeners( SWT.Selection, new Event() );
      }
    }
  }

  @Override
  public void setVisible( boolean visible )
  {
    checkWidget();
    if( visible )
      resize();
    super.setVisible( visible );
  }

  /**
   * Removes the listener from the collection of listeners who will be notified when the receiver's selection changes.
   * 
   * @param listener
   *          the listener which should no longer be notified
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *              </ul>
   * @see SelectionListener
   * @see #addSelectionListener(SelectionListener)
   * @since 3.0
   */
  public void removeSelectionListener( SelectionListener listener )
  {
    checkWidget();
    if( listener == null )
    {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    removeListener( SWT.Selection, listener );
    removeListener( SWT.DefaultSelection, listener );
  }

  void resize( )
  {
    if( m_row == null )
    {
      setBounds( -200, -200, 0, 0 );
    }
    else
    {
      int columnIndex = m_column == null ? 0 : m_table.indexOf( m_column );
      setBounds( m_row.getBounds( columnIndex ) );
    }
  }

  /**
   * Returns the column over which the TableCursor is positioned.
   * 
   * @return the column for the current position
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *              </ul>
   */
  public int getColumn( )
  {
    checkWidget();
    return m_column == null ? 0 : m_table.indexOf( m_column );
  }

  /**
   * Returns the row over which the TableCursor is positioned.
   * 
   * @return the item for the current position
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *              </ul>
   */
  public TableItem getRow( )
  {
    checkWidget();
    return m_row;
  }

  @Override
  public void setBackground( Color color )
  {
    if( color == null )
      color = getDisplay().getSystemColor( BACKGROUND );
    super.setBackground( color );
    redraw();
  }

  @Override
  public void setForeground( Color color )
  {
    if( color == null )
      color = getDisplay().getSystemColor( FOREGROUND );
    super.setForeground( color );
    redraw();
  }

  /**
   * Positions the TableCursor over the cell at the given row and column in the parent table.
   * <p>
   * Does not notify any listeners.
   * 
   * @param row
   *          the index of the row for the cell to select
   * @param column
   *          the index of column for the cell to select
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *              </ul>
   */
  public void setSelection( int row, int column )
  {
    setSelection( row, column, false );
  }

  /**
   * Positions the TableCursor over the cell at the given row and column in the parent table.
   * 
   * @param row
   *          the index of the row for the cell to select
   * @param column
   *          the index of column for the cell to select
   * @param notify
   *          notify the listeners
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *              </ul>
   */
  public void setSelection( int row, int column, boolean notify )
  {
    checkWidget();
    int columnCount = m_table.getColumnCount();
    int maxColumnIndex = columnCount == 0 ? 0 : columnCount - 1;
    if( row < 0 || row >= m_table.getItemCount() || column < 0 || column > maxColumnIndex )
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    setRowColumn( row, column, notify );
  }

  /**
   * Positions the TableCursor over the cell at the given row and column in the parent table.
   * 
   * @param row
   *          the TableItem of the row for the cell to select
   * @param column
   *          the index of column for the cell to select
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *              </ul>
   */
  public void setSelection( TableItem row, int column )
  {
    checkWidget();
    int columnCount = m_table.getColumnCount();
    int maxColumnIndex = columnCount == 0 ? 0 : columnCount - 1;
    if( row == null || row.isDisposed() || column < 0 || column > maxColumnIndex )
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    setRowColumn( m_table.indexOf( row ), column, false );
  }
}
