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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.impl.DefaultAxis;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.timeseries.TimeseriesUtils;

/**
 * @author Jessica Huebsch, <a href="mailto:j.huebsch@tuhh.de">j.huebsch@tuhh.de</a>
 */
public class NativeObservationDWDmdAdapter implements INativeObservationAdapter
{
  private final DateFormat m_dwdMDDateFormat = new SimpleDateFormat( "ddMMyyyyHHmmss" ); //$NON-NLS-1$

  public static Pattern DWD_MD_FIRST_HEADER_PATTERN = Pattern.compile( "[\\d]{5}[\\d\\w\\s]{15}(.{30}).+?" ); //$NON-NLS-1$

  public static Pattern DWD_MD_SECOND_HEADER_PATTERN = Pattern.compile( ".{20}(.{5}).{4}([0-9]{1}).{28}(.{5}).+?" ); //$NON-NLS-1$

  public static Pattern DWD_MD_DATA_PATTERN = Pattern.compile( "([0-9]{5})([\\s\\d]{2}[\\s\\d]{2}[0-9]{4}[\\d\\s]{2}[\\d\\s]{2}[\\s\\d]{2})(.{1})(.+?)" ); //$NON-NLS-1$

  private String m_title;

  private String m_axisTypeValue;

  private String m_name;

  private int m_intervall;

  private Integer m_dimension;

  private int m_div;

  private static final int SEARCH_BLOCK_HEADER = 0;

  private static final int SEARCH_VALUES = 1;

  private static final int MAX_NO_OF_ERRORS = 30;

  /**
   * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
   *      java.lang.String, java.lang.Object)
   */
  @Override
  public void setInitializationData( final IConfigurationElement config, final String propertyName, final Object data )
  {
    m_title = config.getAttribute( "label" ); //$NON-NLS-1$
    m_axisTypeValue = config.getAttribute( "axisType" ); //$NON-NLS-1$
  }

  @Override
  public IObservation createObservationFromSource( final File source, TimeZone timeZone, final boolean continueWithErrors ) throws Exception
  {
    final MetadataList metaDataList = new MetadataList();

    /* this is due to backwards compatibility */
    if( timeZone == null )
      timeZone = TimeZone.getTimeZone( "GMT+1" ); //$NON-NLS-1$

    m_dwdMDDateFormat.setTimeZone( timeZone );
    // create axis
    final IAxis[] axis = createAxis();
    final ITupleModel tuppelModel = createTuppelModel( source, axis, continueWithErrors );
    if( tuppelModel == null )
      return null;
    return new SimpleObservation( "href", m_name, metaDataList, tuppelModel ); //$NON-NLS-1$ //$NON-NLS-2$
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
    int step = SEARCH_BLOCK_HEADER;
    while( (lineIn = reader.readLine()) != null )
    {
      if( !continueWithErrors && numberOfErrors > MAX_NO_OF_ERRORS )
        return null;
      switch( step )
      {
        case SEARCH_BLOCK_HEADER:
          Matcher matcher = DWD_MD_FIRST_HEADER_PATTERN.matcher( lineIn );
          if( matcher.matches() )
          {
            m_name = matcher.group( 1 ).trim();
            lineIn = reader.readLine();
            matcher = DWD_MD_SECOND_HEADER_PATTERN.matcher( lineIn );
            if( matcher.matches() )
            {
              m_intervall = Integer.parseInt( matcher.group( 1 ).trim() ) * 60 * 1000;
              m_dimension = Integer.parseInt( matcher.group( 2 ) );
              if( m_dimension == 2 )
                m_div = 100;
              else if( m_dimension == 3 )
                m_div = 1000;
              // read not needed comment lines
              for( int i = 0; i < Integer.parseInt( matcher.group( 3 ).trim() ); i++ )
              {
                lineIn = reader.readLine();
              }
            }
          }
          else
          {
            errorBuffer.append( "line " + reader.getLineNumber() + Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDWDmdAdapter.10" ) + lineIn + "\"\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            numberOfErrors++;
          }
          step++;
          break;
        case SEARCH_VALUES:
          matcher = DWD_MD_DATA_PATTERN.matcher( lineIn );
          if( matcher.matches() )
          {
            Date date = null;
            String valueLine = null;
            try
            {
              date = m_dwdMDDateFormat.parse( matcher.group( 2 ) );
            }
            catch( final Exception e )
            {
              errorBuffer.append( "line " + reader.getLineNumber() + Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDWDmdAdapter.13" ) + lineIn + "\"\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
              numberOfErrors++;
            }
            try
            {
              final String label = matcher.group( 3 ).trim();
              if( "".equals( label ) ) //$NON-NLS-1$
              {
                valueLine = matcher.group( 4 );
                final long startDate = date.getTime();
                for( int i = 0; i < 12; i++ )
                {
                  final String valueString = valueLine.substring( i * 5, 5 * (i + 1) );
                  final Double value = new Double( Double.parseDouble( valueString ) ) / m_div;
                  valueCollector.add( value );
                  final Date valueDate = new Date( startDate + i * m_intervall );
                  dateCollector.add( valueDate );
                }
              }
              // No precipitation the whole day (24 hours * 12 values = 288 values)
              else if( "N".equals( label ) ) //$NON-NLS-1$
              {
                final Double value = 0.0;
                final long startDate = date.getTime();
                for( int i = 0; i < 288; i++ )
                {
                  valueCollector.add( value );
                  final Date valueDate = new Date( startDate + i * m_intervall );
                  dateCollector.add( valueDate );
                }
              }
              else if( "A".equals( label ) ) //$NON-NLS-1$
              {
                final Double value = 9999.0;
                final long startDate = date.getTime();
                for( int i = 0; i < 12; i++ )
                {
                  valueCollector.add( value );
                  final Date valueDate = new Date( startDate + i * m_intervall );
                  dateCollector.add( valueDate );
                }
              }
              else if( "E".equals( label ) ) //$NON-NLS-1$
              {
                // do nothing
              }
            }
            catch( final Exception e )
            {
              errorBuffer.append( "line " + reader.getLineNumber() + Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDWDmdAdapter.20" ) + lineIn + "\"\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
              numberOfErrors++;
            }
          }
          else
          {
            errorBuffer.append( "line " + reader.getLineNumber() + Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDWDmdAdapter.23" ) + lineIn + "\"\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            numberOfErrors++;
          }
          break;
        default:
          break;
      }

    }
    if( !continueWithErrors && numberOfErrors > MAX_NO_OF_ERRORS )
    {

      final MessageBox messageBox = new MessageBox( null, SWT.ICON_QUESTION | SWT.YES | SWT.NO );
      messageBox.setMessage( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDWDmdAdapter.25" ) ); //$NON-NLS-1$
      messageBox.setText( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDWDmdAdapter.26" ) ); //$NON-NLS-1$
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

  @Override
  public String toString( )
  {
    return m_title;
  }

  /**
   * @see org.kalypso.ogc.sensor.adapter.INativeObservationAdapter#createAxis()
   */
  @Override
  public IAxis[] createAxis( )
  {
    final IAxis dateAxis = new DefaultAxis( Messages.getString( "org.kalypso.ogc.sensor.adapter.NativeObservationDWDmdAdapter.27" ), ITimeseriesConstants.TYPE_DATE, "", Date.class, true ); //$NON-NLS-1$ //$NON-NLS-2$
    TimeseriesUtils.getUnit( m_axisTypeValue );
    final IAxis valueAxis = new DefaultAxis( TimeseriesUtils.getName( m_axisTypeValue ), m_axisTypeValue, TimeseriesUtils.getUnit( m_axisTypeValue ), Double.class, false );
    final IAxis[] axis = new IAxis[] { dateAxis, valueAxis };
    return axis;
  }

  /**
   * @see org.kalypso.ogc.sensor.adapter.INativeObservationAdapter#getAxisTypeValue()
   */
  @Override
  public String getAxisTypeValue( )
  {
    return m_axisTypeValue;
  }
}