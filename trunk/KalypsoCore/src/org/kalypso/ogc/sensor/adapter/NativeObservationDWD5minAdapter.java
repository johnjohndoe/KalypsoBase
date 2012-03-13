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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
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
  private static final Pattern DWD_BLOCK_PATTERN = Pattern.compile( "[7]{2}\\s+([0-9]{5})\\s+([0-9]{6}).+?" ); //$NON-NLS-1$

  private static final Pattern DATE_PATTERN = Pattern.compile( "([0-9]{2})([0-9]{2})([0-9]{2})" ); //$NON-NLS-1$

  private final int m_timeStep = 300000;

  private static final int SEARCH_BLOCK_HEADER = 0;

  private static final int SEARCH_VALUES = 1;

  private static final int MAX_NO_OF_ERRORS = 30;

  @Override
  public IStatus doImport( final File source, final TimeZone timeZone, final String valueType, final boolean continueWithErrors )
  {
    final List<IStatus> stati = new ArrayList<>();
    final List<NativeObservationDataSet> datasets = new ArrayList<>();

    try
    {
      Collections.addAll( stati, parse( source, timeZone, datasets, continueWithErrors ) );
    }
    catch( final Exception ex )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), ex.getMessage() );
      stati.add( status );
    }

    final MetadataList metadata = new MetadataList();
    final ITupleModel model = createTuppelModel( valueType, datasets );
    setObservation( new SimpleObservation( source.getAbsolutePath(), source.getName(), metadata, model ) );

    return StatusUtilities.createStatus( stati, "DWD Observation Import" );

  }

  private IStatus[] parse( final File source, final TimeZone timeZone, final List<NativeObservationDataSet> datasets, final boolean continueWithErrors ) throws IOException
  {
    final SimpleDateFormat sdf = new SimpleDateFormat( "yyMMdd" ); //$NON-NLS-1$
    sdf.setTimeZone( timeZone );

    final List<IStatus> stati = new ArrayList<>();

    int numberOfErrors = 0;

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
        if( !continueWithErrors && numberOfErrors > MAX_NO_OF_ERRORS )
          return stati.toArray( new IStatus[] {} );

        try
        {
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
                  final Date parseDate = sdf.parse( startDateString );
                  startDate = parseDate.getTime();
                }
                else
                {
                  final String msg = String.format( "Das Format des Headers (Startdatum) passt nicht. Input: %s, Pattern: %s", startDateString, DATE_PATTERN.toString() );

                  final IStatus status = new Status( IStatus.INFO, KalypsoCorePlugin.getID(), msg );
                  throw new CoreException( status );
                }
              }
              else
              {
                final String msg = String.format( "Header not parsable: : %s", lineIn );

                final IStatus status = new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), msg );
                throw new CoreException( status );
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
                  System.out.println( "Messfehler" );
                  value = 0.0;
                }
                // Datenfilter für 0.0 - um Datenbank nicht mit unnötigen Werten zu füllen (Zur Zeit nicht verwendet, da
                // Rohdaten benötigt)

                buffer.append( " " ); // separator //$NON-NLS-1$
                final Date valueDate = new Date( startDate + i * m_timeStep + (valuesLine - 1) * 16 * m_timeStep );
                buffer.append( valueDate.toString() );

                datasets.add( new NativeObservationDataSet( valueDate, value ) );
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
        catch( final Exception e )
        {
          IStatus status;
          if( e instanceof CoreException )
          {
            final CoreException coreException = (CoreException) e;
            final IStatus status2 = coreException.getStatus();
            if( IStatus.INFO != status2.getSeverity() )
              numberOfErrors++;

            status = new Status( status2.getSeverity(), KalypsoCorePlugin.getID(), String.format( "Line #%d: %s", reader.getLineNumber(), status2.getMessage() ) );
          }
          else
          {
            final String message = e.getMessage();
            status = new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), String.format( "Line #%d: %s", reader.getLineNumber(), message ) );

            numberOfErrors++;
          }

          if( numberOfErrors < MAX_NO_OF_ERRORS )
            stati.add( status );
        }
      }
    }
    finally
    {
      IOUtils.closeQuietly( reader );
    }

    return stati.toArray( new IStatus[] {} );
  }
}