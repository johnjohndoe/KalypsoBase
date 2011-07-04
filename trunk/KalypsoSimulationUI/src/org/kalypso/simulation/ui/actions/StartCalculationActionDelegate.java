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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.kalypso.contribs.eclipse.core.runtime.HandleDoneJobChangeAdapter;
import org.kalypso.simulation.ui.calccase.CalcCaseJob;
import org.kalypso.simulation.ui.i18n.Messages;

/**
 * @author Gernot Belger
 */
public class StartCalculationActionDelegate extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked( event );
    final Shell shell = HandlerUtil.getActiveShellChecked( event );

    final ISelection selection = window.getSelectionService().getSelection( IPageLayout.ID_RES_NAV );

    final IFolder[] calcCasesToCalc = CalcCaseHelper.chooseCalcCases( shell, selection, Messages.getString( "org.kalypso.simulation.ui.actions.StartCalculationActionDelegate.0" ), Messages.getString( "org.kalypso.simulation.ui.actions.StartCalculationActionDelegate.1" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    if( calcCasesToCalc == null )
      return null;

    final Queue<IFolder> folders = new LinkedList<IFolder>( Arrays.asList( calcCasesToCalc ) );
    startNextJob( shell, folders );

    return null;
  }

  /**
   * Starts the next calculation until the queue of folders is empty.<br/>
   * A bit dirty, but using scheduling rules does not work as the job set workspaces rules internally.<br/>
   * TODO: we should run all calculation in on e big operation inside of a dialog that shows the console output.
   */
  void startNextJob( final Shell shell, final Queue<IFolder> folders )
  {
    final IFolder folder = folders.poll();
    if( folder == null )
      return;

    final Job calcJob = new CalcCaseJob( folder );
    final String messageTitle = Messages.getString( "org.kalypso.simulation.ui.actions.StartCalculationActionDelegate.2", folder.getName() ); //$NON-NLS-1$
    final String messageFirstline = Messages.getString( "org.kalypso.simulation.ui.actions.StartCalculationActionDelegate.3" ); //$NON-NLS-1$
    calcJob.addJobChangeListener( new HandleDoneJobChangeAdapter( shell, messageTitle, messageFirstline, true, true ) );
    calcJob.addJobChangeListener( new JobChangeAdapter()
    {
      @Override
      public void done( final IJobChangeEvent event )
      {
        if( event.getResult().isOK() )
          startNextJob( shell, folders );
      }
    } );

    calcJob.schedule();
  }
}