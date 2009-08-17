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
package org.kalypso.service.ogc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * @author Alex Burtscher, Holger Albert
 */
public class OGCServiceExtensions
{
  private static Map<String, IConfigurationElement> THE_MAP = null;

  private OGCServiceExtensions( )
  {
    // will not be instantiated
  }

  /**
   * This function will return all Parameters, that are mandatory for the given service.
   */
  public static List<String> getMandadoryParameters( final String id )
  {
    final List<String> parameters = new LinkedList<String>();
    final Map<String, IConfigurationElement> elts = getOperations();

    IConfigurationElement element = elts.get( id );
    if( element == null )
      return null;

    IConfigurationElement[] children = element.getChildren( "parameter" );
    if( children.length == 0 )
      return parameters;

    for( int i = 0; i < children.length; i++ )
    {
      IConfigurationElement child = children[i];
      parameters.add( child.getAttribute( "parameterName" ) );
    }

    return parameters;
  }

  public static IOGCService createService( final String id ) throws CoreException
  {
    final Map<String, IConfigurationElement> elts = getOperations();
    System.out.println( "looking for service: " + id );

    final IConfigurationElement element = elts.get( id );
    if( element == null )
      return null;

    System.out.println( "Found service: " + id );
    return (IOGCService) element.createExecutableExtension( "class" );
  }

  /**
   * This function returns all registered services.
   * 
   * @return All known services.
   */
  public static Map<String, IOGCService> createServices( ) throws CoreException
  {
    /* A list for the services. */
    final Map<String, IOGCService> services = new LinkedHashMap<String, IOGCService>();

    /* The map containing the registered elements. */
    final Map<String, IConfigurationElement> elts = getOperations();

    Iterator<String> itr = elts.keySet().iterator();
    while( itr.hasNext() )
    {
      String key = itr.next();
      IConfigurationElement element = elts.get( key );
      services.put( key, (IOGCService) element.createExecutableExtension( "class" ) );
    }

    return services;
  }

  private synchronized static Map<String, IConfigurationElement> getOperations( )
  {
    if( THE_MAP != null )
      return THE_MAP;

    THE_MAP = new HashMap<String, IConfigurationElement>();

    final IExtensionRegistry er = Platform.getExtensionRegistry();
    if( er != null )
    {
      final IConfigurationElement[] configurationElementsFor = er.getConfigurationElementsFor( "org.kalypso.service.ogc.ogcService" );
      for( final IConfigurationElement element : configurationElementsFor )
      {
        final String id = element.getAttribute( "serviceName" );
        THE_MAP.put( id, element );

        System.out.println( "adding service: " + id );
      }
    }
    else
    {
      // Logger.trace( "Error: cant find ExtensionRegistry" );
    }

    return THE_MAP;
  }
}