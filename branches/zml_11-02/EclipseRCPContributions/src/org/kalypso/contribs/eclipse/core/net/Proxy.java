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
package org.kalypso.contribs.eclipse.core.net;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * This class provides easy access to proxy data. There are two possibilities of getting the proxy data.
 * <ol>
 * <li>You can use the functions as
 * <ul>
 * <li>useProxy()</li>
 * <li>getProxyHost()</li>
 * <li>getProxyPort()</li>
 * <li>getProxyUser()</li>
 * <li>getProxyPassword()</li>
 * </ul>
 * This functions will retrieve the data out of the java system properties. </li>
 * <li>Or you can get an instance of the proxy service of Eclipse. This service will get its data from the Eclipse
 * preference page.</li>
 * </ol>
 * <strong>Hint:</strong><br>
 * The Eclipse preference page will set the java system properties as well.<br>
 * <br>
 * 
 * @author Holger Albert
 */
public class Proxy
{
  /**
   * The constructor.
   */
  public Proxy( )
  {
  }

  /**
   * This function returns the password.
   * 
   * @return The password.
   */
  public String getPassword( )
  {
    String password = System.getProperty( "http.proxyPassword" );
    if( password != null )
      return password;

    return null;
  }

  /**
   * This function returns the proxy host.
   * 
   * @return The proxy host.
   */
  public String getProxyHost( )
  {
    String proxyHost = System.getProperty( "http.proxyHost" );
    if( proxyHost != null )
      return proxyHost;

    return null;
  }

  /**
   * This function returns the proxy port.
   * 
   * @return The proxy port If none is set, it defaults to 8080.
   */
  public int getProxyPort( )
  {
    String proxyPort = System.getProperty( "http.proxyPort" );
    if( proxyPort != null && Integer.valueOf( proxyPort ) > 0 )
      return Integer.valueOf( proxyPort );

    return 8080;
  }

  /**
   * This function is true, if a proxy should be used.
   * 
   * @return True, if a proxy should be used.
   */
  public boolean useProxy( )
  {
    String proxySet = System.getProperty( "http.proxySet" );
    if( proxySet != null && proxySet.equals( "true" ) )
      return true;

    return false;
  }

  /**
   * This function returns the user.
   * 
   * @return The user.
   */
  public String getUser( )
  {
    String user = System.getProperty( "http.proxyUser" );
    if( user != null )
      return user;

    return null;
  }

  /**
   * This function returns a list of hosts, that should use no proxy.
   * 
   * @return A list of hosts, that should use no proxy, or an empty list, if none are set.
   */
  public List<String> getNonProxyHosts( )
  {
    /* The list of all hosts, that should use no proxy. */
    ArrayList<String> noneProxies = new ArrayList<String>();

    /* Get the system property. */
    String noneProxyHosts = System.getProperty( "http.nonProxyHosts" );
    if( noneProxyHosts == null )
      return noneProxies;

    /* Collect the hosts. */
    StringTokenizer tokenizer = new StringTokenizer( noneProxyHosts, "|" );
    while( tokenizer.hasMoreElements() )
      noneProxies.add( tokenizer.nextToken() );

    return noneProxies;
  }

  /**
   * This function returns the proxy service of the Eclipse platform.<br>
   * The service will use the settings of the preference page in Eclipse.
   * 
   * @param plugin
   *            The plugin, over which context the service will be taken.
   * @return The proxy service.
   */
  public static IProxyService getProxyService( Plugin plugin )
  {
    BundleContext bundleContext = plugin.getBundle().getBundleContext();

    /* Get the reference to the proxy service. */
    ServiceReference serviceReference = bundleContext.getServiceReference( IProxyService.class.getName() );
    if( serviceReference == null )
      return null;

    /* Get the proxy service. */
    IProxyService service = (IProxyService) bundleContext.getService( serviceReference );

    return service;
  }
}