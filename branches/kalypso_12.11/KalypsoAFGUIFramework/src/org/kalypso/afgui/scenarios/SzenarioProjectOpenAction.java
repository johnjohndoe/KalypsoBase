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
package org.kalypso.afgui.scenarios;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.kalypso.afgui.internal.scenario.ScenarioSelectionPopup;
import org.kalypso.core.status.StatusDialog;
import org.kalypso.module.welcome.actions.AbstractModuleProjectOpenAction;

import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.cases.IScenarioManager;
import de.renew.workflow.connector.cases.ScenarioHandlingProjectNature;

/**
 * @author Gernot Belger
 * @author Dirk Kuch
 */
public class SzenarioProjectOpenAction extends AbstractModuleProjectOpenAction
{
  private static final String STR_TITLE = "Open Scenario";

  public SzenarioProjectOpenAction( final String moduleID )
  {
    super( moduleID );
  }

  @Override
  public IStatus open( final Shell shell, final Point mousePosition, final IProject project ) throws CoreException
  {
    /* access case manager */
    final ScenarioHandlingProjectNature nature = ScenarioHandlingProjectNature.toThisNature( project );
    final IScenarioManager caseManager = nature.getCaseManager();
    final IStatus status = caseManager.getStatus();
    if( !status.isOK() )
      throw new CoreException( status );

    final IScenario caze = chooseScenario( shell, mousePosition, project, caseManager );
    if( caze == null )
      return Status.CANCEL_STATUS;

    return activateScenario( shell, mousePosition, project, caze );
  }

  IStatus activateScenario( final Shell shell, final Point mousePosition, final IProject project, final IScenario caze ) throws CoreException
  {
    final IStatus openStatus = super.open( shell, mousePosition, project );
    if( openStatus.matches( IStatus.CANCEL ) )
      return openStatus;

    ScenarioHelper.activateScenario2( shell, caze );

    return openStatus;
  }

  private IScenario chooseScenario( final Shell shell, final Point mousePosition, final IProject project, final IScenarioManager caseManager )
  {
    /* find base scenario */
    final List<IScenario> cases = caseManager.getCases();
    final IScenario baseScenario = cases.get( 0 );

    /* if only the base scenario exists, we can directly open it */
    final List<IScenario> children = baseScenario.getDerivedScenarios().getScenarios();
    if( children.size() == 0 )
      return baseScenario;

    final ScenarioSelectionPopup selectionPopup = new ScenarioSelectionPopup( shell, project, STR_TITLE, mousePosition );
    selectionPopup.open();

    /* activate the first selected scenario */
    final ScenarioActivationListener activationListener = new ScenarioActivationListener( this, shell, mousePosition, project, selectionPopup );
    selectionPopup.setSelectionListener( activationListener );

    // REMARK: does not block, directly return without doing anything
    return null;
  }

  protected void handlePopupScenarioSelected( final Shell shell, final Point mousePosition, final IProject project, final IScenario scenario )
  {
    try
    {
      activateScenario( shell, mousePosition, project, scenario );
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      StatusDialog.open( shell, e.getStatus(), STR_TITLE );
    }
  }

  @Override
  protected IStatus doOpen( final Shell shell, final Point mousePosition, final IWorkbenchPage page, final IProject project )
  {
    return Status.OK_STATUS;
  }
}