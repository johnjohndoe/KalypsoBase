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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;

/**
 * Collects all visited IFiles with a given file extension and deletes duplicates.
 *
 * @author Thomas Jung
 */
public class CollectFilesWithExtensionVisitor implements IResourceVisitor
{
  private final Set<IFile> m_files = new HashSet<>();

  private String m_extension;

  @Override
  public boolean visit( final IResource resource )
  {
    if( resource.getType() == IResource.FILE )
    {
      final IFile file = (IFile) resource;
      if( m_extension != null )
      {
        if( file.getFileExtension().equals( m_extension ) )
        {
          m_files.add( file );
        }
      }
      else
      {
        m_files.add( file );
      }
    }
    return true;
  }

  /**
   * Clears collected files, so visitor can be used again.
   */
  public void reset( )
  {
    m_files.clear();
  }

  public IFile[] getFiles( )
  {
    return m_files.toArray( new IFile[m_files.size()] );
  }

  public void setExtension( final String extension )
  {
    m_extension = extension;
  }
}