/*--------------- Kalypso-Header ------------------------------------------

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

 --------------------------------------------------------------------------*/
package org.kalypso.metaproject.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Provides a tree control with contents from projects and their dependencies.
 * 
 * @author schlienger
 */
public class ProjectsTreeContentProvider implements ITreeContentProvider
{
  /**
   * reduced view flag: if true, only the projects that have no refenrencing projects are firstly displayed. Other
   * projects still appear in the tree, but as children of some "parent" (refenrencing) project
   */
  private boolean m_reduced = false;

  /**
   * Children of a project are its dependencies (referenced projects)
   * 
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
   */
  public Object[] getChildren( Object parentElement )
  {
    if( parentElement instanceof IProject )
      try
      {
        return ( (IProject)parentElement ).getReferencedProjects();
      }
      catch( CoreException e )
      {
        e.printStackTrace();
      }

    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  public Object getParent( Object element )
  {
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
   */
  public boolean hasChildren( Object element )
  {
    if( element instanceof IProject )
      try
      {
        return ( (IProject)element ).getReferencedProjects().length > 0;
      }
      catch( CoreException e )
      {
        e.printStackTrace();
      }

    return false;
  }

  /**
   * Handles both case where the view should be reduced or not.
   * 
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements( Object inputElement )
  {
    if( inputElement instanceof IProject[] )
    {
      if( m_reduced )
      {
        /*
         * filter the projects and only take the ones that have no referencing projects
         */

        final IProject[] projects = (IProject[])inputElement;
        final List<IProject> list = new ArrayList<IProject>( projects.length );

        for( int i = 0; i < projects.length; i++ )
        {
          if( projects[i].getReferencingProjects().length == 0 )
            list.add( projects[i] );
        }

        return list.toArray();
      }
      else
        return (IProject[])inputElement;
    }

    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  public void dispose()
  {
  // empty
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
   *      java.lang.Object)
   */
  public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
  {
  // empty
  }

  public void setLayoutReduced( boolean bReduced )
  {
    m_reduced = bReduced;
  }
}
