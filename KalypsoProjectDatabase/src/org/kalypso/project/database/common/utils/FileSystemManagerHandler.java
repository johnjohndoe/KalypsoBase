/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.project.database.common.utils;

import java.io.File;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.kalypso.commons.io.VFSUtilities;

/**
 * @author kuch
 */
public class FileSystemManagerHandler
{

  private final FileSystemManager m_manager;

  public FileSystemManagerHandler( final FileSystemManager manager )
  {
    m_manager = manager;
  }

  public FileObject resolveFile( final String path ) throws FileSystemException
  {
    final FileObject object;
    if( path.toLowerCase().startsWith( "ftp" ) )
    {
      object = m_manager.resolveFile( path, VFSUtilities.setFtpPassiveMode() );
    }
    else
    {
      object = m_manager.resolveFile( path );
    }

    return object;
  }

  public FileObject resolveFile( final File dir, final String name ) throws FileSystemException
  {
    return m_manager.resolveFile( dir, name );
  }

}
