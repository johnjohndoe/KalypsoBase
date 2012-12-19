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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;

/**
 * Collects all visited {@link org.eclipse.core.resources.IFolder}s and deletes duplicates.
 *
 * @author belger
 */
public class CollectFolderVisitor implements IResourceVisitor
{
  private final Set<IFolder> m_folder = new HashSet<>();

  private final List<IFolder> m_excludeList;

  /**
   * @param foldersToExclude
   *          These folders will be excluded from the results.
   * @see #getFolders()
   */
  public CollectFolderVisitor( final IFolder[] foldersToExclude )
  {
    m_excludeList = Arrays.asList( foldersToExclude );
  }

  /**
   * @see org.eclipse.core.resources.IResourceVisitor#visit(org.eclipse.core.resources.IResource)
   */
  @Override
  public boolean visit( final IResource resource )
  {
    if( resource.getType() == IResource.FOLDER && !m_excludeList.contains( resource ) )
      m_folder.add( (IFolder) resource );

    return true;
  }

  /**
   * Clears collected files, so visitor can be used again.
   */
  public void reset( )
  {
    m_folder.clear();
  }

  public IFolder[] getFolders( )
  {
    return m_folder.toArray( new IFolder[m_folder.size()] );
  }
}
