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
package org.kalypso.services.observation.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.request.RequestFactory;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.services.observation.KalypsoServiceObs;
import org.kalypso.services.observation.ObservationServiceUtils;
import org.kalypso.services.observation.i18n.Messages;
import org.kalypso.services.observation.sei.DataBean;
import org.kalypso.services.observation.sei.IObservationService;
import org.osgi.service.url.AbstractURLStreamHandlerService;

/**
 * Observation Collection Service URL Stream Handler.
 * <p>
 * It extends the <code>AbstractURLStreamHandlerService</code> of the OSGI-Platform in order to be registered as a
 * URLStreamHandler since Eclipse manages the handlers this way.
 * 
 * @author schlienger
 */
public class OcsURLStreamHandler extends AbstractURLStreamHandlerService
{
  private final Logger m_logger = Logger.getLogger( getClass().getName() );

  @Override
  public final URLConnection openConnection( final URL u ) throws IOException
  {
    /**
     * bad @hack to implement proxy handling of time series - called from ant launch
     */
    try
    {
      return openOcsConnection( u );
    }
    catch( final Exception ex )
    {
      throw new IOException( "Resolving url connection failed", ex ); //$NON-NLS-1$
    }

  }

  private URLConnection openOcsConnection( final URL u ) throws IOException
  {
    final String href = u.toExternalForm();

    m_logger.info( Messages.getString( "org.kalypso.services.observation.client.OcsURLStreamHandler.0" ) + href ); //$NON-NLS-1$

    // check if an empty id is provided, in that case use the request if provided
    if( org.kalypso.ogc.sensor.zml.ZmlURL.isEmpty( href ) )
    {
      try
      {
        m_logger.warning( Messages.getString( "org.kalypso.services.observation.client.OcsURLStreamHandler.1" ) ); //$NON-NLS-1$

        return tryWithRequest( href, null );
      }
      catch( final Exception e )
      {
        m_logger.warning( Messages.getString( "org.kalypso.services.observation.client.OcsURLStreamHandler.2" ) + e.getLocalizedMessage() ); //$NON-NLS-1$

        throw new IOException( Messages.getString( "org.kalypso.services.observation.client.OcsURLStreamHandler.3" ) + e.getLocalizedMessage() ); //$NON-NLS-1$
      }
    }

    InputStream ins = null;
    File file = null;

    try
    {
      // use the default url connection if this is not a kalypso server-side one
      if( !ObservationServiceUtils.isServerSide( href ) )
        return u.openConnection();

      // else fetch the observation from the server
      final IObservationService srv = KalypsoServiceObs.getDefault().getDefaultObservationService();

      final DataBean data = srv.readData( href );

      // create a local temp file for storing the zml
      file = FileUtilities.createNewUniqueFile( "zml", FileUtilities.TMP_DIR ); //$NON-NLS-1$
      file.deleteOnExit();
      // TODO: dirty.... the stream to file never gets closed

      ins = data.getDataHandler().getInputStream();
      FileUtilities.makeFileFromStream( false, file, ins );
      ins.close();

      srv.clearTempData( data.getId() );

      return file.toURI().toURL().openConnection();
    }
    catch( final Throwable e ) // generic exception caught for simplicity
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e, Messages.getString( "org.kalypso.services.observation.client.OcsURLStreamHandler.4" ) + href ); //$NON-NLS-1$
      KalypsoServiceObs.getDefault().getLog().log( status );

      try
      {
        m_logger.warning( Messages.getString( "org.kalypso.services.observation.client.OcsURLStreamHandler.5" ) ); //$NON-NLS-1$

        return tryWithRequest( href, file );
      }
      catch( final Exception se )
      {
        m_logger.warning( Messages.getString( "org.kalypso.services.observation.client.OcsURLStreamHandler.6" ) ); //$NON-NLS-1$
        final IStatus status2 = StatusUtilities.statusFromThrowable( se, Messages.getString( "org.kalypso.services.observation.client.OcsURLStreamHandler.7", href ) ); //$NON-NLS-1$
        KalypsoServiceObs.getDefault().getLog().log( status2 );
      }

      throw new IOException( Messages.getString( "org.kalypso.services.observation.client.OcsURLStreamHandler.8", e.getLocalizedMessage() ) ); //$NON-NLS-1$
    }
    finally
    {
      IOUtils.closeQuietly( ins );
    }
  }

  /**
   * Helper that tries to load the observation from its optional request
   * 
   * @param file
   *          temp file where to store the observation locally. Can be null, in that case a temp file is created
   */
  private URLConnection tryWithRequest( final String href, File file ) throws SensorException, IOException
  {
    // create a local temp file for storing the zml if not provided
    if( file == null )
    {
      file = FileUtilities.createNewUniqueFile( "zml", FileUtilities.TMP_DIR ); //$NON-NLS-1$
      file.deleteOnExit();
    }

    // we might be here because the server is down. If the href contains
    // a request, let create a default observation according to it.
    final IObservation obs = RequestFactory.createDefaultObservation( href );

    m_logger.info( Messages.getString( "org.kalypso.services.observation.client.OcsURLStreamHandler.9", obs.getName() ) ); //$NON-NLS-1$ 

    ZmlFactory.writeToFile( obs, file );

    return file.toURI().toURL().openConnection();
  }
}