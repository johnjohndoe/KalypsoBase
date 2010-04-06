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
package org.kalypso.ftp.service;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.commons.bind.JaxbUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ftp.service.ftplet.KalypsoFtplet;
import org.kalypso.ftp.service.schema.FtpServiceType;
import org.kalypso.ftp.service.schema.ObjectFactory;

/**
 * @author Dirk Kuch
 */
public final class KalypsoFtpFactory
{
  private static KalypsoFtpFactory INSTANCE = null;

  private FtpServer m_server;

  private final Boolean m_enabeld;

  private KalypsoFtpFactory( )
  {
    m_enabeld = Boolean.valueOf( System.getProperty( IKalypsoFtpServiceConstants.FTP_SERVICE_ENABLE, "false" ) );
  }

  public static KalypsoFtpFactory getInstance( )
  {
    if( INSTANCE != null )
      return INSTANCE;

    INSTANCE = new KalypsoFtpFactory();

    return INSTANCE;
  }

  public void start( ) throws CoreException
  {
    if( !m_enabeld )
      return;

    try
    {
      final JAXBContext context = JaxbUtilities.createQuiet( ObjectFactory.class );
      final Unmarshaller unmarshaller = context.createUnmarshaller();
      final URL xmlConfigUrl = new URL( System.getProperty( IKalypsoFtpServiceConstants.FTP_CONFIGURATION_XML ) );
      final JAXBElement<FtpServiceType> element = (JAXBElement<FtpServiceType>) unmarshaller.unmarshal( xmlConfigUrl );
      final FtpServiceType service = element.getValue();

      final FtpServerFactory serverFactory = new FtpServerFactory();

      /* adding the default kalypso ftplet (not needed at the moment */
      final Map<String, Ftplet> ftplets = new HashMap<String, Ftplet>();
      ftplets.put( KalypsoFtplet.class.getName(), new KalypsoFtplet() );
      serverFactory.setFtplets( ftplets );

      /* user management */
      final PropertiesUserManagerFactory userMangerFactory = new PropertiesUserManagerFactory();
      userMangerFactory.setUrl( new URL( service.getUserFile() ) );
      serverFactory.setUserManager( userMangerFactory.createUserManager() );

      final ListenerFactory factory = new ListenerFactory();
      factory.setPort( service.getPort().intValue() );

      // replace the default listener
      serverFactory.addListener( "default", factory.createListener() );

      m_server = serverFactory.createServer();
      m_server.start();
    }
    catch( final Exception e )
    {
      throw new CoreException( StatusUtilities.createErrorStatus( "Starting of the ftp server failed.", e ) );
    }
  }

  public void stop( )
  {
    if( m_server == null )
      return;

    m_server.stop();
  }
}
