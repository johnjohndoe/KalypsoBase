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
  private final Set<IFolder> m_folder = new HashSet<IFolder>();

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