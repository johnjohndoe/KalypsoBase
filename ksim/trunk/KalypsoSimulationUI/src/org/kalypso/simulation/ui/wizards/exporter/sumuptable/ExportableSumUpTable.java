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

package org.kalypso.simulation.ui.wizards.exporter.sumuptable;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.io.CharsetUtilities;
import org.kalypso.contribs.java.util.CalendarUtilities;
import org.kalypso.metadoc.IExportableObject;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.timeseries.TimeserieUtils;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.simulation.ui.wizards.exporter.ExporterHelper;
import org.kalypso.simulation.ui.wizards.exporter.ExporterHelper.UrlArgument;

/**
 * The exportable object
 * 
 * @author schlienger
 */
public final class ExportableSumUpTable implements IExportableObject
{
  private final Arguments m_args;
  private final URL m_context;

  public ExportableSumUpTable( final Arguments args, final URL context )
  {
    m_args = args;
    m_context = context;
  }

  /**
   * @see org.kalypso.metadoc.IExportableObject#getPreferredDocumentName()
   */
  public String getPreferredDocumentName()
  {
    return m_args.getProperty( "documentName", "übersicht.csv" );
  }

  /**
   * @see org.kalypso.metadoc.IExportableObject#exportObject(java.io.OutputStream,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  public IStatus exportObject( final OutputStream output, final IProgressMonitor monitor )
  {
    // fetch basic arguments
    final String sep = m_args.getProperty( "separator", ";" );
    final String charset = m_args.getProperty( "charset", CharsetUtilities.getDefaultCharset() );
    final String axisType = m_args.getProperty( "axisType" );
    final String timeUnit = m_args.getProperty( "timeUnit" );
    final String timeStep = m_args.getProperty( "timeStep" );
    final String delta = m_args.getProperty( "delta" );
    final String dateFormat = m_args.getProperty( "dateFormat" );

    final DateFormat df = new SimpleDateFormat( dateFormat );
    final NumberFormat nf = TimeserieUtils.getNumberFormatFor( axisType );

    Writer writer = null;
    
    try
    {
      // create model
      final SumUpForecastTable table = new SumUpForecastTable( axisType,
          CalendarUtilities.getCalendarField( timeUnit ), Integer.valueOf( timeStep ).intValue(), Double
              .valueOf( delta ).doubleValue() );

      final List stati = new ArrayList();

      // for each observation
      final UrlArgument[] items = ExporterHelper.createUrlItems( "obs", m_args, m_context );
      final IObservation[] obses = new IObservation[items.length];
      for( int i = 0; i < items.length; i++ )
      {
        try
        {
          obses[i] = ZmlFactory.parseXML( items[i].getUrl(), "" );

          final IStatus status = table.addObservation( items[i], obses[i] );
          stati.add( status );
        }
        catch( final SensorException e )
        {
          stati.add( StatusUtilities.createStatus( IStatus.WARNING , "Zeitreihe existiert nicht oder ist fehlerhaft: " + items[i].getUrl(), e) );
        }
      }

      // fetch flexible columns specifications
      final ColumnSpec[] cols = ColumnSpec.getColumns( m_args );

      writer = new OutputStreamWriter( output, charset );

      // output header (flexible columns)
      for( int i = 0; i < cols.length; i++ )
      {
        writer.write( cols[i].toString() );
        writer.write( sep );
      }

      // output header (fix columns)
      final Date[] dates = table.writeHeader( writer, sep, df );

      writer.write( "\n" );

      // output rows
      for( int i = 0; i < items.length; i++ )
      {
        // for user defined columns
        for( int j = 0; j < cols.length; j++ )
        {
          final String string = cols[j].resolveContent( items[i].getArguments(), obses[i] );

          writer.write( string );

          if( j < cols.length - 1 )
            writer.write( sep );
        }

        // for model
        table.writeRow( items[i], writer, sep, df, nf, dates );

        writer.write( "\n" );
      }

      table.dispose();

      IStatus status = StatusUtilities.createStatus( stati,
          "Fehler beim erzeugen des Übersichtsdokument: eine oder mehrere Zeitreihe konnte nicht hinzugefügt werden" );

      // wrap as warning because even if some obs is missing, export is still possible
      return StatusUtilities.wrapStatus( status, IStatus.WARNING, IStatus.WARNING | IStatus.ERROR );
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      return StatusUtilities.statusFromThrowable( e );
    }
    finally
    {
      IOUtils.closeQuietly( writer );
    }
  }
}