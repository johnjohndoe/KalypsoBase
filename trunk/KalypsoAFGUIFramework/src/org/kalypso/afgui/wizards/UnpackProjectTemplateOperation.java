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
import org.apache.commons.io.filefilter.WildcardFilter;
import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.afgui.i18n.Messages;
import org.kalypso.commons.java.util.zip.ZipUtilities;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;

public final class UnpackProjectTemplateOperation extends WorkspaceModifyOperation
{
  private final URL m_dataLocation;

  private final IProject m_project;

  private final NewProjectWizard m_newProjectWizard;

  public UnpackProjectTemplateOperation( final NewProjectWizard newProjectWizard, final URL dataLocation, final IProject project )
  {
    super( project.getWorkspace().getRoot() );
    m_newProjectWizard = newProjectWizard;
    m_dataLocation = dataLocation;
    m_project = project;
  }

  @Override
  public void execute( final IProgressMonitor monitor ) throws CoreException, InvocationTargetException
  {
    final String newName = m_project.getName();

    final SubMonitor progress = SubMonitor.convert( monitor, Messages.getString( "org.kalypso.afgui.wizards.NewProjectWizard.2" ), 90 ); //$NON-NLS-1$
    try
    {
      // REMARK: we unpack into a closed project here (not using unzip(URL, IFolder)), as else
      // the project description will not be up-to-date in time, resulting in missing natures.
      m_project.close( progress.newChild( 10 ) );

      /* Unpack project from template */
      final File destinationDir = m_project.getLocation().toFile();
      unpackProjectData( m_dataLocation, destinationDir );
      ProgressUtilities.worked( progress, 40 );
      m_project.open( progress.newChild( 10 ) );

      // IMPORTANT: As the project was already open once before, we need to refresh here, else
      // not all resources are up-to-date
      m_project.refreshLocal( IResource.DEPTH_INFINITE, progress.newChild( 10 ) );

      /* Re-set name to new name, as un-zipping probably did change the internal name */
      final IProjectDescription description = m_project.getDescription();
      description.setName( newName );
      // HACK: in order to enforce the change, we also change the comment a bit, else
      // the description does not recognise any change and the .project file does not get written
      description.setComment( description.getComment() + " " ); //$NON-NLS-1$
      m_project.setDescription( description, IResource.FORCE | IResource.AVOID_NATURE_CONFIG, progress.newChild( 10 ) );

      /* validate and configure all natures of this project. */
      final String[] natureIds = (String[]) ArrayUtils.removeElement( description.getNatureIds(), NewProjectWizard.PDE_NATURE_ID );
      final IStatus validateNatureSetStatus = m_project.getWorkspace().validateNatureSet( natureIds );
      if( !validateNatureSetStatus.isOK() )
        throw new CoreException( validateNatureSetStatus );

      progress.setWorkRemaining( natureIds.length + 1 );

      for( final String natureId : natureIds )
      {
        final IProjectNature nature = m_project.getNature( natureId );
        nature.configure();
        ProgressUtilities.worked( progress, 1 );
      }

      /* Let inherited wizards change the project */
      m_newProjectWizard.postCreateProject( m_project, progress.newChild( 1 ) );

      m_newProjectWizard.openProject( m_project );
    }
    catch( final CoreException t )
    {
      // If anything went wrong, clean up the project
      progress.setWorkRemaining( 10 );
      m_project.delete( true, progress );

      throw t;
    }
    catch( final Throwable t )
    {
      // If anything went wrong, clean up the project
      progress.setWorkRemaining( 10 );
      m_project.delete( true, progress );

      throw new InvocationTargetException( t );
    }
  }

  private void unpackProjectData( final URL data, final File destinationDir ) throws IOException, CoreException
  {
    final String location = data.toString();
    final String extension = FilenameUtils.getExtension( location );
    if( "zip".equalsIgnoreCase( extension ) )
      ZipUtilities.unzip( data, destinationDir );
    else
    {
      final File dataDir = FileUtils.toFile( data );
      if( dataDir == null )
      {
        final String msg = String.format( "Invalid dataLocation: %s", data );
        final IStatus status = new Status( IStatus.ERROR, KalypsoAFGUIFrameworkPlugin.PLUGIN_ID, msg );
        throw new CoreException( status );
      }

      if( dataDir.isDirectory() )
      {
        FileUtils.copyDirectory( dataDir, destinationDir );
        removePDEfiles( destinationDir );
      }
      else
      {
        final String msg = String.format( "Invalid dataLocation (not a directory): %s", data );
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
    final File manifestDir = new File( destinationDir, "META-INF" );
    FileUtils.deleteDirectory( manifestDir );

    new File( destinationDir, "plugin.xml" ).delete();

    final File[] propertyFiles = destinationDir.listFiles( (FilenameFilter) new WildcardFilter( "plugin*.properties" ) );
    if( propertyFiles != null )
    {
      for( final File file : propertyFiles )
        file.delete();
    }
  }
}