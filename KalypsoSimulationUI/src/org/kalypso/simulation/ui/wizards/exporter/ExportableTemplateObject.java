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
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.commons.java.io.ReaderUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.lang.DisposeHelper;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.metadoc.IExportableObject;
import org.kalypso.ogc.sensor.diagview.DiagView;
import org.kalypso.ogc.sensor.diagview.DiagViewUtils;
import org.kalypso.ogc.sensor.diagview.jfreechart.ExportableChart;
import org.kalypso.ogc.sensor.diagview.jfreechart.ObservationChart;
import org.kalypso.ogc.sensor.tableview.TableView;
import org.kalypso.ogc.sensor.tableview.TableViewUtils;
import org.kalypso.ogc.sensor.tableview.swing.ExportableObservationTable;
import org.kalypso.ogc.sensor.tableview.swing.ObservationTable;
import org.kalypso.template.obsdiagview.Obsdiagview;
import org.kalypso.template.obstableview.Obstableview;

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
  private final Arguments m_arguments;

  private final URL m_context;

  private final URL m_templateUrl;

  private final Properties m_replaceTokens;

  private final String m_identifierPrefix;

  private final String m_category;

  private final Integer m_kennzifferIndex;

  private final String m_preferredFilename;

  private final DisposeHelper m_disposeHelper;

  private IStatus m_status;

  private final IExportableObject m_exportableObject;

  /**
   * constructor without replaceTokens. This constructor should be used when you want to parse the template and create
   * its corresponding exportable object without pattern-replacement: the whole template is used as-is.
   */
  public ExportableTemplateObject( final Arguments arguments, final URL context, final String documentName, final String documentTitle, final URL templateUrl, final String identifierPrefix, final String category, final Integer kennzifferIndex )
  {
    this( arguments, context, documentName, documentTitle, templateUrl, null, identifierPrefix, category, kennzifferIndex );
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
  public ExportableTemplateObject( final Arguments arguments, final URL context, final String documentName, final String documentTitle, final URL templateUrl, final Properties replaceTokens, final String identifierPrefix, final String category, final Integer kennzifferIndex )
  {
    m_arguments = arguments;
    m_context = context;
    m_templateUrl = templateUrl;
    m_replaceTokens = replaceTokens;
    m_identifierPrefix = identifierPrefix;
    m_category = category;
    m_kennzifferIndex = kennzifferIndex;
    m_status = null;
    m_disposeHelper = new DisposeHelper();

    final String strUrl = m_templateUrl.toExternalForm();
    if( strUrl.endsWith( "odt" ) )
    {
      m_preferredFilename = FileUtilities.validateName( documentName + "." + m_arguments.getProperty( "imageFormat", ExportableChart.DEFAULT_FORMAT ), "_" );
      m_exportableObject = createObsDiagram( documentTitle );
    }
    else if( strUrl.endsWith( "ott" ) )
    {
      m_preferredFilename = FileUtilities.validateName( documentName + ".csv", "_" );
      m_exportableObject = createObsTable( documentTitle );
    }
    else
      throw new IllegalArgumentException( "Kann Vorlagendatei nicht öffnen: " + strUrl + ". Nur .odt und .ott Vorlagen werden unterstützt." );
  }

  /**
   * @see org.kalypso.metadoc.IExportableObject#exportObject(java.io.OutputStream,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  public IStatus exportObject( final OutputStream output, final IProgressMonitor monitor )
  {
    try
    {
      if( m_exportableObject != null )
      {
        /* Export it. */
        final IStatus status = m_exportableObject.exportObject( output, new SubProgressMonitor( monitor, 1 ) );

        return StatusUtilities.createStatus( new IStatus[] { m_status, status }, "Fehler während des Exports der Vorlage: " + m_templateUrl.getFile() );
      }

      /* There was no exportable object created. */
      return m_status;
    }
    finally
    {
      /* Dispose. */
      m_disposeHelper.dispose();
    }
  }

  public String getPreferredDocumentName( )
  {
    return m_preferredFilename;
  }

  /**
   * @see org.kalypso.metadoc.IExportableObject#getIdentifier()
   */
  public String getIdentifier( )
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
  public String getCategory( )
  {
    return m_category;
  }

  /**
   * @see org.kalypso.metadoc.IExportableObject#getStationIDs()
   */
  public String getStationIDs( )
  {
    if( m_exportableObject == null )
      return "";

    return m_exportableObject.getStationIDs();
  }

  /**
   * This function creates a diagram based on the diagram-template (odt).
   * 
   * @return The exportable object.
   */
  private IExportableObject createObsDiagram( final String documentTitle )
  {
    Reader reader = null;

    try
    {
      reader = UrlResolverSingleton.getDefault().createReader( m_templateUrl );

      // replace-tokens are optional
      if( m_replaceTokens != null )
        reader = ReaderUtilities.createTokenReplaceReader( reader, m_replaceTokens, '%', '%' );

      final DiagView tpl = new DiagView();
      final ObservationChart chart = new ObservationChart( tpl, true );
      chart.setBackgroundPaint( Color.WHITE );

      /* For later disposal. */
      m_disposeHelper.addDisposeCandidate( tpl ).addDisposeCandidate( chart );

      final Obsdiagview xml = DiagViewUtils.loadDiagramTemplateXML( reader );
      final IStatus status = DiagViewUtils.applyXMLTemplate( tpl, xml, m_context, true, ExporterHelper.MSG_TOKEN_NOT_FOUND );
      if( documentTitle != null )
        tpl.setTitle( documentTitle );

      if( tpl.getItems().length == 0 )
      {
        m_status = StatusUtilities.createWarningStatus( "Diagramm " + tpl.getTitle() + " (" + getCategory() + ") beinhaltet keine Daten. Der Export findet also nicht statt." );
        return null;
      }

      /* Wrap as warning because even if an obs is missing we can still export diagram. */
      m_status = StatusUtilities.wrapStatus( status, IStatus.WARNING, IStatus.WARNING | IStatus.ERROR );

      final int width = Integer.parseInt( m_arguments.getProperty( "width", "800" ) );
      final int height = Integer.parseInt( m_arguments.getProperty( "height", "600" ) );
      final String format = m_arguments.getProperty( "imageFormat", ExportableChart.DEFAULT_FORMAT );

      return new ExportableChart( chart, format, width, height, getIdentifier(), getCategory(), getKennzifferIndex() );
    }
    catch( final Exception e )
    {
      m_disposeHelper.dispose();
      m_status = StatusUtilities.statusFromThrowable( e );
      return null;
    }
    finally
    {
      IOUtils.closeQuietly( reader );
    }
  }

  /**
   * This function creates an observation table based on the table-template (ott).
   * 
   * @return The exportable object.
   */
  private IExportableObject createObsTable( final String documentTitle )
  {
    Reader reader = null;

    try
    {
      reader = UrlResolverSingleton.getDefault().createReader( m_templateUrl );

      // replace-tokens are optional
      if( m_replaceTokens != null )
        reader = ReaderUtilities.createTokenReplaceReader( reader, m_replaceTokens, '%', '%' );

      final TableView tpl = new TableView();
      final ObservationTable table = new ObservationTable( tpl, true, false );

      /* For later disposal. */
      m_disposeHelper.addDisposeCandidate( tpl ).addDisposeCandidate( table );

      final Obstableview xml = TableViewUtils.loadTableTemplateXML( reader );
      final IStatus status = TableViewUtils.applyXMLTemplate( tpl, xml, m_context, true, ExporterHelper.MSG_TOKEN_NOT_FOUND );
      if( tpl.getItems().length == 0 )
      {
        m_status = StatusUtilities.createWarningStatus( "Tabelle (" + getCategory() + ") beinhaltet keine Daten. Der Export findet also nicht statt." );
        return null;
      }

      /* Wrap as warning because even if an obs is missing we can still export diagram. */
      m_status = StatusUtilities.wrapStatus( status, IStatus.WARNING, IStatus.WARNING | IStatus.ERROR );

      // We are using the documentTitle as preferredDocumentName here, it will be written as first line of the table
      return new ExportableObservationTable( table, getIdentifier(), getCategory(), documentTitle, getKennzifferIndex() );
    }
    catch( final Exception e )
    {
      m_disposeHelper.dispose();
      m_status = StatusUtilities.statusFromThrowable( e );
      return null;
    }
    finally
    {
      IOUtils.closeQuietly( reader );
    }
  }
  
  private Integer getKennzifferIndex( )
  {
    return m_kennzifferIndex;
  }
}