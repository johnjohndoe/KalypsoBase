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

import org.apache.commons.configuration.Configuration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.wizard.ArrayChooserPage;
import org.kalypso.metadoc.IExportableObject;
import org.kalypso.metadoc.configuration.IPublishingConfiguration;
import org.kalypso.metadoc.impl.AbstractExporter;
import org.kalypso.simulation.ui.wizards.exporter.ExporterHelper.UrlArgument;

/**
 * Exports a template as defined in a template-file. No feature selection is provided since the features exported are
 * implicitely defined from the template itself. No token replacement takes place. More than one template can be
 * specified in the arguments of the exporter, and thus template selection is provided.
 * <p>
 * The typical arguments would look like the following:
 * 
 * <pre>
 &lt;arg name="exporterObservation"&gt;
 &lt;arg name="id" value="simpleTemplateExporter"/&gt;
 &lt;arg name="width" value="1024"/&gt;
 &lt;arg name="height" value="768"/&gt;
 &lt;arg name="imageFormat" value="png"/&gt;
 &lt;arg name="documentName" value=""/&gt;
 &lt;arg name="templateDiag"&gt;
 &lt;arg name="label" value="Abfluﬂdiagramm, Gebiet obere Spree"/&gt;
 &lt;arg name="templateFile" value="project:/.templates/calcCase/berichtDiagObereSpree.odt"/&gt;
 &lt;/arg&gt;
 &lt;/arg&gt;
 * </pre>
 * 
 * <p>
 * This wizard has one wizard page: the selection of a template.
 * 
 * @author schlienger
 */
public class SimpleTemplateExporter extends AbstractExporter
{
  private ArrayChooserPage m_page;

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
   * Loads the corresponding template and makes an exportable object out of it.
   */
  private void createExportableObjectsWith( final UrlArgument item, final List objects ) throws CoreException
  {
    // from supplier
    final Arguments arguments = (Arguments)getFromSupplier( "arguments" );
    final URL context = (URL)getFromSupplier( "context" );

    // category is specified in the sub-arguments of the exporter, actually the ones in item
    String category = item.getProperty( "category" );
    if( category == null )
      category = item.getProperty( "label", arguments.getProperty( "name", "unbekannt" ) );
  
    try
    {
      final URL templateUrl = item.getUrl();

      // the name of the document to create is specified in the arguments
      final String name = item.getProperty( "documentName" );

      final String idPrefix = getClass().getName() + templateUrl.getFile();

      final IExportableObject exportable = new ExportableTemplateObject( arguments, context, name, templateUrl,
          idPrefix, category );

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

  /**
   * @see org.kalypso.metadoc.IExportableObjectFactory#createWizardPages(org.kalypso.metadoc.configuration.IPublishingConfiguration,
   *      ImageDescriptor)
   */
  public IWizardPage[] createWizardPages( final IPublishingConfiguration configuration, ImageDescriptor defaultImage )
      throws CoreException
  {
    final Arguments arguments = (Arguments)getFromSupplier( "arguments" );
    final URL context = (URL)getFromSupplier( "context" );

    // create the possible template items
    final UrlArgument[] templateItems = ExporterHelper.createUrlItems( "template", arguments, context );

    // create wizard page for selecting the templates
    m_page = new ArrayChooserPage( templateItems, templateItems, templateItems, "templateSelection",
        "W‰hlen Sie die Exportarten", defaultImage );

    return new IWizardPage[]
    { m_page };
  }
}
