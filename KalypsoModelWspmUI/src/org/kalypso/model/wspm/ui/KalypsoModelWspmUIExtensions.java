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
package org.kalypso.model.wspm.ui;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.profil.wizard.pointsInsert.IPointsSource;
import org.kalypso.model.wspm.ui.profil.wizard.pointsInsert.IPointsTarget;
import org.kalypso.model.wspm.ui.view.chart.IProfilLayerProvider;

public final class KalypsoModelWspmUIExtensions
{
  private KalypsoModelWspmUIExtensions( )
  {
  }

  public static IPointsTarget[] createProfilPointTargets( )
  {
    return createExtensions( "org.kalypso.model.wspm.ui.profilPointsTarget", new IPointsTarget[0] ); //$NON-NLS-1$
  }

  public static IPointsSource[] createProfilPointSources( )
  {
    return createExtensions( "org.kalypso.model.wspm.ui.profilPointsSource", new IPointsSource[0] ); //$NON-NLS-1$
  }

  public static <T> T[] createExtensions( final String extensionPoint, final T[] a )
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IConfigurationElement[] elements = registry.getConfigurationElementsFor( extensionPoint );

    // elements[0].getAttribute( "id" );
    // elements[0].createExecutableExtension( "class" );

    final Collection<T> targets = new ArrayList<>( elements.length );
    for( final IConfigurationElement element : elements )
    {
      try
      {
        targets.add( (T)element.createExecutableExtension( "class" ) ); //$NON-NLS-1$
      }
      catch( final CoreException e )
      {
        KalypsoModelWspmUIPlugin.getDefault().getLog().log( e.getStatus() );
      }
    }

    return targets.toArray( a );
  }

  /**
   * @return the first LayerProvider for the given profile type
   */
  public static IProfilLayerProvider createProfilLayerProvider( final String profiletype )
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IConfigurationElement[] elements = registry.getConfigurationElementsFor( "org.kalypso.model.wspm.ui.profilChartLayerProvider" ); //$NON-NLS-1$

    for( final IConfigurationElement element : elements )
    {
      final String type = element.getAttribute( "profiletype" ); //$NON-NLS-1$
      if( type.equals( profiletype ) )
      {
        try
        {
          final Object layerProvider = element.createExecutableExtension( "provider" ); //$NON-NLS-1$
          if( layerProvider instanceof IProfilLayerProvider )
            return (IProfilLayerProvider)layerProvider;
        }
        catch( final CoreException e )
        {
          KalypsoModelWspmUIPlugin.getDefault().getLog().log( e.getStatus() );
        }
      }
    }
    final IStatus status = new Status( IStatus.WARNING, KalypsoModelWspmUIPlugin.ID, Messages.getString( "org.kalypso.model.wspm.ui.KalypsoModelWspmUIExtensions.12" ) + profiletype ); //$NON-NLS-1$
    KalypsoModelWspmUIPlugin.getDefault().getLog().log( status );
    return null;
  }
}
