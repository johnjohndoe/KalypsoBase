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

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.progress.IProgressService;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.afgui.perspective.Perspective;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.core.status.StatusDialog;

import de.renew.workflow.base.IWorkflow;
import de.renew.workflow.connector.WorkflowProjectNature;
import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.cases.IScenarioManager;
import de.renew.workflow.connector.cases.ScenarioHandlingProjectNature;
import de.renew.workflow.connector.context.ActiveWorkContext;
import de.renew.workflow.connector.worklist.ITaskExecutor;

/**
 * @author Stefan Kurzbach
 */
public class ScenarioHelper
{
  /**
   * Find the root of the given scenario.<br>
   * If the scenario has no parent, itself is returned.
   */
  public static IScenario findRootScenario( final IScenario scenario )
  {
    final IScenario parentScenario = scenario.getParentScenario();
    if( parentScenario == null )
      return scenario;
    else
      return findRootScenario( parentScenario );
  }

  public static IWorkflow findWorkflow( final IScenario scenario, final ScenarioHandlingProjectNature newProject )
  {
    try
    {
      if( scenario == null || newProject == null )
      {
        return null;
      }

      final WorkflowProjectNature workflowNature = WorkflowProjectNature.toThisNature( newProject.getProject() );
      if( workflowNature == null )
      {
        return null;
      }

      return workflowNature.getCurrentWorklist();
    }
    catch( final CoreException e )
    {
      KalypsoAFGUIFrameworkPlugin.getDefault().getLog().log( e.getStatus() );
    }

    return null;
  }

  /**
   * This function activates a given scenario.<br/>
   * Must be invoked in the swt thread.
   * 
   * @param scenario
   *          The scenario to activate.
   */
  public static void activateScenario2( final Shell shell, final IScenario scenario )
  {
    /* Always make sure the workflow perspective is visible, even if the scenario does not change */
    final IWorkbench workbench = PlatformUI.getWorkbench();
    if( scenario != null && !workbench.isClosing() )
    {
      try
      {
        final IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
        workbench.showPerspective( Perspective.ID, activeWindow );
      }
      catch( final WorkbenchException e )
      {
        final IStatus status = new Status( IStatus.ERROR, KalypsoAFGUIFrameworkPlugin.PLUGIN_ID, "Failed to activate workflow perspective", e ); //$NON-NLS-1$
        KalypsoAFGUIFrameworkPlugin.getDefault().getLog().log( status );
      }
    }

    final ActiveWorkContext context = KalypsoAFGUIFrameworkPlugin.getActiveWorkContext();

    final IScenario currentCase = context.getCurrentCase();
    if( ObjectUtils.equals( currentCase, scenario ) )
      return;

    // TODO: maybe move this outside to make this more reusable
    final ITaskExecutor executor = KalypsoAFGUIFrameworkPlugin.getTaskExecutor();
    if( !executor.stopActiveTask() )
      return;

    /* load and activate scenario */
    final ICoreRunnableWithProgress operation = new ScenarioActivationOperation( scenario );

    final IProgressService progressService = workbench.getProgressService();
    final IStatus status = RunnableContextHelper.execute( progressService, true, false, operation );
    if( !status.isOK() )
      StatusDialog.open( shell, status, Messages.getString( "org.kalypso.afgui.handlers.ActivateScenarioHandler.0" ) ); //$NON-NLS-1$
  }

  /**
   * This function activates a given scenario.
   * 
   * @param scenario
   *          The scenario.
   * @deprecated Use {@link #activateScenario2(IScenario)} instead.
   */
  @Deprecated
  public static void activateScenario( final IScenario scenario ) throws CoreException
  {
    final ActiveWorkContext context = KalypsoAFGUIFrameworkPlugin.getActiveWorkContext();

    final IScenario currentCase = context.getCurrentCase();
    if( currentCase == scenario )
      return;

    // TODO: move outside, task must be stopped by clients
    final ITaskExecutor executor = KalypsoAFGUIFrameworkPlugin.getTaskExecutor();
    if( executor.stopActiveTask() )
    {
      /* Activate the scenario. */
      context.setCurrentCase( scenario, new NullProgressMonitor() );
    }
  }

  /**
   * This function returns the active scenario.
   * 
   * @return The active scenario.
   */
  public static IScenario getActiveScenario( )
  {
    final ActiveWorkContext activeWorkContext = KalypsoAFGUIFrameworkPlugin.getActiveWorkContext();

    return activeWorkContext.getCurrentCase();
  }

  /**
   * TODO: probably that does not belong here, move to ScenarioHelper instead? This method will try to find a model file
   * in this scenario and its parent scenarios. Needs refactoring!
   */
  public static IContainer findModelContext( final IContainer szenarioFolder, final String modelFile )
  {
    if( szenarioFolder == null )
      return null;

    final Path path = new Path( modelFile );
    if( szenarioFolder.getFile( path ).exists() )
      return szenarioFolder;

    final IContainer parent = szenarioFolder.getParent();
    if( parent.getType() != IResource.PROJECT )
      return findModelContext( parent, modelFile );

    return null;
  }

  /**
   * Returns the current scenario's base folder
   */
  public static IFolder getScenarioFolder( )
  {
    final IScenario scenario = getActiveScenario();
    if( scenario != null )
      return scenario.getFolder();

    return null;
  }

  public static boolean isSubScenario( final IScenario parentScenario, final IScenario subScenario )
  {
    final IFolder parentFolder = parentScenario.getFolder();
    final IFolder subFolder = subScenario.getFolder();

    final IPath parentPath = parentFolder.getFullPath();
    final IPath subPath = subFolder.getFullPath();

    return parentPath.isPrefixOf( subPath );
  }

  /**
   * search the scenario that has the given container as scenario folder.
   */
  public static IScenario findScenario( final IContainer scenarioFolder ) throws CoreException
  {
    final IProject project = scenarioFolder.getProject();

    final ScenarioHandlingProjectNature nature = ScenarioHandlingProjectNature.toThisNature( project );
    if( nature == null )
      return null;

    final IScenarioManager caseManager = nature.getCaseManager();
    if( caseManager == null )
      return null;

    final IPath scenarioFullPath = scenarioFolder.getFullPath();
    final String scenarioPath = scenarioFullPath.makeRelative().toPortableString();

    /* try old style uri's */
    final String oldUri = String.format( "%s%s", IScenario.OLD_CASE_BASE_URI, scenarioPath );
    final IScenario oldStyleScenario = caseManager.getCase( oldUri );
    if( oldStyleScenario != null )
      return oldStyleScenario;

    /* try new style uri's */
    final IPath scenarioRelativePath = scenarioFullPath.makeRelativeTo( scenarioFolder.getProject().getFullPath() );
    final String scenarioPathWithoutProject = scenarioRelativePath.toPortableString();
    final String newUri = String.format( "%s%s", IScenario.NEW_CASE_BASE_URI, scenarioPathWithoutProject );
    final IScenario newStyleScenario = caseManager.getCase( newUri );
    if( newStyleScenario != null )
      return newStyleScenario;

    return null;
  }

  /**
   * Returns the base scenario for the given project.<br/>
   * Returns <code>null</code>, if an exception occurs.
   */
  public static IScenario getBaseScenarioQuiet( final IProject project )
  {
    try
    {
      final ScenarioHandlingProjectNature nature = ScenarioHandlingProjectNature.toThisNature( project );
      final IScenarioManager caseManager = nature.getCaseManager();

      final List<IScenario> cases = caseManager.getCases();
      return cases.get( 0 );
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      return null;
    }
  }
}