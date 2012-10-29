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
package org.kalypso.commons.net;

import org.apache.commons.httpclient.StatusLine;

/**
 * A http response.
 * 
 * @author Holger Albert
 */
public class HttpResponse
{
  /**
   * The status line. The status line contains the status code and error message.
   */
  private final StatusLine m_statusLine;

  /**
   * The mime type of the response. May be null.
   */
  private final String m_mimeType;

  /**
   * The text of the response. May be null.
   */
  private final String m_response;

  /**
   * The constcutor.
   * 
   * @param statusLine
   *          The status line. The status line contains the status code and error message.
   * @param mimeType
   *          The mime type of the response. May be null.
   * @param response
   *          The text of the response. May be null.
   */
  public HttpResponse( final StatusLine statusLine, final String mimeType, final String response )
  {
    m_statusLine = statusLine;
    m_mimeType = mimeType;
    m_response = response;
  }

  /**
   * This function returns the status line. The status line contains the status code and error message.
   * 
   * @return The status line. The status line contains the status code and error message.
   */
  public StatusLine getStatusLine( )
  {
    return m_statusLine;
  }

  /**
   * This function returns the mime type of the response. May be null.
   * 
   * @return The mime type of the response. May be null.
   */
  public String getMimeType( )
  {
    return m_mimeType;
  }

  /**
   * This function returns the text of the response. May be null.
   * 
   * @return The text of the response. May be null.
   */
  public String getResponse( )
  {
    return m_response;
  }
}