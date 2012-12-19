/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.kalypso.afgui.handlers.AddScenarioHandler;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.afgui.scenarios.ScenarioHelper;
import org.kalypso.contribs.eclipse.core.commands.HandlerUtils;

import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.cases.IScenarioList;

/**
 * A handler which copies a scenario.
 * 
 * @author Holger Albert
 */
public class RenameScenarioHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event )
  {
    final Shell shell = HandlerUtil.getActiveShell( event );
    final String commandName = HandlerUtils.getCommandName( event );

    /* Find scenario */
    final IScenario scenario = AddScenarioHandler.findScenario( event );
    if( scenario == null )
    {
      final String message = Messages.getString("RenameScenarioHandler_0"); //$NON-NLS-1$
      return showInformation( shell, commandName, message );
    }

    /* Do not allow active scenario to be renamed */
    final IScenario currentCase = ScenarioHelper.getActiveScenario();
    if( currentCase == scenario )
    {
      final String message = Messages.getString("RenameScenarioHandler_1"); //$NON-NLS-1$
      return showInformation( shell, commandName, message );
    }

    final IScenario parentScenario = scenario.getParentScenario();
    if( parentScenario == null )
    {
      final String message = Messages.getString("RenameScenarioHandler_2"); //$NON-NLS-1$
      showInformation( shell, commandName, message );
      return null;
    }

    if( currentCase != null && ScenarioHelper.isSubScenario( scenario, currentCase ) )
    {
      final String message = Messages.getString("RenameScenarioHandler_3"); //$NON-NLS-1$
      return showInformation( shell, commandName, message );
    }

    final IScenarioList derivedScenarios = scenario.getDerivedScenarios();
    if( derivedScenarios != null && derivedScenarios.getScenarios().size() > 0 )
    {
      final String message = Messages.getString("RenameScenarioHandler_4"); //$NON-NLS-1$
      return showInformation( shell, commandName, message );
    }

    /* Initialize data */
    final IScenarioOperation operation = new RenameScenarioOperation();
    final ScenarioData data = new ScenarioData( parentScenario, scenario, operation, null );
    data.setDerivedVisible( false );
    data.setName( scenario.getName() );
    data.setComment( scenario.getDescription() );

    ScenarioWizard.stopTaskAndOpenWizard( shell, data );

    return null;
  }

  private Object showInformation( final Shell shell, final String windowTitle, final String message )
  {
    MessageDialog.openInformation( shell, windowTitle, message );
    return null;
  }
}