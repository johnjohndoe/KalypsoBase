/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.ui.editor.styleeditor.graphic;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.kalypso.commons.eclipse.jface.viewers.ITabItem;
import org.kalypso.commons.eclipse.jface.viewers.TabViewer;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.editor.styleeditor.MessageBundle;

/**
 * @author Gernot Belger
 */
public class AddMarkItemAction extends Action
{
  private final TabViewer m_tabViewer;

  public AddMarkItemAction( final TabViewer tabViewer )
  {
    m_tabViewer = tabViewer;

    setImageDescriptor( ImageProvider.IMAGE_STYLEEDITOR_ADD_RULE );
    setToolTipText( MessageBundle.STYLE_EDITOR_ADD_MARK );
  }
  /**
   * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
   */
  @Override
  public void runWithEvent( final Event event )
  {
    final GraphicElementsTabList list = (GraphicElementsTabList) m_tabViewer.getInput();

    final ITabItem item = list.addNewMark();
    if( item != null )
      m_tabViewer.setSelection( new StructuredSelection( item ) );
  }
}
