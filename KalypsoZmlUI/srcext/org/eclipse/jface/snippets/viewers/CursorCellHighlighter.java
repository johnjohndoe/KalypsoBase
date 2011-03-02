/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *     @author changed / updated by: Dirk Kuch
 ******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationListener;
import org.eclipse.jface.viewers.ColumnViewerEditorDeactivationEvent;
import org.eclipse.jface.viewers.FocusCellHighlighter;
import org.eclipse.jface.viewers.ViewerCell;

/**
 * @since 3.3
 */
public class CursorCellHighlighter extends FocusCellHighlighter
{
  private final ColumnViewer m_viewer;

  protected final AbstractCellCursor m_cursor;

  /**
   * @param viewer
   * @param cursor
   */
  public CursorCellHighlighter( final ColumnViewer viewer, final AbstractCellCursor cursor )
  {
    super( viewer );

    m_viewer = viewer;
    m_cursor = cursor;
  }

  @Override
  protected void focusCellChanged( final ViewerCell cell )
  {
    super.focusCellChanged( cell );
    if( !m_viewer.isCellEditorActive() )
    {
      m_cursor.setSelection( cell );
      m_cursor.setVisible( true );
    }
  }

  @Override
  protected void init( )
  {
    hookListener();
  }

  private void hookListener( )
  {
    final ColumnViewerEditorActivationListener listener = new ColumnViewerEditorActivationListener()
    {

      @Override
      public void afterEditorActivated( final ColumnViewerEditorActivationEvent event )
      {

      }

      @Override
      public void afterEditorDeactivated( final ColumnViewerEditorDeactivationEvent event )
      {
        m_cursor.setVisible( true );
        m_cursor.setSelection( getFocusCell() );
      }

      @Override
      public void beforeEditorActivated( final ColumnViewerEditorActivationEvent event )
      {
        m_cursor.setVisible( false );
      }

      @Override
      public void beforeEditorDeactivated( final ColumnViewerEditorDeactivationEvent event )
      {

      }
    };

    m_viewer.getColumnViewerEditor().addEditorActivationListener( listener );
  }
}
