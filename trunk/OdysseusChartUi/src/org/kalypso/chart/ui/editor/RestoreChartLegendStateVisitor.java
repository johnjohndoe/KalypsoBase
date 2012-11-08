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
import org.eclipse.jface.viewers.StructuredSelection;
import org.kalypso.commons.exception.CancelVisitorException;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;

import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor2;

/**
 * Saves the state of the current legend into a settings object.
 * 
 * @author Gernot Belger
 */
class RestoreChartLegendStateVisitor implements IChartLayerVisitor2
{
  private final IDialogSettings m_settings;

  private final CheckboxTreeViewer m_treeViewer;

  private final Set<Object> m_selectedLayers = new HashSet<>();

  private final Set<Object> m_expandedLayers = new HashSet<>();

  public RestoreChartLegendStateVisitor( final IDialogSettings settings, final CheckboxTreeViewer treeViewer )
  {
    m_settings = settings;
    m_treeViewer = treeViewer;
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

    /* selection */
    if( DialogSettingsUtils.hasSetting( m_settings, identifier + SaveChartLegendStateVisitor.SETTING_SELECTED ) )
    {
      final boolean selected = m_settings.getBoolean( identifier + SaveChartLegendStateVisitor.SETTING_SELECTED );
      if( selected )
        m_selectedLayers.add( layer );
    }

    /* visibility */
    if( DialogSettingsUtils.hasSetting( m_settings, identifier + SaveChartLegendStateVisitor.SETTING_VISIBLE ) )
    {
      final boolean visible = m_settings.getBoolean( identifier + SaveChartLegendStateVisitor.SETTING_VISIBLE );
      layer.setVisible( visible );
    }

    /* expansion state */
    if( DialogSettingsUtils.hasSetting( m_settings, identifier + SaveChartLegendStateVisitor.SETTING_EXPANDED ) )
    {
      final boolean expanded = m_settings.getBoolean( identifier + SaveChartLegendStateVisitor.SETTING_EXPANDED );
      if( expanded )
        m_expandedLayers.add( layer );
    }

    return true;
  }

  public void applyState( )
  {
    m_treeViewer.setSelection( new StructuredSelection( m_selectedLayers.toArray() ) );

    m_treeViewer.setExpandedElements( m_expandedLayers.toArray() );
  }
}