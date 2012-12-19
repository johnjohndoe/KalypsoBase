/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
import java.io.FileNotFoundException;
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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.status.KalypsoStati;

import au.com.bytecode.opencsv.CSVReader;

/**
 * adapter for Timeseries in 'csv' format date format dd MM yyy hh mm value format comma seperator example: 02.06.2002
 * 16:30;0,0010
 * 
 * @author huebsch
 * @author Dirk Kuch
 */
public class NativeObservationCSVAdapter extends AbstractObservationImporter
{
  public static final String SOURCE_ID = "source://native.observation.csv.import"; //$NON-NLS-1$

  private static final Pattern DATE_PATTERN = Pattern.compile( "([0-9 ]{2}) ([0-9 ]{2}) ([0-9]{2,4}) ([0-9 ]{2}) ([0-9 ]{2})" ); //$NON-NLS-1$

  public NativeObservationCSVAdapter( )
  {
    // TODO Auto-generated constructor stub
  }

  @Override
  protected List<NativeObservationDataSet> parse( final File source, final TimeZone timeZone, final boolean continueWithErrors, final IStatusCollector stati ) throws FileNotFoundException, IOException
  {
    final List<NativeObservationDataSet> datasets = new ArrayList<>();

    final DateFormat sdf2 = new SimpleDateFormat( "dd MM yy HH mm" ); //$NON-NLS-1$
    sdf2.setTimeZone( timeZone );

    final DateFormat sdf4 = new SimpleDateFormat( "dd MM yyyy HH mm" ); //$NON-NLS-1$
    sdf4.setTimeZone( timeZone );

    final char separator = ';';

    try( final FileReader fileReader = new FileReader( source ); final LineNumberReader reader = new LineNumberReader( fileReader ); final CSVReader csv = new CSVReader( reader, separator ) )
    {
      String[] lineIn = null;
      while( ArrayUtils.isNotEmpty( lineIn = csv.readNext() ) )
      {
        if( !continueWithErrors && getErrorCount() > getMaxErrorCount() )
          return datasets;

        final int lineNumber = reader.getLineNumber();

        try
        {
          readLine( stati, datasets, sdf2, sdf4, separator, lineIn, lineNumber );
        }
        catch( final Exception e )
        {
          stati.add( IStatus.ERROR, String.format( Messages.getString( "NativeObservationCSVAdapter_2" ), lineNumber, e.getLocalizedMessage() ) ); //$NON-NLS-1$
          tickErrorCount();
        }
      }
    }

    return datasets;
  }

  private void readLine( final IStatusCollector stati, final List<NativeObservationDataSet> datasets, final DateFormat sdf2, final DateFormat sdf4, final char separator, final String[] lineIn, final int lineNumber ) throws ParseException
  {
    if( ArrayUtils.getLength( lineIn ) == 2 )
    {
      final String dateString = lineIn[0];
      final String valueString = lineIn[1];

      final String formatedvalue = valueString.replaceAll( "\\,", "\\." ); //$NON-NLS-1$ //$NON-NLS-2$
      final Double value = new Double( Double.parseDouble( formatedvalue ) );

      final String formatedDate = dateString.replaceAll( "[:;\\.]", " " ); //$NON-NLS-1$ //$NON-NLS-2$

      final Matcher dateMatcher = DATE_PATTERN.matcher( formatedDate );
      if( dateMatcher.matches() )
      {
        final StringBuffer buffer = new StringBuffer();

        DateFormat sdf = sdf2;
        for( int i = 1; i <= dateMatcher.groupCount(); i++ )
        {
          // separator
          if( i > 1 )
            buffer.append( " " ); //$NON-NLS-1$

          /* Get the group. */
          final String group = dateMatcher.group( i );
          if( i == 3 && group.length() == 4 )
            sdf = sdf4;

          // correct empty fields
          buffer.append( group.replaceAll( " ", "0" ) ); // //$NON-NLS-1$ //$NON-NLS-2$
        }

        // FIXME: Why add the 00 here? Seconds do not appear in the pattern or is this the timezone?
        buffer.append( " 00" ); //$NON-NLS-1$

        final String correctDate = buffer.toString();
        final Date date = sdf.parse( correctDate );

        datasets.add( new NativeObservationDataSet( date, value, KalypsoStati.BIT_OK, SOURCE_ID ) );
      }
      else
      {
        final String line = StringUtils.join( lineIn, separator );
        stati.add( IStatus.WARNING, String.format( Messages.getString( "NativeObservationCSVAdapter_0" ), lineNumber, line ) ); //$NON-NLS-1$
        tickErrorCount();
      }
    }
    else
    {
      final String line = StringUtils.join( lineIn, separator );
      stati.add( IStatus.WARNING, String.format( Messages.getString( "NativeObservationCSVAdapter_1" ), lineNumber, line ) ); //$NON-NLS-1$
      tickErrorCount();
    }
  }
}