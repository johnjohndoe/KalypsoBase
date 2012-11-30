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
package org.kalypso.afgui.internal.handlers;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.afgui.scenarios.ScenarioHelper;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.core.status.StatusDialog;

import de.renew.workflow.base.ITask;
import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.worklist.ITaskExecutionAuthority;

/**
 * @author Gernot Belger
 */
public class ScenarioWizard extends Wizard
{
  private final ScenarioData m_data;

  public static void stopTaskAndOpenWizard( final Shell shell, final ScenarioData data )
  {
    /* Stop current task */
    final ITaskExecutionAuthority taskExecutionAuthority = KalypsoAFGUIFrameworkPlugin.getTaskExecutionAuthority();
    final ITask activeTask = KalypsoAFGUIFrameworkPlugin.getTaskExecutor().getActiveTask();
    if( !taskExecutionAuthority.canStopTask( activeTask ) )
    {
      /* Cancelled by user */
      return;
    }

    /* Show wizard */
    final ScenarioWizard wizard = new ScenarioWizard( data );
    final WizardDialog wd = new WizardDialog( shell, wizard );
    wd.open();
  }

  public ScenarioWizard( final ScenarioData data )
  {
    m_data = data;

    final String title = Messages.getString( "org.kalypso.afgui.handlers.NewSimulationModelControlBuilder.13" ); //$NON-NLS-1$
    setWindowTitle( title );

    setNeedsProgressMonitor( true );

    addPage( new ScenarioWizardPage( data ) );
  }

  @Override
  public boolean performFinish( )
  {
    final IScenarioOperation operation = m_data.getOperation();
    final IStatus result = RunnableContextHelper.execute( getContainer(), true, false, operation );

    if( !result.isOK() )
      StatusDialog.open( getShell(), result, getWindowTitle() );

    if( result.matches( IStatus.ERROR ) )
      return false;

    if( m_data.getActivateScenario() )
    {
      final IScenario scenarioToActivate = operation.getScenarioForActivation();
      ScenarioHelper.activateScenario2( getShell(), scenarioToActivate );
    }

    return true;
  }
}