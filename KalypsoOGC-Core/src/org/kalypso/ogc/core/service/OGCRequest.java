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
package org.kalypso.ogc.core.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;

/**
 * This class represents a request of a OGC service. Its main purpose is, to copy needed data of the servlet request, so
 * that it can be destroyed (thus freeing resources), after it was beeing handled. This is necessary, because OGC
 * services may execute long running, parallel operations.
 * 
 * @author Toni DiNardo
 */
public class OGCRequest
{
  /**
   * The type of the servlet request. True for a POST request. False for a GET request.
   */
  private final boolean m_post;

  /**
   * The parameter of the servlet request.
   */
  private Map<String, String[]> m_parameter = null;

  /**
   * The body of the servlet request.
   */
  private String m_body;

  /**
   * The constructor.
   * 
   * @param post
   *          The type of the servlet request. True for a POST request. False for a GET request.
   * @param request
   *          The servlet request.
   */
  public OGCRequest( final boolean post, final HttpServletRequest request )
  {
    m_post = post;
    m_parameter = null;
    m_body = null;

    init( request );
  }

  /**
   * This function returns true, if the request was send with the POST method.
   * 
   * @boolean True, if the request was send with the POST method.
   */
  public boolean isPost( )
  {
    return m_post;
  }

  /**
   * This function returns a parameter value for a parameter name.
   * 
   * @param key
   *          The parameter name.
   * @return The parameter value or null, if it does not exist.
   */
  public String getParameterValue( final String key )
  {
    final String[] values = m_parameter.get( key.toLowerCase() );
    if( values != null && values.length > 0 )
      return values[0];

    return null;
  }

  /**
   * This function returns the body, if the request was send with the POST method and a body is available. Otherwise it
   * returns null.
   * 
   * @return The body or null.
   */
  public String getBody( )
  {
    if( !m_post )
      return null;

    return m_body;
  }

  /**
   * This function initializes the OGC request.
   * 
   * @param request
   *          The servlet request.
   */
  private void init( final HttpServletRequest request )
  {
    m_parameter = initParameter( request );
    m_body = initBody( request );
  }

  /**
   * This function initializes the parameter.
   * 
   * @param request
   *          The servlet request.
   * @return The parameter.
   */
  private Map<String, String[]> initParameter( final HttpServletRequest request )
  {
    final Map<String, String[]> parameter = new HashMap<>();

    final Set<Entry<String, String[]>> entries = request.getParameterMap().entrySet();
    for( final Entry<String, String[]> entry : entries )
      parameter.put( entry.getKey().toLowerCase(), entry.getValue() );

    return parameter;
  }

  /**
   * This function initializes the body.
   * 
   * @param request
   *          The servlet request.
   * @return The body.
   */
  private String initBody( final HttpServletRequest request )
  {
    /* The reader. */
    BufferedReader reader = null;

    try
    {
      /* If the request was not sent via the POST method, no body is available. */
      if( !m_post )
        return null;

      /* Only xml bodies may be handled. */
      final String contentType = request.getContentType();
      if( contentType == null || !contentType.contains( "text/xml" ) ) //$NON-NLS-1$
        return null;

      /* Memory for the results. */
      final StringBuffer buffer = new StringBuffer();

      /* Get the reader. */
      reader = request.getReader();

      /* Read the body. */
      String line = ""; //$NON-NLS-1$
      while( (line = reader.readLine()) != null )
      {
        buffer.append( line );
        buffer.append( System.getProperty( "line.separator", "\n\r" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      }

      return buffer.toString();
    }
    catch( final IOException ex )
    {
      /* Print the exception. */
      ex.printStackTrace();
      return null;
    }
    finally
    {
      /* Close the reader. */
      IOUtils.closeQuietly( reader );
    }
  }
}