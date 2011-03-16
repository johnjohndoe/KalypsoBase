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

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.kalypso.commons.java.lang.Objects;

/**
 * @since 3.3
 */
public abstract class AbstractCellCursor extends Canvas
{
  private final ColumnViewer m_viewer;

  private ViewerCell m_focusCell;

  /**
   * @param viewer
   * @param style
   */
  public AbstractCellCursor( final TableViewer viewer )
  {
    super( (Composite) viewer.getControl(), SWT.NONE );
    m_viewer = viewer;

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
        else if( SWT.FocusIn == event.type )
        {
          if( isVisible() )
            forceFocus();
        }
      }

    };

    addListener( SWT.Paint, listener );
    addListener( SWT.KeyDown, listener );
    addListener( SWT.MouseDown, listener );
    addListener( SWT.MouseDoubleClick, listener );

    final Table table = viewer.getTable();
    final ScrollBar verticalBar = table.getVerticalBar();
    verticalBar.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        setFocusCell( getFocusCell() );
      }
    } );

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

    if( Objects.isNotNull( m_focusCell ) )
      setBounds( m_focusCell.getBounds() );

    redraw();
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
    final Point p = m_viewer.getControl().toControl( toDisplay( event.x, event.y ) );
    cEvent.x = p.x;
    cEvent.y = p.y;

    return cEvent;
  }

  /**
   * @param event
   */
  protected abstract void paint( Event event );
}
