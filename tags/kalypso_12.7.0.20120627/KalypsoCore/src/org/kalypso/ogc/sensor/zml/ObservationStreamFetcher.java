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
package org.kalypso.ogc.sensor.zml;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.xml.sax.InputSource;

/**
 * @author Gernot Belger
 */
public final class ObservationStreamFetcher
{
  private ObservationStreamFetcher( )
  {
  }

  public static IObservation loadObservation( final URL url ) throws SensorException
  {
    InputStream inputStream = null;

    try
    {
      final InputStream zmlStream = openZmlStream( url );

      inputStream = new BufferedInputStream( zmlStream );

      // url is given as an argument here (and not tmpUrl) in order not to
      // loose the query part we might have removed because of Eclipse's
      // url handling.

      final ObservationUnmarshaller unmarshaller = new ObservationUnmarshaller( new InputSource( inputStream ), url );
      final IStatus status = unmarshaller.execute( new NullProgressMonitor() );
      if( status.getSeverity() == IStatus.ERROR )
        throw new SensorException( Messages.getString("ObservationStreamFetcher.0"), new CoreException( status ) ); //$NON-NLS-1$

      return unmarshaller.getObservation();
    }
    catch( final IOException e )
    {
      throw new SensorException( Messages.getString( "org.kalypso.ogc.sensor.zml.ZmlFactory.5" ) + url.toExternalForm(), e ); //$NON-NLS-1$
    }
    finally
    {
      IOUtils.closeQuietly( inputStream );
    }
  }

  private static InputStream openZmlStream( final URL url ) throws IOException
  {
    final InputStream inputStream = openZmlBaseStream( url );

    /* Handle compressed data */
    final String file = url.getFile();
    if( file.toLowerCase().endsWith( ".zmlz" ) ) //$NON-NLS-1$
      return new GZIPInputStream( inputStream );

    return inputStream;
  }

  private static InputStream openZmlBaseStream( final URL url ) throws IOException
  {
    final String protocol = url.getProtocol();
    if( protocol.startsWith( "file" ) || protocol.startsWith( "platform" ) || protocol.startsWith( "jar" ) || protocol.startsWith( "bundleresource" ) ) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    {
      final String identifierPart = ZmlURL.getIdentifierPart( url );
      /*
       * if this is a local url, we remove the query part because Eclipse Platform's URLStreamHandler cannot deal with
       * it.
       */

      // only take the simple part of the url
      final URL tmpUrl = new URL( identifierPart );

      return tmpUrl.openStream();
    }
    else
    {
      // default behaviour (might use a specific stream handler like
      // the OCSUrlStreamHandler )
      return url.openStream();
    }
  }
}