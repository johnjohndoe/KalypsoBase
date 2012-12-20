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
package org.kalypso.model.wspm.ui.featureview;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlbeans.XmlException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.i18n.Messages;

import de.openali.odysseus.chart.factory.config.ChartConfigurationLoader;
import de.openali.odysseus.chart.factory.config.ChartExtensionLoader;
import de.openali.odysseus.chart.factory.config.ChartFactory;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.util.ChartUtilities;
import de.openali.odysseus.chartconfig.x020.ChartType;

/**
 * @author Gernot Belger
 */
class ChartLoaderJob extends Job
{
  private final IChartModel m_chartModel;

  private final String m_configurationUrl;

  private final String m_chartName;

  public ChartLoaderJob( final IChartModel chartModel, final String configurationUrl, final String chartName )
  {
    super( Messages.getString("ChartLoaderJob_0") ); //$NON-NLS-1$

    m_chartModel = chartModel;
    m_configurationUrl = configurationUrl;
    m_chartName = chartName;
  }

  @Override
  protected IStatus run( final IProgressMonitor monitor )
  {
    try
    {
      // TODO: it should also be possible to use a url-context here in order to load .kod relative to the gft file
      // In order to do this, we need the context-url from the framework.
      final URL configUrl = new URL( m_configurationUrl );

      final ChartConfigurationLoader ccl = new ChartConfigurationLoader( configUrl );
      final ChartType[] charts = ccl.getCharts();

      final ChartType chartType = findChartType( charts );

      /* Configure. */
      ChartFactory.doConfiguration( m_chartModel, ccl, chartType, ChartExtensionLoader.getInstance(), configUrl );

      /* Configure via a chart provider. */
      // doConfiguration( m_chartModel );

      /* Maximise. */
      ChartUtilities.maximize( m_chartModel );

      return Status.OK_STATUS;
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();

      final String message = String.format( "Bad loacation '%s'", m_configurationUrl ); //$NON-NLS-1$
      return new Status( IStatus.WARNING, KalypsoModelWspmUIPlugin.ID, message, e );
    }
    catch( final XmlException e )
    {
      e.printStackTrace();

      final String message = String.format( "Bad .kod file '%s'", m_configurationUrl ); //$NON-NLS-1$
      return new Status( IStatus.WARNING, KalypsoModelWspmUIPlugin.ID, message, e );
    }
    catch( final IOException e )
    {
      e.printStackTrace();

      final String message = String.format( "Failed to load '%s'", m_configurationUrl ); //$NON-NLS-1$
      return new Status( IStatus.WARNING, KalypsoModelWspmUIPlugin.ID, message, e );
    }
    catch( final CoreException e )
    {
      return e.getStatus();
    }
    catch( final Throwable e )
    {
      final String message = String.format( "Unexpected error while initializing chart '%s'", m_configurationUrl ); //$NON-NLS-1$
      return new Status( IStatus.ERROR, KalypsoModelWspmUIPlugin.ID, message, e );
    }
  }

  private ChartType findChartType( final ChartType[] charts ) throws CoreException
  {
    if( m_chartName == null )
    {
      if( charts.length == 0 )
      {
        final IStatus status = new Status( IStatus.WARNING, KalypsoModelWspmUIPlugin.ID, "kod file contains no chart" ); //$NON-NLS-1$
        throw new CoreException( status );
      }

      if( charts.length == 1 )
        return charts[0];

      if( charts.length > 1 )
      {
        final String message = String.format( "kod file contains more than one chart. You need to specify which to use with parameter '%s'", ChartFeatureControlFactory.PARAMETER_CHART ); //$NON-NLS-1$
        final IStatus status = new Status( IStatus.WARNING, KalypsoModelWspmUIPlugin.ID, message );
        throw new CoreException( status );
      }
    }
    else
    {
      for( final ChartType chartType : charts )
      {
        if( chartType.getId().equals( m_chartName ) )
          return chartType;
      }

      final String message = String.format( "Chart with id '%s' not found", m_chartName ); //$NON-NLS-1$
      final IStatus status = new Status( IStatus.ERROR, KalypsoModelWspmUIPlugin.ID, message );
      throw new CoreException( status );
    }

    throw new IllegalStateException();
  }

  /**
   * This function configures the chart via a chart provider.
   *
   * @param chartModel
   *          The chart model.
   */
  // FIXME: only used in inform.dss, probably we can solve this in another way, the layer provider can fetch the feature from the chart data!
  // FIXME: deactivated for now; instead, the layer provider should fetch the feature via the feature key mechanism, or, more kod style, explicitely define layers via kod.
//  private void doConfiguration( final IChartModel chartModel, final Feature feature, final IPropertyType ftp )
//  {
//    try
//    {
    /* Get the chart provider. */
//      final IChartProvider chartProvider = getChartProvider( m_chartProviderID );
//      if( chartProvider == null )
//        return;

    /* Get the feature. */
//      final Feature chartFeature = null; // getChartFeature( feature, ftp );

    /* Configure. */
//      chartProvider.configure( chartModel, chartFeature );
//    }
//    catch( final CoreException ex )
//    {
    /* Log the error message. */
//      KalypsoModelWspmUIPlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( ex ) );
//    }
//  }

//  /**
//   * This function looks up a chart provider with the given ID.
//   *
//   * @param chartProviderID
//   *          The ID of the chart provider.
//   * @return The chart provider or null.
//   */
//  private IChartProvider getChartProvider( final String chartProviderID ) throws CoreException
//  {
//    /* Get the extension registry. */
//    final IExtensionRegistry registry = Platform.getExtensionRegistry();
//
//    /* Get all elements for the extension point. */
//    final IConfigurationElement[] elements = registry.getConfigurationElementsFor( "org.kalypso.model.wspm.ui.chartProvider" ); //$NON-NLS-1$
//    for( final IConfigurationElement element : elements )
//    {
//      /* Get the id. */
//      final String id = element.getAttribute( "id" ); //$NON-NLS-1$
//      if( id != null && id.length() > 0 && id.equals( chartProviderID ) )
//        return (IChartProvider)element.createExecutableExtension( "class" ); //$NON-NLS-1$
//    }
//
//    return null;
//  }
}