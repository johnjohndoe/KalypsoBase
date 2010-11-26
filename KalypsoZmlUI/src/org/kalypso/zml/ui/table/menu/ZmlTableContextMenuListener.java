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
package org.kalypso.zml.ui.table.menu;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.kalypso.contribs.eclipse.jface.action.ContributionUtils;
import org.kalypso.zml.ui.table.ZmlTableComposite;
import org.kalypso.zml.ui.table.binding.BaseColumn;
import org.kalypso.zml.ui.table.model.references.IZmlValueReference;

import com.google.common.base.Objects;

/**
 * @author Dirk Kuch
 */
public class ZmlTableContextMenuListener implements ISelectionChangedListener
{
  String m_lastUri;

  private final ZmlTableComposite m_table;

  public ZmlTableContextMenuListener( final ZmlTableComposite table )
  {
    m_table = table;
  }

  private void setMenu( final String uri )
  {
    if( Objects.equal( m_lastUri, uri ) )
      return;

    final Control control = m_table.getTableViewer().getControl();
    if( uri != null )
    {
      final MenuManager menuManager = new MenuManager();
      final Menu menu = menuManager.createContextMenu( control );
      ContributionUtils.populateContributionManager( PlatformUI.getWorkbench(), menuManager, uri );

      control.setMenu( menu );
    }
    else
      control.setMenu( new Menu( control ) );

    m_lastUri = uri;
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
   */
  @Override
  public void selectionChanged( final SelectionChangedEvent event )
  {
    final IZmlValueReference reference = m_table.getActiveCell();
    if( reference != null )
    {
      final BaseColumn column = reference.getColumn();
      final String uri = column.getUriContextMenu();

      setMenu( uri );
    }
    else
      setMenu( null );
  }
}
