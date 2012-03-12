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
import java.text.DateFormat;
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
 * @author Jessica Huebsch, <a href="mailto:j.huebsch@tuhh.de">j.huebsch@tuhh.de</a>
 */
public class NativeObservationDWDstdAdapter extends AbstractObservationImporter
{
  private final DateFormat m_dwdSTDDateFormat = new SimpleDateFormat( "yyyyMMdd" ); //$NON-NLS-1$

  public static Pattern DWD_STD_PATTERN = Pattern.compile( "\\s\\s\\s\\s([0-9]{5})([0-9]{4}[0-9]{2}[0-9]{2})(.+?)" ); //$NON-NLS-1$

  private String m_titel;

  private static final int MAX_NO_OF_ERRORS = 30;

  @Override
  public IStatus doImport( final File source, final TimeZone timeZone, final String valueType, final boolean continueWithErrors )
  {
    try
    {
      final MetadataList metaDataList = new MetadataList();

      m_dwdSTDDateFormat.setTimeZone( timeZone );

      final IAxis[] axis = createAxis( valueType );
      final ITupleModel tuppelModel = createTuppelModel( source, axis, continueWithErrors );
      setObservation( new SimpleObservation( "href", m_titel, metaDataList, tuppelModel ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return new Status( IStatus.OK, KalypsoCorePlugin.getID(), "DWD Timeseries Import" );
    }
    catch( final Exception e )
    {
      return new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), e.getMessage() );
    }
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
    while( (lineIn = reader.readLine()) != null )
    {
      if( !continueWithErrors && numberOfErrors > MAX_NO_OF_ERRORS )
        return null;
      try
      {
        final Matcher matcher = DWD_STD_PATTERN.matcher( lineIn );
        if( matcher.matches() )
        {
          Date date = null;
          String valueLine = null;
          try
          {
            m_titel = matcher.group( 1 );
            date = m_dwdSTDDateFormat.parse( matcher.group( 2 ) );
          }
          catch( final Exception e )
          {
            errorBuffer.append( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDWDstdAdapter.7" ) + reader.getLineNumber() + Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDWDstdAdapter.8" ) + lineIn + "\"\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            numberOfErrors++;
          }
          try
          {
            valueLine = matcher.group( 3 );
            final long startDate = date.getTime();
            for( int i = 0; i < 24; i++ )
            {
              final String valueString = valueLine.substring( i * 6, 6 * (i + 1) );
              final Double value = new Double( Double.parseDouble( valueString ) ) / 1000;
              valueCollector.add( value );
              final Date valueDate = new Date( startDate + i * 60 * 60 * 1000 );
              dateCollector.add( valueDate );
            }
          }
          catch( final Exception e )
          {
            System.out.println( e.getStackTrace() );
            errorBuffer.append( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDWDstdAdapter.10" ) + reader.getLineNumber() + Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDWDstdAdapter.11" ) + lineIn + "\"\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            numberOfErrors++;
          }
        }
        else
        {
          errorBuffer.append( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDWDstdAdapter.13" ) + reader.getLineNumber() + Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDWDstdAdapter.14" ) + lineIn + "\"\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          numberOfErrors++;
        }
      }
      catch( final Exception e )
      {
        errorBuffer.append( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDWDstdAdapter.16" ) + reader.getLineNumber() + Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDWDstdAdapter.17" ) + e.getLocalizedMessage() + "\"\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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