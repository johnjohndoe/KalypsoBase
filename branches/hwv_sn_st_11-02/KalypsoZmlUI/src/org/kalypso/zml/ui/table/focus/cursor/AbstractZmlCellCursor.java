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

package org.kalypso.zml.ui.table.focus.cursor;

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
import org.kalypso.contribs.eclipse.swt.widgets.ControlUtils;
import org.kalypso.zml.ui.table.focus.ZmlTableFocusCellManager;
import org.kalypso.zml.ui.table.model.IZmlTableCell;

/**
 * @since 3.3
 */
public abstract class AbstractZmlCellCursor extends Canvas
{

  private ZmlTableFocusCellManager m_cellManager;

  private final TableViewer m_viewer;

  /**
   * @param viewer
   * @param style
   */
  public AbstractZmlCellCursor( final TableViewer viewer )
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
          m_viewer.getControl().forceFocus();
        }
      }
    };

    addListener( SWT.Paint, listener );
    addListener( SWT.KeyDown, listener );
    addListener( SWT.MouseDown, listener );
    addListener( SWT.MouseDoubleClick, listener );
    addListener( SWT.FocusIn, listener );

    final ScrollBar verticalBar = m_viewer.getTable().getVerticalBar();
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
// throw new NotImplementedException();

// final ZmlSelectionUpdater updater = new ZmlSelectionUpdater( getTable() );
// updater.run();
//
// new UIJob( "" )
// {
//
// @Override
// public IStatus runInUIThread( final IProgressMonitor monitor )
// {
// redraw();
// return Status.OK_STATUS;
// }
//
// }.schedule();

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
      final ViewerCell focusCell = getFocusCell();
      if( Objects.isNotNull( focusCell ) && !focusCell.getControl().isDisposed() )
        setBounds( focusCell.getBounds() );
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
  protected IZmlTableCell getFocusTableCell( )
  {
    if( m_cellManager == null )
      return null;

    return m_cellManager.getFocusTableCell();
  }

  protected ViewerCell getFocusCell( )
  {
    if( Objects.isNull( m_cellManager ) )
      return null;

    return m_cellManager.getFocusCell();
  }

  protected Event copyEvent( final Event event )
  {
    final Event cEvent = ControlUtils.copyEvent( event );

    final ViewerCell focusCell = getFocusCell();
    cEvent.item = focusCell == null ? null : focusCell.getControl();
    final Point p = m_viewer.getControl().toControl( toDisplay( event.x, event.y ) );
    cEvent.x = p.x;
    cEvent.y = p.y;

    return cEvent;
  }

  public void setCellManager( final ZmlTableFocusCellManager cellManager )
  {
    m_cellManager = cellManager;
  }

  /**
   * @param event
   */
  protected abstract void paint( Event event );
}
