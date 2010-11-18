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
package org.kalypso.contribs.eclipse.core.runtime;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.kalypso.contribs.eclipse.utils.ConfigUtils;
import org.osgi.framework.Bundle;

/**
 * Utilities for plugins.
 * 
 * @author Belger
 */
@SuppressWarnings("restriction")
public class PluginUtilities
{
  private PluginUtilities( )
  {
    // do not instantiate
  }

  /**
   * Logs the given data to the plugin *
   * 
   * @param severity
   *          the severity; one of <code>OK</code>, <code>ERROR</code>, <code>INFO</code>, <code>WARNING</code>, or
   *          <code>CANCEL</code>
   */
  public final static void logToPlugin( final Plugin plugin, final int severity, final String messsage, final Throwable t )
  {
    final IStatus status = new Status( severity, plugin.getBundle().getSymbolicName(), 0, messsage, t );
    plugin.getLog().log( status );
  }

  /**
   * Log information to given plugin.
   * <p>
   * Formats message wirth args
   * </p>
   */
  public static void logInfo( final Plugin plugin, final String message, final Object... args )
  {
    final String msg = String.format( message, args );
    plugin.getLog().log( new Status( IStatus.INFO, id( plugin ), 0, msg, null ) );
  }

  /**
   * Returns the settings with the given name for a plugin. If no settings with this name already exists, one is
   * created.
   */
  public static IDialogSettings getDialogSettings( final AbstractUIPlugin plugin, final String sectionName )
  {
    final IDialogSettings workbenchSettings = plugin.getDialogSettings();
    return getSection( workbenchSettings, sectionName );
  }

  /**
   * Returns a sub-section of a given {@link IDialogSettings}.<br>
   * If the section does not yet exist, it is created.
   * 
   * @return <code>null</code>, if the given <code>settings</code> are <code>null</code>.
   */
  public static IDialogSettings getSection( final IDialogSettings settings, final String sectionName )
  {
    if( settings == null )
      return null;

    final IDialogSettings section = settings.getSection( sectionName );
    if( section == null )
      return settings.addNewSection( sectionName );
    return section;
  }

  /**
   * Search for a resource in the plugin (any file) and its fragments.
   * 
   * @return null, if something goes wrong
   * @throws IllegalArgumentException
   *           if arguments are null
   */
  public static URL findResource( final String pluginId, final String pluginRelativePath )
  {
    if( pluginId == null || pluginRelativePath == null )
      throw new IllegalArgumentException();

    // if the bundle is not ready then there is no resource
    final Bundle bundle = Platform.getBundle( pluginId );
    if( !BundleUtility.isReady( bundle ) )
      return null;

    // look for the image (this will check both the plugin and fragment folders
    return FileLocator.find( bundle, new Path( pluginRelativePath ), null );
  }

  public static String id( final Plugin plugin )
  {
    return plugin.getBundle().getSymbolicName();
  }

  /**
   * Loads a class from a given bundle.
   * 
   * @throws ClassNotFoundException
   *           If either the class or the bundle cannot be found.
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> findClass( final String className, final String pluginId ) throws ClassNotFoundException
  {
    final Bundle bundle = Platform.getBundle( pluginId );
    if( bundle == null || !BundleUtility.isReady( bundle ) )
    {
      final String msg = String.format( "Could not load class %s: Bundle not fond or not ready: %s", className, pluginId );
      throw new ClassNotFoundException( msg );
    }

    return bundle.loadClass( className );
  }

  /**
   * @deprecated Use {@link ConfigUtils} instead.
   */
  @Deprecated
  public static URL findConfigLocation( final Plugin plugin, final String path, final String fallbackPath ) throws IOException
  {
    try
    {
      final Location configurationLocation = Platform.getConfigurationLocation();
      final URL configurationURL = configurationLocation.getURL();
      return ConfigUtils.checkConfigLocation( configurationURL, path );
    }
    catch( final IOException e )
    {
      // ignore exception for now, second try
      try
      {
        final URL configResource = plugin.getBundle().getResource( fallbackPath );
        return ConfigUtils.checkConfigLocation( configResource, path );
      }
      catch( final IOException e1 )
      {
        // we throw the originial, first exception, as this is the primary location that should work
        throw e;
      }
    }
  }

}
