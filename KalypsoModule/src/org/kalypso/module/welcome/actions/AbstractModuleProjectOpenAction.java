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
package org.kalypso.module.welcome.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.progress.IProgressService;
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

  @Override
  public IStatus open( final Shell shell, final Point mousePosition, final IProject project ) throws CoreException
  {
    final IWorkbenchPage page = findActivePage();
    if( page == null )
      return Status.CANCEL_STATUS;

    final IStatus versionStatus = checkProjectModule( project );
    if( !versionStatus.isOK() )
      return versionStatus;

    /* now hide welcome page */
    hideIntro();

    return doOpen( shell, mousePosition, page, project );
  }

  protected IWorkbenchPage findActivePage( )
  {
    final IWorkbench workbench = PlatformUI.getWorkbench();
    final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
    if( window == null )
      return null;

    return window.getActivePage();
  }

  protected abstract IStatus doOpen( final Shell shell, final Point mousePosition, final IWorkbenchPage page, final IProject project ) throws CoreException;

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

    final IProgressService progress = (IProgressService)PlatformUI.getWorkbench().getService( IProgressService.class );
    return RunnableContextHelper.execute( progress, true, false, operation );
  }

  private void hideIntro( )
  {
    /* hide intro */
    final IWorkbench workbench = PlatformUI.getWorkbench();
    final IIntroManager introManager = workbench.getIntroManager();
    introManager.closeIntro( introManager.getIntro() );
  }
}