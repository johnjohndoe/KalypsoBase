/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.commons.eclipse.core.resources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.kalypso.commons.KalypsoCommonsPlugin;

import com.google.common.base.Charsets;

/**
 * Helper methods for {@link IProject}.
 *
 * @author Gernot Belger
 */
public final class ProjectUtilities
{
  private static final String FILE_PROJECT = ".project"; //$NON-NLS-1$

  private ProjectUtilities( )
  {
    // do not instantiate
  }

  /**
   * Findet alle Projekte einer Selektion von Resourcen
   *
   * @param selection
   * @return list of projects (not null)
   */
  public static IProject[] findProjectsFromSelection( final ISelection selection )
  {
    // gleiche Projekte sollen nur einen Eintrag gebens
    final Collection<IResource> projects = new HashSet<>();
    if( selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection )
    {
      final IStructuredSelection ssel = (IStructuredSelection) selection;
      for( final Iterator< ? > iter = ssel.iterator(); iter.hasNext(); )
      {
        final Object resource = iter.next();
        if( resource instanceof IResource )
          projects.add( ((IResource) resource).getProject() );
        else if( resource instanceof IAdaptable )
        {
          final IResource res = (IResource) ((IAdaptable) resource).getAdapter( IResource.class );
          if( res != null )
            projects.add( res.getProject() );
        }
      }
    }

    return projects.toArray( new IProject[projects.size()] );
  }

  /**
   * TODO does this work? seems not... Note from Marc: this only works when the navigator has an active selection
   * Returns the currently selected project from the navigator.
   *
   * @return list of selected projects
   */
  public static IProject[] getSelectedProjects( )
  {
    final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    final ISelection selection = window.getSelectionService().getSelection( IPageLayout.ID_RES_NAV );

    final IProject[] projects = findProjectsFromSelection( selection );

    return projects;
  }

  /**
   * Adds a nature to a project.<br>
   * Does nothing if the project is already of the requested nature.
   */
  public static void addNature( final IProject project, final String natureId, final IProgressMonitor monitor ) throws CoreException
  {
    if( project.hasNature( natureId ) )
      return;

    final IProjectDescription description = project.getDescription();
    final String[] natures = description.getNatureIds();
    final String[] newNatures = new String[natures.length + 1];
    System.arraycopy( natures, 0, newNatures, 0, natures.length );
    newNatures[natures.length] = natureId;
    description.setNatureIds( newNatures );
    project.setDescription( description, monitor );
  }

  /**
   * Finds all current open workspace projects of a given nature.
   */
  public static IProject[] allOfNature( final String natureId )
  {

    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    final IProject[] projects = root.getProjects();

    final List<IProject> result = new ArrayList<>( projects.length );

    for( final IProject project : projects )
    {
      if( !project.isOpen() )
        continue;

      try
      {
        final IProjectDescription description = project.getDescription();
        // REMARK: description is used here instead of IProject#getNature in order to avoid
        // the nature's plug-ins to get activated.
        final String[] natureIds = description.getNatureIds();
        for( final String id : natureIds )
        {
          if( natureId.equals( id ) )
          {
            result.add( project );
            break;
          }
        }
      }
      catch( final CoreException e )
      {
        // REMARK: according to the javadoc of IProject#getDescription this should never happen
        e.printStackTrace();
      }
    }

    return result.toArray( new IProject[result.size()] );
  }

  /**
   * Hack method to directly change the projects name in the project file.<br/>
   * Setting the project name to the project description actually does not work.<br>
   * We resolve this by directly tweaking the .project file, which is not nice but works.
   */
  public static void setProjectName( final IProject project, final String nameToSet ) throws CoreException
  {
    try
    {
      final IFile projectResource = project.getFile( FILE_PROJECT );
      final File projectFile = projectResource.getLocation().toFile();

      // REMARK: does not work correctly, if project was created this moment.
      // But we know, that .project is normally 'UTF-8' encoded.
      // final String projectEncoding = projectResource.getCharset( true );

      final String projectEncoding = Charsets.UTF_8.name();

      final String projectContents = FileUtils.readFileToString( projectFile, projectEncoding );
      final String nameTag = String.format( "<name>%s</name>", nameToSet ); //$NON-NLS-1$
      final String cleanedProjectContents = projectContents.replaceAll( "<name>.*</name>", nameTag ); //$NON-NLS-1$

      FileUtils.writeStringToFile( projectFile, cleanedProjectContents, projectEncoding );

      projectResource.refreshLocal( IResource.DEPTH_ONE, new NullProgressMonitor() );
    }
    catch( final IOException e )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoCommonsPlugin.getID(), "Failed to write project name into .project file.", e ); //$NON-NLS-1$
      throw new CoreException( status );
    }
  }
}