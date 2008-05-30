/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and
 
 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de
 
 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.simulation.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.kalypso.contribs.eclipse.core.runtime.HandleDoneJobChangeAdapter;
import org.kalypso.simulation.ui.calccase.ModelNature;

/**
 * @author huebsch
 */
public class SetBasicModelDelegate implements IWorkbenchWindowActionDelegate
{
  private IWorkbenchWindow m_window;

  /**
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  public void dispose()
  {
  // nichts zu tun
  }

  /**
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
   */
  public void init( final IWorkbenchWindow window )
  {
    m_window = window;
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run( final IAction action )
  {
    final ISelection selection = m_window.getSelectionService().getSelection( IPageLayout.ID_RES_NAV );

    IResource resource = null;
    if( selection instanceof IStructuredSelection )
    {
      final IStructuredSelection struct = (IStructuredSelection)selection;
      if( struct.size() == 1 )
        resource = (IResource)struct.getFirstElement();
    }

    if( resource == null || !( resource instanceof IFolder ) )
    {
      MessageDialog.openInformation( m_window.getShell(), "Rechenvariante in Basismodell übernehmen",
          "Bitte wählen Sie eine Rechenvariante im Navigator aus" );
      return;
    }

    final IFolder calcCase = (IFolder)resource;
    final IFile file = calcCase.getFile( ModelNature.CONTROL_NAME );
    if( !file.exists() )
    {
      MessageDialog.openInformation( m_window.getShell(), "Rechenvariante in Basismodell übernehmen",
          "Bitte wählen Sie eine Rechenvariante im Navigator aus" );
      return;
    }
    final Job job = new Job( "Aktualisiere Basismodell durch Rechenvariante: " + calcCase.getName() )
    {
      /**
       * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
       */
      @Override
      protected IStatus run( final IProgressMonitor monitor )
      {
        try
        {
          final ModelNature nature = (ModelNature) calcCase.getProject().getNature( ModelNature.ID );

          final IStatus status = nature.setBasicModel( calcCase, monitor );

          return status;
        }
        catch( final CoreException e )
        {
          e.printStackTrace();

          return e.getStatus();
        }
      }
    };
    // TODO see if autoRemoveListener (argument of HandleDoneJobChangeAdapter) should be true?
    job.addJobChangeListener( new HandleDoneJobChangeAdapter( m_window.getShell(), "Zeitreihen aktualisieren", "Siehe Details:", false ) );
    job.setUser( true );
    job.setRule( calcCase.getProject() );
    job.schedule();

//    final CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation( m_window.getShell() );
//    operation.copyResources( new IResource[]
//    { resource }, resource.getParent() );
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   *      org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged( final IAction action, final ISelection selection )
  {
  // mir egal
  }
}
