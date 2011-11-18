/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import java.net.URL;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.status.StatusComposite;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.view.chart.provider.IChartProvider;
import org.kalypso.ogc.gml.featureview.control.AbstractFeatureControl;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

import de.openali.odysseus.chart.factory.config.ChartConfigurationLoader;
import de.openali.odysseus.chart.factory.config.ChartExtensionLoader;
import de.openali.odysseus.chart.factory.config.ChartFactory;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.impl.ChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.util.ChartUtilities;
import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.impl.ChartImageComposite;
import de.openali.odysseus.chartconfig.x020.ChartType;
import de.openali.odysseus.chartconfig.x020.TitleType;

/**
 * @author Gernot Belger
 */
public class ChartFeatureControl extends AbstractFeatureControl
{
  /**
   * These settings are used locally to remember the last selected tab-folder.
   */
  private static final IDialogSettings SETTINGS = new DialogSettings( "bla" ); //$NON-NLS-1$

  private static final String STR_SETTINGS_TAB = "tabIndex"; //$NON-NLS-1$

  private final ChartConfigurationLoader m_ccl;

  private final ChartType[] m_chartTypes;

  private final URL m_context;

  private final Map<String, Integer> m_commands;

  /**
   * The ID of the chart provider. May be null.
   */
  private final String m_chartProviderID;

  /**
   * The chart tabs.
   */
  private ChartTabItem[] m_chartTabs;

  /**
   * The chart composites. Each corresponds to one tab.
   */
  private IChartComposite[] m_charts;

  private final String m_featureKeyName;

  /**
   * The constructor.
   * 
   * @param feature
   * @param ftp
   * @param ccl
   * @param chartTypes
   * @param context
   * @param commands
   * @param chartProviderID
   */
  public ChartFeatureControl( final String featureKeyName, final Feature feature, final IPropertyType ftp, final ChartConfigurationLoader ccl, final ChartType[] chartTypes, final URL context, final Map<String, Integer> commands, final String chartProviderID )
  {
    super( feature, ftp );
    
    m_featureKeyName = featureKeyName;
    m_ccl = ccl;
    m_chartTypes = chartTypes;
    m_context = context;
    m_commands = commands;
    m_chartProviderID = chartProviderID;
    m_chartTabs = null;
    m_charts = null;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.IFeatureControl#createControl(org.eclipse.swt.widgets.Composite, int)
   */
  @Override
  public Control createControl( final Composite parent, final int style )
  {
    /* Prepare the chart tabs and the charts. */
    m_chartTabs = new ChartTabItem[m_chartTypes.length];
    m_charts = new ChartImageComposite[m_chartTypes.length];

    /* If there are no tabs show a warning. */
    if( m_chartTabs.length == 0 )
    {
      final IStatus warningStatus = StatusUtilities.createStatus( IStatus.WARNING, org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.featureview.ChartFeatureControl.0" ), null ); //$NON-NLS-1$
      final StatusComposite statusComposite = new StatusComposite( parent, SWT.NONE );
      statusComposite.setStatus( warningStatus );
      return statusComposite;
    }

    /* Only one chart? */
    if( m_chartTabs.length == 1 )
    {
      /* REMARK: We do not tab, if we have only one chart. */
      m_chartTabs[0] = new ChartTabItem( m_featureKeyName, getFeature(), parent, style, m_commands );

      /* Update the controls. */
      updateControl();

      return m_chartTabs[0];
    }

    /* Only show tabs, which are not empty. */
    final TabFolder folder = new TabFolder( parent, SWT.TOP );

    /* Create a tab for each type. */
    for( int i = 0; i < m_chartTypes.length; i++ )
    {
      /* Check for each type, if it is empty... */
      final ChartType chartType = m_chartTypes[i];
      if( !hasData( chartType ) )
      {
        m_chartTabs[i] = null;
        m_charts[i] = null;
        continue;
      }

      /* The tab item */
      final TabItem item = new TabItem( folder, SWT.NONE );

      /* Set the title. */
      final TitleType[] title = chartType.getTitleArray();
      if( !ArrayUtils.isEmpty( title ) )
      {
        item.setText( title[0].getStringValue() );
      }

      /* Set the tooltip. */
      item.setToolTipText( chartType.getDescription() );

      /* Create the chart tab. */
      m_chartTabs[i] = new ChartTabItem( m_featureKeyName, getFeature(), folder, style, m_commands );

      /* Set the chart tab to the tab. */
      item.setControl( m_chartTabs[i] );
    }

    final String selectedTabStr = SETTINGS.get( STR_SETTINGS_TAB );
    final int selectedTab = selectedTabStr == null ? 0 : Integer.parseInt( selectedTabStr );
    if( selectedTab < folder.getTabList().length )
    {
      folder.setSelection( selectedTab );
    }

    folder.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        handleFolderSelectionChanged( folder.getSelectionIndex() );
      }
    } );

    updateControl();

    return folder;
  }

  protected void handleFolderSelectionChanged( final int selectionIndex )
  {
    ChartFeatureControl.SETTINGS.put( ChartFeatureControl.STR_SETTINGS_TAB, selectionIndex );
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.IFeatureControl#isValid()
   */
  @Override
  public boolean isValid( )
  {
    return true;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.IFeatureControl#addModifyListener(org.eclipse.swt.events.ModifyListener)
   */
  @Override
  public void addModifyListener( final ModifyListener l )
  {
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.IFeatureControl#removeModifyListener(org.eclipse.swt.events.ModifyListener)
   */
  @Override
  public void removeModifyListener( final ModifyListener l )
  {
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.IFeatureControl#updateControl()
   */
  @Override
  public void updateControl( )
  {
    /* HINT: m_chartTypes, m_chartTabs and m_charts will have the same length. */
    for( int i = 0; i < m_chartTabs.length; i++ )
    {
      /* Configured tabs, for whose there is no data, will have no chart tabs. */
      if( m_chartTabs[i] == null )
      {
        continue;
      }

      /* If the chart was previously loaded, it will contain layers - these have to be removed. */
      final IChartComposite chart = m_chartTabs[i].getChartComposite();
      final IChartModel chartModel = chart.getChartModel();
      final ILayerManager lm = chartModel.getLayerManager();
      final IChartLayer[] layers = lm.getLayers();
      for( final IChartLayer chartLayer : layers )
      {
        lm.removeLayer( chartLayer );
      }
      chartModel.getSettings().clearTitles();

      /* Configure. */
      ChartFactory.doConfiguration( chartModel, m_ccl, m_chartTypes[i], ChartExtensionLoader.getInstance(), m_context );

      /* Configure via a chart provider. */
      doConfiguration( chartModel );

      /* Maximise. */
      ChartUtilities.maximize( chartModel );
    }
  }

  private boolean hasData( final ChartType chartType )
  {
    /* Make a dummy chart model. */
    final IChartModel chartModel = new ChartModel();

    /* Configure. */
    ChartFactory.doConfiguration( chartModel, m_ccl, chartType, ChartExtensionLoader.getInstance(), m_context );

    /* Configure via a chart provider. */
    doConfiguration( chartModel );

    /* Check, if all layers are emtpy. */
    final ILayerManager layerManager = chartModel.getLayerManager();
    final IChartLayer[] layers = layerManager.getLayers();
    for( final IChartLayer layer : layers )
    {
      final IDataRange<Number> domainRange = layer.getDomainRange();
      final IDataRange<Number> targetRange = layer.getTargetRange( null );
      if( domainRange != null && targetRange != null )
      {
        final Number domainMin = domainRange.getMin();
        final Number domainMax = domainRange.getMax();
        final Number targetMin = targetRange.getMin();
        final Number targetMax = targetRange.getMax();

        if( domainMin.doubleValue() > Double.NEGATIVE_INFINITY )
          return true;

        if( domainMax.doubleValue() < Double.POSITIVE_INFINITY )
          return true;

        if( targetMin.doubleValue() > Double.NEGATIVE_INFINITY )
          return true;

        if( targetMax.doubleValue() < Double.POSITIVE_INFINITY )
          return true;

        return false;
      }
    }

    return false;
  }

  /**
   * This function configures the chart via a chart provider.
   * 
   * @param chartModel
   *          The chart model.
   */
  private void doConfiguration( final IChartModel chartModel )
  {
    try
    {
      /* Get the chart provider. */
      final IChartProvider chartProvider = getChartProvider( m_chartProviderID );
      if( chartProvider == null )
        return;

      /* Get the feature. */
      final Feature feature = getChartFeature( getFeature(), getFeatureTypeProperty() );

      /* Configure. */
      chartProvider.configure( chartModel, feature );
    }
    catch( final CoreException ex )
    {
      /* Log the error message. */
      KalypsoModelWspmUIPlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( ex ) );
    }
  }

  /**
   * This function looks up a chart provider with the given ID.
   * 
   * @param chartProviderID
   *          The ID of the chart provider.
   * @return The chart provider or null.
   */
  private IChartProvider getChartProvider( final String chartProviderID ) throws CoreException
  {
    /* Get the extension registry. */
    final IExtensionRegistry registry = Platform.getExtensionRegistry();

    /* Get all elements for the extension point. */
    final IConfigurationElement[] elements = registry.getConfigurationElementsFor( "org.kalypso.model.wspm.ui.chartProvider" ); //$NON-NLS-1$
    for( final IConfigurationElement element : elements )
    {
      /* Get the id. */
      final String id = element.getAttribute( "id" ); //$NON-NLS-1$
      if( id != null && id.length() > 0 && id.equals( chartProviderID ) )
        return (IChartProvider) element.createExecutableExtension( "class" ); //$NON-NLS-1$
    }

    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.AbstractFeatureControl#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_chartTabs != null )
    {
      for( final ChartTabItem item : m_chartTabs )
      {
        if( item != null )
        {
          item.dispose();
        }
      }
    }

    super.dispose();
  }

  public static Feature getChartFeature( final Feature feature, final IPropertyType pt )
  {
    final Object property = pt == null ? null : feature.getProperty( pt );
    final Feature childFeature = FeatureHelper.getFeature( feature.getWorkspace(), property );
    final Feature chartFeature = childFeature == null ? feature : childFeature;
    return chartFeature;
  }
}