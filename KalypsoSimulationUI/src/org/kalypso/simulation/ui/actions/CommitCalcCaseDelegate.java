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

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.kalypso.contribs.eclipse.core.runtime.HandleDoneJobChangeAdapter;
import org.kalypso.simulation.ui.calccase.ModelSynchronizer;
import org.kalypso.ui.KalypsoGisPlugin;

/**
 * @author belger
 */
public class CommitCalcCaseDelegate implements IWorkbenchWindowActionDelegate
{
  public static final String RECHENVARIANTEN_K�NNEN_NICHT_ARCHIVIERT_WERDEN_ = "Rechenvarianten k�nnen nicht archiviert werden.";

  private static final String RECHENVARIANTEN_ARCHIVIEREN = "Rechenvarianten archivieren";

  public static final class CommitCalcCaseJob extends Job
  {
    private final IFolder[] m_calcCases;

    private final IProject m_project;

    /**
     * A job to commit the lokal calc cases to the server. Add a {@link HandleDoneJobChangeAdapter}, because it it
     * probable, that we get a warning-status
     */
    public CommitCalcCaseJob( final IProject project, final IFolder[] calcCases )
    {
      super( RECHENVARIANTEN_ARCHIVIEREN );

      m_project = project;
      m_calcCases = calcCases;
    }

    @Override
    protected IStatus run( final IProgressMonitor monitor )
    {
      monitor.beginTask( RECHENVARIANTEN_ARCHIVIEREN, m_calcCases.length * 1000 );

      final ModelSynchronizer synchronizer;
      try
      {
        final File serverRoot = ModelActionHelper.getServerRoot();
        final File serverProject = ModelActionHelper.checkIsServerMirrored( serverRoot, m_project );
        synchronizer = new ModelSynchronizer( m_project, serverProject );
      }
      catch( final CoreException ce )
      {
        return ce.getStatus();
      }

      final Collection<IStatus> errorStati = new LinkedList<IStatus>();
      for( int i = 0; i < m_calcCases.length; i++ )
      {
        final IFolder folder = m_calcCases[i];

        try
        {
          synchronizer.commitFolder( folder, new SubProgressMonitor( monitor, 1000 ) );
        }
        catch( final CoreException e )
        {
          errorStati.add( e.getStatus() );
          e.printStackTrace();
        }
      }

      if( errorStati.isEmpty() )
        return Status.OK_STATUS;

      final IStatus[] stati = errorStati.toArray( new IStatus[errorStati.size()] );
      return new MultiStatus( KalypsoGisPlugin.getId(), 0, stati,
          "Nicht alle Rechenvarianten konnten archiviert werden.", null );
    }
  }

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
    try
    {
      final IProject project = ModelActionHelper.chooseOneProject( m_window );
      final ISelection selection = m_window.getSelectionService().getSelection( IPageLayout.ID_RES_NAV );
      final IFolder[] calcCases = CalcCaseHelper.chooseCalcCases( m_window.getShell(), selection,
          RECHENVARIANTEN_ARCHIVIEREN, "Folgende Rechenvarianten werden archiviert:" );
      if( calcCases == null )
        return;

      final Job job = new CommitCalcCaseJob( project, calcCases );

      //    TODO see if autoRemoveListener (argument of HandleDoneJobChangeAdapter) should be true?
      job.addJobChangeListener( new HandleDoneJobChangeAdapter( m_window.getShell(), RECHENVARIANTEN_ARCHIVIEREN,
          RECHENVARIANTEN_K�NNEN_NICHT_ARCHIVIERT_WERDEN_, false ) );

      job.setUser( true );
      job.schedule();
    }
    catch( final CoreException ce )
    {
      ErrorDialog.openError( m_window.getShell(), RECHENVARIANTEN_ARCHIVIEREN,
          RECHENVARIANTEN_K�NNEN_NICHT_ARCHIVIERT_WERDEN_, ce.getStatus() );
    }
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   *      org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged( final IAction action, final ISelection selection )
  {
  // egal, aktuelle Selektion wird in run ermittelt
  }

}