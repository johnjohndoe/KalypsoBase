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
package org.kalypso.service.wps.utils;

import org.eclipse.osgi.framework.internal.core.FrameworkProperties;

/**
 * This class provides easy access to proxy data.
 * 
 * @author Holger Albert
 */
public class ProxyUtilities
{
  /**
   * This variable stores, if a proxy should be used.
   */
  private boolean m_useProxy;

  /**
   * This variable stores the proxy host.
   */
  private String m_proxyHost;

  /**
   * This variable stores the proxy port.
   */
  private int m_proxyPort;

  /**
   * This variable stores the user, which must authenticate to the proxy.
   */
  private String m_user;

  /**
   * The password of the password.
   */
  private String m_password;

  /**
   * The instance of this class.
   */
  private static ProxyUtilities m_instance = null;

  /**
   * The constructor.
   */
  private ProxyUtilities( )
  {
    /* Initialize the values. */
    initProxy();
  }

  /**
   * This function initializes the values of this class. It reads them out of their location in Eclipse.
   */
  @SuppressWarnings("restriction")
  public void initProxy( )
  {
    /* Use no proxy. */
    m_useProxy = false;

    /* No default values. */
    m_proxyHost = null;
    m_proxyPort = 0;

    /* No default credentials. */
    m_user = null;
    m_password = null;

    /* Get all values. */
    String proxySet = FrameworkProperties.getProperty( "proxySet" );
    if( proxySet != null && proxySet.equals( "true" ) )
      m_useProxy = true;

    String proxyHost = FrameworkProperties.getProperty( "proxyHost" );
    if( proxyHost != null )
      m_proxyHost = proxyHost;

    String proxyPort = FrameworkProperties.getProperty( "proxyPort" );
    if( proxyPort != null )
      m_proxyPort = Integer.valueOf( proxyPort );

    String user = FrameworkProperties.getProperty( "proxyUser" );
    if( user != null )
      m_user = user;

    String password = FrameworkProperties.getProperty( "proxyPassword" );
    if( password != null )
      m_password = password;
  }

  /**
   * This function creates the instance of this class.
   * 
   * @return The instance of this class.
   */
  public static ProxyUtilities getInstance( )
  {
    if( m_instance == null )
      m_instance = new ProxyUtilities();

    return m_instance;
  }

  /**
   * This function returns the password.
   * 
   * @return The password.
   */
  public String getPassword( )
  {
    return m_password;
  }

  /**
   * This function returns the proxy host.
   * 
   * @return The proxy host.
   */
  public String getProxyHost( )
  {
    return m_proxyHost;
  }

  /**
   * This function returns the proxy port.
   * 
   * @return The proxy port.
   */
  public int getProxyPort( )
  {
    return m_proxyPort;
  }

  /**
   * This function is true, if a proxy should be used.
   * 
   * @return True, if a proxy should be used.
   */
  public boolean useProxy( )
  {
    return m_useProxy;
  }

  /**
   * This function returns the user.
   * 
   * @return The user.
   */
  public String getUser( )
  {
    return m_user;
  }
}