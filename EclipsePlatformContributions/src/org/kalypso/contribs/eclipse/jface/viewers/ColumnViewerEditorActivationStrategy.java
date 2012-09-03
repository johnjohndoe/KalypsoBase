/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */

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
   *          the viewer the editor support is attached to
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
   *          the event triggering the action
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
            && (event.keyCode == SWT.F2 || " -+,.;:öäüÖÄÜ´ß?`=!\"§$%&\\/()={}^°_#'<>|€µ".indexOf( event.character ) >= 0 || event.character >= '0' && event.character <= 'z' || event.character >= 'A' && event.character <= 'Z'); //$NON-NLS-1$

      default:
        return false;
    }

  }
}
