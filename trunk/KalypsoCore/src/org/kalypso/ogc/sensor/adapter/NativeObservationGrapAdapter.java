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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.status.KalypsoStati;

/**
 * @author doemming
 * @author Dirk Kuch
 */
public class NativeObservationGrapAdapter extends AbstractObservationImporter
{
  public static final String SOURCE_ID = "source://native.observation.grap.import";

  private static final Pattern GRAP_PATTERN = Pattern.compile( "([0-9]{1,2}.+?[0-9]{1,2}.+?[0-9]{2,4}.+?[0-9]{1,2}.+?[0-9]{1,2}.[0-9 ]{1,2})(.*-?[0-9\\.]+)" ); //$NON-NLS-1$

  private static final Pattern DATE_PATTERN = Pattern.compile( "([0-9 ]{2}) ([0-9 ]{2}) ([0-9]{4}) ([0-9 ]{2}) ([0-9 ]{2}) ([0-9 ]{2})" ); //$NON-NLS-1$

  @Override
  protected void parse( final File source, final TimeZone timeZone, final boolean continueWithErrors, final IStatusCollector stati ) throws Exception
  {
    final SimpleDateFormat sdf = new SimpleDateFormat( "dd MM yyyy HH mm ss" ); //$NON-NLS-1$
    sdf.setTimeZone( timeZone );

    final FileReader fileReader = new FileReader( source );
    final LineNumberReader reader = new LineNumberReader( fileReader );

    try
    {
      String lineIn = null;

      while( (lineIn = reader.readLine()) != null )
      {
        if( !continueWithErrors && getErrorCount() > getMaxErrorCount() )
          return;

        final Matcher matcher = GRAP_PATTERN.matcher( lineIn );
        if( matcher.matches() )
        {
          final Date date = parseDate( matcher.group( 1 ), sdf );
          final Double value = NumberUtils.parseDouble( matcher.group( 2 ) );

          addDataSet( new NativeObservationDataSet( date, value, KalypsoStati.BIT_OK, SOURCE_ID ) );
        }
        else
        {
          stati.add( IStatus.ERROR, String.format( "Line %d: Date not parsable: %s", reader.getLineNumber(), lineIn ) );
          tickErrorCount();
        }
      }
    }
    finally
    {
      reader.close();
    }

  }

  private Date parseDate( final String dateString, final SimpleDateFormat sdf ) throws ParseException, CoreException
  {
    final String formatedDate = dateString.replaceAll( "[:\\.]", " " ); //$NON-NLS-1$ //$NON-NLS-2$

    final Matcher dateMatcher = DATE_PATTERN.matcher( formatedDate );

    if( !dateMatcher.matches() )
    {
      throw new CoreException( new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), String.format( "Date not parsable: %s", dateString ) ) );
    }

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
    final Date date = sdf.parse( correctDate );

    return date;
  }
}