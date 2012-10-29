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

import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Event;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.i18n.Messages;

/**
 * Removes layers from the list of chosen layers.
 *
 * @author Gernot Belger
 */
public class RemoveLayerAction extends Action
{
  private final ListViewer m_layerViewer;

  private final ImportWmsData m_data;

  public RemoveLayerAction( final ListViewer layerViewer, final ImportWmsData data )
  {
    m_layerViewer = layerViewer;
    m_data = data;

    setImageDescriptor( ImageProvider.IMAGE_STYLEEDITOR_REMOVE );
    setToolTipText( Messages.getString( "org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.9" ) ); //$NON-NLS-1$

    m_layerViewer.addSelectionChangedListener( new ISelectionChangedListener()
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
    final IStructuredSelection selection = (IStructuredSelection) m_layerViewer.getSelection();
    final IObservableSet chosenLayerSet = m_data.getChosenLayerSet();

    chosenLayerSet.removeAll( selection.toList() );

    m_layerViewer.refresh();
  }
}