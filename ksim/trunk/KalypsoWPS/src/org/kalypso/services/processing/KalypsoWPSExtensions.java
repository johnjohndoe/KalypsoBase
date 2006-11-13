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
package org.kalypso.services.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.kalypso.simulation.core.ISimulation;

/**
 * Helper class to read extension points from registry.
 * 
 * @author skurzbach
 */
public class KalypsoWPSExtensions
{
  private final static String EXT_ELEMENT_SERVICE = "webProcessingService";

  private final static String EXT_ATTRIB_SERVICEID = "id";

  private final static String EXT_ATTRIB_SERVICECLASS = "class";

  private static final String EXT_ATTRIB_SERVICETITLE = "title";

  private static Map<String, IConfigurationElement> m_elements;

  static
  {
    readServiceExtensions();
  }

  /**
   * Reads the extension points.
   */
  private static void readServiceExtensions( )
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();

    final IExtensionPoint point = registry.getExtensionPoint( KalypsoWPSPlugin.getID(), EXT_ELEMENT_SERVICE );

    final IExtension[] extensions = point.getExtensions();

    final Map<String, IConfigurationElement> elements = new HashMap<String, IConfigurationElement>();
    for( final IExtension extension : extensions )
    {
      final IConfigurationElement[] configurationElements = extension.getConfigurationElements();
      for( final IConfigurationElement element : configurationElements )
      {
        final String typeID = element.getAttribute( EXT_ATTRIB_SERVICEID );
        elements.put( typeID, element );
      }
    }

    m_elements = elements;
  }

  /**
   * @return null, if the id is unknown
   */
  public static IWebProcessingService getWebProcessingService( final String id ) throws CoreException
  {
    final IConfigurationElement element = m_elements.get( id );
    if( element == null )
      return null;

    return new SimulationServiceWrapper( (ISimulation) element.createExecutableExtension( EXT_ATTRIB_SERVICECLASS ), element.getAttribute( EXT_ATTRIB_SERVICETITLE ) );
  }

  /**
   * Returns all registered elements of type {@link IWebProcessingService}
   */
  public static IWebProcessingService[] getRegisteredServices( ) throws CoreException
  {
    final List<IWebProcessingService> services = new ArrayList<IWebProcessingService>();
    for( final String id : m_elements.keySet() )
    {
      final IWebProcessingService service = getWebProcessingService( id );
      services.add( service );
    }
    return services.toArray( new IWebProcessingService[services.size()] );
  }

  /**
   * Returns the IDs of all registered Web Processing Services
   */
  public static String[] getRegisteredServiceIDs( )
  {
    return m_elements.keySet().toArray( new String[m_elements.size()] );
  }
}
