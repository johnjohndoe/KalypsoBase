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
package org.apache.commons.vfs;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URLStreamHandlerFactory;
import java.util.Collection;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.commons.vfs.operations.FileOperationProvider;

/**
 * @author Dirk Kuch
 */
public class FileSystemManagerWrapper implements FileSystemManager
{
  private final FileSystemManager m_manager;

  private final IFileSystemManagerResolveDelegate m_delegate;

  public FileSystemManagerWrapper( final FileSystemManager manager, final IFileSystemManagerResolveDelegate delegate )
  {
    m_manager = manager;
    m_delegate = delegate;
  }

  public void close( )
  {
    if( m_manager instanceof StandardFileSystemManager )
      ((StandardFileSystemManager) m_manager).close();
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#addOperationProvider(java.lang.String,
   *      org.apache.commons.vfs.operations.FileOperationProvider)
   */
  @Override
  public void addOperationProvider( final String scheme, final FileOperationProvider operationProvider ) throws FileSystemException
  {
    m_manager.addOperationProvider( scheme, operationProvider );
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#addOperationProvider(java.lang.String[],
   *      org.apache.commons.vfs.operations.FileOperationProvider)
   */
  @Override
  public void addOperationProvider( final String[] schemes, final FileOperationProvider operationProvider ) throws FileSystemException
  {
    m_manager.addOperationProvider( schemes, operationProvider );
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#canCreateFileSystem(org.apache.commons.vfs.FileObject)
   */
  @Override
  public boolean canCreateFileSystem( final FileObject file ) throws FileSystemException
  {
    return m_manager.canCreateFileSystem( file );
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#closeFileSystem(org.apache.commons.vfs.FileSystem)
   */
  @Override
  public void closeFileSystem( final FileSystem filesystem )
  {
    m_manager.closeFileSystem( filesystem );

  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#createFileSystem(org.apache.commons.vfs.FileObject)
   */
  @Override
  public FileObject createFileSystem( final FileObject file ) throws FileSystemException
  {
    return m_manager.createFileSystem( file );
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#createFileSystem(java.lang.String, org.apache.commons.vfs.FileObject)
   */
  @Override
  public FileObject createFileSystem( final String provider, final FileObject file ) throws FileSystemException
  {
    return m_manager.createFileSystem( provider, file );
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#createVirtualFileSystem(java.lang.String)
   */
  @Override
  public FileObject createVirtualFileSystem( final String rootUri ) throws FileSystemException
  {
    return m_manager.createVirtualFileSystem( rootUri );
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#createVirtualFileSystem(org.apache.commons.vfs.FileObject)
   */
  @Override
  public FileObject createVirtualFileSystem( final FileObject rootFile ) throws FileSystemException
  {
    return m_manager.createVirtualFileSystem( rootFile );
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#getBaseFile()
   */
  @Override
  public FileObject getBaseFile( ) throws FileSystemException
  {
    return m_manager.getBaseFile();
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#getCacheStrategy()
   */
  @Override
  public CacheStrategy getCacheStrategy( )
  {
    return m_manager.getCacheStrategy();
  }


  /**
   * @see org.apache.commons.vfs.FileSystemManager#getFileContentInfoFactory()
   */
  @Override
  public FileContentInfoFactory getFileContentInfoFactory( )
  {
    return m_manager.getFileContentInfoFactory();
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#getFileObjectDecorator()
   */
  @Override
  public Class getFileObjectDecorator( )
  {
    return m_manager.getFileObjectDecorator();
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#getFileObjectDecoratorConst()
   */
  @Override
  public Constructor getFileObjectDecoratorConst( )
  {
    return m_manager.getFileObjectDecoratorConst();
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#getFileSystemConfigBuilder(java.lang.String)
   */
  @Override
  public FileSystemConfigBuilder getFileSystemConfigBuilder( final String scheme ) throws FileSystemException
  {
    return m_manager.getFileSystemConfigBuilder( scheme );
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#getFilesCache()
   */
  @Override
  public FilesCache getFilesCache( )
  {
    return m_manager.getFilesCache();
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#getOperationProviders(java.lang.String)
   */
  @Override
  public FileOperationProvider[] getOperationProviders( final String scheme ) throws FileSystemException
  {
    return m_manager.getOperationProviders( scheme );
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#getProviderCapabilities(java.lang.String)
   */
  @Override
  public Collection getProviderCapabilities( final String scheme ) throws FileSystemException
  {
    return m_manager.getProviderCapabilities( scheme );
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#getSchemes()
   */
  @Override
  public String[] getSchemes( )
  {
    return m_manager.getSchemes();
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#getURLStreamHandlerFactory()
   */
  @Override
  public URLStreamHandlerFactory getURLStreamHandlerFactory( )
  {
    return m_manager.getURLStreamHandlerFactory();
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#resolveFile(java.lang.String)
   */
  @Override
  public FileObject resolveFile( final String name ) throws FileSystemException
  {
    if( m_delegate != null )
      return m_delegate.resolveFile( m_manager, name );

    return m_manager.resolveFile( name );
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#resolveFile(java.lang.String,
   *      org.apache.commons.vfs.FileSystemOptions)
   */
  @Override
  public FileObject resolveFile( final String name, final FileSystemOptions fileSystemOptions ) throws FileSystemException
  {
    if( m_delegate != null )
      return m_delegate.resolveFile( m_manager, name, fileSystemOptions );

    return m_manager.resolveFile( name, fileSystemOptions );
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#resolveFile(org.apache.commons.vfs.FileObject, java.lang.String)
   */
  @Override
  public FileObject resolveFile( final FileObject baseFile, final String name ) throws FileSystemException
  {
    if( m_delegate != null )
      return m_delegate.resolveFile( m_manager, baseFile, name );

    return m_manager.resolveFile( baseFile, name );
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#resolveFile(java.io.File, java.lang.String)
   */
  @Override
  public FileObject resolveFile( final File baseFile, final String name ) throws FileSystemException
  {
    if( m_delegate != null )
      return m_delegate.resolveFile( m_manager, baseFile, name );

    return m_manager.resolveFile( baseFile, name );
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#resolveName(org.apache.commons.vfs.FileName, java.lang.String)
   */
  @Override
  public FileName resolveName( final FileName root, final String name ) throws FileSystemException
  {
    return m_manager.resolveName( root, name );
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#resolveName(org.apache.commons.vfs.FileName, java.lang.String,
   *      org.apache.commons.vfs.NameScope)
   */
  @Override
  public FileName resolveName( final FileName root, final String name, final NameScope scope ) throws FileSystemException
  {
    return m_manager.resolveName( root, name, scope );
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#resolveURI(java.lang.String)
   */
  @Override
  public FileName resolveURI( final String uri ) throws FileSystemException
  {
    return m_manager.resolveURI( uri );
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#setLogger(org.apache.commons.logging.Log)
   */
  @Override
  public void setLogger( final org.apache.commons.logging.Log log )
  {
    m_manager.setLogger( log );
  }

  /**
   * @see org.apache.commons.vfs.FileSystemManager#toFileObject(java.io.File)
   */
  @Override
  public FileObject toFileObject( final File file ) throws FileSystemException
  {
    return m_manager.toFileObject( file );
  }
}
