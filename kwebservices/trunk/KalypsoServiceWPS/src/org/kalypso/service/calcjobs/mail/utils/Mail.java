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
package org.kalypso.service.calcjobs.mail.utils;

/**
 * This is an e-mail data container.
 * 
 * @author Holger Albert
 */
public class Mail implements IMail
{
  /**
   * The e-mail adress of the sender.
   */
  private String m_sender;

  /**
   * The e-mail adress of the receiver.
   */
  private String m_receiver;

  /**
   * The SMTP server, to which the mails will be send.
   */
  private String m_relais;

  /**
   * The username of the account for the SMTP server.
   */
  private String m_username;

  /**
   * The password of the account for the SMTP server.
   */
  private String m_password;

  /**
   * The type of the e-mail.
   */
  private String m_type;

  /**
   * The title of the e-mail.
   */
  private String m_title;

  /**
   * The text body of the e-mail.
   */
  private String m_text;

  /**
   * The constructor.
   * 
   * @param sender
   *            The e-mail adress of the sender.
   * @param receiver
   *            The e-mail adress of the receiver.
   * @param relais
   *            The SMTP server, to which the mails will be send.
   * @param username
   *            The username of the account for the SMTP server.
   * @param password
   *            The password of the account for the SMTP server.
   * @param type
   *            The type of the e-mail.
   * @param title
   *            The title of the e-mail.
   * @param text
   *            The text body of the e-mail.
   */
  public Mail( String sender, String to, String receiver, String username, String password, String type, String title, String text )
  {
    m_sender = sender;
    m_receiver = to;
    m_relais = receiver;
    m_username = username;
    m_password = password;
    m_type = type;
    m_title = title;
    m_text = text;
  }

  /**
   * @see org.kalypso.service.calcjobs.mail.utils.IMail#getFrom()
   */
  public String getSender( )
  {
    return m_sender;
  }

  /**
   * @see org.kalypso.service.calcjobs.mail.utils.IMail#getTo()
   */
  public String getReceiver( )
  {
    return m_receiver;
  }

  /**
   * @see org.kalypso.service.calcjobs.mail.utils.IMail#getRelais()
   */
  public String getRelais( )
  {
    return m_relais;
  }

  /**
   * @see org.kalypso.service.calcjobs.mail.utils.IMail#getUserName()
   */
  public String getUserName( )
  {
    return m_username;
  }

  /**
   * @see org.kalypso.service.calcjobs.mail.utils.IMail#getPassword()
   */
  public String getPassword( )
  {
    return m_password;
  }

  /**
   * @see org.kalypso.service.calcjobs.mail.utils.IMail#getType()
   */
  public String getType( )
  {
    return m_type;
  }

  /**
   * @see org.kalypso.service.calcjobs.mail.utils.IMail#getTitle()
   */
  public String getTitle( )
  {
    return m_title;
  }

  /**
   * @see org.kalypso.service.calcjobs.mail.utils.IMail#getText()
   */
  public String getText( )
  {
    return m_text;
  }
}