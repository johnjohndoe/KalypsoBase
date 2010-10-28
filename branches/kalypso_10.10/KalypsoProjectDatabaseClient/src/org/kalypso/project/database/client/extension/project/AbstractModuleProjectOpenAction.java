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
package org.kalypso.project.database.client.extension.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.views.navigator.ResourceNavigator;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.module.IKalypsoModuleProjectOpenAction;
import org.kalypso.module.nature.ModuleNature;

/**
 * Holds common behavior for all open actions.
 * 
 * @author Gernot Belger
 */
public abstract class AbstractModuleProjectOpenAction implements IKalypsoModuleProjectOpenAction
{
  private final String m_moduleID;

  public AbstractModuleProjectOpenAction( final String moduleID )
  {
    m_moduleID = moduleID;
  }

  /**
   * @see org.kalypso.project.database.client.extension.project.IKalypsoModuleProjectOpenAction#open(org.eclipse.core.resources.IProject)
   */
  @Override
  public final IStatus open( final IProject project ) throws CoreException
  {
    final IWorkbench workbench = PlatformUI.getWorkbench();
    final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
    if( window == null )
      return Status.CANCEL_STATUS;

    final IWorkbenchPage page = window.getActivePage();
    if( page == null )
      return Status.CANCEL_STATUS;

    // TODO: we should also have some kind of close action: close the currently open project (for example, Scenario
    // based projects should unload the currently active scenario ). We could equally close a project before it is
    // deleted.

    final IStatus versionStatus = checkProjectModule( project );
    if( !versionStatus.isOK() )
      return versionStatus;

    /* We need to do this first, the open actions sometimes depend on it */
    final String perspective = getFinalPerspective();
    hideIntroAndOpenPerspective( page, perspective );

    if( revealProjectInExplorer() )
      revealProjectInExplorer( page, project );

    return doOpen( page, project );
  }

  /**
   * Returns the id of the perspective that should be opened when the project is opened. <br/>
   * Return <code>null</code>, if the perspective should not be changed.
   */
  protected abstract String getFinalPerspective( );

  /**
   * Return <code>true</code>, if the opened project should be revealed in the project explorer resp. navigator.
   */
  protected abstract boolean revealProjectInExplorer( );

  protected abstract IStatus doOpen( IWorkbenchPage page, IProject project ) throws CoreException;

  /**
   * For backwards compatibility: we enforce the ModuleNature here, setting the module-id if it was never set before.
   */
  private IStatus checkProjectModule( final IProject project )
  {
    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    final String moduleID = m_moduleID;
    final WorkspaceModifyOperation operation = new WorkspaceModifyOperation( root )
    {
      @Override
      protected void execute( final IProgressMonitor monitor ) throws CoreException
      {
        ModuleNature.enforceNature( project, moduleID );
      }
    };

    final IProgressService progress = (IProgressService) PlatformUI.getWorkbench().getService( IProgressService.class );
    return RunnableContextHelper.execute( progress, true, false, operation );
  }

  private void revealProjectInExplorer( final IWorkbenchPage page, final IProject project ) throws PartInitException
  {
    // At least show project in Resource Navigator
    final StructuredSelection projectSelection = new StructuredSelection( project );

    final CommonNavigator projectExplorer = (CommonNavigator) page.findView( IPageLayout.ID_PROJECT_EXPLORER );
    if( projectExplorer != null )
    {
      page.showView( IPageLayout.ID_PROJECT_EXPLORER, null, IWorkbenchPage.VIEW_ACTIVATE );
      final CommonViewer commonViewer = projectExplorer.getCommonViewer();
      commonViewer.collapseAll();
      commonViewer.setSelection( projectSelection );
      commonViewer.expandToLevel( project, 1 );
    }
    else
    {
      final ResourceNavigator view = (ResourceNavigator) page.showView( IPageLayout.ID_RES_NAV );
      if( view != null )
      {
        final TreeViewer treeViewer = view.getTreeViewer();
        treeViewer.collapseAll();
        view.selectReveal( projectSelection );
        treeViewer.expandToLevel( project, 1 );
      }
    }
  }

  private void hideIntroAndOpenPerspective( final IWorkbenchPage page, final String perspective )
  {
    /* hide intro */
    final IWorkbench workbench = PlatformUI.getWorkbench();
    final IIntroManager introManager = workbench.getIntroManager();
    introManager.closeIntro( introManager.getIntro() );

    if( perspective == null )
      return;

    /* Open desired perspective */
    if( page == null )
      return;

    final IPerspectiveRegistry perspectiveRegistry = workbench.getPerspectiveRegistry();

    final IPerspectiveDescriptor descriptor = perspectiveRegistry.findPerspectiveWithId( perspective );
    if( descriptor != null )
      page.setPerspective( descriptor );
  }

}
