/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import java.io.File;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.commons.java.util.zip.ZipUtilities;
import org.kalypso.contribs.eclipse.EclipsePlatformContributionsExtensions;
import org.kalypso.contribs.eclipse.core.resources.ProjectTemplate;

import de.renew.workflow.base.IWorkflow;
import de.renew.workflow.connector.WorkflowProjectNature;
import de.renew.workflow.connector.cases.CaseHandlingProjectNature;
import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.context.ActiveWorkContext;
import de.renew.workflow.connector.worklist.ITaskExecutor;
import de.renew.workflow.contexts.ICaseHandlingSourceProvider;

/**
 * @author Stefan Kurzbach
 */
public class ScenarioHelper
{
  /**
   * Retrieves the folder of the currently active scenario via the current evaluation context of the handler service.
   */
  public static SzenarioDataProvider getScenarioDataProvider( )
  {
    final IWorkbench workbench = PlatformUI.getWorkbench();
    final IHandlerService handlerService = (IHandlerService) workbench.getService( IHandlerService.class );
    final IEvaluationContext context = handlerService.getCurrentState();
    return (SzenarioDataProvider) context.getVariable( ICaseHandlingSourceProvider.ACTIVE_CASE_DATA_PROVIDER_NAME );
  }

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

  public static IWorkflow findWorkflow( final IScenario scenario, final CaseHandlingProjectNature newProject )
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

  // FIXME: probably (hopefully) not needed any more...; remove?
  public static boolean ensureBackwardsCompatibility( final CaseHandlingProjectNature nature )
  {
    // FIXME: this is dirty fix only for this release 2.3
    // should be implemented in other way, we just do not have any time now
    try
    {
      if( nature != null && nature.getProject().hasNature( "org.kalypso.kalypso1d2d.pjt.Kalypso1D2DProjectNature" ) )
      {
        final ProjectTemplate[] lTemplate = EclipsePlatformContributionsExtensions.getProjectTemplates( "org.kalypso.kalypso1d2d.pjt.projectTemplate" );
        try
        {
          /* Unpack project from template */
          final File destinationDir = nature.getProject().getLocation().toFile();
          final URL data = lTemplate[0].getData();
          final String location = data.toString();
          final String extension = FilenameUtils.getExtension( location );
          if( "zip".equalsIgnoreCase( extension ) )
          {
            try
            {
              ZipUtilities.unzip( data.openStream(), destinationDir, false );
            }
            finally
            {
            }
          }
          else
          {
            final URL fileURL = FileLocator.toFileURL( data );
            final File dataDir = FileUtils.toFile( fileURL );
            if( dataDir == null )
            {
              return false;
            }
            final IOFileFilter lFileFilter = new WildcardFileFilter( new String[] { "wind.gml" } );
            final IOFileFilter lDirFilter = TrueFileFilter.INSTANCE;
            final Collection< ? > windFiles = FileUtils.listFiles( destinationDir, lFileFilter, lDirFilter );

            if( dataDir.isDirectory() && (windFiles == null || windFiles.size() == 0) )
            {
              final WildcardFileFilter lCopyFilter = new WildcardFileFilter( new String[] { "*asis", "models", "wind.gml" } );
              FileUtils.copyDirectory( dataDir, destinationDir, lCopyFilter );
            }
            else
            {
              return true;
            }
          }
        }
        catch( final Throwable t )
        {
          t.printStackTrace();
          return false;
        }
        nature.getProject().refreshLocal( IResource.DEPTH_INFINITE, null );
      }
    }
    catch( final CoreException e )
    {
      KalypsoAFGUIFrameworkPlugin.getDefault().getLog().log( e.getStatus() );
      e.printStackTrace();
      return false;
    }

    return true;
  }

  /**
   * This function activates a given scenario.
   *
   * @param scenario
   *          The scenario.
   */
  public static void activateScenario( final IScenario scenario ) throws CoreException
  {
    final KalypsoAFGUIFrameworkPlugin plugin = KalypsoAFGUIFrameworkPlugin.getDefault();
    final ActiveWorkContext context = plugin.getActiveWorkContext();

    final ITaskExecutor executor = plugin.getTaskExecutor();

    /* Only do it, if the scenario is not already active. */
    if( context.getCurrentCase() != scenario )
    {
      if( executor.stopActiveTask() )
      {
        /* Activate the scenario. */
        context.setCurrentCase( scenario );
      }
    }
  }

  /**
   * This function returns the active scenario.
   * 
   * @return The active scenario.
   */
  public static IScenario getActiveScenario( )
  {
    final KalypsoAFGUIFrameworkPlugin plugin = KalypsoAFGUIFrameworkPlugin.getDefault();
    final ActiveWorkContext activeWorkContext = plugin.getActiveWorkContext();

    return activeWorkContext.getCurrentCase();
  }
}