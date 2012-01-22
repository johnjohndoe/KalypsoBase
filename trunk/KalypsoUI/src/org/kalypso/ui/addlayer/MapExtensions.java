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
package org.kalypso.ui.addlayer;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.dialogs.WizardCollectionElement;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.addlayer.dnd.IMapDropTarget;
import org.kalypso.ui.addlayer.internal.dnd.MapDropTarget;

/**
 * @author Gernot Belger
 */
@SuppressWarnings("restriction")
public final class MapExtensions
{
  /** Constant for all Kalypso data import wizards (Extension point schema org.kalypso.ui.dataImportWizard.exsd) */
  private static final String EXTENSION_POINT_ADD_LAYER_WIZARD = "addLayerWizard"; //$NON-NLS-1$

  /** Constant for all Kalypso data import wizards (Extension point schema org.kalypso.ui.mapDropTarget.exsd) */
  private static final String EXTENSION_POINT_MAP_DROP_TARGET = "mapDropTarget"; //$NON-NLS-1$

  private static final String EXTENSION_TARGET = "target"; //$NON-NLS-1$

  private MapExtensions( )
  {
    throw new UnsupportedOperationException();
  }

  public static WizardCollectionElement getAvailableWizards( )
  {
    final WizardsRegistryReader reader = new WizardsRegistryReader( KalypsoGisPlugin.PLUGIN_ID, EXTENSION_POINT_ADD_LAYER_WIZARD );
    return reader.getWizardElements();
  }

  public static IMapDropTarget[] getDropTargets( )
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IConfigurationElement[] elements = registry.getConfigurationElementsFor( KalypsoGisPlugin.PLUGIN_ID, EXTENSION_POINT_MAP_DROP_TARGET );

    final Collection<IMapDropTarget> targets = new ArrayList<>( elements.length );

    for( final IConfigurationElement element : elements )
    {
      if( EXTENSION_TARGET.equals( element.getName() ) )
        targets.add( new MapDropTarget( element ) );
    }

    return targets.toArray( new IMapDropTarget[targets.size()] );
  }
}