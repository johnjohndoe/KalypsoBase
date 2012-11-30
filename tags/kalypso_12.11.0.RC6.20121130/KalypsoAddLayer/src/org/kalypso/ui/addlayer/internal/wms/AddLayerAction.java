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
package org.kalypso.ui.addlayer.internal.wms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Event;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.i18n.Messages;
/**
 * Adds a layer to the chosen layers from the tree selection.
 *
 * @author Gernot Belger
 */
class AddLayerAction extends Action
{
  private final ImportWmsData m_data;

  private final StructuredViewer m_capabilitiesViewer;

  private final StructuredViewer m_layerViewer;

  public AddLayerAction( final StructuredViewer capabilitiesViewer, final StructuredViewer layerViewer, final ImportWmsData data )
  {
    m_capabilitiesViewer = capabilitiesViewer;
    m_layerViewer = layerViewer;
    m_data = data;

    setImageDescriptor( ImageProvider.IMAGE_STYLEEDITOR_FORWARD );
    setToolTipText( Messages.getString( "org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.8" ) ); //$NON-NLS-1$

    m_capabilitiesViewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        final boolean isEmpty = selection.isEmpty();
        setEnabled( !isEmpty );
      }
    } );
  }

  @Override
  public void runWithEvent( final Event event )
  {
    final IStructuredSelection selection = (IStructuredSelection) m_capabilitiesViewer.getSelection();
    if( selection.isEmpty() )
      return;

    final List<Layer> layers = new ArrayList<>();
    findLayers( selection.toArray(), layers );

    final IObservableSet chosenLayers = m_data.getChosenLayerSet();
    chosenLayers.addAll( layers );

    m_layerViewer.setSelection( new StructuredSelection( layers ) );
  }

  private void findLayers( final Object[] elements, final Collection<Layer> result )
  {
    for( final Object element : elements )
    {
      if( element instanceof Layer )
      {
        final Layer layer = (Layer) element;
        result.add( layer );

        findLayers( layer.getLayer(), result );
      }
    }
  }
}