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
import java.net.URL;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.IFileSystemManagerResolveDelegate;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.ftp.FtpFileSystemConfigBuilder;

/**
 * @author Dirk Kuch
 */
public class PlanerClientFileSystemManager implements IFileSystemManagerResolveDelegate
{

  private final FileSystemOptions m_ftpOptions;

  public PlanerClientFileSystemManager( )
  {
    m_ftpOptions = new FileSystemOptions();
    FtpFileSystemConfigBuilder.getInstance().setPassiveMode( m_ftpOptions, true );
  }

  /**
   * @see org.apache.commons.vfs.impl.DefaultFileSystemManager#resolveFile(org.apache.commons.vfs.FileObject,
   *      java.lang.String, org.apache.commons.vfs.FileSystemOptions)
   */
  public FileObject resolveFile( final FileSystemManager manager, final FileObject baseFile, final String uri, final FileSystemOptions fileSystemOptions ) throws FileSystemException
  {
    if( isFtpProtocol( baseFile, uri ) )
      configurePassiveMode( fileSystemOptions );

    if( manager instanceof DefaultFileSystemManager )
    {
      final DefaultFileSystemManager def = (DefaultFileSystemManager) manager;

      return def.resolveFile( baseFile, uri, m_ftpOptions );
    }

    return manager.resolveFile( baseFile, uri );
  }

  private void configurePassiveMode( final FileSystemOptions options )
  {
    FtpFileSystemConfigBuilder.getInstance().setPassiveMode( options, true );
  }

  /**
   * @see org.apache.commons.vfs.impl.DefaultFileSystemManager#resolveFile(org.apache.commons.vfs.FileObject,
   *      java.lang.String)
   */
  @Override
  public FileObject resolveFile( final FileSystemManager manager, final FileObject baseFile, final String uri ) throws FileSystemException
  {
    if( isFtpProtocol( baseFile, uri ) ) 
    {
      if( manager instanceof DefaultFileSystemManager )
      {
        final DefaultFileSystemManager def = (DefaultFileSystemManager) manager;

        return def.resolveFile( baseFile, uri, m_ftpOptions );
      }
    }
     

    return manager.resolveFile( baseFile, uri );
  }

  /**
   * @see org.apache.commons.vfs.impl.DefaultFileSystemManager#resolveFile(java.lang.String)
   */
  @Override
  public FileObject resolveFile( final FileSystemManager manager, final String uri ) throws FileSystemException
  {
    if( isFtpProtocol( uri ) )
      return manager.resolveFile( uri, m_ftpOptions );

    return manager.resolveFile( uri );
  }

  /**
   * @see org.apache.commons.vfs.impl.DefaultFileSystemManager#resolveFile(java.lang.String,
   *      org.apache.commons.vfs.FileSystemOptions)
   */
  @Override
  public FileObject resolveFile( final FileSystemManager manager, final String uri, final FileSystemOptions fileSystemOptions ) throws FileSystemException
  {
    if( isFtpProtocol( uri ) )
      configurePassiveMode( fileSystemOptions );

    return manager.resolveFile( uri, fileSystemOptions );
  }

  private boolean isFtpProtocol( final String uri )
  {
    return uri.toLowerCase().startsWith( "ftp" ); //$NON-NLS-1$
  }

  private boolean isFtpProtocol( final FileObject baseFile, final String uri ) throws FileSystemException
  {
    if( baseFile != null )
    { 
      final URL url = baseFile.getURL();
      if( url.getProtocol().toLowerCase().startsWith( "ftp" ) ) //$NON-NLS-1$
        return true;
    }

    return isFtpProtocol( uri );
  }

  /**
   * @see org.kalypso.commons.io.IFileSystemManagerResolveDelegate#resolveFile(org.apache.commons.vfs.FileSystemManager,
   *      java.io.File, java.lang.String)
   */
  @Override
  public FileObject resolveFile( final FileSystemManager manager, final File baseFile, final String name ) throws FileSystemException
  {
    return manager.resolveFile( baseFile, name );
  }
}
