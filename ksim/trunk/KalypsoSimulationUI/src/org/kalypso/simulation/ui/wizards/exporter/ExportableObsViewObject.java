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
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.commons.java.io.ReaderUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.lang.DisposeHelper;
import org.kalypso.metadoc.IExportableObject;
import org.kalypso.ogc.sensor.diagview.DiagView;
import org.kalypso.ogc.sensor.diagview.DiagViewUtils;
import org.kalypso.ogc.sensor.diagview.jfreechart.ExportableChart;
import org.kalypso.ogc.sensor.diagview.jfreechart.ObservationChart;
import org.kalypso.ogc.sensor.tableview.TableView;
import org.kalypso.ogc.sensor.tableview.TableViewUtils;
import org.kalypso.ogc.sensor.tableview.swing.ExportableObservationTable;
import org.kalypso.ogc.sensor.tableview.swing.ObservationTable;
import org.kalypso.template.obsdiagview.ObsdiagviewType;
import org.kalypso.template.obstableview.ObstableviewType;

/**
 * Handles the export of an observation in the form of a table or diagram depending on the template used. This class is
 * designed to be used by the {@link org.kalypso.simulation.ui.wizards.exporter.ObservationFeatureExporter}.
 * 
 * @author schlienger
 */
public class ExportableObsViewObject implements IExportableObject
{
  private final static int EXPORT_TYPE_DIAGRAM = 0;
  private final static int EXPORT_TYPE_TABLE = 1;

  private final Arguments m_arguments;
  private final URL m_context;
  private final String m_documentName;
  private final URL m_templateUrl;
  private final Properties m_replaceTokens;

  private final int m_exportType;

  /**
   * @param templateUrl can denote an odt or an ott template file
   */
  public ExportableObsViewObject( final Arguments arguments, final URL context, final String documentName,
      final URL templateUrl, final Properties replaceTokens )
  {
    m_arguments = arguments;
    m_context = context;
    m_templateUrl = templateUrl;
    m_documentName = documentName;
    m_replaceTokens = replaceTokens;

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
        return m_documentName + "." + format;
      case EXPORT_TYPE_TABLE:
        return m_documentName + ".csv";
      default:
        return "<default>";
    }
  }

  public IStatus exportObject( final OutputStream output, final IProgressMonitor monitor )
  {
    Reader reader = null;
    try
    {
      final URLConnection connection = m_templateUrl.openConnection();
      reader = new InputStreamReader( connection.getInputStream(), "UTF-8" );
      reader = ReaderUtilities.createTokenReplaceReader( reader, m_replaceTokens, '%', '%' );

      switch( m_exportType )
      {
        case EXPORT_TYPE_DIAGRAM:
          return exportDiagramm( reader, output, new SubProgressMonitor( monitor, 1 ) );

        case EXPORT_TYPE_TABLE:
          return exportTable( reader, output, new SubProgressMonitor( monitor, 1 ) );
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

  private IStatus exportDiagramm( final Reader reader, final OutputStream output, final SubProgressMonitor monitor )
  {
    DiagView tpl = null;
    ObservationChart chart = null;
    try
    {
      tpl = new DiagView();
      chart = new ObservationChart( tpl, true );
      chart.setBackgroundPaint( Color.WHITE );
      
      final ObsdiagviewType xml = DiagViewUtils.loadDiagramTemplateXML( reader );

      final IStatus status = DiagViewUtils.applyXMLTemplate( tpl, xml, m_context, true, ExporterHelper.MSG_TOKEN_NOT_FOUND );

      final int width = Integer.parseInt( m_arguments.getProperty( "width", "800" ) );
      final int height = Integer.parseInt( m_arguments.getProperty( "height", "600" ) );
      final String format = m_arguments.getProperty( "imageFormat", ExportableChart.DEFAULT_FORMAT );

      final IStatus status2 = new ExportableChart( chart, format, width, height ).exportObject( output, monitor );

      return StatusUtilities.createStatus( new IStatus[] {status, status2}, "Fehler während der Export von " + m_templateUrl.getFile() );
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      return StatusUtilities.statusFromThrowable( e );
    }
    finally
    {
      new DisposeHelper().addDisposeCandidate( tpl ).addDisposeCandidate( chart ).dispose();
    }
  }

  private IStatus exportTable( final Reader reader, final OutputStream output, final SubProgressMonitor monitor )
  {
    TableView tpl = null;
    ObservationTable table = null;
    try
    {
      tpl = new TableView();
      table = new ObservationTable( tpl, true, false );
      final ObstableviewType xml = TableViewUtils.loadTableTemplateXML( reader );

      final IStatus status = TableViewUtils.applyXMLTemplate( tpl, xml, m_context, true );

      final IStatus status2 = new ExportableObservationTable( table ).exportObject( output, monitor );

      return StatusUtilities.createStatus( new IStatus[] {status, status2}, "Fehler während der Export von " + m_templateUrl.getFile() );
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      return StatusUtilities.statusFromThrowable( e );
    }
    finally
    {
      new DisposeHelper().addDisposeCandidate( tpl ).addDisposeCandidate( table ).dispose();
    }
  }
}