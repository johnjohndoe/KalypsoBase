/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     @author changed / updated by: Dirk Kuch
 ******************************************************************************/

package org.kalypso.zml.ui.table.cursor;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.ui.table.IZmlTable;

/**
 * @since 3.3
 */
public abstract class AbstractCellCursor extends Canvas
{
  private final IZmlTable m_table;

  private ViewerCell m_focusCell;

  /**
   * @param viewer
   * @param style
   */
  public AbstractCellCursor( final IZmlTable table )
  {
    super( (Composite) table.getTableViewer().getControl(), SWT.NONE );
    m_table = table;

    final Listener listener = new Listener()
    {
      @Override
      public void handleEvent( final Event event )
      {
        if( SWT.Paint == event.type )
        {
          paint( event );
        }
        else if( SWT.KeyDown == event.type )
        {
          keyDown( event );
        }
        else if( SWT.MouseDown == event.type )
        {
          mouseDown( event );
        }
        else if( SWT.MouseDoubleClick == event.type )
        {
          getParent().notifyListeners( SWT.MouseDoubleClick, copyEvent( event ) );
        }
// else if( SWT.FocusIn == event.type )
// {
// if( isVisible() )
// forceFocus();
// }
      }

    };

    addListener( SWT.Paint, listener );
    addListener( SWT.KeyDown, listener );
    addListener( SWT.MouseDown, listener );
    addListener( SWT.MouseDoubleClick, listener );

    final TableViewer viewer = table.getTableViewer();
    final ScrollBar verticalBar = viewer.getTable().getVerticalBar();
    verticalBar.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        redraw();
      }
    } );

    // triggered from chart layer mouse selection
    viewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        redraw();
      }
    } );
  }

  protected IZmlTable getTable( )
  {
    return m_table;
  }

  protected void keyDown( final Event event )
  {
    getParent().notifyListeners( SWT.KeyDown, event );
  }

  protected void mouseDown( final Event event )
  {
    if( event.button == 3 )
      getParent().notifyListeners( SWT.MouseDown | SWT.MenuDetect, copyEvent( event ) );

    getParent().notifyListeners( SWT.MouseDown, copyEvent( event ) );
  }

  public void setFocusCell( final ViewerCell cell )
  {
    m_focusCell = cell;

    redraw();
  }

  /**
   * @see org.eclipse.swt.widgets.Control#redraw()
   */
  @Override
  public void redraw( )
  {
    if( isDisposed() )
      return;

    try
    {
      if( Objects.isNotNull( m_focusCell ) )
        setBounds( m_focusCell.getBounds() );
    }
    catch( final SWTException ex )
    {
      // ignore
    }

    super.redraw();
  }

  /**
   * @return the cells who should be highlighted
   */
  public ViewerCell getFocusCell( )
  {
    return m_focusCell;
  }

  protected Event copyEvent( final Event event )
  {
    final Event cEvent = new Event();
    cEvent.button = event.button;
    cEvent.character = event.character;
    cEvent.count = event.count;
    cEvent.data = event.data;
    cEvent.detail = event.detail;
    cEvent.display = event.display;
    cEvent.doit = event.doit;
    cEvent.end = event.end;
    cEvent.gc = event.gc;
    cEvent.height = event.height;
    cEvent.index = event.index;
    cEvent.item = getFocusCell().getControl();
    cEvent.keyCode = event.keyCode;
    cEvent.start = event.start;
    cEvent.stateMask = event.stateMask;
    cEvent.text = event.text;
    cEvent.time = event.time;
    cEvent.type = event.type;
    cEvent.widget = event.widget;
    cEvent.width = event.width;
    final Point p = m_table.getTableViewer().getControl().toControl( toDisplay( event.x, event.y ) );
    cEvent.x = p.x;
    cEvent.y = p.y;

    return cEvent;
  }

  /**
   * @param event
   */
  protected abstract void paint( Event event );
}
