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
package org.kalypso.contribs.eclipse.osgi;

import org.kalypso.contribs.eclipse.EclipsePlatformContributionsPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * This class provides functions for the framework (e.g. functions for dealing with the
 * {@link org.eclipse.osgi.framework.internal.core.FrameworkProperties}).
 * 
 * @author Holger Albert
 */
public final class FrameworkUtilities
{
  /**
   * The constructor.
   */
  private FrameworkUtilities( )
  {
  }

  /**
   * Returns the value of the specified property. If the key is not found in the Framework properties, the system
   * properties are then searched. The method returns null if the property is not found.
   * 
   * @param key
   *          The name of the requested property.
   * @param defaultValue
   *          The default value.
   * @return The value of the requested property, or the default value if the property is undefined.
   */
  public static String getProperty( final String key, final String defaultValue )
  {
    /* Get the bundle. */
    final Bundle bundle = EclipsePlatformContributionsPlugin.getDefault().getBundle();

    /* Get the bundle context. */
    final BundleContext bundleContext = bundle.getBundleContext();

    /* Get the property. */
    final String property = bundleContext.getProperty( key );
    if( property != null && property.length() > 0 )
      return property;

    return defaultValue;
  }
}