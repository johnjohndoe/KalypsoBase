/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.geotools;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.geotools.referencing.factory.custom.ICustomCRSProvider;

/**
 * Manages the extension point of geo tools.
 * 
 * @author Holger Albert
 */
public class GeoToolsExtensions
{
  private static final String CUSTOM_CRS_PROVIDER_EXTENSION_POINT = "org.geotools.customCRSProvider"; //$NON-NLS-1$

  private static final String CUSTOM_CRS_PROVIDER_CUSTOM_CRS_PROVIDER_ELEMENT = "customCRSProvider"; //$NON-NLS-1$

  private static final String CUSTOM_CRS_PROVIDER_CUSTOM_CRS_PROVIDER_CLASS = "class"; //$NON-NLS-1$

  /**
   * The constructor.
   */
  private GeoToolsExtensions( )
  {
  }

  /**
   * This function returns the custom CRS provider.
   * 
   * @return The custom CRS provider.
   */
  public static ICustomCRSProvider[] createCustomCRSProvider( ) throws CoreException
  {
    /* Memory for the results. */
    final List<ICustomCRSProvider> result = new ArrayList<>();

    /* Get the extension registry. */
    final IExtensionRegistry registry = Platform.getExtensionRegistry();

    /* Get the extension point. */
    final IExtensionPoint extensionPoint = registry.getExtensionPoint( CUSTOM_CRS_PROVIDER_EXTENSION_POINT );

    /* Get all configuration elements. */
    final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
    for( final IConfigurationElement element : configurationElements )
    {
      /* If the configuration element is not the custom crs provider element, continue. */
      if( !CUSTOM_CRS_PROVIDER_CUSTOM_CRS_PROVIDER_ELEMENT.equals( element.getName() ) )
        continue;

      /* Add the the custom crs provider. */
      ICustomCRSProvider customCrsProvider = (ICustomCRSProvider)element.createExecutableExtension( CUSTOM_CRS_PROVIDER_CUSTOM_CRS_PROVIDER_CLASS );
      result.add( customCrsProvider );
    }

    return result.toArray( new ICustomCRSProvider[] {} );
  }
}