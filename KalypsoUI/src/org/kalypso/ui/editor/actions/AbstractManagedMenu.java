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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.SubMenuManager;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author Gernot Belger
 */
public abstract class AbstractManagedMenu implements IManagedMenu
{
  private final String m_menuPath;

  private IMenuManager m_menuManager;

  private final String m_id;

  private final String m_menuLabel;

  private String m_groupName;

  private IWorkbenchPart m_part;

  public AbstractManagedMenu( final String id, final String menuPath, final String menuLabel )
  {
    m_menuPath = menuPath;
    m_id = id;
    m_menuLabel = menuLabel;
  }

  public final void setGroupName( final String groupName )
  {
    m_groupName = groupName;
  }

  /**
   * @see org.kalypso.ui.editor.actions.IManagedMenu#setPart(org.eclipse.ui.IWorkbenchPart)
   */
  @Override
  public final void setPart( final IWorkbenchPart part )
  {
    m_part = part;
  }

  protected final IWorkbenchPart getPart( )
  {
    return m_part;
  }

  /**
   * @see org.kalypso.ui.editor.actions.IManagedMenu#createSubMenu(org.eclipse.jface.action.IMenuManager)
   */
  @Override
  public final void createSubMenu( final IMenuManager menuManager )
  {
    if( m_menuPath == null )
      return;

    if( m_menuManager != null )
      return;

    final IMenuManager subMenuManager = StringUtils.isEmpty( m_menuPath ) ? menuManager : menuManager.findMenuUsingPath( m_menuPath );
    if( subMenuManager == null )
      return;

    m_menuManager = new MenuManager( m_menuLabel, m_id ); //$NON-NLS-1$ //$NON-NLS-2$

    if( m_groupName == null )
      subMenuManager.add( m_menuManager ); //$NON-NLS-1$
    else
      subMenuManager.appendToGroup( m_groupName, m_menuManager ); //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.ui.editor.actions.IManagedMenu#disposeMenu()
   */
  @Override
  public void disposeMenu( )
  {
    if( m_menuManager != null )
    {
      m_menuManager.dispose();

      if( m_menuManager instanceof SubMenuManager )
        ((SubMenuManager) m_menuManager).disposeManager();

      m_menuManager = null;
    }
  }

  /**
   * @see org.kalypso.ui.editor.actions.IManagedMenu#updateMenu()
   */
  @Override
  public final void updateMenu( )
  {
    if( m_menuManager == null )
      return;

    m_menuManager.removeAll();
    m_menuManager.setVisible( false );

    // TODO: probably nicer if we put this parallel to the selection menu
    fillMenu( m_menuManager );

    if( m_menuManager.getItems().length > 0 )
      m_menuManager.setVisible( true );
  }

  protected abstract void fillMenu( final IMenuManager menuManager );
}
