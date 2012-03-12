/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypso.ogc.sensor.adapter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.MetadataList;

/**
 * @author huebsch adapter for Timeseries in 'dwd' format (5 minutes values dayly blocks) Kopfsatz 5-Minuten-Datei (neue
 *         Struktur) Feld-Nr. 1 2 3 4 5 Inhalt 77 Stations-nummer Datumjj/mm/tt gemesseneNiederschlagshöhe(in 1/10 mm)
 *         Tagessumme der 5-Min-Werte(in 1/1000 mm)Kalendertag! ch. v. b. 1-2 4-8 10-15 17-20 22-27 Anz. ch. 2 5 6 4 6
 *         Ein Datenblock besteht aus 18 Datensätzen: Datensatz 5-Minuten-Datei (neue Struktur) Feld-Nr. 1-16 Inhalt 16
 *         5-Minutenwerte der Niederschlagshöhe(in 1/1000 mm) ch. v. b. 1-80 Anz.ch. 80 example: 77 48558 960101 00 0 0
 *         0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 *         0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 *         0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 *         0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 *         0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 *         0 0 0 0 0 0 0 0 0 0 0 0
 */
public class NativeObservationDWD5minAdapter extends AbstractObservationImporter
{
  private final Pattern m_dwdBlockPattern = Pattern.compile( "[7]{2}\\s+([0-9]{5})\\s+([0-9]{6}).+?" ); //$NON-NLS-1$

  private final int m_timeStep = 300000;

  private DateFormat m_dateFormat;

  private static final int SEARCH_BLOCK_HEADER = 0;

  private static final int SEARCH_VALUES = 1;

  private static final int MAX_NO_OF_ERRORS = 30;

  @Override
  public IStatus doImport( final File source, final TimeZone timeZone, final String valueType, final boolean continueWithErrors )
  {
    try
    {
      final MetadataList metaDataList = new MetadataList();

      m_dateFormat = new SimpleDateFormat( "yyMMdd" ); //$NON-NLS-1$
      m_dateFormat.setTimeZone( timeZone );

      final IAxis[] axis = createAxis( valueType );
      final ITupleModel tuppelModel = createTuppelModel( source, axis, continueWithErrors );
      setObservation( new SimpleObservation( "href", "titel", metaDataList, tuppelModel ) ); //$NON-NLS-1$ //$NON-NLS-2$

      return new Status( IStatus.OK, KalypsoCorePlugin.getID(), "DWD Timeseries Import" );
    }
    catch( final Exception e )
    {
      return new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), e.getMessage() );
    }
  }

  private ITupleModel createTuppelModel( final File source, final IAxis[] axis, final boolean continueWithErrors ) throws IOException, ParseException
  {
    int numberOfErrors = 0;

    final StringBuffer errorBuffer = new StringBuffer();
    final FileReader fileReader = new FileReader( source );
    final LineNumberReader reader = new LineNumberReader( fileReader );
    final List<Date> dateCollector = new ArrayList<Date>();
    final List<Double> valueCollector = new ArrayList<Double>();
    String lineIn = null;
    int valuesLine = 0;
    int lineNumber = 0;
    int step = SEARCH_BLOCK_HEADER;
    final StringBuffer buffer = new StringBuffer();
    long startDate = 0;
    while( (lineIn = reader.readLine()) != null )
    {
      if( !continueWithErrors && numberOfErrors > MAX_NO_OF_ERRORS )
        return null;
      lineNumber = reader.getLineNumber();
      // System.out.println( "Lese Zeile:" + lineNumber );
      switch( step )
      {
        case SEARCH_BLOCK_HEADER:
          final Matcher matcher = m_dwdBlockPattern.matcher( lineIn );
          if( matcher.matches() )
          {
            // String DWDID = matcher.group( 1 );
            final String startDateString = matcher.group( 2 );
            final Pattern datePattern = Pattern.compile( "([0-9]{2})([0-9]{2})([0-9]{2})" ); //$NON-NLS-1$
            final Matcher dateMatcher = datePattern.matcher( startDateString );
            if( dateMatcher.matches() )
            {
              // System.out.println( "Startdatum Header:" + startDateString );
              final Date parseDate = m_dateFormat.parse( startDateString );
              startDate = parseDate.getTime();
            }
            else
            {
              System.out.println( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDWD5minAdapter.9" ) + startDateString + Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDWD5minAdapter.10" ) + datePattern.toString() ); //$NON-NLS-1$ //$NON-NLS-2$
            }
          }
          else
          {
            errorBuffer.append( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDWD5minAdapter.11" ) + lineNumber + Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDWD5minAdapter.12" ) + lineIn + Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDWD5minAdapter.13" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            numberOfErrors++;
          }
          step++;
          break;
        case SEARCH_VALUES:
          valuesLine = valuesLine + 1;
          for( int i = 0; i < 16; i++ )
          {
            final String valueString = lineIn.substring( i * 5, 5 * (i + 1) );
            Double value = new Double( Double.parseDouble( valueString ) ) / 1000;
            // TODO: Write status
            if( value > 99.997 )
            {
              System.out.println( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDWD5minAdapter.14" ) ); //$NON-NLS-1$
              value = 0.0;
            }
            // Datenfilter für 0.0 - um Datenbank nicht mit unnötigen Werten zu füllen (Zur Zeit nicht verwendet, da
            // Rohdaten benötigt)
            // if( value != 0.0 )
            // {
            valueCollector.add( value );

            buffer.append( " " ); // separator //$NON-NLS-1$
            final Date valueDate = new Date( startDate + i * m_timeStep + (valuesLine - 1) * 16 * m_timeStep );
            buffer.append( valueDate.toString() );
            dateCollector.add( valueDate );
            // }
          }
          if( valuesLine == 18 )
          {
            step = SEARCH_BLOCK_HEADER;
            valuesLine = 0;
          }
          break;
        default:
          break;
      }
    }
    final Object[][] tupelData = new Object[dateCollector.size()][2];
    for( int i = 0; i < dateCollector.size(); i++ )
    {
      tupelData[i][0] = dateCollector.get( i );
      tupelData[i][1] = valueCollector.get( i );
    }
    // TODO handle error
    System.out.println( errorBuffer.toString() );
    return new SimpleTupleModel( axis, tupelData );
  }
}