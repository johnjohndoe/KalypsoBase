/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  DenickestraÃŸe 22
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
package org.kalypso.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.kalypso.module.internal.Module;
import org.kalypso.module.utils.ModuleComparator;

/**
 * @author Gernot Belger
 */
public class ModuleExtensions
{
  public static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

  private static final String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$

  private static IKalypsoModule[] KALYPSO_MODULES = null;

  public static IKalypsoModule getKalypsoModule( final String moduleId )
  {
    if( moduleId == null )
      return null;

    final IKalypsoModule[] modules = getKalypsoModules();
    for( final IKalypsoModule module : modules )
    {
      final String id = module == null ? null : module.getId();
      if( moduleId.equals( id ) )
        return module;
    }

    return null;
  }

  /**
   * @return list of feature binding handlers, handling a special featureType qname
   */
  public synchronized static IKalypsoModule[] getKalypsoModules( )
  {
    // fill binding map
    if( KALYPSO_MODULES == null )
    {
      final List<IKalypsoModule> modules = new ArrayList<>();
      /* get extension points */
      final IExtensionRegistry registry = Platform.getExtensionRegistry();
      final IConfigurationElement[] elements = registry.getConfigurationElementsFor( IKalypsoModule.EXTENSION_POINT_ID );

      for( final IConfigurationElement element : elements )
      {
        try
        {
          final IKalypsoModule instance = (IKalypsoModule) element.createExecutableExtension( ATTRIBUTE_CLASS );
          modules.add( instance );
        }
        catch( final CoreException e )
        {
          Module.getDefault().getLog().log( e.getStatus() );
        }
      }

      Collections.sort( modules, new ModuleComparator() );
      KALYPSO_MODULES = modules.toArray( new IKalypsoModule[modules.size()] );
    }

    return KALYPSO_MODULES;
  }
}
