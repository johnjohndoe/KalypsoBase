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

import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.commons.java.io.ReaderUtilities;
import org.kalypso.commons.java.net.UrlResolverSingleton;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.io.FileUtilities;
import org.kalypso.metadoc.IExportableObject;
import org.kalypso.ogc.sensor.diagview.jfreechart.ExportableChart;

/**
 * Handles the export of a template in the form of a table, diagram, map, etc. depending on the template used. This
 * class is designed to be used by the {@link org.kalypso.simulation.ui.wizards.exporter.FeatureWithTemplateExporter}.
 * <p>
 * This class can handle both cases:
 * <ul>
 * <li>template used as-is
 * <li>template used with pattern-replacement
 * </ul>
 * <p>
 * TODO extend it to handle gis-map stuff
 * 
 * @author schlienger
 */
public class ExportableTemplateObject implements IExportableObject
{
  private final static int EXPORT_TYPE_DIAGRAM = 0;
  private final static int EXPORT_TYPE_TABLE = 1;

  private final Arguments m_arguments;
  private final URL m_context;
  private final String m_documentName;
  private final URL m_templateUrl;
  private final Properties m_replaceTokens;

  private final int m_exportType;
  private final String m_identifierPrefix;
  private final String m_category;

  /**
   * constructor without replaceTokens. This constructor should be used when you want to parse the template and create
   * its corresponding exportable object without pattern-replacement: the whole template is used as-is.
   */
  public ExportableTemplateObject( final Arguments arguments, final URL context, final String documentName,
      final URL templateUrl, final String identifierPrefix, final String category )
  {
    this( arguments, context, documentName, templateUrl, null, identifierPrefix, category );
  }

  /**
   * constructor with pattern-replacement. Before the template file is parsed, the given tokens are replaced with their
   * corresponding values found in the properties.
   * 
   * @param templateUrl
   *          can denote an odt or an ott template file
   * @param replaceTokens
   *          tokens used to replace elements of the template file that relate to values of specific gml-feature
   *          properties. This is for instance used with the {@link FeatureWithTemplateExporter}.
   * @param category
   *          the category of the document resulting from the export of this object
   */
  public ExportableTemplateObject( final Arguments arguments, final URL context, final String documentName,
      final URL templateUrl, final Properties replaceTokens, final String identifierPrefix, final String category )
  {
    m_arguments = arguments;
    m_context = context;
    m_templateUrl = templateUrl;
    m_documentName = documentName;
    m_replaceTokens = replaceTokens;
    m_identifierPrefix = identifierPrefix;
    m_category = category;

    final String strUrl = m_templateUrl.toExternalForm();
    if( strUrl.endsWith( "odt" ) )
      m_exportType = EXPORT_TYPE_DIAGRAM;
    else if( strUrl.endsWith( "ott" ) )
      m_exportType = EXPORT_TYPE_TABLE;
    else
      throw new IllegalArgumentException( "Kann Vorlagendatei nicht öffnen: " + strUrl
          + ". Nur .odt und .ott Vorlagen werden unterstützt." );
  }

public String getPreferredDocumentName()
  {
    switch( m_exportType )
    {
    case EXPORT_TYPE_DIAGRAM:
      final String format = m_arguments.getProperty( "imageFormat", ExportableChart.DEFAULT_FORMAT );
      return FileUtilities.validateName( m_documentName + "." + format,"_");
    case EXPORT_TYPE_TABLE:
      return FileUtilities.validateName( m_documentName + ".csv", "_" );
    default:
      return FileUtilities.validateName( "<default>", "_" );
    }
  }  public IStatus exportObject( final OutputStream output, final IProgressMonitor monitor,
      final Configuration metadataExtensions )
  {
    Reader reader = null;
    try
    {
      reader = UrlResolverSingleton.getDefault().createReader( m_templateUrl );

      // replace-tokens are optional
      if( m_replaceTokens != null )
        reader = ReaderUtilities.createTokenReplaceReader( reader, m_replaceTokens, '%', '%' );

      switch( m_exportType )
      {
      case EXPORT_TYPE_DIAGRAM:
        return ExporterHelper.exportObsDiagram( m_context, m_arguments, reader, output, new SubProgressMonitor(
            monitor, 1 ), m_templateUrl, metadataExtensions, getIdentifier(), getCategory() );

      case EXPORT_TYPE_TABLE:
        return ExporterHelper.exportObsTable( m_context, m_arguments, reader, output, new SubProgressMonitor( monitor,
            1 ), m_templateUrl, metadataExtensions, getIdentifier(), getCategory() );
      }

      // should never come here
      return Status.CANCEL_STATUS;
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      return StatusUtilities.statusFromThrowable( e );
    }
    finally
    {
      IOUtils.closeQuietly( reader );
    }
  }

  /**
   * @see org.kalypso.metadoc.IExportableObject#getIdentifier()
   */
  public String getIdentifier()
  {
    return m_identifierPrefix + getPreferredDocumentName();
  }

  /**
   * The category of an ExportableTemplateObject can be specified in the arguments of its configuration. The argument
   * named "category" is used, but is optional. If it is not provided, the value of the argument "label" is used. If no
   * value could be found, "unbekannt" is returned.
   * 
   * @see org.kalypso.metadoc.IExportableObject#getCategory()
   */
  public String getCategory()
  {
    return m_category;
  }
}