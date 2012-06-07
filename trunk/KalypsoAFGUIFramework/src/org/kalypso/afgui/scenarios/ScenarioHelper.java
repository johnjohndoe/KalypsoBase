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

import java.io.File;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.commons.java.util.zip.ZipUtilities;
import org.kalypso.contribs.eclipse.EclipsePlatformContributionsExtensions;
import org.kalypso.contribs.eclipse.core.resources.ProjectTemplate;

import de.renew.workflow.base.IWorkflow;
import de.renew.workflow.connector.WorkflowProjectNature;
import de.renew.workflow.connector.cases.IScenario;
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

  // FIXME: probably (hopefully) not needed any more...; remove?
  public static boolean ensureBackwardsCompatibility( final ScenarioHandlingProjectNature nature )
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
    final ActiveWorkContext context = KalypsoAFGUIFrameworkPlugin.getActiveWorkContext();

    final IScenario currentCase = context.getCurrentCase();
    if( currentCase == scenario )
      return;

    // TODO: move outside, task must be stopped by clients
    final ITaskExecutor executor = KalypsoAFGUIFrameworkPlugin.getTaskExecutor();
    if( executor.stopActiveTask() )
    {
      /* Activate the scenario. */
      context.setCurrentCase( scenario );
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
  public static IFolder findModelContext( final IFolder szenarioFolder, final String modelFile )
  {
    if( szenarioFolder == null )
      return null;

    final Path path = new Path( modelFile );
    if( szenarioFolder.getFile( path ).exists() )
      return szenarioFolder;

    final IContainer parent = szenarioFolder.getParent();
    if( parent.getType() != IResource.PROJECT )
      return findModelContext( (IFolder) parent, modelFile );

    return null;
  }

  /**
   * Returns the current scenario's base folder
   */
  public static IContainer getScenarioFolder( )
  {
    final IScenario scenario = getActiveScenario();
    if( scenario != null )
      return scenario.getFolder();

    return null;
  }
}