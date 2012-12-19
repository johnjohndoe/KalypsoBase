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
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.status.KalypsoStati;

/**
 * @author Dejan Antanaskovic, <a href="mailto:dejan.antanaskovic@tuhh.de">dejan.antanaskovic@tuhh.de</a>
 * @author Dirk Kuch
 */
public class NativeObservationDVWKAdapter extends AbstractObservationImporter
{
  public static final String SOURCE_ID = "source://native.observation.dvwk.import"; //$NON-NLS-1$

  public static final String SOURCE_ID_MISSING_VALUE = SOURCE_ID + MISSING_VALUE_POSTFIX;

  private static final Pattern PATTERN_LINE = Pattern.compile( "[A-Za-z0-9]{4}\\s([0-9\\s]{10})\\s*([0-9]{1,2})\\s*([0-9]{1,2})([A-Za-z\\s]{1})(.*)" ); //$NON-NLS-1$

  private static final Pattern SUB_PATTERN_DATA = Pattern.compile( "\\s*([0-9]{1,5})\\s*([0-9]{1,5})\\s*([0-9]{1,5})\\s*([0-9]{1,5})\\s*([0-9]{1,5})\\s*([0-9]{1,5})\\s*([0-9]{1,5})\\s*([0-9]{1,5})\\s*([0-9]{1,5})\\s*([0-9]{1,5})\\s*([0-9]{1,5})\\s*([0-9]{1,5})" ); //$NON-NLS-1$

  @Override
  protected List<NativeObservationDataSet> parse( final File source, final TimeZone timeZone, final boolean continueWithErrors, final IStatusCollector stati ) throws IOException
  {
    final List<NativeObservationDataSet> datasets = new ArrayList<>();

    final FileReader fileReader = new FileReader( source );
    final LineNumberReader reader = new LineNumberReader( fileReader );
    String lineIn = null;
    GregorianCalendar previousNlineCalendar = null;
    while( (lineIn = reader.readLine()) != null )
    {
      if( !continueWithErrors && getErrorCount() > getMaxErrorCount() )
        return datasets;

      try
      {
        final Matcher matcher = PATTERN_LINE.matcher( lineIn );
        if( matcher.matches() )
        {
          // end of file?
          if( "E".equals( matcher.group( 4 ) ) ) //$NON-NLS-1$
            break;

          final String dateTime = matcher.group( 1 );
          final int day = Integer.parseInt( dateTime.substring( 0, 2 ).trim() );
          final int month = Integer.parseInt( dateTime.substring( 2, 4 ).trim() );
          final int year = Integer.parseInt( dateTime.substring( 4, 8 ).trim() );
          final int hour = Integer.parseInt( dateTime.substring( 8, 10 ).trim() );

          // comment line
          if( day == 0 && month == 0 && year == 0 )
            continue;

          final GregorianCalendar calendar = new GregorianCalendar( timeZone );
          calendar.set( year, month - 1, day, hour, 0, 0 );

          if( previousNlineCalendar != null )
          {
            while( previousNlineCalendar.compareTo( calendar ) < 0 )
            {
              datasets.add( new NativeObservationDataSet( previousNlineCalendar.getTime(), 0.0, KalypsoStati.BIT_CHECK, SOURCE_ID_MISSING_VALUE ) );

              previousNlineCalendar.add( Calendar.MINUTE, 5 );
            }
          }

          // data line
          if( ITimeseriesConstants.TYPE_RAINFALL.equals( matcher.group( 4 ) ) ) //$NON-NLS-1$
          {
            // TODO check if this is means all zeros for the whole day
            // or just for this hour,
            // or all zeros until the next entry?

            // all zeros until the next entry!
            previousNlineCalendar = calendar;
            continue;
          }
          else
          {
            previousNlineCalendar = null;
            final Matcher dataMatcher = SUB_PATTERN_DATA.matcher( matcher.group( 5 ) );
            if( dataMatcher.matches() )
            {
              for( int i = 1; i < 13; i++ )
              {
                try
                {
                  final double value = new Double( dataMatcher.group( i ) );

                  datasets.add( new NativeObservationDataSet( calendar.getTime(), value, KalypsoStati.BIT_OK, SOURCE_ID ) );
                }
                catch( final Exception e )
                {
                  stati.add( IStatus.WARNING, String.format( Messages.getString( "NativeObservationDVWKAdapter_0" ), reader.getLineNumber(), lineIn ) ); //$NON-NLS-1$
                  tickErrorCount();
                }
                calendar.add( Calendar.MINUTE, 5 );
              }
            }
          }
        }
        else
          tickErrorCount();
      }
      catch( final Exception e )
      {
        stati.add( IStatus.ERROR, String.format( Messages.getString( "NativeObservationDVWKAdapter_1" ), reader.getLineNumber(), e.getLocalizedMessage() ) ); //$NON-NLS-1$
        tickErrorCount();
      }

      if( !continueWithErrors && getErrorCount() > getMaxErrorCount() )
      {
        final MessageBox messageBox = new MessageBox( null, SWT.ICON_QUESTION | SWT.YES | SWT.NO );
        messageBox.setMessage( Messages.getString( "NativeObservationDVWKAdapter_2" ) ); //$NON-NLS-1$
        messageBox.setText( Messages.getString( "NativeObservationDVWKAdapter_3" ) ); //$NON-NLS-1$
        if( messageBox.open() == SWT.NO )
          return null;
      }
    }

    return datasets;
  }
}