/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.service.calcjobs.sendmail;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.kalypso.service.calcjobs.sendmail.utils.MailUtilities;
import org.kalypso.service.calcjobs.sendmail.utils.SendMailAuthenticator;
import org.kalypso.service.wps.i18n.Messages;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.ISimulationDataProvider;
import org.kalypso.simulation.core.ISimulationMonitor;
import org.kalypso.simulation.core.ISimulationResultEater;
import org.kalypso.simulation.core.SimulationException;

/**
 * This is a small job that accepts data from a client and sends it via mail.
 * 
 * @author Holger Albert
 */
@SuppressWarnings("restriction")
public class SendMailJob implements ISimulation
{
  /**
   * The model specification.
   */
  private static final String SIMULATION_SPEC = "mail_specification.xml"; //$NON-NLS-1$

  private static final String INPUT_SENDER = "SENDER"; //$NON-NLS-1$

  private static final String INPUT_RECEIVER = "RECEIVER"; //$NON-NLS-1$

  private static final String INPUT_TYPE = "TYPE"; //$NON-NLS-1$

  private static final String INPUT_TITLE = "TITLE"; //$NON-NLS-1$

  private static final String INPUT_TEXT = "TEXT"; //$NON-NLS-1$

  private static final String OUTPUT_RESULT = "RESULT"; //$NON-NLS-1$

  private static final String CFG_RELAIS = "org.kalypso.wps.sendmail.relais"; //$NON-NLS-1$

  private static final String CFG_RELAIS_USERNAME = "org.kalypso.wps.sendmail.relais.username"; //$NON-NLS-1$

  private static final String CFG_RELAIS_PASSWORD = "org.kalypso.wps.sendmail.relais.password"; //$NON-NLS-1$

  /**
   * The constructor.
   */
  public SendMailJob( )
  {
  }

  /**
   * @see org.kalypso.simulation.core.ISimulation#getSpezifikation()
   */
  public URL getSpezifikation( )
  {
    return getClass().getResource( SIMULATION_SPEC );
  }

  /**
   * @see org.kalypso.simulation.core.ISimulation#run(java.io.File, org.kalypso.simulation.core.ISimulationDataProvider,
   *      org.kalypso.simulation.core.ISimulationResultEater, org.kalypso.simulation.core.ISimulationMonitor)
   */
  public void run( File tmpdir, ISimulationDataProvider inputProvider, ISimulationResultEater resultEater, ISimulationMonitor monitor )
  {
    /* Update monitor. */
    monitor.setMessage( Messages.getString("org.kalypso.service.calcjobs.sendmail.SendMailJob.0") ); //$NON-NLS-1$

    try
    {
      /* Get all neccessary inputs. */
      String sender = (String) inputProvider.getInputForID( INPUT_SENDER );
      String receiver = (String) inputProvider.getInputForID( INPUT_RECEIVER );
      String type = (String) inputProvider.getInputForID( INPUT_TYPE );
      String title = (String) inputProvider.getInputForID( INPUT_TITLE );
      String text = (String) inputProvider.getInputForID( INPUT_TEXT );

      /* Check all neccessary inputs. */
      if( sender == null || sender.length() == 0 )
        throw new SimulationException( Messages.getString("org.kalypso.service.calcjobs.sendmail.SendMailJob.1") + INPUT_SENDER + "' property is not correct configured ...", null ); //$NON-NLS-1$ //$NON-NLS-2$

      if( receiver == null || receiver.length() == 0 )
        throw new SimulationException( Messages.getString("org.kalypso.service.calcjobs.sendmail.SendMailJob.1") + INPUT_RECEIVER + "' property is not correct configured ...", null ); //$NON-NLS-1$ //$NON-NLS-2$

      if( type == null || type.length() == 0 )
        throw new SimulationException( Messages.getString("org.kalypso.service.calcjobs.sendmail.SendMailJob.1") + INPUT_TYPE + "' property is not correct configured ...", null ); //$NON-NLS-1$ //$NON-NLS-2$

      if( title == null || title.length() == 0 )
        throw new SimulationException( Messages.getString("org.kalypso.service.calcjobs.sendmail.SendMailJob.1") + INPUT_TITLE + "' property is not correct configured ...", null ); //$NON-NLS-1$ //$NON-NLS-2$

      if( text == null || text.length() == 0 )
        throw new SimulationException( Messages.getString("org.kalypso.service.calcjobs.sendmail.SendMailJob.1") + INPUT_TEXT + "' property is not correct configured ...", null ); //$NON-NLS-1$ //$NON-NLS-2$

      if( !MailUtilities.checkContentType( type ) )
        throw new SimulationException( Messages.getString("org.kalypso.service.calcjobs.sendmail.SendMailJob.2") + type + "' is not supported, try '" + MailUtilities.TEXT_PLAIN + "'  ...", null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

      /* Configuration inputs. */
      String relais = FrameworkProperties.getProperty( CFG_RELAIS );
      if( relais == null || relais.length() == 0 )
        throw new SimulationException( Messages.getString("org.kalypso.service.calcjobs.sendmail.SendMailJob.3") + CFG_RELAIS + "' directive must be set on the server ...", null ); //$NON-NLS-1$ //$NON-NLS-2$

      /* Configuration, which is optional. */
      String relais_username = FrameworkProperties.getProperty( CFG_RELAIS_USERNAME );
      String relais_password = FrameworkProperties.getProperty( CFG_RELAIS_PASSWORD );

      /* Update monitor. */
      monitor.setProgress( 25 );
      monitor.setMessage( Messages.getString("org.kalypso.service.calcjobs.sendmail.SendMailJob.4") ); //$NON-NLS-1$

      /* Create the properties for the session configuration. */
      Properties properties = new Properties();

      /*
       * Specifies the default Message Access Protocol. The Session.getStore() method returns a Store object that
       * implements this protocol. The client can override this property and explicitly specify the protocol with the
       * Session.getStore(String protocol) method.
       */
      properties.put( "mail.store.protocol", "imap" ); //$NON-NLS-1$ //$NON-NLS-2$

      /*
       * Specifies the default Transport Protocol. The Session.getTransport() method returns a Transport object that
       * implements this protocol. The client can override this property and explicitly specify the protocol by using
       * Session.getTransport(String protocol) method.
       */
      properties.put( "mail.transport.protocol", "smtp" ); //$NON-NLS-1$ //$NON-NLS-2$

      /*
       * Specifies the default Mail server. The Store and Transport object’s connect methods use this property, if the
       * protocol-specific host property is absent, to locate the target host.
       */
      properties.put( "mail.host", relais ); //$NON-NLS-1$

      /*
       * Specifies the username to provide when connecting to a Mail server. The Store and Transport object’s connect
       * methods use this property, if the protocol-specific username property is absent, to obtain the username.
       */
      if( relais_username != null )
        properties.put( "mail.user", relais_username ); //$NON-NLS-1$

      /* Specifies the protocol-specific default Mail server. This overrides the mail.host property. */
      properties.put( "mail.smtp.host", relais ); //$NON-NLS-1$

      /*
       * Specifies the protocol-specific default username for connecting to the Mail server. This overrides the
       * mail.user property.
       */
      if( relais_username != null )
        properties.put( "mail.smtp.user", relais_username ); //$NON-NLS-1$

      /*
       * Specifies the return address of the current user. Used by the InternetAddress.getLocalAddress method to specify
       * the current user’s email address.
       */
      properties.put( "mail.from", sender ); //$NON-NLS-1$

      /*
       * Specifies the initial debug mode. Setting this property to true will turn on debug mode, while setting it to
       * false turns debug mode off.
       */
      properties.put( "mail.debug", false ); //$NON-NLS-1$

      /* Need an authenticator eventually. */
      Authenticator auth = null;
      if( (relais_username != null && relais_username.length() > 0) && (relais_password != null && relais_password.length() > 0) )
        auth = new SendMailAuthenticator( relais_username, relais_password );

      /* Create the settings. */
      Session session = Session.getInstance( properties, auth );

      /* A tokenizer for splitting the string into all contained e-mail addresses. */
      StringTokenizer tokenizer = new StringTokenizer( receiver, ";" ); //$NON-NLS-1$

      /* The from address. */
      Address fromAddr = new InternetAddress( sender );

      /* All to addresses. */
      List<Address> toAddrs = new LinkedList<Address>();
      while( tokenizer.hasMoreElements() )
        toAddrs.add( new InternetAddress( tokenizer.nextToken() ) );

      /* Update monitor. */
      monitor.setProgress( 50 );
      monitor.setMessage( Messages.getString("org.kalypso.service.calcjobs.sendmail.SendMailJob.5") ); //$NON-NLS-1$

      /* Create the message. */
      MimeMessage message = new MimeMessage( session );
      message.setFrom( fromAddr );
      message.setRecipients( Message.RecipientType.TO, toAddrs.toArray( new Address[] {} ) );
      message.setSubject( title );
      message.setSentDate( new Date() );

      /* Set the content and its type. Typically a text and 'text/plain'. */
      message.setContent( text, type );

      /* Sets the header. */
      // message.setHeader( "X-Mailer", "WPSSendMailV1.0" );
      /* Update monitor. */
      monitor.setProgress( 75 );
      monitor.setMessage( Messages.getString("org.kalypso.service.calcjobs.sendmail.SendMailJob.6") ); //$NON-NLS-1$

      /* Sends the message. */
      Transport.send( message );

      /* Update monitor. */
      monitor.setProgress( 100 );
      monitor.setMessage( Messages.getString("org.kalypso.service.calcjobs.sendmail.SendMailJob.7") ); //$NON-NLS-1$

      /* Add the result and end the simulation. */
      resultEater.addResult( OUTPUT_RESULT, Messages.getString("org.kalypso.service.calcjobs.sendmail.SendMailJob.8") ); //$NON-NLS-1$

      /* Finish the job. */
      monitor.setMessage( Messages.getString("org.kalypso.service.calcjobs.sendmail.SendMailJob.9") ); //$NON-NLS-1$
      monitor.setFinishInfo( IStatus.OK, Messages.getString("org.kalypso.service.calcjobs.sendmail.SendMailJob.10") ); //$NON-NLS-1$
    }
    catch( Exception ex )
    {
      monitor.setMessage( Messages.getString("org.kalypso.service.calcjobs.sendmail.SendMailJob.11") + ex.getMessage() ); //$NON-NLS-1$
      monitor.setFinishInfo( IStatus.ERROR, Messages.getString("org.kalypso.service.calcjobs.sendmail.SendMailJob.12") + ex.getMessage() ); //$NON-NLS-1$
    }
  }
}