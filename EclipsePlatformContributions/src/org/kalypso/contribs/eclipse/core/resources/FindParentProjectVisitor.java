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
package org.kalypso.contribs.eclipse.core.resources;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * @author kuepfer
 */
public class FindParentProjectVisitor implements IResourceVisitor
{

  private IProject m_parent;

  /**
   * @see org.eclipse.core.resources.IResourceVisitor#visit(org.eclipse.core.resources.IResource)
   */
  @Override
  public boolean visit( final IResource resource ) throws CoreException
  {
    final IContainer parent = resource.getParent();
    if( parent == null )
      return false;
    if( parent.getType() == IResource.FILE )
      return false;
    if( parent.getType() == IResource.PROJECT )
    {
      m_parent = (IProject) parent;
      return false;
    }
    return visit( parent );
  }

  public IProject getParentProject( )
  {
    return m_parent;
  }
}
