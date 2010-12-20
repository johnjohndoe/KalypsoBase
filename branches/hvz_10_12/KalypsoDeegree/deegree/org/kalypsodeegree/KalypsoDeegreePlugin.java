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

import org.deegree.crs.transformations.TransformationFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.kalypso.preferences.IKalypsoDeegreePreferences;
import org.osgi.framework.BundleContext;

public class KalypsoDeegreePlugin extends Plugin
{
  /**
   * The shared instance.
   */
  private static KalypsoDeegreePlugin PLUGIN;

  /**
   * The constructor.
   */
  public KalypsoDeegreePlugin( )
  {
    super();

    PLUGIN = this;
  }

  /**
   * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start( BundleContext context ) throws Exception
  {
    super.start( context );

    // IMPORTANT: If this deegree code is static-initialised inside an ant-task, the internal
    // Logger will not be correctly set-up and transformation will not work any more.
    // So we force initialisation here.
    TransformationFactory.getInstance();
  }

  /**
   * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop( BundleContext context ) throws Exception
  {
    /* Save the plug-in preferences. */
    InstanceScope instanceScope = new InstanceScope();
    IEclipsePreferences instanceNode = instanceScope.getNode( getBundle().getSymbolicName() );
    instanceNode.flush();

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
    return Platform.getPreferencesService().getString( getBundle().getSymbolicName(), IKalypsoDeegreePreferences.DEFAULT_CRS_SETTING, IKalypsoDeegreePreferences.DEFAULT_CRS_VALUE, null );
  }

  /**
   * Returns the symbolic name of this plug-in.
   */
  public static String getID( )
  {
    return getDefault().getBundle().getSymbolicName();
  }
}