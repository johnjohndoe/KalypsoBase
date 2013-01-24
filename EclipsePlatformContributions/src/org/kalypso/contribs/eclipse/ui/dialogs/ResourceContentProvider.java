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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * @author N. Peiler
 * @author Dejan Antanaskovic, <a href="mailto:dejan.antanaskovic@tuhh.de">dejan.antanaskovic@tuhh.de</a>
 * @deprecated Use {@link org.eclipse.ui.model.WorkbenchContentProvider} and
 *             {@link org.eclipse.jface.viewers.ViewerFilter} instead.
 */
@Deprecated
public class ResourceContentProvider extends BaseResourceContentProvider implements ITreeContentProvider
{
  private final String[] m_allowedResourceExtensions;

  /**
   * Abgeleitet von ContainerContentProvider
   * 
   * @param allowedResourceExtensions
   *          tip: set a ViewerFilter and use {@link BaseResourceContentProvider}
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
      return ((IWorkspace) element).getRoot().getProjects();
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
    if( m_allowedResourceExtensions == null )
      return true;
    boolean returnValue = false;
    for( final String m_allowedResourceExtension : m_allowedResourceExtensions )
    {
      if( extension.equals( m_allowedResourceExtension ) )
      {
        returnValue = true;
      }
    }
    return returnValue;
  }

}