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
package org.kalypso.ogc.gml;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.kalypso.ui.KalypsoGisPlugin;

/**
 * @author kurzbach
 */
public class ThemeFactoryExtension
{
  private static final String KALYPSO_THEME_FACTORY_EXTENSION_POINT = "org.kalypso.ui.themeFactory"; //$NON-NLS-1$

  private static final String ATTRIBUTE_FACTORY = "class"; //$NON-NLS-1$

  private static final String ATTRIBUTE_TYPE = "type"; //$NON-NLS-1$

  @SuppressWarnings( "unused" )
  private static final String ATTRIBUTE_Label = "label"; //$NON-NLS-1$

  private static Map<String, IConfigurationElement> FACTORY_MAP;

  public synchronized static IKalypsoThemeFactory getThemeFactory( final String linktype )
  {
    try
    {
      if( FACTORY_MAP == null )
        init();

      final IConfigurationElement element = FACTORY_MAP.get( linktype.toLowerCase() );
      if( element == null )
        return null;

      return (IKalypsoThemeFactory)element.createExecutableExtension( ATTRIBUTE_FACTORY );
    }
    catch( final CoreException e )
    {
      KalypsoGisPlugin.getDefault().getLog().log( e.getStatus() );
    }

    return null;
  }

  // Hashes the configuration elements in order to quicker retrieval. Does not create the factories (eclipse says, do it
  // lazy!)
  private static void init( )
  {
    FACTORY_MAP = new HashMap<>();

    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IExtensionPoint extensionPoint = registry.getExtensionPoint( KALYPSO_THEME_FACTORY_EXTENSION_POINT );
    final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();

    for( final IConfigurationElement factoryElement : configurationElements )
    {
      final String type = factoryElement.getAttribute( ATTRIBUTE_TYPE );
      FACTORY_MAP.put( type.toLowerCase(), factoryElement );
    }
  }

}
