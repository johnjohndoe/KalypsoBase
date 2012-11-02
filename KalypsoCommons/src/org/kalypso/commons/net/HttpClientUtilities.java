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
package org.kalypso.commons.net;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.KalypsoCommonsPlugin;
import org.kalypso.commons.internal.i18n.Messages;

/**
 * This class contains functions for dealing with the http client.
 * 
 * @author Holger Albert
 */
public class HttpClientUtilities
{
  /**
   * The constructor.
   */
  private HttpClientUtilities( )
  {
  }

  /**
   * This function asks a server for a file, downloads it and copies it to the given file.
   * 
   * @param sourceUrl
   *          The URL of the file, which should be downloaded.
   * @param targetFile
   *          The target file.
   */
  public static void requestFileFromServer( final URL sourceUrl, final File targetFile ) throws CoreException
  {
    /* The streams. */
    InputStream is = null;
    OutputStream os = null;

    try
    {
      /* Create the client. */
      final HttpClient httpClient = ProxyUtilities.getConfiguredHttpClient( 10000, sourceUrl, 0 );

      /* Build the method. */
      final GetMethod method = new GetMethod( sourceUrl.toString() );
      method.setDoAuthentication( true );

      /* Execute the method. */
      final int statusCode = httpClient.executeMethod( method );
      if( statusCode != 200 )
      {
        final String msg = Messages.getString( "org.kalypso.commons.net.HttpClientUtilities.0", statusCode ); //$NON-NLS-1$
        final Status error = new Status( IStatus.ERROR, KalypsoCommonsPlugin.getID(), msg );
        throw new CoreException( error );
      }

      /* Get the response. */
      is = method.getResponseBodyAsStream();
      if( is == null )
      {
        final String msg = Messages.getString( "org.kalypso.commons.net.HttpClientUtilities.1" ); //$NON-NLS-1$
        final Status error = new Status( IStatus.ERROR, KalypsoCommonsPlugin.getID(), msg );
        throw new CoreException( error );
      }

      /* Create the output stream. */
      os = new BufferedOutputStream( new FileOutputStream( targetFile ) );

      /* Copy the stream to the target file. */
      IOUtils.copy( is, os );
    }
    catch( final IOException e )
    {
      final String msg = String.format( Messages.getString("HttpClientUtilities.0"), sourceUrl ); //$NON-NLS-1$
      final Status error = new Status( IStatus.ERROR, KalypsoCommonsPlugin.getID(), msg, e );
      throw new CoreException( error );
    }
    finally
    {
      /* Close the streams. */
      IOUtils.closeQuietly( is );
      IOUtils.closeQuietly( os );
    }
  }

  /**
   * This function is sends a get request.
   * 
   * @param url
   *          The address of the server.
   * @return The response as string.
   */
  public static HttpResponse sendGet( final String url ) throws HttpException, IOException
  {
    /* The input stream. */
    InputStream is = null;

    try
    {
      /* Create the client. */
      final HttpClient client = ProxyUtilities.getConfiguredHttpClient( 10000, new URL( url ), 0 );

      /* Build the method. */
      final GetMethod get = new GetMethod( url );
      get.setDoAuthentication( true );

      /* Execute the method. */
      final int status = client.executeMethod( get );
      if( status != 200 )
        return new HttpResponse( get.getStatusLine(), null, null );

      /* Get the response. */
      is = get.getResponseBodyAsStream();
      if( is == null )
        return new HttpResponse( get.getStatusLine(), null, null );

      /* Get the content type. */
      final Header contentType = get.getResponseHeader( "Content-Type" ); //$NON-NLS-1$

      /* Parse the content type, if available. */
      String mimeType = null;
      String encoding = null;
      if( contentType != null )
      {
        final String contentTypeValue = contentType.getValue();
        final String[] contentTypeSplit = contentTypeValue.split( ";" ); //$NON-NLS-1$
        mimeType = contentTypeSplit[0].trim();
        if( contentTypeSplit.length > 1 )
          encoding = contentTypeSplit[1].trim();
      }

      /* Read the response. */
      final String response = IOUtils.toString( is, encoding );

      return new HttpResponse( get.getStatusLine(), mimeType, response );
    }
    finally
    {
      /* Close the input stream. */
      IOUtils.closeQuietly( is );
    }
  }
}