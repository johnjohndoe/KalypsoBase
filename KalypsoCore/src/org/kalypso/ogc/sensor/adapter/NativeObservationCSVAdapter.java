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
import java.io.LineNumberReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;

/**
 * adapter for Timeseries in 'csv' format date format dd MM yyy hh mm value format comma seperator example: 02.06.2002
 * 16:30;0,0010
 * 
 * @author huebsch
 * @author Dirk Kuch
 */
public class NativeObservationCSVAdapter extends AbstractObservationImporter
{
  private static final Pattern DATE_PATTERN = Pattern.compile( "([0-9 ]{2}) ([0-9 ]{2}) ([0-9]{4}) ([0-9 ]{2}) ([0-9 ]{2})" ); //$NON-NLS-1$

  private static final Pattern CSV_PATTERN = Pattern.compile( "([0-9]{1,2}.+?[0-9]{1,2}.+?[0-9]{2,4}.+?[0-9]{1,2}.+?[0-9]{1,2}).+?([0-9]+\\,+?[0-9]+).+?" ); //$NON-NLS-1$

  @Override
  protected void parse( final File source, final TimeZone timeZone, final boolean continueWithErrors, final IStatusCollector stati ) throws Exception
  {
    final DateFormat sdf = new SimpleDateFormat( "dd MM yyyy HH mm ss" ); //$NON-NLS-1$
    sdf.setTimeZone( timeZone );

    final FileReader fileReader = new FileReader( source );
    final LineNumberReader reader = new LineNumberReader( fileReader );
    String lineIn = null;
    while( (lineIn = reader.readLine()) != null )
    {
      if( !continueWithErrors && getErrorCount() > getMaxErrorCount() )
        return;
      try
      {
        final Matcher matcher = CSV_PATTERN.matcher( lineIn );
        if( matcher.matches() )
        {
          final String dateString = matcher.group( 1 );
          final String valueString = matcher.group( 2 );

          final String formatedvalue = valueString.replaceAll( "\\,", "\\." ); //$NON-NLS-1$ //$NON-NLS-2$
          final Double value = new Double( Double.parseDouble( formatedvalue ) );
          // Double value = new Double( matcher.group( 2 ) );

          final String formatedDate = dateString.replaceAll( "[:;\\.]", " " ); //$NON-NLS-1$ //$NON-NLS-2$

          final Matcher dateMatcher = DATE_PATTERN.matcher( formatedDate );
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
            buffer.append( " 00" ); //$NON-NLS-1$
            final String correctDate = buffer.toString();
            final Date date = sdf.parse( correctDate );

            addDataSet( new NativeObservationDataSet( date, value ) );
          }
          else
          {
            stati.add( IStatus.ERROR, String.format( "Line %d: Date not parsable: %s", reader.getLineNumber(), lineIn ) );
            tickErrorCount();
          }
        }
        else
        {
          stati.add( IStatus.ERROR, String.format( "Line %d: Line not parsable: %s", reader.getLineNumber(), lineIn ) );
          tickErrorCount();

        }
      }
      catch( final Exception e )
      {
        stati.add( IStatus.ERROR, String.format( "Line %d: Exception: %s", reader.getLineNumber(), e.getLocalizedMessage() ) );
        tickErrorCount();
      }
    }
  }
}