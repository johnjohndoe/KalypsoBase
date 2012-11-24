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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.action.CommandWithStyle;
import org.kalypso.contribs.eclipse.swt.SWTUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.view.chart.provider.IChartProvider;
import org.kalypso.ogc.gml.featureview.control.IExtensionsFeatureControlFactory2;
import org.kalypso.ogc.gml.featureview.control.IFeatureControl;
import org.kalypsodeegree.model.feature.Feature;

import de.openali.odysseus.chart.framework.model.IChartModel;

/**
 * A feature control which shows a chart. The chart configuration comes from the parameters of the extension, its
 * context will be the current feature.
 * <p>
 * Supported arguments:
 * <ul>
 * <li>configuration: urn to the configuration file for the chart. The urn will be resolved against the catalogue mechanism and after that relative to the current url.context (TODO: the last thing
 * does not work yet).</li>
 * <li>featureKeyName: The key-name under which the current feature will be put into the ChartDataProvider.</li>
 * </ul>
 * 
 * @author Gernot Belger
 */
public class ChartFeatureControlFactory implements IExtensionsFeatureControlFactory2
{
  private static final String PARAMETER_CONFIGURATION = "configuration"; //$NON-NLS-1$

  static final String PARAMETER_CHART = "chart"; //$NON-NLS-1$

  private static final String PARAMETER_FEATURE_KEY_NAME = "featureKeyName"; //$NON-NLS-1$

  private static final String PARAMETER_COMMANDS = "commands"; //$NON-NLS-1$

  private static final String PARAMETER_COMMAND_STYLES = "commandStyles"; //$NON-NLS-1$

  private static final String PARAMETER_CHART_PROVIDER_ID = "chartProviderID"; //$NON-NLS-1$

  @Override
  public IFeatureControl createFeatureControl( final FormToolkit toolkit, final Feature feature, final IPropertyType pt, final Properties arguments )
  {
    final String configurationUrn = arguments.getProperty( PARAMETER_CONFIGURATION, null );
    final String chartName = arguments.getProperty( PARAMETER_CHART, null );
    final String featureKeyName = arguments.getProperty( PARAMETER_FEATURE_KEY_NAME, null );
    final String cmdIds = arguments.getProperty( PARAMETER_COMMANDS, StringUtils.EMPTY );
    final String cmdStyles = arguments.getProperty( PARAMETER_COMMAND_STYLES, StringUtils.EMPTY );
    final String chartProviderID = arguments.getProperty( PARAMETER_CHART_PROVIDER_ID, null );

    final CommandWithStyle[] commands = parseCommands( cmdIds, cmdStyles );

    final String configurationUrl = KalypsoCorePlugin.getDefault().getCatalogManager().resolve( configurationUrn, configurationUrn );

    final ChartFeatureControl control = new ChartFeatureControl( featureKeyName, feature, pt, configurationUrl, chartName, commands )
    {
      @Override
      protected void handleChartLoaded( final IChartModel chartModel )
      {
        ChartFeatureControlFactory.this.onChartLoaded( chartModel, chartProviderID, this );
      }
    };

    return control;
  }

  private CommandWithStyle[] parseCommands( final String cmdIds, final String cmdStyles )
  {
    final String[] commandIds = StringUtils.splitPreserveAllTokens( cmdIds, ';' );
    final String[] commandStyles = StringUtils.splitPreserveAllTokens( cmdStyles, ';' );

    final Collection<CommandWithStyle> commands = new ArrayList<>();

    if( commandIds.length != commandStyles.length )
    {
      final String msg = Messages.getString( "org.kalypso.model.wspm.ui.featureview.ChartFeatureControlFactory.0", commandIds, commandIds.length, cmdStyles, commandStyles.length ); //$NON-NLS-1$
      final IStatus status = new Status( IStatus.WARNING, KalypsoModelWspmUIPlugin.ID, msg );
      KalypsoCorePlugin.getDefault().getLog().log( status );
    }
    else
    {
      for( int i = 0; i < commandIds.length; i++ )
      {
        final String cmdId = commandIds[i].trim();
        final String cmdStyle = commandStyles[i].trim();

        final int styleFromString = SWTUtilities.createStyleFromString( cmdStyle );
        final int style = styleFromString == 0 ? SWT.PUSH : styleFromString;
        commands.add( new CommandWithStyle( cmdId, style ) );
      }
    }

    return commands.toArray( new CommandWithStyle[commands.size()] );
  }

  void onChartLoaded( final IChartModel chartModel, final String chartProviderID, final ChartFeatureControl featureControl )
  {
    try
    {
      /* Get the chart provider. */
      final IChartProvider chartProvider = getChartProvider( chartProviderID );
      if( chartProvider == null )
        return;

      /* Get the feature. */
      final Feature feature = featureControl.getFeature();
      final IPropertyType property = featureControl.getFeatureTypeProperty();
      final Feature chartFeature = ChartFeatureControlComposite.getChartFeature( feature, property );

      /* Configure. */
      chartProvider.configure( chartModel, chartFeature );

      chartModel.autoscale();
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
    if( StringUtils.isBlank( chartProviderID ) )
      return null;

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
}