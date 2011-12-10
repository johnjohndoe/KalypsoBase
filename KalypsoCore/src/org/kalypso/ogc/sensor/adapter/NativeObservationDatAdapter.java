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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.MetadataList;

/**
 * @author huebsch
 */
public class NativeObservationDatAdapter extends AbstractObservationImporter
{
  private final DateFormat m_grapDateFormat = new SimpleDateFormat( "dd MM yyyy HH mm ss" ); //$NON-NLS-1$

  // Tag1 Zeit1 N - Ombrometer Fuhlsbuettel [mm]
  // 01.01.1971 07:30:00 0,1
  // 02.01.1971 07:30:00 0
  // 03.01.1971 07:30:00 0

  public static Pattern DATE_PATTERN = Pattern.compile( "([0-9]{2}\\.[0-9]{2}\\.[0-9]{4}\\s+[0-9]{2}:[0-9]{2}:[0-9]{2}).+?(-??[0-9\\.]+)" ); //$NON-NLS-1$

  private static final int MAX_NO_OF_ERRORS = 30;

  @Override
  public IObservation importTimeseries( final File source, final TimeZone timeZone, final String valueType, final boolean continueWithErrors ) throws Exception
  {
    final MetadataList metaDataList = new MetadataList();

    m_grapDateFormat.setTimeZone( timeZone );

    final IAxis[] axis = createAxis( valueType );
    final ITupleModel tuppelModel = createTuppelModel( source, axis, continueWithErrors );
    return new SimpleObservation( "href", "titel", metaDataList, tuppelModel ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private ITupleModel createTuppelModel( final File source, final IAxis[] axis, final boolean continueWithErrors ) throws IOException
  {
    int numberOfErrors = 0;

    final StringBuffer errorBuffer = new StringBuffer();
    final FileReader fileReader = new FileReader( source );
    final LineNumberReader reader = new LineNumberReader( fileReader );
    final List<Date> dateCollector = new ArrayList<Date>();
    final List<Double> valueCollector = new ArrayList<Double>();
    String lineIn = null;

    // ignore first row (not data here, by DAT format)
    reader.readLine();

    while( (lineIn = reader.readLine()) != null )
    {
      if( !continueWithErrors && numberOfErrors > MAX_NO_OF_ERRORS )
        return null;
      try
      {
        final Matcher matcher = DATE_PATTERN.matcher( lineIn );
        if( matcher.matches() )
        {
          final String dateString = matcher.group( 1 );
          final String valueString = matcher.group( 2 );

          final String formatedvalue = valueString.replaceAll( "\\,", "\\." ); //$NON-NLS-1$ //$NON-NLS-2$
          final Double value = new Double( Double.parseDouble( formatedvalue ) );
          // Double value = new Double( matcher.group( 2 ) );

          final String formatedDate = dateString.replaceAll( "[:;\\.]", " " ); //$NON-NLS-1$ //$NON-NLS-2$
          final Pattern datePattern = Pattern.compile( "([0-9 ]{2}) ([0-9 ]{2}) ([0-9]{4}) ([0-9 ]{2}) ([0-9 ]{2}) ([0-9 ]{2})" ); //$NON-NLS-1$
          final Matcher dateMatcher = datePattern.matcher( formatedDate );
          if( dateMatcher.matches() )
          {
            final StringBuffer buffer = new StringBuffer();
            for( int i = 1; i <= dateMatcher.groupCount(); i++ )
            {
              if( i > 1 )
                buffer.append( " " ); // separator //$NON-NLS-1$

              buffer.append( dateMatcher.group( i ).replaceAll( " ", "0" ) ); // //$NON-NLS-1$ //$NON-NLS-2$
              // correct
              // empty
              // fields
            }
            final String correctDate = buffer.toString();
            final Date date = m_grapDateFormat.parse( correctDate );
            dateCollector.add( date );
            valueCollector.add( value );
          }
          else
          {
            errorBuffer.append( "line " + reader.getLineNumber() + Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDatAdapter.17" ) + lineIn + "\"\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            numberOfErrors++;
          }
        }
        else
        {
          errorBuffer.append( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDatAdapter.19" ) + reader.getLineNumber() + Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDatAdapter.20" ) + lineIn + "\"\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          numberOfErrors++;
        }
      }
      catch( final Exception e )
      {
        errorBuffer.append( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDatAdapter.22" ) + reader.getLineNumber() + Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDatAdapter.23" ) + e.getLocalizedMessage() + "\"\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        numberOfErrors++;
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