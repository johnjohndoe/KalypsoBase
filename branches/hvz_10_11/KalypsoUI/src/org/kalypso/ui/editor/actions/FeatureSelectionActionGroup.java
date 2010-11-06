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
package org.kalypso.ui.editor.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.SubToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.internal.ObjectActionContributorManager;

/**
 * This action group is used to show actions depending on a selected feature.
 * <p>
 * It is filled from the org.eclipse.ui.....popupMenu extension point.
 * 
 * @author Gernot Belger
 */
public class FeatureSelectionActionGroup extends ActionGroup
{
  private final ISelectionProvider m_provider = new ISelectionProvider()
  {
    @Override
    public void addSelectionChangedListener( final ISelectionChangedListener listener )
    {
    }

    @Override
    public ISelection getSelection( )
    {
      final ActionContext context = getContext();
      return context == null ? null : context.getSelection();
    }

    @Override
    public void removeSelectionChangedListener( final ISelectionChangedListener listener )
    {
    }

    @Override
    public void setSelection( final ISelection selection )
    {
    }
  };

  private final List<IManagedMenu> m_managedMenues = new ArrayList<IManagedMenu>();

  private SubToolBarManager m_toolbarSubManager;

  private IWorkbenchPart m_part = null;

  private IActionBars m_actionBars;

  public void addManagedMenu( final IManagedMenu menu )
  {
    m_managedMenues.add( menu );
  }

  public void setPart( final IWorkbenchPart part )
  {
    m_part = part;

    for( final IManagedMenu managedMenu : m_managedMenues )
      managedMenu.setPart( part );
  }

  /**
   * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
   */
  @Override
  public void fillActionBars( final IActionBars actionBars )
  {
    /* Just remember the action bars. The real business is done in update action bars */
    m_actionBars = actionBars;

    createSubMenues();
  }

  private void createSubMenues( )
  {
    final IMenuManager menuManager = m_actionBars.getMenuManager();
    if( menuManager == null )
      return;

    for( final IManagedMenu managedMenu : m_managedMenues )
      managedMenu.createSubMenu( menuManager );
  }

  /**
   * @see org.eclipse.ui.actions.ActionGroup#updateActionBars()
   */
  @Override
  public void updateActionBars( )
  {
    updateMenu();
    updateToolbar();
  }

  private void updateMenu( )
  {
    if( m_part == null )
      return;

    /* Maybe menu was not yet available when fillActionBars was called, retry now. */
    createSubMenues();

    for( final IManagedMenu managedMenu : m_managedMenues )
      managedMenu.updateMenu();
  }

  private void updateToolbar( )
  {
    if( m_toolbarSubManager == null )
      m_toolbarSubManager = createSubToolbarManager( m_actionBars.getToolBarManager() );

    if( m_toolbarSubManager == null )
      return;

    m_toolbarSubManager.removeAll();

    if( m_part == null )
      return;

    // TODO: also refaktor in external class

    /* first, fill the actions into a fake manager */
    final IMenuManager fakeManager = new MenuManager();
    ObjectActionContributorManager.getManager().contributeObjectActions( m_part, fakeManager, m_provider );

    translateIntoToolbar( fakeManager, m_toolbarSubManager );

    /* release the fake manager */
    fakeManager.removeAll();
    fakeManager.dispose();

    m_toolbarSubManager.update( true );
  }

  /** Translates the contributions from the manager into the toolbar. */
  private static void translateIntoToolbar( final IMenuManager menuManager, final IToolBarManager toolbarManager )
  {
    final IContributionItem[] items = menuManager.getItems();
    for( final IContributionItem item : items )
    {
      if( item instanceof ActionContributionItem )
      {
        final ActionContributionItem aci = (ActionContributionItem) item;

        final IAction action = aci.getAction();
        /* Only add items if they have an image, because else we get problems with editor toolbars. */
        if( action.getImageDescriptor() != null )
        {
          final ActionContributionItem newAci = new ActionContributionItem( action );
          newAci.setMode( aci.getMode() );
          newAci.setVisible( aci.isVisible() );
          toolbarManager.add( newAci );
        }
      }
    }
  }

  /**
   * @see org.eclipse.ui.actions.ActionGroup#dispose()
   */
  @Override
  public void dispose( )
  {
    m_managedMenues.clear();

    if( m_toolbarSubManager != null )
    {
      m_toolbarSubManager.disposeManager();
      m_toolbarSubManager = null;
    }
  }

  /**
   * Creates the sub-toolbar where the selection-dependent contribution items are added.
   * <p>
   * Default implementation just creates a sub-toolbar and makes it visible.
   * </p>
   * <p>
   * Intended to be overwritten by clients.
   * </p>
   */
  protected SubToolBarManager createSubToolbarManager( final IToolBarManager toolBarManager )
  {
    final SubToolBarManager subToolBarManager = new SubToolBarManager( toolBarManager );
    subToolBarManager.setVisible( true );
    return subToolBarManager;
  }
}
