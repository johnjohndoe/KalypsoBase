/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.commons.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.FileUtil;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.UserAuthenticator;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.provider.http.HttpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.webdav.WebdavFileProvider;
import org.apache.commons.vfs2.provider.webdav.WebdavFileSystemConfigBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.kalypso.commons.KalypsoCommonsDebug;
import org.kalypso.commons.internal.i18n.Messages;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.commons.java.util.zip.ZipUtilities;
import org.kalypso.commons.net.ProxyUtilities;
import org.kalypso.commons.vfs.FileSystemManagerWrapper;
import org.kalypso.commons.vfs.IFileSystemManagerResolveDelegate;
import org.kalypso.commons.vfs.VFSProviderExtension;
import org.kalypso.contribs.eclipse.core.net.Proxy;

/**
 * Helpful functions when dealing with VFS. <br>
 * When working with VFS be aware that calling {@link #getManager()} will return a singleton global manager (the default
 * static manager from {@link VFS}). <br>
 * Call {@link #getNewManager()} when you want to have your private manager. This will ensure that you can close any
 * file systems and the connections made when you are finished. Call {@link StandardFileSystemManager#close()} when you
 * don't need the manager anymore. <br>
 * When calling {@link FileObject#getContent()} you should also call {@link FileObject#close()} eventually. Otherwise
 * the resources might never be disposed.
 *
 * @author Holger Albert, Stefan Kurzbach
 */
public class VFSUtilities
{
  private static final FileSystemOptions THE_WEBDAV_OPTIONS = new FileSystemOptions();

  private static final FileSystemOptions THE_HTTP_OPTIONS = new FileSystemOptions();

  private static final FileSystemOptions THE_HTTPS_OPTIONS = new FileSystemOptions();

  private static final String EXTENSION_POINT_ID = "org.kalypso.commons.provider"; //$NON-NLS-1$

  private static IFileSystemManagerResolveDelegate FILE_SYSTEM_MANAGER_DELEGATE = null;

  /**
   * The constructor.
   */
  private VFSUtilities( )
  {
  }

  /**
   * This function returns a singleton FileSystemManager. Do not close this manager or any file systems it manages. <br/>
   * <br/>
   * HINT:<br/>
   * The file system manager of VFS is a singleton. During configuration another part of code may request one and start
   * configuring again. The configure function checks, if a filesystem provider is already configured, but there are
   * still critical parts which needs to be synchronized.
   */
  public static synchronized FileSystemManager getManager( ) throws FileSystemException
  {
    final DefaultFileSystemManager fsManager = (DefaultFileSystemManager) VFS.getManager();
    configureManager( fsManager );

    return new FileSystemManagerWrapper( fsManager, FILE_SYSTEM_MANAGER_DELEGATE );
  }

  /**
   * This function returns a new private StandardFileSystemManager. It is the caller's responsibility to close the
   * manager and release any resources associated with its file systems.
   */
  public static FileSystemManagerWrapper getNewManager( ) throws FileSystemException
  {
    // create new file system manager
    final StandardFileSystemManager fsManager = new StandardFileSystemManager();
    fsManager.setConfiguration( VFSUtilities.class.getResource( "vfs-providers.xml" ) ); //$NON-NLS-1$
    fsManager.init();

    configureManager( fsManager );

    return new FileSystemManagerWrapper( fsManager, FILE_SYSTEM_MANAGER_DELEGATE );
  }

  /**
   * Configures a DefaultFileSystemManager with support for webdav and registered providers.
   */
  private static void configureManager( final DefaultFileSystemManager fsManager ) throws FileSystemException
  {
    final String[] schemes = fsManager.getSchemes();
    final List<String> schemeList = Arrays.asList( schemes );

    // maybe add webdav
    if( !schemeList.contains( "webdav" ) ) //$NON-NLS-1$
    {
      KalypsoCommonsDebug.DEBUG.printf( "Adding webdav file provider ...%n" ); //$NON-NLS-1$
      fsManager.addProvider( "webdav", new WebdavFileProvider() ); //$NON-NLS-1$
    }

    final Map<String, IConfigurationElement> providerLocations = readExtensions();
    for( final Map.Entry<String, IConfigurationElement> entry : providerLocations.entrySet() )
    {
      final IConfigurationElement element = entry.getValue();

      final String scheme = element.getAttribute( "scheme" ); //$NON-NLS-1$
      if( !schemeList.contains( scheme ) )
      {
        try
        {
          final VFSProviderExtension provider = (VFSProviderExtension) element.createExecutableExtension( "class" ); //$NON-NLS-1$
          fsManager.addProvider( scheme, provider.getProvider() );
          provider.init( fsManager );
        }
        catch( final CoreException e )
        {
          throw new FileSystemException( Messages.getString( "org.kalypso.commons.io.VFSUtilities.0" ) + scheme, e ); //$NON-NLS-1$
        }
      }
    }
  }

  /**
   * Returns the registered providers for Apache Commons VFS
   */
  private static Map<String, IConfigurationElement> readExtensions( )
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();

    final IExtensionPoint extensionPoint = registry.getExtensionPoint( EXTENSION_POINT_ID );
    final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
    final Map<String, IConfigurationElement> providerLocations = new HashMap<>( configurationElements.length );

    for( final IConfigurationElement element : configurationElements )
    {
      final String bundleName = element.getContributor().getName();
      providerLocations.put( bundleName, element );
    }
    return providerLocations;
  }

  /**
   * Same as copy(source, destination, true)
   *
   * @see #copy(FileObject, FileObject, boolean)
   */
  public static void copy( final FileObject source, final FileObject destination ) throws IOException
  {
    copy( source, destination, true );
  }

  /**
   * This function copies a source to a given destination. If no filename is given in the destination file handle, the
   * filename of the source is used.<br>
   *
   * @param source
   *          The source file.
   * @param destination
   *          The destination file or path.
   * @param overwrite
   *          If set, always overwrite existing and newer files
   */
  public static void copy( final FileObject source, final FileObject destination, final boolean overwrite ) throws IOException
  {
    if( FileType.FOLDER.equals( source.getType() ) )
    {
      copyDirectoryToDirectory( source, destination, overwrite );
      return;
    }

    copyFileTo( source, destination, overwrite );
  }

  /**
   * Same as copyFileTo(source, destination, true)
   *
   * @see #copyFileTo(FileObject, FileObject, boolean)
   */
  public static void copyFileTo( final FileObject source, final FileObject destination ) throws IOException
  {
    copyFileTo( source, destination, true );
  }

  /**
   * This function copies a source file to a given destination. If no filename is given in the destination file handle,
   * the filename of the source is used.<br>
   * <br>
   * It is tried to copy the file three times. If all three tries has failed, only then an IOException is thrown. <br>
   * All other exceptions are thrown normally.
   *
   * @param source
   *          The source file.
   * @param destination
   *          The destination file or path.
   * @param overwrite
   *          If set, always overwrite existing and newer files
   */
  public static void copyFileTo( final FileObject source, final FileObject destination, final boolean overwrite ) throws IOException
  {
    if( source.equals( destination ) )
    {
      KalypsoCommonsDebug.DEBUG.printf( Messages.getString( "org.kalypso.commons.io.VFSUtilities.1" ), source.getName(), destination.getName() ); //$NON-NLS-1$
      return;
    }

    /* Some variables for handling the errors. */
    boolean success = false;
    int cnt = 0;

    while( success == false )
    {
      try
      {
        if( FileType.FOLDER.equals( source.getType() ) )
          throw new IllegalArgumentException( Messages.getString( "org.kalypso.commons.io.VFSUtilities.2" ) ); //$NON-NLS-1$

        /* If the destination is only a directory, use the sources filename for the destination file. */
        FileObject destinationFile = destination;
        if( FileType.FOLDER.equals( destination.getType() ) )
          destinationFile = destination.resolveFile( source.getName().getBaseName() );

        if( overwrite || !destinationFile.exists() || destinationFile.getContent().getSize() != source.getContent().getSize() )
        {
          /* Copy file. */
          KalypsoCommonsDebug.DEBUG.printf( "Copy file '%s' to '%s'...%n", source.getName(), destinationFile.getName() ); //$NON-NLS-1$
          FileUtil.copyContent( source, destinationFile );
          source.close();
        }

        /* End copying of this file, because it was a success. */
        success = true;
      }
      catch( final IOException e )
      {
        /* An error has occurred while copying the file. */
        KalypsoCommonsDebug.DEBUG.printf( "An error has occured with the message: %s%n", e.getLocalizedMessage() ); //$NON-NLS-1$

        /* If a certain amount (here 2) of retries was reached before, re-throw the error. */
        if( cnt >= 2 )
        {
          KalypsoCommonsDebug.DEBUG.printf( "The second retry has failed, rethrowing the error...%n" ); //$NON-NLS-1$
          throw e;
        }

        /* Retry the copying of the file. */
        cnt++;
        KalypsoCommonsDebug.DEBUG.printf( "Retry: %s%n", String.valueOf( cnt ) ); //$NON-NLS-1$
        success = false;

        /* Wait for some milliseconds. */
        try
        {
          Thread.sleep( 1000 );
        }
        catch( final InterruptedException e1 )
        {
          /*
           * Runs in the next loop then and if no error occurs then, it is ok. If an error occurs again, it is an
           * exception thrown on the last failed retry or it is slept again.
           */
        }
      }
    }
  }

  /**
   * This function will copy one directory to another one. If the destination base directory does not exist, it will be
   * created.
   *
   * @param source
   *          The source directory.
   * @param destination
   *          The destination directory.
   * @param overwrite
   *          If set, always overwrite existing and newer files
   */
  public static void copyDirectoryToDirectory( final FileObject source, final FileObject destination, final boolean overwrite ) throws IOException
  {
    if( !FileType.FOLDER.equals( source.getType() ) )
      throw new IllegalArgumentException( Messages.getString( "org.kalypso.commons.io.VFSUtilities.3" ) + source.getURL() ); //$NON-NLS-1$

    if( destination.exists() )
    {
      if( !FileType.FOLDER.equals( destination.getType() ) )
        throw new IllegalArgumentException( Messages.getString( "org.kalypso.commons.io.VFSUtilities.4" ) + destination.getURL() ); //$NON-NLS-1$
    }
    else
    {
      KalypsoCommonsDebug.DEBUG.printf( "Creating directory '%s'...%", destination.getName() ); //$NON-NLS-1$
      destination.createFolder();
    }

    final FileObject[] children = source.getChildren();
    for( final FileObject child : children )
    {
      if( FileType.FILE.equals( child.getType() ) )
      {
        /* Need a destination file with the same name as the source file. */
        final FileObject destinationFile = destination.resolveFile( child.getName().getBaseName() );

        /* Copy ... */
        copyFileTo( child, destinationFile, overwrite );
      }
      else if( FileType.FOLDER.equals( child.getType() ) )
      {
        /* Need the same name for destination directory, as the source directory has. */
        final FileObject destinationDir = destination.resolveFile( child.getName().getBaseName() );

        /* Copy ... */
        KalypsoCommonsDebug.DEBUG.printf( "Copy directory %s to %s ...", child.getName(), destinationDir.getName() ); //$NON-NLS-1$
        copyDirectoryToDirectory( child, destinationDir, overwrite );
      }
      else
      {
        KalypsoCommonsDebug.DEBUG.printf( "Could not determine the file type ...%n" ); //$NON-NLS-1$
      }
    }
  }

  /**
   * Same as copyDirectoryToDirectory(source, destination, true)
   *
   * @see #copyDirectoryToDirectory(FileObject, FileObject, boolean)
   */
  public static void copyDirectoryToDirectory( final FileObject source, final FileObject destination ) throws IOException
  {
    copyDirectoryToDirectory( source, destination, false );
  }

  /**
   * This function copies a string to a vfs file object.
   *
   * @param value
   *          This string will be copied to the file.
   * @param destination
   *          The destination. It must be a file.
   */
  public static void copyStringToFileObject( final String value, final FileObject destination ) throws IOException
  {
    if( FileType.FOLDER.equals( destination.getType() ) )
      throw new IllegalArgumentException( "Destination is a folder." ); //$NON-NLS-1$

    /* Copy the string to this url. */
    OutputStream outputStream = null;
    StringReader stringReader = null;

    try
    {
      outputStream = destination.getContent().getOutputStream( false );
      stringReader = new StringReader( value );
      IOUtils.copy( stringReader, outputStream );
      outputStream.close();
      stringReader.close();
    }
    finally
    {
      IOUtils.closeQuietly( outputStream );
      IOUtils.closeQuietly( stringReader );
    }
  }

  public static FileObject createTempDirectory( final String prefix, final FileObject parentDir ) throws FileSystemException
  {
    final FileSystemManager fsManager = getManager();
    return createTempDirectory( prefix, parentDir, fsManager );
  }

  /**
   * This function creates a temporary directory, which has a unique file name.
   *
   * @param prefix
   *          This prefix will be used for the temporary directory.
   * @param parentDir
   *          The parent directory. In it the new directory will be created.
   * @return The new unique directory.
   */
  public static FileObject createTempDirectory( final String prefix, final FileObject parentDir, final FileSystemManager fsManager ) throws FileSystemException
  {
    while( true )
    {
      final String dirParent = parentDir.getURL().toExternalForm();
      final String dirName = prefix + String.valueOf( System.currentTimeMillis() );

      final FileObject newDir = fsManager.resolveFile( dirParent + "/" + dirName ); //$NON-NLS-1$
      if( newDir.exists() )
      {
        continue;
      }

      KalypsoCommonsDebug.DEBUG.printf( "Creating folder %s ...%n", newDir.getName().getPath() ); //$NON-NLS-1$
      newDir.createFolder();
      return newDir;
    }
  }

  /**
   * This function will check the string for protocol, and if neccessary applys an proxy object to it.
   *
   * @param absoluteFile
   *          The absolute file path to the file. It should be absolute, because this function was not testet against
   *          relative files.
   * @return The file object.
   */
  public static FileObject checkProxyFor( final String absoluteFile, final FileSystemManager fsManager ) throws FileSystemException
  {
    final Proxy proxy = ProxyUtilities.getProxy();
    KalypsoCommonsDebug.DEBUG.printf( "Should use proxy: %s%n", String.valueOf( proxy.useProxy() ) ); //$NON-NLS-1$

    // TODO: VFS usually accepts file-pathes (without protocoll), but creating an url here will prohibit this.
    // TODO: handle file pathes differently
    if( proxy.useProxy() && !ProxyUtilities.isNonProxyHost( absoluteFile ) )
    {
      final String proxyHost = proxy.getProxyHost();
      final int proxyPort = proxy.getProxyPort();
      KalypsoCommonsDebug.DEBUG.printf( "Proxy host: %s%n", proxyHost ); //$NON-NLS-1$
      KalypsoCommonsDebug.DEBUG.printf( "Proxy port: %s%n", String.valueOf( proxyPort ) ); //$NON-NLS-1$

      /* Get the credentials. */
      final String user = proxy.getUser();
      final String password = proxy.getPassword();

      final Pattern p = Pattern.compile( "(.+)://.+" ); //$NON-NLS-1$
      final Matcher m = p.matcher( absoluteFile );
      if( m.find() == true )
      {
        KalypsoCommonsDebug.DEBUG.printf( "File: %s%n", absoluteFile ); //$NON-NLS-1$
        KalypsoCommonsDebug.DEBUG.printf( "Protocol: %s%n", m.group( 1 ) ); //$NON-NLS-1$

        if( m.group( 1 ).equals( "webdav" ) ) //$NON-NLS-1$
        {
          WebdavFileSystemConfigBuilder.getInstance().setProxyHost( THE_WEBDAV_OPTIONS, proxyHost );
          WebdavFileSystemConfigBuilder.getInstance().setProxyPort( THE_WEBDAV_OPTIONS, proxyPort );

          /* If there are credentials given, set them. */
          if( user != null && password != null )
          {
            final UserAuthenticator authenticator = new StaticUserAuthenticator( null, user, password );
            WebdavFileSystemConfigBuilder.getInstance().setProxyAuthenticator( THE_WEBDAV_OPTIONS, authenticator );
          }

          return fsManager.resolveFile( absoluteFile, THE_WEBDAV_OPTIONS );
        }
        else if( m.group( 1 ).equals( "http" ) ) //$NON-NLS-1$
        {
          HttpFileSystemConfigBuilder.getInstance().setProxyHost( THE_HTTP_OPTIONS, proxyHost );
          HttpFileSystemConfigBuilder.getInstance().setProxyPort( THE_HTTP_OPTIONS, proxyPort );

          /* If there are credentials given, set them. */
          if( user != null && password != null )
          {
            final UserAuthenticator authenticator = new StaticUserAuthenticator( null, user, password );
            HttpFileSystemConfigBuilder.getInstance().setProxyAuthenticator( THE_HTTP_OPTIONS, authenticator );
          }

          return fsManager.resolveFile( absoluteFile, THE_HTTP_OPTIONS );
        }
        else if( m.group( 1 ).equals( "https" ) ) //$NON-NLS-1$
        {
          HttpFileSystemConfigBuilder.getInstance().setProxyHost( THE_HTTPS_OPTIONS, proxyHost );
          HttpFileSystemConfigBuilder.getInstance().setProxyPort( THE_HTTPS_OPTIONS, proxyPort );

          /* If there are credentials given, set them. */
          if( user != null && password != null )
          {
            final UserAuthenticator authenticator = new StaticUserAuthenticator( null, user, password );
            HttpFileSystemConfigBuilder.getInstance().setProxyAuthenticator( THE_HTTPS_OPTIONS, authenticator );
          }

          return fsManager.resolveFile( absoluteFile, THE_HTTPS_OPTIONS );
        }
      }
    }

    return fsManager.resolveFile( absoluteFile );
  }

  /**
   * This function will check the string for protocol, and if neccessary applys an proxy object to it.
   *
   * @param absoluteFile
   *          The absolute file path to the file. It should be absolute, because this function was not testet against
   *          relative files.
   * @return The file object.
   */
  public static FileObject checkProxyFor( final String absoluteFile ) throws FileSystemException
  {
    final FileSystemManager fsManager = getManager();
    return checkProxyFor( absoluteFile, fsManager );
  }

  /**
   * This function deletes the given file. If the file object is a directory, all content and the directory itself will
   * be deleted.
   *
   * @param toDel
   *          The file or directory to be deleted.
   * @return The number of deleted files. 0, if none has been deleted.
   */
  public static int deleteFiles( final FileObject toDel ) throws FileSystemException
  {
    if( FileType.FOLDER.equals( toDel.getType() ) )
    {
      /* Delete the directory. */
      KalypsoCommonsDebug.DEBUG.printf( "Deleting the directory %s ...%n", toDel.getName() ); //$NON-NLS-1$
      return toDel.delete( new AllFileSelector() );
    }
    else if( FileType.FILE.equals( toDel.getType() ) )
    {
      /* Delete the file. */
      KalypsoCommonsDebug.DEBUG.printf( "Deleting the file %s ...%n", toDel.getName() ); //$NON-NLS-1$
      if( toDel.delete() )
        return 1;

      KalypsoCommonsDebug.DEBUG.printf( "Could not delete %s!%n", toDel.getName() ); //$NON-NLS-1$

      return 0;
    }
    else
    {
      /* The type of the file could not be determined, or it is an imaginary one. */
      KalypsoCommonsDebug.DEBUG.printf( "Could not delete %s!%n", toDel.getName() ); //$NON-NLS-1$

      return 0;
    }
  }

  /**
   * bad hack - set a custom file system manager to ensure custom file system settings of the given file system manager
   */
  public static void setCustomFileSystemManager( final IFileSystemManagerResolveDelegate delegate )
  {
    FILE_SYSTEM_MANAGER_DELEGATE = delegate;
  }

  /**
   * This function closes the file object. It does not throw any exceptions. It calls {@link FileObject#close()} of the
   * given file objects.
   *
   * @param files
   *          The file objects which should be closed. May be null or already closed.
   */
  public static void closeQuietly( final FileObject... files )
  {
    for( final FileObject file : files )
    {
      try
      {
        if( Objects.isNotNull( file ) )
        {
          /* Close the file object. */
          file.close();

          /* Close the connection of the file system (e.g. a FTP connection). */
          final FileSystem fileSystem = file.getFileSystem();
          if( fileSystem instanceof AbstractFileSystem )
            ((AbstractFileSystem) fileSystem).closeCommunicationLink();
        }
      }
      catch( final FileSystemException ignore )
      {
        /* If a file system exception is thrown, it was probably already closed. */
      }
      catch( final Exception ex )
      {
        /* On other exceptions, do tell the developer on the console. */
        ex.printStackTrace();
      }
    }
  }

  /**
   * Moves the complete content of one directory into another.
   *
   * @throws IOException
   *           If the move failed.
   */
  public static void moveContents( final File sourceDir, final File dest ) throws IOException
  {
    final FileSystemManager vfsManager = VFSUtilities.getManager();
    final FileObject source = vfsManager.toFileObject( sourceDir );
    final FileObject destDir = vfsManager.toFileObject( dest );

    final FileObject[] findFiles = source.findFiles( new AllFileSelector() );
    // Might happen, if source does not exists... shouldn't we check this?
    if( findFiles == null )
      return;

    for( final FileObject fileObject : findFiles )
    {
      if( FileType.FILE.equals( fileObject.getType() ) )
      {
        final String relPath = source.getName().getRelativeName( fileObject.getName() );
        final FileObject destFile = destDir.resolveFile( relPath, NameScope.DESCENDENT_OR_SELF );
        final FileObject folder = destFile.getParent();
        folder.createFolder();
        fileObject.moveTo( destFile );
      }
    }
  }

  /**
   * resolves the input stream from given {@link FileObject} based on the file extention, known types are gz and zip, in
   * case of zip archiv the first file will be taken.
   */
  public static InputStream getInputStreamFromFileObject( final FileObject file ) throws FileSystemException, IOException, URISyntaxException
  {
    /* open stream */
    if( "gz".equalsIgnoreCase( file.getName().getExtension() ) )//$NON-NLS-1$
      return new GZIPInputStream( new BufferedInputStream( file.getContent().getInputStream() ) );

    if( "zip".equalsIgnoreCase( file.getName().getExtension() ) )//$NON-NLS-1$
      return ZipUtilities.getInputStreamForFirstFile( file.getURL() );

    return new BufferedInputStream( file.getContent().getInputStream() );
  }

  /**
   * Compress the given by sourceFileURL {@link URL} file into given by outputDirURL directory with compression
   * specified by compressKind. Supported compression is "gz", with null or empty string provided as compressKind the
   * file will be just copied.
   */
  public static FileObject compressFileContent( final URL sourceFileURL, final URL outputDirURL, final String compressKind )
  {
    return proceedFileCompressOperation( sourceFileURL, outputDirURL, compressKind, true );
  }

  /**
   * uncompress(unzip ungzip) or/and compress using by @param compressKind given type of compression the source
   * {@link URL} to the output {@link URL}
   */
  private static FileObject proceedFileCompressOperation( final URL sourceFileURL, final URL outputDirURL, final String compressKind, final boolean doCompress )
  {
    if( sourceFileURL == null || outputDirURL == null )
      return null;

    OutputStream outStream = null;
    InputStream inStream = null;
    String lComressKind = compressKind;
    if( lComressKind == null || !doCompress )
    {
      lComressKind = ""; //$NON-NLS-1$
    }
    try
    {
      final FileSystemManagerWrapper vfsManager = VFSUtilities.getNewManager();
      final FileObject fileObjectIn = vfsManager.resolveFile( sourceFileURL.toExternalForm() );
      final FileObject fileObjectOut = vfsManager.resolveFile( outputDirURL.toExternalForm() );
      inStream = getInputStreamFromFileObject( fileObjectIn );

      final File sourceFile = new File( sourceFileURL.toURI() );
      final String sourceFileName = sourceFile.getName();
      final boolean sourceIsKnownArchiv = sourceFileName.toLowerCase().endsWith( ".gz" ) || sourceFileName.endsWith( ".zip" ); //$NON-NLS-1$  //$NON-NLS-2$
      final String sourceFileNameWithoutExt = sourceFileName.substring( 0, sourceIsKnownArchiv ? sourceFileName.lastIndexOf( "." ) : sourceFileName.length() ); //$NON-NLS-1$
      final File outputFile = new File( outputDirURL.getPath(), sourceFileNameWithoutExt + ("".equals( lComressKind ) ? "" : ".") + lComressKind ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      if( doCompress && "gz".equalsIgnoreCase( lComressKind ) ) { //$NON-NLS-1$
        outStream = new GZIPOutputStream( new FileOutputStream( outputFile ) );
      }
      else if( doCompress && "zip".equalsIgnoreCase( lComressKind ) ) { //$NON-NLS-1$
        outStream = new ZipOutputStream( new FileOutputStream( outputFile ) );
        final ZipEntry newEntry = new ZipEntry( sourceFileName );
        ((ZipOutputStream) outStream).putNextEntry( newEntry );
      }
      else
      {
        outStream = new BufferedOutputStream( new FileOutputStream( outputFile ) );
      }
      IOUtils.copy( inStream, outStream );
      return vfsManager.resolveFile( fileObjectOut, outputFile.getName() );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
    finally
    {
      IOUtils.closeQuietly( inStream );
      IOUtils.closeQuietly( outStream );
    }
    return null;

  }

  /**
   * uncompress the given by sourceFileURL {@link URL} file into given by outputDirURL {@link URL} directory supported
   * compression are: "gz". If the compressKind parametr is set, the content will be recompress according to this
   * parameter after uncompressing.
   */
  public static FileObject uncompressFileContent( final URL sourceFileURL, final URL outputDirURL, final String compressKind )
  {
    if( compressKind == null || "".equals( compressKind ) ) //$NON-NLS-1$
      return proceedFileCompressOperation( sourceFileURL, outputDirURL, compressKind, false );

    return proceedFileCompressOperation( sourceFileURL, outputDirURL, compressKind, true );
  }
}