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
package org.kalypso.model.wspm.ui.featureview;

import java.net.URL;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.action.CommandWithStyle;
import org.kalypso.core.status.StatusComposite;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.view.chart.provider.IChartProvider;
import org.kalypso.ogc.gml.featureview.control.AbstractFeatureControl;
import org.kalypso.ogc.gml.featureview.control.composite.TabFolderCompositionControl;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

import de.openali.odysseus.chart.factory.config.ChartConfigurationLoader;
import de.openali.odysseus.chart.factory.config.ChartExtensionLoader;
import de.openali.odysseus.chart.factory.config.ChartFactory;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.util.ChartUtilities;
import de.openali.odysseus.chartconfig.x020.ChartType;
import de.openali.odysseus.chartconfig.x020.TitleType;

/**
 * @author Gernot Belger
 */
public class ChartFeatureControl extends AbstractFeatureControl
{
  /**
   * These settings are used locally to remember the last selected tab-folder.<br/>
   * Static because we want to keep the currently selected tab over selection of features.
   */
  private final static IDialogSettings SETTINGS = new DialogSettings( "bla" ); //$NON-NLS-1$

  private static final String STR_SETTINGS_TAB = "tabIndex"; //$NON-NLS-1$

  private final ChartConfigurationLoader m_ccl;

  private final ChartType[] m_chartTypes;

  private final URL m_context;

  private final CommandWithStyle[] m_commands;

  /**
   * The ID of the chart provider. May be null.
   */
  private final String m_chartProviderID;

  /**
   * The chart tabs.
   */
  private final ChartTabItem[] m_chartTabs;

  private final String m_featureKeyName;

  public ChartFeatureControl( final String featureKeyName, final Feature feature, final IPropertyType ftp, final ChartConfigurationLoader ccl, final ChartType[] chartTypes, final URL context, final CommandWithStyle[] commands, final String chartProviderID )
  {
    super( feature, ftp );

    m_featureKeyName = featureKeyName;
    m_ccl = ccl;
    m_chartTypes = chartTypes;
    m_context = context;
    m_commands = commands;
    m_chartProviderID = chartProviderID;
    m_chartTabs = new ChartTabItem[m_chartTypes.length];
  }

  @Override
  public Control createControl( final Composite parent, final int style )
  {
    /* If there are no tabs show a warning. */
    if( m_chartTabs.length == 0 )
    {
      final IStatus warningStatus = new Status( IStatus.WARNING, KalypsoModelWspmUIPlugin.ID, Messages.getString( "org.kalypso.model.wspm.ui.featureview.ChartFeatureControl.0" ) ); //$NON-NLS-1$
      final StatusComposite statusComposite = new StatusComposite( parent, SWT.NONE );
      statusComposite.setStatus( warningStatus );
      return statusComposite;
    }

    /* initialize charts */
    initCharts();

    /* Only one chart? */
    if( m_chartTabs.length == 1 )
    {
      final Control tabControl = m_chartTabs[0].createControl( parent, style );

      if( !hasData( m_chartTabs[0] ) )
        tabControl.setData( TabFolderCompositionControl.DATA_HIDE_TAB, Boolean.TRUE );

      /* Update the controls. */
      updateControl();

      return tabControl;
    }

    /* Only show tabs, which are not empty. */
    final TabFolder folder = new TabFolder( parent, SWT.TOP );

    /* Create a tab for each type. */
    for( int i = 0; i < m_chartTypes.length; i++ )
    {
      /* Check for each type, if it is empty... */
      final ChartType chartType = m_chartTypes[i];
      final ChartTabItem chartTabItem = m_chartTabs[i];

      if( !hasData( chartTabItem ) )
      {
        chartTabItem.dispose();
        m_chartTabs[i] = null;
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
      final Control tabControl = chartTabItem.createControl( folder, style );

      /* Set the chart tab to the tab. */
      item.setControl( tabControl );
    }

    final String selectedTabStr = SETTINGS.get( STR_SETTINGS_TAB );
    final int selectedTab = selectedTabStr == null ? 0 : Integer.parseInt( selectedTabStr );
    if( selectedTab < folder.getTabList().length )
    {
      folder.setSelection( selectedTab );
    }

    folder.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        handleFolderSelectionChanged( folder.getSelectionIndex() );
      }
    } );

    updateControl();

    return folder;
  }

  private void initCharts( )
  {
    final Feature feature = getFeature();

    /* Create a tab for each type. */
    for( int i = 0; i < m_chartTypes.length; i++ )
    {
      /* Check for each type, if it is empty... */
      final ChartType chartType = m_chartTypes[i];

      m_chartTabs[i] = new ChartTabItem( m_commands );

      final IChartModel chartModel = m_chartTabs[i].getChartModel();

      // remember the feature from the feature control
      chartModel.setData( m_featureKeyName, feature );

      /* Configure. */
      ChartFactory.doConfiguration( chartModel, m_ccl, chartType, ChartExtensionLoader.getInstance(), m_context );

      /* Configure via a chart provider. */
      doConfiguration( chartModel );

      /* Maximise. */
      ChartUtilities.maximize( chartModel );
    }
  }

  protected void handleFolderSelectionChanged( final int selectionIndex )
  {
    ChartFeatureControl.SETTINGS.put( ChartFeatureControl.STR_SETTINGS_TAB, selectionIndex );
  }

  @Override
  public boolean isValid( )
  {
    return true;
  }

  @Override
  public void updateControl( )
  {
    for( int i = 0; i < m_chartTypes.length; i++ )
    {
      final IChartModel chartModel = m_chartTabs[i].getChartModel();
      /* Maximise. */
      ChartUtilities.maximize( chartModel );
    }
  }

  private boolean hasData( final ChartTabItem chartItem )
  {
    final IChartModel chartModel = chartItem.getChartModel();

    /* Check, if all layers are emtpy. */
    final ILayerManager layerManager = chartModel.getLayerManager();
    final IChartLayer[] layers = layerManager.getLayers();
    for( final IChartLayer layer : layers )
    {
      final IDataRange< ? > domainRange = layer.getDomainRange();
      final IDataRange< ? > targetRange = layer.getTargetRange( null );
      if( domainRange != null && targetRange != null )
      {
        final Number domainMin = (Number)domainRange.getMin();
        final Number domainMax = (Number)domainRange.getMax();
        final Number targetMin = (Number)targetRange.getMin();
        final Number targetMax = (Number)targetRange.getMax();

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
        return (IChartProvider)element.createExecutableExtension( "class" ); //$NON-NLS-1$
    }

    return null;
  }

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