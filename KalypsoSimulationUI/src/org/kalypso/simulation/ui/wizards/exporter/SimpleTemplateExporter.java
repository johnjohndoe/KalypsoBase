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

import org.eclipse.core.runtime.CoreException;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.metadoc.IExportableObject;
import org.kalypso.metadoc.impl.AbstractExporter;
import org.kalypso.metadoc.ui.ExportableTreeItem;
import org.kalypso.simulation.ui.wizards.exporter.ExporterHelper.UrlArgument;

/**
 * Exports a template as defined in a template-file. No feature selection is provided since the features exported are
 * implicitely defined from the template itself. No token replacement takes place. More than one template can be
 * specified in the arguments of the exporter, and thus template selection is provided.
 * <p>
 * The typical arguments would look like the following:
 * 
 * <pre>
 *   &lt;arg name=&quot;exporterObservation&quot;&gt;
 *   &lt;arg name=&quot;id&quot; value=&quot;simpleTemplateExporter&quot;/&gt;
 *   &lt;arg name=&quot;width&quot; value=&quot;1024&quot;/&gt;
 *   &lt;arg name=&quot;height&quot; value=&quot;768&quot;/&gt;
 *   &lt;arg name=&quot;imageFormat&quot; value=&quot;png&quot;/&gt;
 *   &lt;arg name=&quot;documentName&quot; value=&quot;&quot;/&gt;
 *   &lt;arg name=&quot;templateDiag&quot;&gt;
 *   &lt;arg name=&quot;label&quot; value=&quot;Abfluﬂdiagramm, Gebiet obere Spree&quot;/&gt;
 *   &lt;arg name=&quot;templateFile&quot; value=&quot;project:/.templates/calcCase/berichtDiagObereSpree.odt&quot;/&gt;
 *   &lt;/arg&gt;
 *   &lt;/arg&gt;
 * </pre>
 * <p>
 * This wizard has one wizard page: the selection of a template.
 * 
 * @author schlienger
 */
public class SimpleTemplateExporter extends AbstractExporter
{
  /**
   * Loads the corresponding template and makes an exportable object out of it.
   */
  private IExportableObject createExportableObjectsWith( final UrlArgument item ) throws CoreException
  {
    // from supplier
    final Arguments arguments = (Arguments) getFromSupplier( "arguments" ); //$NON-NLS-1$
    final URL context = (URL) getFromSupplier( "context" ); //$NON-NLS-1$

    // category is specified in the sub-arguments of the exporter, actually the ones in item
    String category = item.getProperty( "category" ); //$NON-NLS-1$
    if( category == null )
      category = item.getProperty( "label", arguments.getProperty( "name", "unbekannt" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    final String kennzifferIndexProp = item.getProperty( "kennzifferIndex", null ); //$NON-NLS-1$
    final Integer kennzifferIndex = kennzifferIndexProp == null ? null : new Integer( kennzifferIndexProp );

    try
    {
      final URL templateUrl = item.getUrl();

      final String documentFormatString = item.getProperty( "documentName" ); //$NON-NLS-1$
      final String documentName = validateDocumentName( documentFormatString );
      final String documentTitle = item.getProperty( "documentTitle" ); //$NON-NLS-1$

      final String idPrefix = getClass().getName() + templateUrl.getFile();

      final IExportableObject exportable = new ExportableTemplateObject( arguments, context, documentName, documentTitle, templateUrl, idPrefix, category, kennzifferIndex );

      return exportable;
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      if( e instanceof CoreException )
        throw (CoreException) e;

      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  @Override
  protected ExportableTreeItem[] createTreeItems( final ExportableTreeItem parent ) throws CoreException
  {
    final Arguments arguments = (Arguments) getFromSupplier( "arguments" ); //$NON-NLS-1$
    final URL context = (URL) getFromSupplier( "context" ); //$NON-NLS-1$

    // create the possible template items
    final UrlArgument[] templateItems = ExporterHelper.createUrlItems( "template", arguments, context ); //$NON-NLS-1$
    final ExportableTreeItem[] items = new ExportableTreeItem[templateItems.length];
    for( int i = 0; i < templateItems.length; i++ )
    {
      final UrlArgument templateItem = templateItems[i];
      final IExportableObject expObj = createExportableObjectsWith( templateItem );
      items[i] = new ExportableTreeItem( templateItem.getLabel(), null, parent, expObj, true, false );
    }

    return items;
  }

}