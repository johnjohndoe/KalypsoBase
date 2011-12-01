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
package org.kalypso.contribs.eclipse.core.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Helper methods for {@link IProject}.
 * 
 * @author Gernot Belger
 */
public class ProjectUtilities
{
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
    final Collection<IResource> projects = new HashSet<IResource>();
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
  public static final void addNature( final IProject project, final String natureId, final IProgressMonitor monitor ) throws CoreException
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

    final List<IProject> result = new ArrayList<IProject>( projects.length );

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

}