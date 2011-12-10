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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.IMetadataConstants;
import org.kalypso.ogc.sensor.metadata.MetadataList;

/**
 * @author Dejan Antanaskovic, <a href="mailto:dejan.antanaskovic@tuhh.de">dejan.antanaskovic@tuhh.de</a>
 */
public class NativeObservationZrxAdapter extends AbstractObservationImporter
{
  private final DateFormat m_zrxDateFormat = new SimpleDateFormat( "yyyyMMddHHmm" ); //$NON-NLS-1$

  private final DateFormat m_zrxDateFormatSec = new SimpleDateFormat( "yyyyMMddHHmmss" ); //$NON-NLS-1$

  private static Pattern ZRX_HEADER_PATTERN = Pattern.compile( "#.*" ); //$NON-NLS-1$

  private static Pattern ZRX_DATA_PATTERN = Pattern.compile( "([0-9]{12,14})\\s+(-??[0-9]+(.[0-9]*))\\s*" ); //$NON-NLS-1$

  private static Pattern ZRX_SNAME_PATTERN = Pattern.compile( "(#\\S*SNAME)(\\w+)(\\|\\*\\|\\S*)" ); //$NON-NLS-1$

  private String m_sName = "titel"; //$NON-NLS-1$

  private static final int MAX_NO_OF_ERRORS = 30;

  @Override
  public IObservation importTimeseries( final File source, final TimeZone timeZone, final String valueType, final boolean continueWithErrors ) throws Exception
  {
    final MetadataList metaDataList = new MetadataList();
    metaDataList.put( IMetadataConstants.MD_ORIGIN, source.getAbsolutePath() );

    m_zrxDateFormat.setTimeZone( timeZone );
    m_zrxDateFormatSec.setTimeZone( timeZone );

    final IAxis[] axis = createAxis( valueType );
    final ITupleModel tuppelModel = createTuppelModel( source, axis, continueWithErrors );
    if( tuppelModel == null )
      return null;
    return new SimpleObservation( "href", m_sName, metaDataList, tuppelModel ); //$NON-NLS-1$
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
    while( (lineIn = reader.readLine()) != null )
    {
      if( !continueWithErrors && numberOfErrors > MAX_NO_OF_ERRORS )
        return null;
      try
      {
        Matcher matcher = ZRX_DATA_PATTERN.matcher( lineIn );
        if( matcher.matches() )
        {
          Date date = null;
          Double value = null;
          if( matcher.group( 1 ).length() == 14 ) // date with seconds
          {
            try
            {
              date = m_zrxDateFormatSec.parse( matcher.group( 1 ) );
            }
            catch( final Exception e )
            {
              errorBuffer.append( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationZrxAdapter.11" ) + reader.getLineNumber() + Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationZrxAdapter.12" ) + lineIn + "\"\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
              numberOfErrors++;
            }
          }
          else
          {
            try
            {
              date = m_zrxDateFormat.parse( matcher.group( 1 ) );
            }
            catch( final Exception e )
            {
              errorBuffer.append( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationZrxAdapter.14" ) + reader.getLineNumber() + Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationZrxAdapter.15" ) + lineIn + "\"\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
              numberOfErrors++;
            }
          }
          try
          {
            value = new Double( matcher.group( 2 ) );
          }
          catch( final Exception e )
          {
            errorBuffer.append( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationZrxAdapter.17" ) + reader.getLineNumber() + Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationZrxAdapter.18" ) + lineIn + "\"\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            numberOfErrors++;
          }
          dateCollector.add( date );
          valueCollector.add( value );
        }
        else
        {
          matcher = ZRX_HEADER_PATTERN.matcher( lineIn );
          if( matcher.matches() )
          {
            matcher = ZRX_SNAME_PATTERN.matcher( lineIn );
            if( matcher.matches() )
              m_sName = matcher.group( 2 );
          }
          else
          {
            errorBuffer.append( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationZrxAdapter.20" ) + reader.getLineNumber() + Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationZrxAdapter.21" ) + lineIn + "\"\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            numberOfErrors++;
          }
        }
      }
      catch( final Exception e )
      {
        errorBuffer.append( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationZrxAdapter.23" ) + reader.getLineNumber() + Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationZrxAdapter.24" ) + e.getLocalizedMessage() + "\"\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        numberOfErrors++;
      }
    }
    if( !continueWithErrors && numberOfErrors > MAX_NO_OF_ERRORS )
    {

      final MessageBox messageBox = new MessageBox( null, SWT.ICON_QUESTION | SWT.YES | SWT.NO );
      messageBox.setMessage( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationZrxAdapter.26" ) ); //$NON-NLS-1$
      messageBox.setText( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationZrxAdapter.27" ) ); //$NON-NLS-1$
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