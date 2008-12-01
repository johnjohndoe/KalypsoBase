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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.commons.java.net.UrlResolver;
import org.kalypso.metadoc.IExportableObject;
import org.kalypso.metadoc.configuration.IPublishingConfiguration;
import org.kalypso.metadoc.impl.AbstractExporter;
import org.kalypso.ogc.gml.filterdialog.model.FilterReader;
import org.kalypso.simulation.ui.KalypsoSimulationUIPlugin;
import org.kalypso.simulation.ui.wizards.exporter.ExporterHelper.UrlArgument;
import org.kalypso.simulation.ui.wizards.exporter.featureWithTemplate.ExportableTreeItem;
import org.kalypso.simulation.ui.wizards.exporter.featureWithTemplate.ExportableTreePage;
import org.kalypso.simulation.ui.wizards.exporter.featureWithTemplate.TreeSelectionPage;
import org.kalypsodeegree.filterencoding.Filter;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree_impl.filterencoding.FeatureFilter;
import org.kalypsodeegree_impl.filterencoding.FeatureId;

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
 &lt;arg name="label" value="Abflußgrafik" /&gt; 
 &lt;arg name="category" value="Abflußgrafik" /&gt;
 &lt;arg name="templateFile" value="project:/.templates/calcCase/berichtDiagQ.odt" /&gt;
 &lt;arg name="excludes">
 &lt;arg name="#featureId" value="Usti"/>
 &lt;/arg>
 &lt;/arg&gt;
 &lt;/arg&gt;
 * </pre>
 * 
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
 * 
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
  private TreeSelectionPage m_page = null;

  /**
   * @see org.kalypso.metadoc.IExportableObjectFactory#createWizardPages(org.kalypso.metadoc.configuration.IPublishingConfiguration,
   *      ImageDescriptor)
   */
  public IWizardPage[] createWizardPages( final IPublishingConfiguration configuration, ImageDescriptor defaultImage ) throws CoreException
  {
    if( m_page == null )
    {
      final ExportableTreeItem[] treeItems = createTreeItems();

      final ImageDescriptor imgDesc = AbstractUIPlugin.imageDescriptorFromPlugin( KalypsoSimulationUIPlugin.getID(), "icons/wizban/bericht_wiz.gif" );

      m_page = new ExportableTreePage( "templatePage", "Wählen Sie die zu exportierenden Dokumente", imgDesc );
      m_page.setViewerSorter( new ViewerSorter() );
      m_page.setInput( treeItems );

      final List checkedItems = new ArrayList();
      final List grayedItems = new ArrayList();
      filterChecked( treeItems, checkedItems, grayedItems );
      m_page.setChecked( checkedItems.toArray( new Object[checkedItems.size()] ) );
      m_page.setGrayed( grayedItems.toArray( new Object[grayedItems.size()] ) );
    }

    return new IWizardPage[]
    { m_page };
  }

  /**
   * @throws CoreException
   */
  private ExportableTreeItem[] createTreeItems() throws CoreException
  {
    // create the possible template items
    final Arguments arguments = (Arguments)getFromSupplier( "arguments" );
    final URL context = (URL)getFromSupplier( "context" );
    final UrlArgument[] templateItems = ExporterHelper.createUrlItems( "template", arguments, context );
    ExportableTreeItem[] rootItems = new ExportableTreeItem[templateItems.length];
    for( int i = 0; i < templateItems.length; i++ )
    {
      final UrlArgument urlArg = templateItems[i];

      rootItems[i] = new ExportableTreeItem( urlArg.getLabel(), null, null, false, false );
      ExportableTreeItem[] children = createExportableObjectsWith( urlArg, rootItems[i] );
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
    final Arguments arguments = (Arguments)getFromSupplier( "arguments" );
    final URL context = (URL)getFromSupplier( "context" );

    final FeatureList features = (FeatureList)getFromSupplier( "features" );
    final FeatureList selectedFeatures = (FeatureList)getFromSupplier( "selectedFeatures" );
    final String nameProperty = (String)getFromSupplier( "propertyName" );

    // from arguments
    final Arguments tokens = arguments.getArguments( "tokens" );

    // category is specified in the sub-arguments of the exporter, actually the ones in item
    String category = item.getProperty( "category" );
    if( category == null )
      category = item.getProperty( "label", arguments.getProperty( "name", "unbekannt" ) );
    final String kennzifferIndexProp = item.getProperty( "kennzifferIndex", null );
    final Integer kennzifferIndex = kennzifferIndexProp == null ? null : new Integer( kennzifferIndexProp );

    final Collection excludeList = createExcludeList( context, item, features );

    final ExportableTreeItem[] items = new ExportableTreeItem[features.size()];
    for( int i = 0; i < features.size(); i++ )
    {
      final Feature feature = (Feature)features.get( i );

      final String label = (String)feature.getProperty( nameProperty );

      // the name of the document to export is built using a property of the feature
      final String featurename = arguments.getProperty( "nameproperty", "Name" );
      final Object nameProp = feature.getProperty( featurename );
      final String name = nameProp == null ? "<unbekannt>" : nameProp.toString();

      final Properties replacetokens = ExporterHelper.createReplaceTokens( feature, tokens );
      final URL templateUrl = item.getUrl();

      final String id = getClass().getName() + templateUrl.getFile();

      final IExportableObject exportable = new ExportableTemplateObject( arguments, context, name, templateUrl, replacetokens, id, category, kennzifferIndex );

      final boolean checked = selectedFeatures.contains( feature );
      final boolean grayed = excludeList.contains( feature );

      items[i] = new ExportableTreeItem( label, parentItem, exportable, checked, grayed );
    }

    return items;
  }

  private Collection createExcludeList( final URL context, final UrlArgument item, final FeatureList features )
  {
    final Collection result = new HashSet();

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
        final Feature feature = (Feature)iter.next();
        if( filter.evaluate( feature ) )
          result.add( feature );
      }
    }
    catch( Exception e )
    {
      e.printStackTrace();
    }

    return result;
  }

  private Filter findFilter( final URL context, Arguments excludeArguments ) throws Exception
  {
    final String fid = excludeArguments.getProperty( "#featureid" );
    if( fid != null )
    {
      FeatureFilter filter = new FeatureFilter();
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
        Filter filter = FilterReader.readFilterFragment( is );
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

  /**
   * @see org.kalypso.metadoc.IExportableObjectFactory#createExportableObjects(org.apache.commons.configuration.Configuration)
   */
  public IExportableObject[] createExportableObjects( Configuration configuration )
  {
    final List result = new ArrayList();

    final Object[] checkedElements = m_page.getCheckedElements();
    Object[] grayedElements = m_page.getGrayedElements();
    List grayedList = Arrays.asList( grayedElements );

    for( int i = 0; i < checkedElements.length; i++ )
    {
      final ExportableTreeItem item = (ExportableTreeItem)checkedElements[i];
      IExportableObject exportableObject = item.getExportableObject();
      if( exportableObject != null && !grayedList.contains( item ) )
        result.add( exportableObject );
    }

    return (IExportableObject[])result.toArray( new IExportableObject[result.size()] );
  }

  private void filterChecked( final ExportableTreeItem[] items, final List checkedItems, final List grayedItems )
  {
    for( int i = 0; i < items.length; i++ )
    {
      final ExportableTreeItem item = items[i];
      if( item.isChecked() )
        checkedItems.add( item );

      if( item.isGrayed() )
        grayedItems.add( item );

      filterChecked( item.getChildren(), checkedItems, grayedItems );
    }
  }
}
