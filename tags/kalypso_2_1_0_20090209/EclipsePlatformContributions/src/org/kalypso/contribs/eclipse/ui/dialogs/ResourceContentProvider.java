/***********************************************************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: IBM Corporation - initial API and implementation
 **********************************************************************************************************************/
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

public class ResourceContentProvider extends BaseResourceContentProvider implements ITreeContentProvider
{
  private final boolean m_showClosedProjects = true;

  private final String[] m_allowedResourceExtensions;

  /**
   * Abgeleitet von ContainerContentProvider
   * 
   * @param allowedResourceExtensions
   *          tip: set a ViewerFilter and use {@link BaseResourceContentProvider}
   * @author N. Peiler
   * @author Dejan Antanaskovic, <a href="mailto:dejan.antanaskovic@tuhh.de">dejan.antanaskovic@tuhh.de</a>
   */
  public ResourceContentProvider( final String[] allowedResourceExtensions )
  {
    super();
    m_allowedResourceExtensions = allowedResourceExtensions;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
   */
  @Override
  public Object[] getChildren( final Object element )
  {
    if( element instanceof IWorkspace )
    {
      // check if closed projects should be shown
      final IProject[] allProjects = ((IWorkspace) element).getRoot().getProjects();
      if( m_showClosedProjects )
        return allProjects;

      final ArrayList<IProject> accessibleProjects = new ArrayList<IProject>();
      for( int i = 0; i < allProjects.length; i++ )
      {
        if( allProjects[i].isOpen() )
          accessibleProjects.add( allProjects[i] );
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
          final List<IResource> children = new ArrayList<IResource>();
          final IResource[] members = container.members();
          for( int i = 0; i < members.length; i++ )
          {
            if( members[i].getType() == IResource.FILE )
            {
              if( checkExtension( ((IFile) members[i]).getFileExtension() ) )
                children.add( members[i] );
            }
            else
            {
              children.add( members[i] );
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
    if( m_allowedResourceExtensions == null )
      return true;
    boolean returnValue = false;
    for( int i = 0; i < m_allowedResourceExtensions.length; i++ )
    {
      if( extension.equals( m_allowedResourceExtensions[i] ) )
      {
        returnValue = true;
      }
    }
    return returnValue;
  }

}