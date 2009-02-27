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
package org.kalypso.service.calcjobs.sendmail.utils;

/**
 * Helpfull functions for dealing with mails.
 * 
 * @author Holger Albert
 */
public class MailUtilities
{
  /**
   * x-java-content-handler=com.sun.mail.handlers.text_plain
   */
  public static final String TEXT_PLAIN = "text/plain";

  /**
   * x-java-content-handler=com.sun.mail.handlers.text_html
   */
  public static final String TEXT_HTML = "text/html";

  /**
   * x-java-content-handler=com.sun.mail.handlers.text_xml
   */
  public static final String TEXT_XML = "text/xml";

  /**
   * x-java-content-handler=com.sun.mail.handlers.multipart_mixed; x-java-fallback-entry=true
   */
  public static final String MULTIPART_ALL = "multipart/*";

  /**
   * x-java-con tent-handler=com.sun.mail.handlers.message_rfc822
   */
  public static final String MESSAGE_RFC822 = "message/rfc822";

  /**
   * The constructor.
   */
  private MailUtilities( )
  {
  }

  /**
   * This function checks, if the content type, given by the user is a supported one. If not, this function returns
   * false.
   * 
   * @param type
   *            The type given by the user.
   * @return True, if this content type is allowed.
   */
  public static boolean checkContentType( String type )
  {
    if( TEXT_PLAIN.equals( type ) )
      return true;

    if( TEXT_HTML.equals( type ) )
      return true;

    if( TEXT_XML.equals( type ) )
      return true;

    if( MULTIPART_ALL.equals( type ) )
      return true;

    if( MESSAGE_RFC822.equals( type ) )
      return true;

    return false;
  }
}