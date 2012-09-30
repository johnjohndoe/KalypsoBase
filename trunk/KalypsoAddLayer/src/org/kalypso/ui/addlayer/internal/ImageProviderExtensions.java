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
package org.kalypso.ui.addlayer.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * Accessor to the WMSImageProvider extension point.
 *
 * @author Gernot Belger
 */
public final class ImageProviderExtensions
{
  private static final String EXTENSION_POINT_ID = "org.kalypso.ui.addlayer.WMSImageProvider"; //$NON-NLS-1$

  /**
   * This function initializes the provider list from the extensions.
   */
  public static Map<String, String> getImageProviders( )
  {
    /* Get the extension registry. */
    final IExtensionRegistry er = Platform.getExtensionRegistry();

    final Map<String, String> mapping = new HashMap<>();

    final IConfigurationElement[] configurationElementsFor = er.getConfigurationElementsFor( EXTENSION_POINT_ID );
    for( final IConfigurationElement element : configurationElementsFor )
    {
      /* Get some attributes. */
      final String id = element.getAttribute( "id" ); //$NON-NLS-1$
      final String name = element.getAttribute( "name" ); //$NON-NLS-1$

      /* Index with the id. */
      mapping.put( id, name );
    }

    return mapping;
  }
}