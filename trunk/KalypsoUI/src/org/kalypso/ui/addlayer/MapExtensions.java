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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.dialogs.WizardCollectionElement;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.addlayer.dnd.IMapDropTarget;
import org.kalypso.ui.addlayer.internal.dnd.MapDropTarget;

/**
 * @author Gernot Belger
 */
@SuppressWarnings( "restriction" )
public final class MapExtensions
{
  /** Constant for all Kalypso data import wizards (Extension point schema org.kalypso.ui.dataImportWizard.exsd) */
  private static final String EXTENSION_POINT_ADD_LAYER_WIZARD = "addLayerWizard"; //$NON-NLS-1$

  /** Constant for all Kalypso data import wizards (Extension point schema org.kalypso.ui.mapDropTarget.exsd) */
  private static final String EXTENSION_POINT_MAP_DROP_TARGET = "mapDropTarget"; //$NON-NLS-1$

  private static final String EXTENSION_TARGET = "target"; //$NON-NLS-1$

  private static final Object ELEMENT_WIZARD_SELECTION = "wizardSelection"; //$NON-NLS-1$

  private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

  private static final String ELEMENT_WIZARD_REF = "wizardRef"; //$NON-NLS-1$

  private static final String ATTRIBUTE_WIZARD_ID = "wizardId"; //$NON-NLS-1$

  private MapExtensions( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @param wizardSelectionId
   *          The id of the wizard selection that should restrict the returned set of wizards. If <code>null</code>, all
   *          available wizards are returned.
   */
  public static WizardCollectionElement getAvailableWizards( final String wizardSelectionId )
  {
    final WizardsRegistryReader reader = new WizardsRegistryReader( KalypsoGisPlugin.PLUGIN_ID, EXTENSION_POINT_ADD_LAYER_WIZARD );
    final WizardCollectionElement wizardElements = reader.getWizardElements();

    if( wizardSelectionId == null )
      return wizardElements;

    final Set<String> positiveList = getWizardSelection( wizardSelectionId );
    return filterWizardElements( wizardElements, positiveList );
  }

  private static Set<String> getWizardSelection( final String wizardSelectionId )
  {
    final Set<String> selectedWizards = new LinkedHashSet<>();

    final IConfigurationElement selectionElement = findWizardSelection( wizardSelectionId );
    final IConfigurationElement[] children = selectionElement.getChildren( ELEMENT_WIZARD_REF );
    for( final IConfigurationElement child : children )
    {
      final String wizardId = child.getAttribute( ATTRIBUTE_WIZARD_ID );
      selectedWizards.add( wizardId );
    }

    return Collections.unmodifiableSet( selectedWizards );
  }

  private static IConfigurationElement findWizardSelection( final String wizardSelectionId )
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IExtensionPoint extensionPoint = registry.getExtensionPoint( KalypsoGisPlugin.PLUGIN_ID, EXTENSION_POINT_ADD_LAYER_WIZARD );
    final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
    for( final IConfigurationElement element : configurationElements )
    {
      if( ELEMENT_WIZARD_SELECTION.equals( element.getName() ) )
      {
        final String id = element.getAttribute( ATTRIBUTE_ID );
        if( id.equals( wizardSelectionId ) )
          return element;
      }
    }

    final String message = String.format( "Failed to find wizardSelection with id: %s", wizardSelectionId ); //$NON-NLS-1$
    final IStatus status = new Status( IStatus.WARNING, KalypsoGisPlugin.PLUGIN_ID, message );
    KalypsoGisPlugin.getDefault().getLog().log( status );

    return null;
  }

  private static WizardCollectionElement filterWizardElements( final WizardCollectionElement wizardElements, final Set<String> positiveList )
  {
    final Set<String> allWizards = new HashSet<>();
    findAllWizardIds( wizardElements, allWizards );

    for( final String wizardId : allWizards )
    {
      if( !positiveList.contains( wizardId ) )
        removeWizard( wizardElements, wizardId );
    }

    return wizardElements;
  }

  private static void findAllWizardIds( final WizardCollectionElement wizardElements, final Set<String> allWizards )
  {
    final IWizardDescriptor[] wizards = wizardElements.getWizards();
    for( final IWizardDescriptor descriptor : wizards )
      allWizards.add( descriptor.getId() );

    final WizardCollectionElement[] collectionElements = wizardElements.getCollectionElements();
    for( final WizardCollectionElement collectionElement : collectionElements )
      findAllWizardIds( collectionElement, allWizards );
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

  private static void removeWizard( final WizardCollectionElement wizards, final String id )
  {
    final IWizardDescriptor badWizard = wizards.findWizard( id );
    if( badWizard == null )
      return;

    final IWizardCategory category = badWizard.getCategory();
    if( !(category instanceof WizardCollectionElement) )
      return;

    final WizardCollectionElement collection = (WizardCollectionElement)category;
    collection.remove( badWizard );
  }
}