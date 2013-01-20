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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.afgui.helper.ScenarioHandlerUtils;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.afgui.wizards.SetAsBaseScenarioWizard;
import org.kalypso.contribs.eclipse.core.commands.HandlerUtils;
import org.kalypso.module.IKalypsoModule;
import org.kalypso.module.ModuleExtensions;
import org.kalypso.module.nature.ModuleNature;

import de.renew.workflow.base.ITask;
import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.worklist.ITaskExecutionAuthority;

/**
 * A handler which sets a scenario as base scenario.
 * 
 * @author Holger Albert
 */
public class SetAsBaseScenarioHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event )
  {
    /* Get the shell. */
    final Shell shell = HandlerUtil.getActiveShell( event );

    /* Find scenario. */
    final IScenario scenario = ScenarioHandlerUtils.findScenario( event );
    if( scenario == null )
      return showInformation( shell, HandlerUtils.getCommandName( event ), Messages.getString( "SetAsBaseScenarioHandler_0" ) ); //$NON-NLS-1$

    /* Stop current task. */
    final ITaskExecutionAuthority taskExecutionAuthority = KalypsoAFGUIFrameworkPlugin.getTaskExecutionAuthority();
    final ITask activeTask = KalypsoAFGUIFrameworkPlugin.getTaskExecutor().getActiveTask();
    if( !taskExecutionAuthority.canStopTask( activeTask ) )
    {
      /* Cancelled by user. */
      return null;
    }

    /* Get the module id and the category id. */
    final IProject project = scenario.getProject();
    final ModuleNature nature = ModuleNature.toThisNature( project );
    final String moduleID = nature.getModule();
    final IKalypsoModule module = ModuleExtensions.getKalypsoModule( moduleID );
    final String categoryId = module.getNewProjectCategoryId();

    /* Show wizard. */
    final SetAsBaseScenarioWizard wizard = new SetAsBaseScenarioWizard( categoryId, moduleID, scenario );
    wizard.init( PlatformUI.getWorkbench(), null );

    final WizardDialog wd = new WizardDialog( shell, wizard );
    wd.open();

    return null;
  }

  private Object showInformation( final Shell shell, final String windowTitle, final String message )
  {
    MessageDialog.openInformation( shell, windowTitle, message );
    return null;
  }
}