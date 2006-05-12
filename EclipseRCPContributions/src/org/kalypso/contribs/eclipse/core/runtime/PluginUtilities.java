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

import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.util.BundleUtility;
import org.osgi.framework.Bundle;

/**
 * Utilities for plugins.
 * 
 * @author Belger
 */
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
   *          the severity; one of <code>OK</code>, <code>ERROR</code>, <code>INFO</code>, <code>WARNING</code>,
   *          or <code>CANCEL</code>
   */
  public final static void logToPlugin( final Plugin plugin, final int severity, final String messsage, final Throwable t )
  {
    final IStatus status = new Status( severity, plugin.getBundle().getSymbolicName(), 0, messsage, t );
    plugin.getLog().log( status );
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
    return Platform.find( bundle, new Path( pluginRelativePath ) );
  }

  public static String id( final Plugin plugin )
  {
    return plugin.getBundle().getSymbolicName();
  }

}
