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
package org.kalypso.module.project.local;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.module.project.local.actions.LocalWorkspaceItemProvider;

/**
 * Access to the workspace extension point.
 *
 * @author Gernot Belger
 */
public final class ProjectHandleExtensions
{
  private static final String NAMESPACE = "org.kalypso.module"; //$NON-NLS-1$

  private static final String PROJECT_HANDLE_EXTENSION_POINT = "projecthandle"; //$NON-NLS-1$

  private static final String PROVIDER_ELEMENT = "handleProvider"; //$NON-NLS-1$

  public static final String ATTRIBUTE_CLASS = "class";//$NON-NLS-1$

  public static final String ATTRIBUTE_ID = "id";//$NON-NLS-1$

  private static IProjectHandleProvider GLOBAL_PROVIDER = null;

  private static IProjectHandleProvider[] PROVIDERS = null;

  private ProjectHandleExtensions( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" ); //$NON-NLS-1$
  }

  /**
   * Returns the global handle provider.
   */
  public static synchronized IProjectHandleProvider getGlobalProvider( )
  {
    if( GLOBAL_PROVIDER == null )
      GLOBAL_PROVIDER = new LocalWorkspaceItemProvider();
    // FIXME
// return ProjectHandleExtensions.getProvider( Localh.ID );
    return GLOBAL_PROVIDER;
  }

  public static synchronized IProjectHandleProvider[] getProviders( )
  {
    if( PROVIDERS == null )
      PROVIDERS = loadItemProviders();

    return PROVIDERS;
  }

  private static IProjectHandleProvider[] loadItemProviders( )
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IConfigurationElement[] elements = registry.getConfigurationElementsFor( NAMESPACE, PROJECT_HANDLE_EXTENSION_POINT );

    final Collection<IProjectHandleProvider> providers = new ArrayList<>( elements.length );

    for( final IConfigurationElement element : elements )
    {
      try
      {
        if( element.getName().equals( PROVIDER_ELEMENT ) )
        {
          final IProjectHandleProvider provider = (IProjectHandleProvider) element.createExecutableExtension( ATTRIBUTE_CLASS );
          providers.add( provider );
        }
      }
      catch( final CoreException e )
      {
        KalypsoCorePlugin.getDefault().getLog().log( e.getStatus() );
      }
    }

    return providers.toArray( new IProjectHandleProvider[providers.size()] );
  }

  public static synchronized IProjectHandleProvider getProvider( final String id )
  {
    final IProjectHandleProvider[] providers = getProviders();
    for( final IProjectHandleProvider provider : providers )
    {
      if( provider.getID().equals( id ) )
        return provider;
    }

    return null;
  }

}
