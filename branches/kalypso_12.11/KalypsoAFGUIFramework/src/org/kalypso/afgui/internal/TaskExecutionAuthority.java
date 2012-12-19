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
package org.kalypso.afgui.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.core.status.StatusDialog;

import de.renew.workflow.base.ITask;
import de.renew.workflow.connector.cases.IScenarioDataProvider;
import de.renew.workflow.connector.worklist.ITaskExecutionAuthority;

/**
 * @author Stefan Kurzbach
 */
public class TaskExecutionAuthority implements ITaskExecutionAuthority
{
  @Override
  public boolean canStopTask( final ITask task )
  {
    final IScenarioDataProvider dataProvider = KalypsoAFGUIFrameworkPlugin.getDataProvider();

    // check if any model is dirty
    if( !dataProvider.isDirty() )
      return true;

    final Shell activeShell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
    final String[] dialogButtonLabels = new String[] { Messages.getString( "org.kalypso.afgui.scenarios.TaskExecutionAuthority.2" ), //$NON-NLS-1$
        Messages.getString( "org.kalypso.afgui.scenarios.TaskExecutionAuthority.3" ), //$NON-NLS-1$
        Messages.getString( "org.kalypso.afgui.scenarios.TaskExecutionAuthority.4" ) //$NON-NLS-1$
    };

    final String dialogTitle = Messages.getString( "org.kalypso.afgui.scenarios.TaskExecutionAuthority.0" ); //$NON-NLS-1$
    final String dialogMessage = Messages.getString( "org.kalypso.afgui.scenarios.TaskExecutionAuthority.1" ); //$NON-NLS-1$

    final MessageDialog confirmDialog = new MessageDialog( null, dialogTitle, null, dialogMessage, MessageDialog.QUESTION, dialogButtonLabels, 2 );

    final int decision = confirmDialog.open();
    if( decision != 0 && decision != 1 )
      return false;

    final ICoreRunnableWithProgress op = new ICoreRunnableWithProgress()
    {
      @Override
      public IStatus execute( final IProgressMonitor monitor ) throws CoreException
      {
        switch( decision )
        {
          case 0:
            dataProvider.saveModel( monitor );
            break;

          case 1:
            if( PlatformUI.getWorkbench().isClosing() )
            {
              /* clear dirty state, so kalypso won't ask for saving the gml files and just releases them */
              dataProvider.resetDirty();
            }
            else
            {
              // reload model and thus dischard changes
              dataProvider.reloadModel();
            }
            break;
        }
        return Status.OK_STATUS;
      }
    };

    final IStatus status = ProgressUtilities.busyCursorWhile( op );

    if( !status.isOK() )
    {
      StatusDialog.open( activeShell, status, dialogTitle );

      KalypsoAFGUIFrameworkPlugin.getDefault().getLog().log( status );
    }

    return true;
  }
}
