/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     											 - fix for bug 187817
 *******************************************************************************/

package org.kalypso.contribs.eclipse.jface.viewers;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;

/**
 * This class is responsible to determine if a cell selection event is triggers an editor activation. Implementors can
 * extend and overwrite to implement custom editing behavior
 * 
 * @since 3.3
 */
public class ColumnViewerEditorActivationStrategy extends org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy
{
  private boolean m_keyEnabled;

  /**
   * @param viewer
   *            the viewer the editor support is attached to
   */
  public ColumnViewerEditorActivationStrategy( final ColumnViewer viewer )
  {
    super( viewer );
  }

  @Override
  /**
   * Overwritten in order to remember if keyboard should be allowed.
   */
  public void setEnableEditorActivationWithKeyboard( final boolean enable )
  {
    m_keyEnabled = enable;
    super.setEnableEditorActivationWithKeyboard( enable );
  }

  /**
   * @param event
   *            the event triggering the action
   * @return <code>true</code> if this event should open the editor
   */
  @Override
  protected boolean isEditorActivationEvent( final ColumnViewerEditorActivationEvent event )
  {
    final boolean singleSelect = ((IStructuredSelection) getViewer().getSelection()).size() == 1;
    if( !singleSelect )
      return false;

    switch( event.eventType )
    {
      case ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION:
        final MouseEvent mouseEvent = (MouseEvent) event.sourceEvent;
        // Only left-click starts editing, else, context-menu is never shown
        return mouseEvent.button == 1 && mouseEvent.stateMask == 0;

      case ColumnViewerEditorActivationEvent.PROGRAMMATIC:
      case ColumnViewerEditorActivationEvent.TRAVERSAL:
        return true;

      case ColumnViewerEditorActivationEvent.KEY_PRESSED:
        return m_keyEnabled
            && (event.keyCode == SWT.F2 || (" -+,.;:öäüÖÄÜ´ß?`=!\"§$%&\\/()={}^°_#'<>|€µ".indexOf( event.character ) >= 0 || (event.character >= '0' && event.character <= 'z') || (event.character >= 'A' && event.character <= 'Z')));

      default:
        return false;
    }

  }
}
