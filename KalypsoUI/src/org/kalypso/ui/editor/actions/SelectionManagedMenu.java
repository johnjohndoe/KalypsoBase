/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.ui.editor.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.ObjectActionContributorManager;
import org.kalypso.i18n.Messages;

/**
 * Fills the managed menu with all commands that apply to the current selection.
 * 
 * @author Gernot Belger
 */
public class SelectionManagedMenu extends AbstractManagedMenu
{
  public SelectionManagedMenu( final String menuPath )
  {
    super( "selectionMenuManager", menuPath, Messages.getString( "org.kalypso.ui.editor.gmleditor.part.GmlEditorActionBarContributor.2" ) );//$NON-NLS-1$
  }

  /**
   * @see org.kalypso.ui.editor.actions.AbstractManagedMenu#fillMenu(org.eclipse.jface.action.IMenuManager)
   */
  @Override
  protected void fillMenu( final IMenuManager menuManager )
  {
    final IWorkbenchPart part = getPart();
    if( part == null )
      return;

    final ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();

    ObjectActionContributorManager.getManager().contributeObjectActions( part, menuManager, selectionProvider );
  }

}
