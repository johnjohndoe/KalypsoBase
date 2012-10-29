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

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.IPath;

/**
 * die Visitor sammelt alle IFile-Objekte, deren Location auf einen festgelegten FileFilter passen.
 *
 * @author belger
 */
public class FileFilterVisitor implements IResourceVisitor
{
  private final FileFilter m_filter;

  private final Collection<IFile> m_foundFiles = new ArrayList<>();

  public FileFilterVisitor( final FileFilter filter )
  {
    m_filter = filter;
  }

  public IFile[] getFiles( )
  {
    return m_foundFiles.toArray( new IFile[m_foundFiles.size()] );
  }

  @Override
  public boolean visit( final IResource resource )
  {
    if( resource instanceof IFile )
    {
      final IFile file = (IFile) resource;
      final IPath location = file.getLocation();
      final File fileFile = location.toFile();
      if( m_filter.accept( fileFile ) )
        m_foundFiles.add( file );
    }

    return true;
  }
}