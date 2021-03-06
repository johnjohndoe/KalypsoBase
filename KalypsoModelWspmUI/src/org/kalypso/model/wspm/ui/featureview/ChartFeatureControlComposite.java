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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.chart.ui.editor.commandhandler.ChartSourceProvider;
import org.kalypso.commons.eclipse.ui.EmbeddedSourceToolbarManager;
import org.kalypso.commons.java.lang.Doubles;
import org.kalypso.contribs.eclipse.jface.action.CommandWithStyle;
import org.kalypso.contribs.eclipse.swt.widgets.ControlUtils;
import org.kalypso.core.status.StatusComposite;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.impl.ChartImageComposite;

/**
 * @author Gernot Belger
 */
class ChartFeatureControlComposite extends Composite implements IChartPart
{
  private final CommandWithStyle[] m_commands;

  private ChartImageComposite m_chartComposite = null;

  private EmbeddedSourceToolbarManager m_sourceManager = null;

  private final IChartModel m_model;

  public ChartFeatureControlComposite( final IChartModel model, final CommandWithStyle[] commands, final Composite parent, final int style )
  {
    super( parent, style );

    m_model = model;
    m_commands = commands;

    GridLayoutFactory.fillDefaults().spacing( 0, 0 ).applyTo( this );

    final ToolBarManager manager = new ToolBarManager( SWT.HORIZONTAL | SWT.FLAT );
    final ToolBar toolBar = manager.createControl( this );

    m_chartComposite = new ChartImageComposite( this, SWT.BORDER, m_model, new RGB( 255, 255, 255 ) );
    m_chartComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    final IWorkbench sourceLocator = PlatformUI.getWorkbench();
    m_sourceManager = new EmbeddedSourceToolbarManager( sourceLocator, ChartSourceProvider.ACTIVE_CHART_NAME, ChartFeatureControlComposite.this.getChartComposite() );
    m_sourceManager.fillToolbar( manager, m_commands );

    // TODO: this is still an ugly place, the information which command to trigger (if any) should come from outside
    if( m_commands.length > 0 )
      EmbeddedSourceToolbarManager.executeCommand( sourceLocator, manager, m_commands[0].getCommandID() );

    final GridData toolbarData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    toolBar.setLayoutData( toolbarData );
    final boolean hasCommands = m_commands.length > 0;
    toolbarData.exclude = !hasCommands;
    toolBar.setVisible( hasCommands );

    parent.layout();

    ControlUtils.addDisposeListener( this );
  }

  @Override
  public IChartComposite getChartComposite( )
  {
    return m_chartComposite;
  }

  @Override
  public void dispose( )
  {
    if( m_sourceManager != null )
      m_sourceManager.dispose();
  }

  boolean hasData( )
  {
    /* Check, if all layers are emtpy. */
    final ILayerManager layerManager = m_model.getLayerManager();
    final IChartLayer[] layers = layerManager.getLayers();
    for( final IChartLayer layer : layers )
    {
      // FIXME: we should hasData instead
      // layer.hasData();

      final IDataRange< ? > domainRange = layer.getDomainRange();
      final IDataRange< ? > targetRange = layer.getTargetRange( null );
      if( domainRange != null && targetRange != null )
      {
        final Number domainMin = (Number)domainRange.getMin();
        final Number domainMax = (Number)domainRange.getMax();
        final Number targetMin = (Number)targetRange.getMin();
        final Number targetMax = (Number)targetRange.getMax();

        final boolean hasValues = !Doubles.isNullOrInfinite( domainMin, domainMax, targetMin, targetMax );
        if( hasValues )
          return true;
      }
    }

    return false;
  }

  static Feature getChartFeature( final Feature feature, final IPropertyType pt )
  {
    final Object property = pt == null ? null : feature.getProperty( pt );
    final Feature childFeature = FeatureHelper.getFeature( feature.getWorkspace(), property );
    return childFeature == null ? feature : childFeature;
  }

  public void addStatus( final IStatus result )
  {
    /* hide toolbar and chart */
    final Control[] children = m_chartComposite.getParent().getChildren();
    for( final Control child : children )
    {
      final GridData layoutData = (GridData)child.getLayoutData();
      layoutData.exclude = !result.isOK();
      child.setVisible( false );
    }

    /* show status */
    final StatusComposite statusComposite = new StatusComposite( this, SWT.NONE );
    statusComposite.setStatus( result );
    statusComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );

    layout();
  }
}