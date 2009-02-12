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
package org.kalypso.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.kalypso.services.processing.KalypsoWPSPlugin;

/**
 * @author skurzbach TODO: move this class to KalypsoServicesCore
 */
public class KalypsoServiceExtensions
{

  private static final String EXT_ELEMENT_SERVICE = "ogcWebService";

  private static final String EXT_ATTRIB_SERVICEID = "id";

  private static final String EXT_ATTRIB_SERVICECLASS = "serviceClass";

  private static final String EXT_ATTRIB_SERVICECAPABILITIES = "serviceCapabilities";

  private static final String EXT_ATTRIB_SERVICEENTRYADAPTERFACTORY = "serviceEntryAdapterFactory";

  private static final String EXT_ATTRIB_SERVICEOBJECTFACTORY = "serviceObjectFactory";

  private static Map<String, IConfigurationElement> m_configurationElements;

  private static List<Class> m_registeredServiceTypes;

  static
  {
    readServiceExtensions();
    registerServices();
  }

  public static void initializeServiceExtensions( )
  {
  }

  /**
   * Reads the extension points.
   */
  private static void readServiceExtensions( )
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();

    final IExtensionPoint point = registry.getExtensionPoint( KalypsoWPSPlugin.getID(), EXT_ELEMENT_SERVICE );

    final IExtension[] extensions = point.getExtensions();

    m_configurationElements = new HashMap<String, IConfigurationElement>();
    for( final IExtension extension : extensions )
    {
      final IConfigurationElement[] configurationElements = extension.getConfigurationElements();
      for( final IConfigurationElement element : configurationElements )
      {
        final String typeID = element.getAttribute( EXT_ATTRIB_SERVICEID );
        m_configurationElements.put( typeID, element );
      }
    }
  }

  private static void registerServices( )
  {
    m_registeredServiceTypes = new ArrayList<Class>();
    for( final IConfigurationElement element : m_configurationElements.values() )
    {
      try
      {
        final IAdapterFactory serviceEntryAdapterFactory = (IAdapterFactory) element.createExecutableExtension( EXT_ATTRIB_SERVICEENTRYADAPTERFACTORY );
        Platform.getAdapterManager().registerAdapters( serviceEntryAdapterFactory, Class.forName( element.getAttribute( EXT_ATTRIB_SERVICECLASS ) ) );
        Platform.getAdapterManager().registerAdapters( serviceEntryAdapterFactory, Class.forName( element.getAttribute( EXT_ATTRIB_SERVICECAPABILITIES ) ) );
        m_registeredServiceTypes.add( Class.forName( element.getAttribute( EXT_ATTRIB_SERVICEOBJECTFACTORY ) ) );
      }
      catch( final CoreException e )
      {
        e.printStackTrace();
      }
      catch( final InvalidRegistryObjectException e )
      {
        e.printStackTrace();
      }
      catch( final ClassNotFoundException e )
      {
        e.printStackTrace();
      }
    }
  }

  public static Class[] getRegisteredServiceTypes( )
  {
    return m_registeredServiceTypes.toArray( new Class[m_registeredServiceTypes.size()] );
  }
}
