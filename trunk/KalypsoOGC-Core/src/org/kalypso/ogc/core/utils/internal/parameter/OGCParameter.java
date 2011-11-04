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
package org.kalypso.ogc.core.utils.internal.parameter;

/**
 * This class holds some parameter for a OGC service.
 * 
 * @author Toni DiNardo
 */
public class OGCParameter
{
  /**
   * The service parameter is mandatory and describes the name of the requested service.
   */
  private final String m_service;

  /**
   * The request parameter is mandatory and describes the name of the requested operation.
   */
  private final String m_request;

  /**
   * The version parameter is mandatory for the DescribeProcess and Execute operation.
   */
  private final String m_version;

  /**
   * The acceptVersions parameter is optional and may only be provided by the GetCapabilities operation.
   */
  private final String[] m_acceptVersions;

  /**
   * The language parameter is optional and describes the requested language.
   */
  private final String m_language;

  /**
   * The constructor.
   * 
   * @param service
   *          The service parameter is mandatory and describes the name of the requested service.
   * @param request
   *          The request parameter is mandatory and describes the name of the requested operation.
   * @param version
   *          The version parameter is mandatory for the DescribeProcess and Execute operation.
   * @param acceptVersions
   *          The acceptVersions parameter is optional and may only be provided by the GetCapabilities operation.
   * @param language
   *          The language parameter is optional and describes the requested language.
   */
  public OGCParameter( final String service, final String request, final String version, final String[] acceptVersions, final String language )
  {
    m_service = service;
    m_request = request;
    m_version = version;
    m_acceptVersions = acceptVersions;
    m_language = language;
  }

  /**
   * This function returns the service parameter. The service parameter is mandatory and describes the name of the
   * requested service.
   * 
   * @return The service parameter.
   */
  public String getService( )
  {
    return m_service;
  }

  /**
   * This function returns the request parameter. The request parameter is mandatory and describes the name of the
   * requested operation.
   * 
   * @return The request parameter.
   */
  public String getRequest( )
  {
    return m_request;
  }

  /**
   * This function returns the version parameter. The version parameter is mandatory for the DescribeProcess and Execute
   * operation.
   * 
   * @return The version parameter.
   */
  public String getVersion( )
  {
    return m_version;
  }

  /**
   * This function returns the acceptVersions parameter. The acceptVersions parameter is optional and may only be
   * provided by the GetCapabilities operation.
   * 
   * @return The acceptVersions parameter.
   */
  public String[] getAcceptVersions( )
  {
    return m_acceptVersions;
  }

  /**
   * This function returns the language parameter. The language parameter is optional and describes the requested
   * language.
   * 
   * @return The language parameter.
   */
  public String getLanguage( )
  {
    return m_language;
  }
}