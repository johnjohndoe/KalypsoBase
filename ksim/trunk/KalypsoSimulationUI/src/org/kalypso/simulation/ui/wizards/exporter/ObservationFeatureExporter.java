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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.configuration.Configuration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.commons.java.net.UrlResolverSingleton;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.wizard.ArrayChooserPage;
import org.kalypso.metadoc.IExportableObject;
import org.kalypso.metadoc.configuration.IPublishingConfiguration;
import org.kalypso.simulation.ui.KalypsoSimulationUIPlugin;
import org.kalypsodeegree.model.feature.Feature;

/**
 * This explorer can handle the export of observations defined in the properties of features using templates
 * 
 * @author schlienger
 */
public class ObservationFeatureExporter extends AbstractFeatureExporter
{
  /**
   * Argument: arguments which name begins with 'template' denote a template. The user can select the ones which should
   * be used when exporting the features.
   * <p>
   * Each template-argument is composed of the following sub-arguments:
   * <ul>
   * <li>'label'
   * <li>'templateFile'
   * </ul>
   */
  private final static String PROP_TEMPLATE = "template";
  private ArrayChooserPage m_page;

  /**
   * @see org.kalypso.simulation.ui.wizards.exporter.AbstractFeatureExporter#contributeWizardPages(java.util.List,
   *      org.kalypso.metadoc.configuration.IPublishingConfiguration)
   */
  protected void contributeWizardPages( final List pages, final IPublishingConfiguration configuration )
      throws CoreException
  {
    // create the possible template items
    final TemplateItem[] templateItems = createTemplateItems();

    // create wizard page for selecting the templates
    m_page = new ArrayChooserPage( templateItems, templateItems, templateItems, "templateSelection",
        "Wählen Sie die Exportarten", null );

    pages.add( m_page );
  }

  /**
   * @return list of template items found in the arguments that we got from the supplier (ex: ExportResultsWizardPage)
   */
  private TemplateItem[] createTemplateItems() throws CoreException
  {
    final Arguments arguments = (Arguments)getFromSupplier( "arguments" );
    final URL context = (URL)getFromSupplier( "context" );

    final Collection items = new ArrayList();
    final Collection stati = new ArrayList();

    for( final Iterator aIt = arguments.entrySet().iterator(); aIt.hasNext(); )
    {
      final Map.Entry entry = (Entry)aIt.next();
      final String key = (String)entry.getKey();
      if( key.startsWith( PROP_TEMPLATE ) )
      {
        final Arguments args = (Arguments)entry.getValue();
        final String label = args.getProperty( "label", "<unbekannt>" );
        final String templateFile = args.getProperty( "templateFile" );
        if( templateFile == null )
          stati.add( new Status( IStatus.WARNING, KalypsoSimulationUIPlugin.getID(), 0,
              "Keine Datei-Angabe für das Template: " + key, null ) );

        try
        {
          items.add( new TemplateItem( label, UrlResolverSingleton.resolveUrl( context, templateFile ) ) );
        }
        catch( final MalformedURLException e )
        {
          stati.add( new Status( IStatus.WARNING, KalypsoSimulationUIPlugin.getID(), 0, "Ungültiger Pfad: "
              + templateFile, e ) );
        }
      }
    }

    if( stati.size() > 0 )
      throw new CoreException( new MultiStatus( KalypsoSimulationUIPlugin.getID(), 0, (IStatus[])stati
          .toArray( new IStatus[stati.size()] ), "Siehe Details", null ) );

    return (TemplateItem[])items.toArray( new TemplateItem[items.size()] );
  }

  /**
   * @see org.kalypso.metadoc.IExportableObjectFactory#createExportableObjects(org.apache.commons.configuration.Configuration)
   */
  public IExportableObject[] createExportableObjects( final Configuration configuration ) throws CoreException
  {
    final List objects = new ArrayList();

    final Object[] templates = m_page.getChoosen();
    for( int i = 0; i < templates.length; i++ )
      createExportableObjectsWith( configuration, (TemplateItem)templates[i], objects );

    return (IExportableObject[])objects.toArray( new IExportableObject[objects.size()] );
  }

  /**
   * Creates the adequates exportable-objects based on the given template
   */
  private void createExportableObjectsWith( final Configuration configuration, final TemplateItem item,
      final List objects ) throws CoreException
  {
    // from supplier
    final Arguments arguments = (Arguments)getFromSupplier( "arguments" );
    final URL context = (URL)getFromSupplier( "context" );

    // from arguments
    final Arguments tokens = arguments.getArguments( "tokens" );

    final Feature[] features = getSelectedFeatures();
    for( int i = 0; i < features.length; i++ )
    {
      final Feature feature = features[i];

      final String featurename = arguments.getProperty( "nameproperty", "Name" );
      final Object nameProp = feature.getProperty( featurename );
      final String name = nameProp == null ? "<unbekannt>" : nameProp.toString();

      try
      {
        final Properties replacetokens = ExporterHelper.createReplaceTokens( feature, tokens );
        final URL templateUrl = item.getTemplateUrl();

        final IExportableObject exportable = new ExportableObsViewObject( arguments, context, name, templateUrl,
            replacetokens );

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

  /**
   * Denotes the items which are displayed in the template selection wizard page
   * 
   * @author schlienger
   */
  public static class TemplateItem
  {
    private final String m_label;
    private final URL m_templateUrl;

    public TemplateItem( final String label, final URL templateUrl )
    {
      m_label = label;
      m_templateUrl = templateUrl;
    }

    public String getLabel()
    {
      return m_label;
    }

    public URL getTemplateUrl()
    {
      return m_templateUrl;
    }

    public String toString()
    {
      return m_label;
    }
  }
}
