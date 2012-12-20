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
package org.kalypso.afgui.wizards;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.commons.eclipse.core.resources.ProjectUtilities;
import org.kalypso.commons.java.util.zip.ZipUtilities;
import org.kalypso.contribs.eclipse.core.resources.ProjectTemplate;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.module.INewProjectHandler;
import org.kalypso.module.nature.ModuleNature;

public final class UnpackProjectTemplateOperation extends WorkspaceModifyOperation
{
  private static final String FILE_ABOUT_HTML = "about.html"; //$NON-NLS-1$

  private final NewProjectData m_data;

  public UnpackProjectTemplateOperation( final NewProjectData data )
  {
    super( ResourcesPlugin.getWorkspace().getRoot() );

    m_data = data;

  }

  @Override
  public void execute( final IProgressMonitor monitor ) throws CoreException, InvocationTargetException, InterruptedException
  {
    final IProject project = m_data.getProject();
    final ProjectTemplate template = m_data.getTemplate();
    final URL dataLocation = template.getData();

    final SubMonitor progress = SubMonitor.convert( monitor, Messages.getString( "org.kalypso.afgui.wizards.NewProjectWizard.2" ), 100 ); //$NON-NLS-1$
    try
    {
      // REMARK: we unpack into a closed project here (not using unzip(URL, IFolder)), as else
      // the project description will not be up-to-date in time, resulting in missing natures.
      project.close( progress.newChild( 10 ) );

      /* Unpack project from template */
      monitor.subTask( Messages.getString( "UnpackProjectTemplateOperation.1" ) ); //$NON-NLS-1$
      final File destinationDir = project.getLocation().toFile();
      unpackProjectData( dataLocation, destinationDir );
      ProgressUtilities.worked( progress, 30 );

      /* Reset project to its own value, else we get the name from the zip, which leads to problems later. */
      ProjectUtilities.setProjectName( project, project.getName() );

      project.open( progress.newChild( 10 ) );

      /* Necessary to remove all build specs from the data projects (we have at least the manifest builder now) */
      removeBuildspec( project );

      // IMPORTANT: As the project was already open once before, we need to refresh here, else
      // not all resources are up-to-date
      project.refreshLocal( IResource.DEPTH_INFINITE, progress.newChild( 10 ) );

      /* Let inherited wizards change the project */
      final INewProjectHandler handler = m_data.getHandler();
      if( handler == null )
        return;

      final IStatus postCreateStatus = handler.postCreateProject( project, template, progress.newChild( 30, SubMonitor.SUPPRESS_NONE ) );
      if( !postCreateStatus.matches( IStatus.ERROR ) )
        handler.openProject( project );

      if( postCreateStatus != Status.OK_STATUS )
        throw new CoreException( postCreateStatus );

      /* Enforce and cleanup natures and description */
      final String moduleID = m_data.getModuleID();
      ModuleNature.enforceNature( project, moduleID );

      final String[] natureIds = cleanDescription( project, progress );
      configureNatures( project, natureIds, progress );
    }
    catch( final CoreException t )
    {
      final IStatus status = t.getStatus();
      if( status.matches( IStatus.ERROR | IStatus.CANCEL ) )
      {
        // If anything went wrong, clean up the project
        // We use new monitor, else delete will not work if the monitor is already cancelled.
        project.delete( true, new NullProgressMonitor() );
      }

      if( status.matches( IStatus.CANCEL ) )
        throw new InterruptedException( Messages.getString( "UnpackProjectTemplateOperation.2" ) ); //$NON-NLS-1$

      throw t;
    }
    catch( final Throwable t )
    {
      // If anything went wrong, clean up the project
      progress.setWorkRemaining( 10 );
      project.delete( true, progress );

      throw new InvocationTargetException( t );
    }
  }

  private void removeBuildspec( final IProject project ) throws CoreException
  {
    final IProjectDescription description = project.getDescription();
    description.setBuildSpec( new ICommand[0] );
    project.setDescription( description, null );
  }

  /**
   * Cleans the description of the freshly created project: reset the name and remove PDE-nature if it was configured.
   */
  private String[] cleanDescription( final IProject project, final SubMonitor progress ) throws CoreException
  {
    /* Re-set name to new name, as un-zipping probably did change the internal name */
    final IProjectDescription description = project.getDescription();
    description.setName( project.getName() );
    final String[] natureIds = description.getNatureIds();
    /* Also remove the PDE-nature, if it is present. This is needed for self-hosted project templates. */
    final String[] cleanedNatureIds = ArrayUtils.removeElement( natureIds, NewProjectWizard.PDE_NATURE_ID );
    description.setNatureIds( cleanedNatureIds );

    project.setDescription( description, IResource.FORCE /* | IResource.AVOID_NATURE_CONFIG */, progress.newChild( 10 ) );
    return cleanedNatureIds;
  }

  private void configureNatures( final IProject project, final String[] natureIds, final SubMonitor progress ) throws CoreException
  {
    /* validate and configure all natures of this project. */
    final IStatus validateNatureSetStatus = project.getWorkspace().validateNatureSet( natureIds );
    if( !validateNatureSetStatus.isOK() )
      throw new CoreException( validateNatureSetStatus );

    progress.setWorkRemaining( natureIds.length + 1 );

    for( final String natureId : natureIds )
    {
      final IProjectNature nature = project.getNature( natureId );
      nature.configure();
      ProgressUtilities.worked( progress, 1 );
    }
  }

  private void unpackProjectData( final URL data, final File destinationDir ) throws IOException, CoreException
  {
    final String location = data.toString();
    final String extension = FilenameUtils.getExtension( location );
    if( "zip".equalsIgnoreCase( extension ) ) //$NON-NLS-1$
      ZipUtilities.unzip( data, destinationDir );
    else
    {
      final URL fileURL = FileLocator.toFileURL( data );
      final File dataDir = FileUtils.toFile( fileURL );
      if( dataDir == null )
      {
        final String msg = String.format( "Invalid dataLocation: %s", data ); //$NON-NLS-1$
        final IStatus status = new Status( IStatus.ERROR, KalypsoAFGUIFrameworkPlugin.PLUGIN_ID, msg );
        throw new CoreException( status );
      }

      if( dataDir.isDirectory() )
      {
        FileUtils.copyDirectory( dataDir, destinationDir, FileFilterUtils.makeSVNAware( null ) );
        removePDEfiles( destinationDir );
      }
      else
      {
        final String msg = String.format( "Invalid dataLocation (not a directory): %s", data ); //$NON-NLS-1$
        final IStatus status = new Status( IStatus.ERROR, KalypsoAFGUIFrameworkPlugin.PLUGIN_ID, msg );
        throw new CoreException( status );
      }
    }
  }

  /**
   * A bit hacky: remove all PDE-specific files from project templates.
   */
  private void removePDEfiles( final File destinationDir ) throws IOException
  {
    final File manifestDir = new File( destinationDir, "META-INF" ); //$NON-NLS-1$
    FileUtils.deleteDirectory( manifestDir );

    new File( destinationDir, "plugin.xml" ).delete(); //$NON-NLS-1$
    new File( destinationDir, "build.properties" ).delete(); //$NON-NLS-1$

    final File[] propertyFiles = destinationDir.listFiles( (FilenameFilter)new WildcardFileFilter( "plugin*.properties" ) ); //$NON-NLS-1$
    if( propertyFiles != null )
    {
      for( final File file : propertyFiles )
        file.delete();
    }

    /* Purge the code licence, we only want it in the sources */
    new File( destinationDir, FILE_ABOUT_HTML ).delete();
  }
}