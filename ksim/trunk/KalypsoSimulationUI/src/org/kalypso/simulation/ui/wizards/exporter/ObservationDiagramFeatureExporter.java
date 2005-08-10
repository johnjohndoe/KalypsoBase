/*--------------- Kalypso-Header ------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 --------------------------------------------------------------------------*/

package org.kalypso.simulation.ui.wizards.exporter;

import java.awt.Color;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.commons.java.io.ReaderUtilities;
import org.kalypso.commons.java.net.UrlResolver;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.metadoc.IExportableObject;
import org.kalypso.metadoc.configuration.IPublishingConfiguration;
import org.kalypso.ogc.sensor.diagview.DiagView;
import org.kalypso.ogc.sensor.diagview.DiagViewUtils;
import org.kalypso.ogc.sensor.diagview.jfreechart.ExportableChart;
import org.kalypso.ogc.sensor.diagview.jfreechart.ObservationChart;
import org.kalypso.template.obsdiagview.ObsdiagviewType;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.calcwizard.bericht.ExporterHelper;
import org.kalypsodeegree.model.feature.Feature;

/**
 * Exports diagrams from features
 * 
 * @author schlienger
 */
public class ObservationDiagramFeatureExporter extends AbstractFeatureExporter
{
  /**
   * @see org.kalypso.metadoc.IExportableObjectFactory#createExportableObjects(org.apache.commons.configuration.Configuration)
   */
  public IExportableObject[] createExportableObjects( final Configuration configuration ) throws CoreException
  {
    final Arguments arguments = (Arguments)getFromSupplier( "arguments" );
    final URL m_context = (URL)getFromSupplier( "context" );

    final String templateurl = arguments.getProperty( "template", null );
    final int width = Integer.parseInt( arguments.getProperty( "width", "800" ) );
    final int height = Integer.parseInt( arguments.getProperty( "height", "600" ) );
    final Arguments tokens = arguments.getArguments( "tokens" );

    final List exports = new ArrayList();

    final Feature[] features = getSelectedFeatures();
    for( int i = 0; i < features.length; i++ )
    {
      final Feature feature = features[i];
      final String featurename = arguments.getProperty( "nameproperty", "Name" );
      final Object nameProp = feature.getProperty( featurename );
      final String name = nameProp == null ? "<unbekannt>" : nameProp.toString();

      DiagView tpl = null;
      ObservationChart chart = null;
      try
      {
        // - replacetokens / featureprops
        final Properties replacetokens = ExporterHelper.createReplaceTokens( feature, tokens );

        final URL url = new UrlResolver().resolveURL( m_context, templateurl );
        final URLConnection connection = url.openConnection();
        final Reader reader = new InputStreamReader( connection.getInputStream(), "UTF-8" );
        final Reader reader2 = ReaderUtilities.createTokenReplaceReader( reader, replacetokens, '%', '%' );

        final ObsdiagviewType xml = DiagViewUtils.loadDiagramTemplateXML( reader2 );

        tpl = new DiagView();
        final MultiStatus result = new MultiStatus( KalypsoGisPlugin.getId(), 0, this.toString() + " - " + name + ": ",
            null );
        DiagViewUtils.applyXMLTemplate( tpl, xml, m_context, true, result );

        chart = new ObservationChart( tpl );
        chart.setBackgroundPaint( Color.WHITE );
        final ExportableChart expChart = new ExportableChart( chart, arguments.getProperty(
            "imageFormat", ExportableChart.DEFAULT_FORMAT ), width, height );

        exports.add( expChart );
      }
      catch( final Exception e )
      {
        e.printStackTrace();

        if( e instanceof CoreException )
          throw (CoreException)e;

        throw new CoreException( RunnableContextHelper.statusFromThrowable( e ) );
      }
      finally
      {
        if( chart != null )
          chart.dispose();
        if( tpl != null )
          tpl.dispose();
      }
    }

    return (IExportableObject[])exports.toArray( new IExportableObject[exports.size()] );
  }

  /**
   * @see org.kalypso.simulation.ui.wizards.exporter.AbstractFeatureExporter#contributeWizardPages(java.util.List, org.kalypso.metadoc.configuration.IPublishingConfiguration)
   */
  protected void contributeWizardPages( List pages, IPublishingConfiguration configuration ) throws CoreException
  {
    // empty
  }
}
