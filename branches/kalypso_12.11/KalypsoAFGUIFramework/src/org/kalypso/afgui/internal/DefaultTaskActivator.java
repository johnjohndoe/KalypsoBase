/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.afgui.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.afgui.scenarios.ScenarioHelper;

import de.renew.workflow.base.ITask;
import de.renew.workflow.base.IWorkflow;
import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.cases.ScenarioHandlingProjectNature;
import de.renew.workflow.connector.context.IActiveScenarioChangeListener;

/**
 * Context listener that is responsible to activate the default task of every scenario that gets activated.
 * 
 * @author Gernot Belger
 */
public class DefaultTaskActivator implements IActiveScenarioChangeListener
{
  @Override
  public void activeScenarioChanged( final ScenarioHandlingProjectNature newProject, final IScenario scenario )
  {
    final UIJob job = new UIJob( "Activating default task" ) //$NON-NLS-1$
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        doActivateDefaultTask( newProject, scenario );

        return Status.OK_STATUS;
      }
    };

    job.setUser( false );
    job.setSystem( true );

    // REMARK: tricky: if we immediately execute, the progress dialog for activating the scenario may still be open
    // I this case, we get errors when activating the views (during task activation), because the workflow window
    // is not active right now.
    job.schedule( 250 );
  }

  protected void doActivateDefaultTask( final ScenarioHandlingProjectNature newProject, final IScenario scenario )
  {
    final IWorkflow workflow = ScenarioHelper.findWorkflow( scenario, newProject );

    final ITask defaultTask = WorkflowHelper.getDefaultTask( workflow );

    KalypsoAFGUIFrameworkPlugin.getTaskExecutor().execute( defaultTask );
  }
}