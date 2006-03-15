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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.wizard.ArrayChooserPage;
import org.kalypso.metadoc.IExportableObject;
import org.kalypso.metadoc.configuration.IPublishingConfiguration;
import org.kalypso.simulation.ui.KalypsoSimulationUIPlugin;
import org.kalypso.simulation.ui.wizards.exporter.ExporterHelper.UrlArgument;
import org.kalypsodeegree.model.feature.Feature;

/**
 * This explorer can handle the export of observations defined in the properties of features using templates. It uses
 * the {@link org.kalypso.simulation.ui.wizards.exporter.AbstractFeatureExporter}for basic feature selection
 * functionality. It exports a document for each selected feature and each exporter.
 * <p>
 * The typical arguments syntax for this kind of exporter is as follows:
 * 
 * <pre>
 &lt;arg name="exporterSomeName"&gt;
 &lt;arg name="id" value="featureWithTemplateExporter" /&gt; 
 &lt;arg name="name" value="Vorhersage Export" /&gt;
 &lt;arg name="nameproperty" value="Name" /&gt; 
 &lt;arg name="width" value="1024" /&gt; 
 &lt;arg name="height" value="768" /&gt; 
 &lt;arg name="imageFormat" value="png" /&gt; 
 &lt;arg name="tokens"&gt;
 &lt;arg name="title" value="Name" /&gt; 
 &lt;arg name="href1" value="Wasserstand_gemessen" /&gt; 
 &lt;arg name="href2" value="Wasserstand_gerechnet" /&gt; 
 &lt;arg name="href3" value="Wasserstand_vorhersage" /&gt; 
 &lt;arg name="href4" value="Niederschlag_rechnung" /&gt; 
 &lt;/arg&gt;
 &lt;arg name="templateDiagQ"&gt;
 &lt;arg name="label" value="Abfluﬂgrafik" /&gt; 
 &lt;arg name="category" value="Abfluﬂgrafik" /&gt;
 &lt;arg name="templateFile" value="project:/.templates/calcCase/berichtDiagQ.odt" /&gt; 
 &lt;/arg&gt;
 &lt;/arg&gt;
 * </pre>
 * 
 * <p>
 * The argument "category" is optional and can be defined for each template. It defines the category of the
 * ExportableObject that can be created based on the given template. If it is not specified, then the category defaults
 * to the value of the "label" argument.
 * </p>
 * 
 * <p>
 * This exporter has two wizard pages:
 * <nl>
 * <li>the selection of features
 * <li>the selection of the template to use
 * </nl>
 * 
 * @author schlienger
 */
public class FeatureWithTemplateExporter extends AbstractFeatureExporter
{
  private ArrayChooserPage m_page;

  /**
   * @see org.kalypso.simulation.ui.wizards.exporter.AbstractFeatureExporter#contributeWizardPages(java.util.List,
   *      org.kalypso.metadoc.configuration.IPublishingConfiguration)
   */
  protected void contributeWizardPages( final List pages, final IPublishingConfiguration configuration )
      throws CoreException
  {
    final Arguments arguments = (Arguments)getFromSupplier( "arguments" );
    final URL context = (URL)getFromSupplier( "context" );

    // create the possible template items
    final UrlArgument[] templateItems = ExporterHelper.createUrlItems( "template", arguments, context );

    // create wizard page for selecting the templates
    m_page = new ArrayChooserPage( templateItems, templateItems, templateItems, "templateSelection",
        "W‰hlen Sie die Exportarten", AbstractUIPlugin.imageDescriptorFromPlugin( KalypsoSimulationUIPlugin.getID(),
            "icons/wizban/bericht_wiz.gif" ) );

    pages.add( m_page );
  }

  /**
   * @see org.kalypso.metadoc.IExportableObjectFactory#createExportableObjects(org.apache.commons.configuration.Configuration)
   */
  public IExportableObject[] createExportableObjects( final Configuration configuration ) throws CoreException
  {
    final List objects = new ArrayList();

    final Object[] templates = m_page.getChoosen();
    for( int i = 0; i < templates.length; i++ )
      createExportableObjectsWith( (UrlArgument)templates[i], objects );

    return (IExportableObject[])objects.toArray( new IExportableObject[objects.size()] );
  }

  /**
   * Creates the adequates exportable-objects based on the given template
   */
  private void createExportableObjectsWith( final UrlArgument item, final List objects ) throws CoreException
  {
    // from supplier
    final Arguments arguments = (Arguments)getFromSupplier( "arguments" );
    final URL context = (URL)getFromSupplier( "context" );

    // from arguments
    final Arguments tokens = arguments.getArguments( "tokens" );

    // category is specified in the sub-arguments of the exporter, actually the ones in item
    String category = item.getProperty( "category" );
    if( category == null )
      category = item.getProperty( "label", arguments.getProperty( "name", "unbekannt" ) );
    
    final Feature[] features = getSelectedFeatures();
    for( int i = 0; i < features.length; i++ )
    {
      final Feature feature = features[i];

      // the name of the document to export is built using a property of the feature
      final String featurename = arguments.getProperty( "nameproperty", "Name" );
      final Object nameProp = feature.getProperty( featurename );
      final String name = nameProp == null ? "<unbekannt>" : nameProp.toString();

      try
      {
        final Properties replacetokens = ExporterHelper.createReplaceTokens( feature, tokens );
        final URL templateUrl = item.getUrl();

        final String id = getClass().getName() + templateUrl.getFile();

        final IExportableObject exportable = new ExportableTemplateObject( arguments, context, name, templateUrl,
            replacetokens, id, category );

        objects.add( exportable );
      }
      catch( final Exception e )
      {
        e.printStackTrace();

        if( e instanceof CoreException )
          throw (CoreException)e;

        throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
      }
    }
  }
}
