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
package org.kalypso.model.wspm.ui.featureview;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.chart.ui.editor.commandhandler.ChartSourceProvider;
import org.kalypso.commons.eclipse.ui.EmbeddedSourceToolbarManager;
import org.kalypso.contribs.eclipse.jface.action.CommandWithStyle;
import org.kalypsodeegree.model.feature.Feature;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.impl.ChartModel;
import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.impl.ChartImageComposite;

/**
 * Class for charts inserted as tabs into the chart feature control; this has to be isolated in a seperate class as each
 * IChartPart can only return one ChartComposite and one ChartDragHandler
 *
 * @author burtscher1
 */
public class ChartTabItem extends Composite implements IChartPart
{
  private final ChartImageComposite m_chartComposite;

  private final EmbeddedSourceToolbarManager m_sourceManager;

  private final IChartModel m_chartModel;

  // private final ZmlDiagramLayerListener m_layerManagerListener;

  public ChartTabItem( final String featureKeyName, final Feature feature, final Composite parent, final int style, final CommandWithStyle[] commands )
  {
    super( parent, style );

    GridLayoutFactory.fillDefaults().spacing( 0, 0 ).applyTo( this );

    final ToolBarManager manager = new ToolBarManager( SWT.HORIZONTAL | SWT.FLAT );
    final ToolBar toolBar = manager.createControl( this );

    m_chartModel = new ChartModel();

    // FIXME: only works for zml layers... -> we need a beeter concept
    // m_layerManagerListener = new ZmlDiagramLayerListener( m_chartModel );
    // m_chartModel.getLayerManager().getEventHandler().addListener( m_layerManagerListener );

    m_chartModel.dispose();

    // remember the feature from the feature control
    m_chartModel.setData( featureKeyName, feature );

    m_chartComposite = new ChartImageComposite( this, SWT.BORDER, m_chartModel, new RGB( 255, 255, 255 ) );
    m_chartComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    final IWorkbench sourceLocator = PlatformUI.getWorkbench();
    m_sourceManager = new EmbeddedSourceToolbarManager( sourceLocator, ChartSourceProvider.ACTIVE_CHART_NAME, ChartTabItem.this.getChartComposite() );
    m_sourceManager.fillToolbar( manager, commands );

    // TODO: this is still an ugly place, the information which command to treigger (if any) should come from outside
    if( commands.length > 0 )
      EmbeddedSourceToolbarManager.executeCommand( sourceLocator, manager, commands[0].getCommandID() );

    final GridData toolbarData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    toolBar.setLayoutData( toolbarData );
    final boolean hasCommands = commands.length > 0;
    toolbarData.exclude = !hasCommands;
    toolBar.setVisible( hasCommands );
    layout();
  }

  @Override
  public IChartComposite getChartComposite( )
  {
    return m_chartComposite;
  }

  @Override
  public void dispose( )
  {
    m_sourceManager.dispose();

    if( m_chartComposite != null && !m_chartComposite.isDisposed() )
      m_chartComposite.dispose();

    // m_chartModel.getLayerManager().getEventHandler().removeListener( m_layerManagerListener );
    m_chartModel.dispose();
  }
}