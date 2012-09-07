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
package org.kalypso.core.internal.layoutwizard;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.kalypso.core.layoutwizard.ILayoutPartFactory;

/**
 * This class manages the layout extension point.
 *
 * @author Holger Albert
 */
public class LayoutExtensions
{
  private static final String EXTENSION_POINT_LAYOUT = "org.kalypso.core.layout"; //$NON-NLS-1$

  private static final String ELEMENT_LAYOUT_PART_FACTORY = "layoutPartFactory"; //$NON-NLS-1$

  private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

  private static final String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$

  /**
   * The cache of the layout part factories.
   */
  private static Map<String, ILayoutPartFactory> LPF_CACHE = null;

  /**
   * The constructor.
   */
  private LayoutExtensions( )
  {
  }

  /**
   * This function returns the layout part factory for the given id.
   *
   * @param id
   *          The id of the layout part factory.
   * @return The layout part factory or null, if none was registered with this id.
   */
  public static ILayoutPartFactory getLayoutPartFactory( final String id ) throws CoreException
  {
    /* Get the cache of the layout part factories. */
    final Map<String, ILayoutPartFactory> lpfCache = getLpfCache();

    return lpfCache.get( id );
  }

  /**
   * This function returns the layout part factory cache.
   *
   * @return The layout part factory cache.
   */
  private static synchronized Map<String, ILayoutPartFactory> getLpfCache( ) throws CoreException
  {
    if( LPF_CACHE == null )
      LPF_CACHE = createLpfCache();

    return LPF_CACHE;
  }

  /**
   * This function creates the layout part factory cache.
   *
   * @return The layout part factory cache.
   */
  private static synchronized Map<String, ILayoutPartFactory> createLpfCache( ) throws CoreException
  {
    /* Create the cache of the layout part factories. */
    final HashMap<String, ILayoutPartFactory> lpfCache = new HashMap<>();

    /* Get the extension registry. */
    final IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();

    /* Get the extension point. */
    final IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint( EXTENSION_POINT_LAYOUT );

    /* Get all configuration elements. */
    final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
    for( final IConfigurationElement configurationElement2 : configurationElements )
    {
      /* Get the configuration element. */
      final IConfigurationElement configurationElement = configurationElement2;

      /* Ignore configuration elements, which define no layout part factories. */
      if( !ELEMENT_LAYOUT_PART_FACTORY.equals( configurationElement.getName() ) )
        continue;

      /* Get the required attributes. */
      final String id = configurationElement.getAttribute( ATTRIBUTE_ID );

      /* Add the layout part factory to the cache. */
      lpfCache.put( id, (ILayoutPartFactory) configurationElement.createExecutableExtension( ATTRIBUTE_CLASS ) );
    }

    return lpfCache;
  }
}