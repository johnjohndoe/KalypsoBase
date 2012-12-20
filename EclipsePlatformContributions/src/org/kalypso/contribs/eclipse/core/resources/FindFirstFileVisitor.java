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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;

public class FindFirstFileVisitor implements IResourceVisitor
{
  private final String m_name;

  private final boolean m_ignoreCase;

  private IFile m_result = null;

  public FindFirstFileVisitor( final String name, final boolean ignoreCase )
  {
    m_name = name;
    m_ignoreCase = ignoreCase;
  }

  public IFile getFile( )
  {
    return m_result;
  }

  @Override
  public boolean visit( final IResource resource )
  {
    if( m_result == null && resource instanceof IFile )
    {
      final IFile file = (IFile) resource;
      final String name = file.getName();
      if( m_ignoreCase && name.equalsIgnoreCase( m_name ) || !m_ignoreCase && name.equals( m_name ) )
        m_result = file;
    }

    return true;
  }

}
