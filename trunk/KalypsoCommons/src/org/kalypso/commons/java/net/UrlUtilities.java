/*--------------- Kalypso-Header ------------------------------------------

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

 --------------------------------------------------------------------------*/

package org.kalypso.commons.java.net;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.java.net.IUrlResolver;
import org.kalypso.contribs.java.net.UrlResolverSingleton;

/**
 * @author belger
 */
public class UrlUtilities
{
  private UrlUtilities( )
  {
    // will not get instantiated
  }

  /**
   * Opens a stream on the given url and copies its content into a string.
   * 
   * @throws IOException
   */
  public static String toString( final URL url, final String encoding ) throws IOException
  {
    InputStream is = null;
    try
    {
      is = url.openStream();
      final String content = IOUtils.toString( is, encoding );
      is.close();
      return content;
    }
    finally
    {
      IOUtils.closeQuietly( is );
    }
  }

  /**
   * Opens a stream on the given url and copies its content into a byte array.
   * 
   * @throws IOException
   */
  public static byte[] toByteArray( final URL url ) throws IOException
  {
    InputStream is = null;
    try
    {
      is = url.openStream();
      final byte[] content = IOUtils.toByteArray( is );
      is.close();
      return content;
    }
    finally
    {
      IOUtils.closeQuietly( is );
    }
  }

  public static URL resolveWithZip( final URL context, final String source ) throws MalformedURLException
  {
    return resolveWithZip( context, source, UrlResolverSingleton.getDefault() );
  }

  public static URL resolveWithZip( final URL context, final String source, final IUrlResolver resolver ) throws MalformedURLException
  {
    // HACK
    // TODO comment
    final int indexOf = source.indexOf( "!" ); //$NON-NLS-1$
    if( indexOf == -1 )
      return resolver.resolveURL( context, source );

    final String zipPath = source.substring( 0, indexOf );
    final String refInZip = source.substring( indexOf );

    final URL zipUrl = resolver.resolveURL( context, zipPath );

    final URL jarUrl = new URL( "jar:" + zipUrl.toExternalForm() + refInZip ); //$NON-NLS-1$

    return jarUrl;
  }

  /**
   * Find content encoding for the given connection. If the connection denotes a platform-url, we fetch the encoding
   * from the underlying resource, as the connection will not return a valid charset.
   */
  public static String findEncoding( final URLConnection connection )
  {
    final IFile file = ResourceUtilities.findFileFromURL( connection.getURL() );
    if( file == null )
      return connection.getContentEncoding();

    try
    {
      return file.getCharset();
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      return connection.getContentEncoding();
    }
  }

  /**
   * Checks if the contents of a url can be accessed.<br/>
   * 
   * @param location
   *          The url to check.
   * @return <code>true</code>, if a stream on the location can pe opened.
   */
  public static boolean checkIsAccessible( final URL location )
  {
    InputStream is = null;
    try
    {
      is = location.openStream();
      return true;
    }
    catch( final IOException e )
    {
      return false;
    }
    finally
    {
      IOUtils.closeQuietly( is );
    }
  }

  /**
   * Tires to find a 'lastModified' timestamp from an {@link URL}.
   */
  public static Date lastModified( final URL location )
  {
    if( location == null )
      return null;

    try
    {
      final URLConnection connection = location.openConnection();
      connection.connect();

      final long lastModified = connection.getLastModified();
      // BUGFIX: some URLConnection implementations (such as eclipse resource-protokoll)
      // do not return lastModified correctly. If we have such a case, we try some more...
      if( lastModified != 0 )
        return new Date( lastModified );

      final File file = FileUtils.toFile( location );
      if( file != null )
        return new Date( file.lastModified() );

      final IPath path = ResourceUtilities.findPathFromURL( location );
      if( path == null )
        return null;

      final File resourceFile = ResourceUtilities.makeFileFromPath( path );
      return new Date( resourceFile.lastModified() );
    }
    catch( final IOException e )
    {
      // ignore, some resources cannot be checked at all
    }

    return null;
  }
}
