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

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.swt.SWTUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.ogc.gml.featureview.control.IExtensionsFeatureControlFactory2;
import org.kalypso.ogc.gml.featureview.control.IFeatureControl;
import org.kalypsodeegree.model.feature.Feature;

import de.openali.odysseus.chart.factory.config.ChartConfigurationLoader;
import de.openali.odysseus.chartconfig.x020.ChartType;

/**
 * A feature control which shows a chart. The chart configuration comes from the parameters of the extension, its
 * context will be the current feature.
 * <p>
 * Supported arguments:
 * <ul>
 * <li>configuration: urn to the configuration file for the chart. The urn will be resolved against the catalogue
 * mechanism and after that relative to the current url.context (TODO: the last thing does not work yet).</li>
 * <li>featureKeyName: The key-name under which the current feature will be put into the ChartDataProvider.</li>
 * </ul>
 * 
 * @author Gernot Belger
 */
public class ChartFeatureControlFactory implements IExtensionsFeatureControlFactory2
{
  @Override
  public IFeatureControl createFeatureControl( final FormToolkit toolkit, final Feature feature, final IPropertyType pt, final Properties arguments ) throws CoreException
  {
    final String configurationUrn = arguments.getProperty( "configuration", null ); //$NON-NLS-1$
    final String chartName = arguments.getProperty( "chart", null ); //$NON-NLS-1$
    final String featureKeyName = arguments.getProperty( "featureKeyName", null ); //$NON-NLS-1$
    final String cmdIds = arguments.getProperty( "commands", "" ); //$NON-NLS-1$ //$NON-NLS-2$
    final String cmdStyles = arguments.getProperty( "commandStyles", "" ); //$NON-NLS-1$ //$NON-NLS-2$
    final String chartProviderID = arguments.getProperty( "chartProviderID", null ); //$NON-NLS-1$

    final String[] commandIds = cmdIds.split( ";" ); //$NON-NLS-1$
    final String[] commandStyles = cmdStyles.split( ";" ); //$NON-NLS-1$

    final Map<String, Integer> commands = new LinkedHashMap<String, Integer>();

    if( commandIds.length != commandStyles.length )
    {
      final String msg = Messages.getString( "org.kalypso.model.wspm.ui.featureview.ChartFeatureControlFactory.0", commandIds, commandIds.length, cmdStyles, commandStyles.length ); //$NON-NLS-1$
      final IStatus status = StatusUtilities.createStatus( IStatus.WARNING, msg, null );
      KalypsoCorePlugin.getDefault().getLog().log( status );
    }
    else
    {
      for( int i = 0; i < commandIds.length; i++ )
      {
        final String cmdId = commandIds[i].trim();
        final String cmdStyle = commandStyles[i].trim();

        if( cmdId.length() > 0 && cmdStyle.length() > 0 )
        {
          final int styleFromString = SWTUtilities.createStyleFromString( cmdStyle );
          final int style = styleFromString == 0 ? SWT.PUSH : styleFromString;
          commands.put( cmdId, style );
        }
      }
    }

    final String configurationUrl = KalypsoCorePlugin.getDefault().getCatalogManager().getBaseCatalog().resolve( configurationUrn, configurationUrn );

    try
    {
      // TODO: it should also be possible to use a url-context here in order to load .kod relative to the gft file
      // IN order to do this, we need the context-url from the framework.
      final URL configUrl = new URL( configurationUrl );

      final ChartConfigurationLoader ccl = new ChartConfigurationLoader( configUrl );

      final ChartType[] chartTypes;
      if( chartName == null )
        chartTypes = ccl.getCharts();
      else
      {
        chartTypes = new ChartType[1];
        for( final ChartType chartType : ccl.getCharts() )
        {
          if( chartType.getId().equals( chartName ) )
            chartTypes[0] = chartType;
        }
      }

      final Feature chartFeature = ChartFeatureControl.getChartFeature( feature, pt );

      ChartDataProvider.FEATURE_MAP.put( featureKeyName, chartFeature );

      return new ChartFeatureControl( chartFeature, pt, ccl, chartTypes, configUrl, commands, chartProviderID );
    }
    catch( final Throwable e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e );
      throw new CoreException( status );
    }
  }
}