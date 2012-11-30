/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.contribs.eclipse.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class BaseResourceContentProvider implements ITreeContentProvider
{
  private boolean m_showClosedProjects = true;

  /**
   * @author Dirk Kuch
   */
  public BaseResourceContentProvider( )
  {
  }

  @Override
  public Object[] getChildren( final Object element )
  {
    if( element instanceof IWorkspace )
    {
      // check if closed projects should be shown
      final IProject[] allProjects = ((IWorkspace) element).getRoot().getProjects();
      if( m_showClosedProjects )
        return allProjects;

      final ArrayList<IProject> accessibleProjects = new ArrayList<>();
      for( final IProject allProject : allProjects )
      {
        if( allProject.isOpen() )
          accessibleProjects.add( allProject );
      }
      return accessibleProjects.toArray();
    }
    else if( element instanceof IContainer )
    {
      final IContainer container = (IContainer) element;
      if( container.isAccessible() )
      {
        try
        {
          final List<IResource> children = new ArrayList<>();
          final IResource[] members = container.members();
          for( final IResource member : members )
          {
            if( member.getType() == IResource.FILE )
            {
              if( checkExtension( ((IFile) member).getFileExtension() ) )
                children.add( member );
            }
            else
            {
              children.add( member );
            }
          }
          return children.toArray();
        }
        catch( final CoreException e )
        {
          // this should never happen because we call #isAccessible before
          // invoking #members
        }
      }
    }
    return new Object[0];
  }

  private boolean checkExtension( final String extension )
  {
    if( extension == null )
      return false;

    return true;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  @Override
  public Object getParent( final Object element )
  {
    if( element instanceof IResource )
      return ((IResource) element).getParent();
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
   */
  @Override
  public boolean hasChildren( final Object element )
  {
    return getChildren( element ).length > 0;
  }

  /**
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  @Override
  public Object[] getElements( final Object inputElement )
  {
    return getChildren( inputElement );
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  @Override
  public void dispose( )
  {
    // do nothing
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
   *      java.lang.Object)
   */
  @Override
  public void inputChanged( final Viewer viewer, final Object oldInput, final Object newInput )
  {
    // do nothing
  }

  public void showClosedProjects( final boolean show )
  {
    m_showClosedProjects = show;
  }

}