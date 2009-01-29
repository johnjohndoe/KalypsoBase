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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.contribs.java.net.UrlResolver;
import org.kalypso.metadoc.IExportableObject;
import org.kalypso.metadoc.impl.AbstractExporter;
import org.kalypso.metadoc.ui.ExportableTreeItem;
import org.kalypso.ogc.gml.filterdialog.model.FilterReader;
import org.kalypso.simulation.ui.wizards.exporter.ExporterHelper.UrlArgument;
import org.kalypsodeegree.filterencoding.Filter;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree_impl.filterencoding.FeatureFilter;
import org.kalypsodeegree_impl.filterencoding.FeatureId;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * This explorer can handle the export of observations defined in the properties of features using templates. It uses
 * the {@link org.kalypso.simulation.ui.wizards.exporter.AbstractFeatureExporter}for basic feature selection
 * functionality. It exports a document for each selected feature and each exporter.
 * <p>
 * The typical arguments syntax for this kind of exporter is as follows:
 * 
 * <pre>
 * &lt;arg name=&quot;exporterSomeName&quot;&gt;
 *  &lt;arg name=&quot;id&quot; value=&quot;featureWithTemplateExporter&quot; /&gt; 
 *  &lt;arg name=&quot;name&quot; value=&quot;Vorhersage Export&quot; /&gt;
 *  &lt;arg name=&quot;nameproperty&quot; value=&quot;Name&quot; /&gt; 
 *  &lt;arg name=&quot;width&quot; value=&quot;1024&quot; /&gt; 
 *  &lt;arg name=&quot;height&quot; value=&quot;768&quot; /&gt; 
 *  &lt;arg name=&quot;imageFormat&quot; value=&quot;png&quot; /&gt; 
 *  &lt;arg name=&quot;tokens&quot;&gt;
 *  &lt;arg name=&quot;title&quot; value=&quot;Name&quot; /&gt; 
 *  &lt;arg name=&quot;href1&quot; value=&quot;Wasserstand_gemessen&quot; /&gt; 
 *  &lt;arg name=&quot;href2&quot; value=&quot;Wasserstand_gerechnet&quot; /&gt; 
 *  &lt;arg name=&quot;href3&quot; value=&quot;Wasserstand_vorhersage&quot; /&gt; 
 *  &lt;arg name=&quot;href4&quot; value=&quot;Niederschlag_rechnung&quot; /&gt; 
 *  &lt;/arg&gt;
 *  &lt;arg name=&quot;templateDiagQ&quot;&gt;
 *  &lt;arg name=&quot;label&quot; value=&quot;Abfluﬂgrafik&quot; /&gt; 
 *  &lt;arg name=&quot;category&quot; value=&quot;Abfluﬂgrafik&quot; /&gt;
 *  &lt;arg name=&quot;templateFile&quot; value=&quot;project:/.templates/calcCase/berichtDiagQ.odt&quot; /&gt;
 *  &lt;arg name=&quot;excludes&quot;&gt;
 *  &lt;arg name=&quot;#featureId&quot; value=&quot;Usti&quot;/&gt;
 *  &lt;/arg&gt;
 *  &lt;/arg&gt;
 *  &lt;/arg&gt;
 * </pre>
 * <p>
 * The argument "category" is optional and can be defined for each template. It defines the category of the
 * ExportableObject that can be created based on the given template. If it is not specified, then the category defaults
 * to the value of the "label" argument.
 * </p>
 * <p>
 * The argument "excludes" is optional and can be defined to specify features that should be exludec from export. Each
 * sub-entry specifies the propertyName ('name') and its value ('value'). '#featureId' can be used as special case
 * instead of the propertyName.
 * </p>
 * <p>
 * This exporter has two wizard pages:
 * <nl>
 * <li>the selection of features
 * <li>the selection of the template to use
 * </nl>
 * 
 * @author Gernot Belger
 */
public class FeatureWithTemplateExporterTree extends AbstractExporter
{
  /**
   * @throws CoreException
   */
  @Override
  protected ExportableTreeItem[] createTreeItems( final ExportableTreeItem parent ) throws CoreException
  {
    // create the possible template items
    final Arguments arguments = (Arguments) getFromSupplier( "arguments" );
    final URL context = (URL) getFromSupplier( "context" );
    final UrlArgument[] templateItems = ExporterHelper.createUrlItems( "template", arguments, context );
    final ExportableTreeItem[] rootItems = new ExportableTreeItem[templateItems.length];
    for( int i = 0; i < templateItems.length; i++ )
    {
      final UrlArgument urlArg = templateItems[i];

      rootItems[i] = new ExportableTreeItem( urlArg.getLabel(), null, parent, null, false, false );
      final ExportableTreeItem[] children = createExportableObjectsWith( urlArg, rootItems[i] );
      rootItems[i].setChildren( children );
    }

    return rootItems;
  }

  /**
   * Creates the adequates exportable-objects based on the given template
   */
  private ExportableTreeItem[] createExportableObjectsWith( final UrlArgument item, final ExportableTreeItem parentItem ) throws CoreException
  {
    // from supplier
    final Arguments arguments = (Arguments) getFromSupplier( "arguments" );
    final URL context = (URL) getFromSupplier( "context" );

    final String documentNameFormat = item.getProperty( "documentName", "¸bersicht.csv" );
    final String documentTitleFormat = item.getProperty( "documentTitle" );

    final FeatureList features = (FeatureList) getFromSupplier( "features" );
    final FeatureList selectedFeatures = (FeatureList) getFromSupplier( "selectedFeatures" );
    final String labelProperty = (String) getFromSupplier( "propertyName" );

    // from arguments
    final Arguments tokens = arguments.getArguments( "tokens" );

    // category is specified in the sub-arguments of the exporter, actually the ones in item
    String category = item.getProperty( "category" );
    if( category == null )
      category = item.getProperty( "label", arguments.getProperty( "name", "unbekannt" ) );
    final String kennzifferIndexProp = item.getProperty( "kennzifferIndex", null );
    final Integer kennzifferIndex = kennzifferIndexProp == null ? null : new Integer( kennzifferIndexProp );

    final Collection<Feature> excludeList = createExcludeList( context, item, features );

    final ExportableTreeItem[] items = new ExportableTreeItem[features.size()];
    for( int i = 0; i < features.size(); i++ )
    {
      final Feature feature = (Feature) features.get( i );

      // the name of the document to export is built using the annotation token-replace mechanism
      // OLD behaviour: use the property denoted by argument 'nameproperty'
      final String documentName = validateDocumentName( FeatureHelper.tokenReplace( feature, documentNameFormat ) );
      final String documentTitle = FeatureHelper.tokenReplace( feature, documentTitleFormat );

      final Properties replacetokens = ExporterHelper.createReplaceTokens( feature, tokens );
      final URL templateUrl = item.getUrl();

      final String id = getClass().getName() + templateUrl.getFile();

      final IExportableObject exportable = new ExportableTemplateObject( arguments, context, documentName, documentTitle, templateUrl, replacetokens, id, category, kennzifferIndex );

      final boolean checked = selectedFeatures.contains( feature );
      final boolean grayed = excludeList.contains( feature );

      final String label = (String) feature.getProperty( labelProperty );
      items[i] = new ExportableTreeItem( label, null, parentItem, exportable, checked, grayed );
    }

    return items;
  }

  private Collection<Feature> createExcludeList( final URL context, final UrlArgument item, final FeatureList features )
  {
    final Collection<Feature> result = new HashSet<Feature>();

    final Arguments itemArguments = item.getArguments();
    final Arguments excludeArguments = itemArguments.getArguments( "excludes" );
    if( excludeArguments == null )
      return result;

    try
    {
      final Filter filter = findFilter( context, excludeArguments );

      // hm, heavy iterations... maybe performance problem for big lists....
      for( final Iterator iter = features.iterator(); iter.hasNext(); )
      {
        final Feature feature = (Feature) iter.next();
        if( filter.evaluate( feature ) )
          result.add( feature );
      }
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    return result;
  }
  
  private Filter findFilter( final URL context, final Arguments excludeArguments ) throws Exception
  {
    final String fid = excludeArguments.getProperty( "#featureid" );
    if( fid != null )
    {
      final FeatureFilter filter = new FeatureFilter();
      filter.addFeatureId( new FeatureId( fid ) );
      return filter;
    }

    final String filterFile = excludeArguments.getProperty( "filter" );
    if( filterFile != null )
    {
      InputStream is = null;
      try
      {
        final URL filterLocation = new UrlResolver().resolveURL( context, filterFile );
        is = new BufferedInputStream( filterLocation.openStream() );
        final Filter filter = FilterReader.readFilter( is );
        is.close();
        return filter;
      }
      finally
      {
        IOUtils.closeQuietly( is );
      }
    }

    throw new IllegalArgumentException( "Excludes muss entweder '#featureId' oder 'filter' Argument haben." );
  }
}
