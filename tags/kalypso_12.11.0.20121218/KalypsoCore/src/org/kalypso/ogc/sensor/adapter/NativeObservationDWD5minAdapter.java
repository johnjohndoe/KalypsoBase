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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.status.KalypsoStati;

/**
 * Adapter for Timeseries in 'dwd' format (5 minutes values dayly blocks) Kopfsatz 5-Minuten-Datei (neue Struktur)
 * Feld-Nr. 1 2 3 4 5 Inhalt 77 Stations-nummer Datumjj/mm/tt gemesseneNiederschlagshöhe(in 1/10 mm) Tagessumme der
 * 5-Min-Werte(in 1/1000 mm)Kalendertag! ch. v. b. 1-2 4-8 10-15 17-20 22-27 Anz. ch. 2 5 6 4 6 Ein Datenblock besteht
 * aus 18 Datensätzen: Datensatz 5-Minuten-Datei (neue Struktur) Feld-Nr. 1-16 Inhalt 16 5-Minutenwerte der
 * Niederschlagshöhe(in 1/1000 mm) ch. v. b. 1-80 Anz.ch. 80 example:
 * 
 * <pre>
 *         77 48558 960101 00 0
 *         0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 *         0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 *         0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 *         0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 *         0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 *         0 0 0 0 0 0 0 0 0 0 0 0 0
 * 
 * </pre>
 * 
 * @author huebsch
 * @author Dirk Kuch
 */
public class NativeObservationDWD5minAdapter extends AbstractObservationImporter
{
  public static final String SOURCE_ID = "source://native.observation.dwd.5min.import"; //$NON-NLS-1$

  public static final String SOURCE_ID_MISSING_VALUE = SOURCE_ID + MISSING_VALUE_POSTFIX;

  private static final Pattern DWD_BLOCK_PATTERN = Pattern.compile( "[7]{2}\\s+([0-9]{5})\\s+([0-9]{6}).+?" ); //$NON-NLS-1$

  private static final Pattern DATE_PATTERN = Pattern.compile( "([0-9]{2})([0-9]{2})([0-9]{2})" ); //$NON-NLS-1$

  private final int m_timeStep = 300000;

  private static final int SEARCH_BLOCK_HEADER = 0;

  private static final int SEARCH_VALUES = 1;

  @Override
  protected List<NativeObservationDataSet> parse( final File source, final TimeZone timeZone, final boolean continueWithErrors, final IStatusCollector stati ) throws IOException
  {
    final List<NativeObservationDataSet> datasets = new ArrayList<>();

    final SimpleDateFormat sdf = new SimpleDateFormat( "yyMMdd" ); //$NON-NLS-1$
    sdf.setTimeZone( timeZone );

    final FileReader fileReader = new FileReader( source );
    final LineNumberReader reader = new LineNumberReader( fileReader );

    try
    {
      String lineIn = null;
      int valuesLine = 0;
      int step = SEARCH_BLOCK_HEADER;
      final StringBuffer buffer = new StringBuffer();
      long startDate = 0;

      while( (lineIn = reader.readLine()) != null )
      {
        if( !continueWithErrors && getErrorCount() > getMaxErrorCount() )
          return datasets;

        switch( step )
        {
          case SEARCH_BLOCK_HEADER:
            final Matcher matcher = DWD_BLOCK_PATTERN.matcher( lineIn );
            if( matcher.matches() )
            {
              // String DWDID = matcher.group( 1 );
              final String startDateString = matcher.group( 2 );

              final Matcher dateMatcher = DATE_PATTERN.matcher( startDateString );
              if( dateMatcher.matches() )
              {
                // System.out.println( "Startdatum Header:" + startDateString );
                try
                {
                  final Date parseDate = sdf.parse( startDateString );
                  startDate = parseDate.getTime();
                }
                catch( final ParseException e )
                {
                  e.printStackTrace();

                  stati.add( IStatus.WARNING, String.format( Messages.getString( "NativeObservationDWD5minAdapter_0" ), reader.getLineNumber(), startDateString, DATE_PATTERN.toString() ), e ); //$NON-NLS-1$
                  tickErrorCount();
                }
              }
              else
              {
                stati.add( IStatus.INFO, String.format( Messages.getString( "NativeObservationDWD5minAdapter_0" ), reader.getLineNumber(), startDateString, DATE_PATTERN.toString() ) ); //$NON-NLS-1$
              }
            }
            else
            {
              stati.add( IStatus.WARNING, String.format( Messages.getString( "NativeObservationDWD5minAdapter_1" ), reader.getLineNumber(), lineIn ) ); //$NON-NLS-1$
              tickErrorCount();
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

              String src = SOURCE_ID;

              if( value > 99.997 )
              {
                value = 0.0;
                src = SOURCE_ID_MISSING_VALUE;
              }

              // Datenfilter für 0.0 - um Datenbank nicht mit unnötigen Werten zu füllen (Zur Zeit nicht verwendet, da
              // Rohdaten benötigt)
              buffer.append( " " ); // separator //$NON-NLS-1$
              final Date valueDate = new Date( startDate + i * m_timeStep + (valuesLine - 1) * 16 * m_timeStep );
              buffer.append( valueDate.toString() );

              datasets.add( new NativeObservationDataSet( valueDate, value, toStatus( src ), src ) );
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
    }
    finally
    {
      IOUtils.closeQuietly( reader );
    }

    return datasets;
  }

  private int toStatus( final String src )
  {
    if( StringUtils.equals( SOURCE_ID, src ) )
      return KalypsoStati.BIT_OK;

    return KalypsoStati.BIT_CHECK;
  }
}