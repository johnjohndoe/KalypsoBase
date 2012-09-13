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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;

/**
 * @author N. Peiler
 * @author Dejan Antanaskovic, <a href="mailto:dejan.antanaskovic@tuhh.de">dejan.antanaskovic@tuhh.de</a>
 * @deprecated Use {@link org.eclipse.ui.model.WorkbenchContentProvider} and {@link org.eclipse.jface.viewers.ViewerFilter} instead.
 */
@Deprecated
public class ResourceContentProvider extends BaseResourceContentProvider
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
      return ((IWorkspace)element).getRoot().getProjects();
    }
    else if( element instanceof IContainer )
    {
      final IContainer container = (IContainer)element;
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
              if( checkExtension( ((IFile)member).getFileExtension() ) )
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
    for( final String allowedResourceExtension : m_allowedResourceExtensions )
    {
      if( extension.equals( allowedResourceExtension ) )
      {
        returnValue = true;
      }
    }
    return returnValue;
  }

}