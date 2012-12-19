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
package org.kalypso.chart.ui.editor;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.kalypso.commons.exception.CancelVisitorException;

import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor2;

/**
 * Saves the state of the current legend into a settings object.
 * 
 * @author Gernot Belger
 */
class SaveChartLegendStateVisitor implements IChartLayerVisitor2
{
  static final String SETTING_VISIBLE = "#visible"; //$NON-NLS-1$

  static final String SETTING_EXPANDED = "#expanded"; //$NON-NLS-1$

  static final String SETTING_SELECTED = "#selected"; //$NON-NLS-1$

  private final IDialogSettings m_settings;

  private final CheckboxTreeViewer m_treeViewer;

  private final Set<Object> m_selectedLayers = new HashSet<>();

  public SaveChartLegendStateVisitor( final IDialogSettings settings, final CheckboxTreeViewer treeViewer )
  {
    m_settings = settings;
    m_treeViewer = treeViewer;

    final IStructuredSelection selection = (IStructuredSelection)m_treeViewer.getSelection();
    m_selectedLayers.addAll( selection.toList() );
  }

  @Override
  public boolean getVisitDirection( )
  {
    return true;
  }

  @Override
  public boolean visit( final IChartLayer layer ) throws CancelVisitorException
  {
    final String identifier = layer.getIdentifier();

    if( StringUtils.isBlank( identifier ) )
      return true;

    final boolean selected = m_selectedLayers.contains( layer );
    final boolean expanded = m_treeViewer.getExpandedState( layer );
    final boolean visible = layer.isVisible();

    m_settings.put( identifier + SETTING_SELECTED, selected );
    m_settings.put( identifier + SETTING_EXPANDED, expanded );
    m_settings.put( identifier + SETTING_VISIBLE, visible );

    return true;
  }
}