/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.IMetadataConstants;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.metadata.MetadataList;

/**
 * @author Dejan Antanaskovic, <a href="mailto:dejan.antanaskovic@tuhh.de">dejan.antanaskovic@tuhh.de</a>
 */
public class NativeObservationDVWKAdapter extends AbstractObservationImporter
{
  public static final Pattern PATTERN_LINE = Pattern.compile( "[A-Za-z0-9]{4}\\s([0-9\\s]{10})\\s*([0-9]{1,2})\\s*([0-9]{1,2})([A-Za-z\\s]{1})(.*)" ); //$NON-NLS-1$

  public static final Pattern SUB_PATTERN_DATA = Pattern.compile( "\\s*([0-9]{1,5})\\s*([0-9]{1,5})\\s*([0-9]{1,5})\\s*([0-9]{1,5})\\s*([0-9]{1,5})\\s*([0-9]{1,5})\\s*([0-9]{1,5})\\s*([0-9]{1,5})\\s*([0-9]{1,5})\\s*([0-9]{1,5})\\s*([0-9]{1,5})\\s*([0-9]{1,5})" ); //$NON-NLS-1$

  private static final int MAX_NO_OF_ERRORS = 30;

  private final String m_sname = "titel"; //$NON-NLS-1$

  private TimeZone m_timeZone;

  @Override
  public IStatus doImport( final File source, final TimeZone timeZone, final String valueType, final boolean continueWithErrors )
  {
    try
    {
      final MetadataList metaDataList = new MetadataList();
      metaDataList.put( IMetadataConstants.MD_ORIGIN, source.getAbsolutePath() );

      m_timeZone = timeZone;

      final IAxis[] axis = createAxis( valueType );
      final ITupleModel tuppelModel = createTuppelModel( source, axis, continueWithErrors );
      if( tuppelModel == null )
        return null;

      setObservation( new SimpleObservation( "href", m_sname, metaDataList, tuppelModel ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return new Status( IStatus.OK, KalypsoCorePlugin.getID(), "DVW Timeseries Import" );
    }
    catch( final IOException e )
    {
      return new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), e.getMessage() );
    }
  }

  private ITupleModel createTuppelModel( final File source, final IAxis[] axis, boolean continueWithErrors ) throws IOException
  {
    int numberOfErrors = 0;

    final StringBuffer errorBuffer = new StringBuffer();
    final FileReader fileReader = new FileReader( source );
    final LineNumberReader reader = new LineNumberReader( fileReader );
    final List<Date> dateCollector = new ArrayList<Date>();
    final List<Double> valueCollector = new ArrayList<Double>();
    String lineIn = null;
    GregorianCalendar previousNlineCalendar = null;
    while( (lineIn = reader.readLine()) != null )
    {
      if( !continueWithErrors && numberOfErrors > MAX_NO_OF_ERRORS )
        return null;
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

          final GregorianCalendar calendar = new GregorianCalendar( m_timeZone );
// final GregorianCalendar calendar = new GregorianCalendar( TimeZone.getTimeZone( "UTC" ) );
          calendar.set( year, month - 1, day, hour, 0, 0 );

          if( previousNlineCalendar != null )
          {
            while( previousNlineCalendar.compareTo( calendar ) < 0 )
            {
              dateCollector.add( previousNlineCalendar.getTime() );
              valueCollector.add( 0.0 );
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

// for( int i = 0; i < 12; i++ )
// {
// dateCollector.add( calendar.getTime() );
// valueCollector.add( 0.0 );
// calendar.add( GregorianCalendar.MINUTE, 5 );
// }
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
                  dateCollector.add( calendar.getTime() );
                  valueCollector.add( value );
                }
                catch( final Exception e )
                {
                  errorBuffer.append( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDVWKAdapter.10" ) + reader.getLineNumber() + Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDVWKAdapter.11" ) + lineIn + "\"\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                  numberOfErrors++;
                }
                calendar.add( Calendar.MINUTE, 5 );
              }
            }
          }
        }
        else
          numberOfErrors++;
      }
      catch( final Exception e )
      {
        errorBuffer.append( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDVWKAdapter.13" ) + reader.getLineNumber() + Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDVWKAdapter.14" ) + e.getLocalizedMessage() + "\"\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        numberOfErrors++;
      }
    }
    if( !continueWithErrors && numberOfErrors > MAX_NO_OF_ERRORS )
    {

      final MessageBox messageBox = new MessageBox( null, SWT.ICON_QUESTION | SWT.YES | SWT.NO );
      messageBox.setMessage( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDVWKAdapter.16" ) ); //$NON-NLS-1$
      messageBox.setText( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDVWKAdapter.17" ) ); //$NON-NLS-1$
      if( messageBox.open() == SWT.NO )
        return null;
      else
        continueWithErrors = true;
    }
    // TODO handle error
    System.out.println( errorBuffer.toString() );

    final Object[][] tuppleData = new Object[dateCollector.size()][2];
    for( int i = 0; i < dateCollector.size(); i++ )
    {
      tuppleData[i][0] = dateCollector.get( i );
      tuppleData[i][1] = valueCollector.get( i );
    }
    return new SimpleTupleModel( axis, tuppleData );
  }
}