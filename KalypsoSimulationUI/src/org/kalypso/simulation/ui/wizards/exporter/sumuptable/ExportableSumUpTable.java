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

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.util.CalendarUtilities;
import org.kalypso.metadoc.IExportableObject;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.timeseries.TimeserieUtils;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.simulation.ui.i18n.Messages;
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

  private final String m_identifierPrefix;

  private final String m_documentName;

  private final String m_documentTitle;

  public ExportableSumUpTable( final Arguments args, final URL context, final String identifierPrefix, final String documentName, final String documentTitle )
  {
    m_args = args;
    m_context = context;
    m_identifierPrefix = identifierPrefix;
    m_documentName = documentName;
    m_documentTitle = documentTitle;
  }

  /**
   * @see org.kalypso.metadoc.IExportableObject#getPreferredDocumentName()
   */
  public String getPreferredDocumentName( )
  {
    return m_documentName + ".csv"; //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.metadoc.IExportableObject#exportObject(java.io.OutputStream,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  public IStatus exportObject( final OutputStream output, final IProgressMonitor monitor )
  {
    // fetch basic arguments
    final String sep = m_args.getProperty( "separator", ";" ); //$NON-NLS-1$ //$NON-NLS-2$
    final String charset = m_args.getProperty( "charset", Charset.defaultCharset().name() ); //$NON-NLS-1$
    final String axisType = m_args.getProperty( "axisType" ); //$NON-NLS-1$
    final String timeUnit = m_args.getProperty( "timeUnit" ); //$NON-NLS-1$
    final String timeStep = m_args.getProperty( "timeStep" ); //$NON-NLS-1$
    final String delta = m_args.getProperty( "delta" ); //$NON-NLS-1$
    final String dateFormat = m_args.getProperty( "dateFormat" ); //$NON-NLS-1$

    final DateFormat df = new SimpleDateFormat( dateFormat );
    final NumberFormat nf = TimeserieUtils.getNumberFormatFor( axisType );

    BufferedWriter writer = null;

    try
    {
      // create model
      final SumUpForecastTable table = new SumUpForecastTable( axisType, CalendarUtilities.getCalendarField( timeUnit ), Integer.valueOf( timeStep ).intValue(), Double.valueOf( delta ).doubleValue() );

      final List<IStatus> stati = new ArrayList<IStatus>();

      // for each observation
      final UrlArgument[] items = ExporterHelper.createUrlItems( "obs", m_args, m_context ); //$NON-NLS-1$
      final IObservation[] obses = new IObservation[items.length];
      for( int i = 0; i < items.length; i++ )
      {
        try
        {
          obses[i] = ZmlFactory.parseXML( items[i].getUrl(), "" ); //$NON-NLS-1$

          final IStatus status = table.addObservation( items[i], obses[i] );
          stati.add( status );
        }
        catch( final SensorException e )
        {
          stati.add( StatusUtilities.createStatus( IStatus.WARNING, Messages.getString( "org.kalypso.simulation.ui.wizards.exporter.sumuptable.ExportableSumUpTable.0", items[i].getUrl() ), e ) ); //$NON-NLS-1$
        }
      }

      // fetch flexible columns specifications
      final ColumnSpec[] cols = ColumnSpec.getColumns( m_args );

      writer = new BufferedWriter( new OutputStreamWriter( output, charset ) );

      // output document Title
      if( m_documentTitle != null )
      {
        writer.write( m_documentTitle );
        writer.newLine();
      }

      // output header (flexible columns)
      for( final ColumnSpec element : cols )
      {
        writer.write( element.toString() );
        writer.write( sep );
      }

      // output header (fix columns)
      table.writeHeader( writer, sep, df );

      writer.newLine();

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
        table.writeRow( items[i], writer, sep, df, nf );

        writer.newLine();
      }

      table.dispose();

      final IStatus status = StatusUtilities.createStatus( stati, Messages.getString( "org.kalypso.simulation.ui.wizards.exporter.sumuptable.ExportableSumUpTable.1" ) ); //$NON-NLS-1$

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

  /**
   * @see org.kalypso.metadoc.IExportableObject#getIdentifier()
   */
  public String getIdentifier( )
  {
    return m_identifierPrefix + getPreferredDocumentName();
  }

  /**
   * The category can be specified in the arguments. The argument named "category" is used, but is optional. If it is
   * not provided, the value of the argument "name" is used. If no value could be found, "unbekannt" is returned.
   * 
   * @see org.kalypso.metadoc.IExportableObject#getCategory()
   */
  public String getCategory( )
  {
    String category = m_args.getProperty( "category" ); //$NON-NLS-1$
    if( category == null )
      category = m_args.getProperty( "name", "unbekannt" ); //$NON-NLS-1$ //$NON-NLS-2$

    return category;
  }

  /**
   * @see org.kalypso.metadoc.IExportableObject#getStationIDs()
   */
  public String getStationIDs( )
  {
    return ""; //$NON-NLS-1$
  }
}