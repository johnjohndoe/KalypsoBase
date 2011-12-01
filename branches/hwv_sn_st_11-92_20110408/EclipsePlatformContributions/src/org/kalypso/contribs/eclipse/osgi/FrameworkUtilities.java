/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
public class FrameworkUtilities
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
  public static String getProperty( String key, String defaultValue )
  {
    /* Get the bundle. */
    Bundle bundle = EclipsePlatformContributionsPlugin.getDefault().getBundle();

    /* Get the bundle context. */
    BundleContext bundleContext = bundle.getBundleContext();

    /* Get the property. */
    String property = bundleContext.getProperty( key );
    if( property != null && property.length() > 0 )
      return property;

    return defaultValue;
  }
}