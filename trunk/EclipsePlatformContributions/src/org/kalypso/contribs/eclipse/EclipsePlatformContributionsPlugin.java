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
package org.kalypso.contribs.eclipse;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class EclipsePlatformContributionsPlugin extends Plugin
{
  // The shared instance.
  private static EclipsePlatformContributionsPlugin INSTANCE;

  // Resource bundle.
  private ResourceBundle m_resourceBundle;

  /**
   * The constructor.
   */
  public EclipsePlatformContributionsPlugin( )
  {
    super();
    INSTANCE = this;
  }

  /**
   * This method is called upon plug-in activation
   */
  @Override
  public void start( final BundleContext context ) throws Exception
  {
    super.start( context );
  }

  /**
   * This method is called when the plug-in is stopped
   */
  @Override
  public void stop( final BundleContext context ) throws Exception
  {
    super.stop( context );
    INSTANCE = null;
    m_resourceBundle = null;
  }

  /**
   * Returns the shared instance.
   */
  public static EclipsePlatformContributionsPlugin getDefault( )
  {
    return INSTANCE;
  }

  /**
   * Returns the string from the plugin's resource bundle, or 'key' if not found.
   */
  public static String getResourceString( final String key )
  {
    final ResourceBundle bundle = EclipsePlatformContributionsPlugin.getDefault().getResourceBundle();
    try
    {
      return bundle != null ? bundle.getString( key ) : key;
    }
    catch( final MissingResourceException e )
    {
      return key;
    }
  }

  /**
   * Returns the plugin's resource bundle,
   */
  public ResourceBundle getResourceBundle( )
  {
    try
    {
      if( m_resourceBundle == null )
        m_resourceBundle = ResourceBundle.getBundle( "org.kalypso.contribs.eclipse.platform.EclipsePlatformContributionsPluginResources" ); //$NON-NLS-1$
    }
    catch( final MissingResourceException x )
    {
      m_resourceBundle = null;
    }
    return m_resourceBundle;
  }

  public static String getID( )
  {
    return getDefault().getBundle().getSymbolicName();
  }
}
