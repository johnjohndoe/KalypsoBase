/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 *
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always.
 *
 * If you intend to use this software in other ways than in kalypso
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree,
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

public class KalypsoDeegreePlugin extends Plugin
{
  /**
   * The shared instance.
   */
  private static KalypsoDeegreePlugin PLUGIN;

  private ScopedPreferenceStore m_preferenceStore;

  /**
   * The constructor.
   */
  public KalypsoDeegreePlugin( )
  {
    super();

    PLUGIN = this;
  }

  @Override
  public void start( final BundleContext context ) throws Exception
  {
    super.start( context );
  }

  @Override
  public void stop( final BundleContext context ) throws Exception
  {
    savePluginStore();
    m_preferenceStore = null;

    PLUGIN = null;

    super.stop( context );
  }

  /**
   * Returns the shared instance.
   */
  public static KalypsoDeegreePlugin getDefault( )
  {
    return PLUGIN;
  }

  /**
   * This function returns the coordinate system set in the preferences.
   * 
   * @return The coordinate system.
   */
  public String getCoordinateSystem( )
  {
    return KalypsoDeegreePreferences.getCoordinateSystem();
  }

  /**
   * Returns the symbolic name of this plug-in.
   */
  public static String getID( )
  {
    return getDefault().getBundle().getSymbolicName();
  }

  /**
   * Returns the preference store for this plug-in.
   * This preference store is used to hold persistent settings for this plug-in in
   * the context of a workbench. Some of these settings will be user controlled,
   * whereas others may be internal setting that are never exposed to the user.
   * <p>
   * If an error occurs reading the preference store, an empty preference store is quietly created, initialized with defaults, and returned.
   * </p>
   * <p>
   * <strong>NOTE:</strong> As of Eclipse 3.1 this method is no longer referring to the core runtime compatibility layer and so plug-ins relying on Plugin#initializeDefaultPreferences will have to
   * access the compatibility layer themselves.
   * </p>
   * 
   * @return the preference store
   */
  synchronized IPreferenceStore getPreferenceStore( )
  {
    // Create the preference store lazily.
    if( m_preferenceStore == null )
    {
      m_preferenceStore = new ScopedPreferenceStore( InstanceScope.INSTANCE, getID() );

      KalypsoDeegreePreferences.initDefaults( m_preferenceStore );
    }

    return m_preferenceStore;
  }

  public void savePluginStore( )
  {
    if( m_preferenceStore == null )
      return;

    try
    {
      InstanceScope.INSTANCE.getNode( getID() ).flush();
    }
    catch( final BackingStoreException e )
    {
      e.printStackTrace();
    }
  }
}