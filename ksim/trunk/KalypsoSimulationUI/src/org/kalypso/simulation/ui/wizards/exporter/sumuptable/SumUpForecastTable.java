/*--------------- Kalypso-Header ------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 --------------------------------------------------------------------------*/

package org.kalypso.simulation.ui.wizards.exporter.sumuptable;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.util.CalendarUtilities;
import org.kalypso.contribs.java.util.DoubleComparator;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITuppleModel;
import org.kalypso.ogc.sensor.MetadataList;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.timeseries.TimeserieConstants;
import org.kalypso.ogc.sensor.timeseries.TimeserieUtils;
import org.kalypso.simulation.ui.wizards.exporter.ExporterHelper.UrlArgument;

/**
 * 
 * @author schlienger
 */
public class SumUpForecastTable
{
  private final String m_axisType;
  private final double m_delta;
  protected final int m_timeUnit;
  protected final int m_timeStep;
  private final ColumnsHeader m_colHeader = new ColumnsHeader();
  private final Map m_map = new HashMap();

  public SumUpForecastTable( final String axisType, final int timeUnit, final int timeStep, final double delta )
  {
    m_axisType = axisType;
    m_timeUnit = timeUnit;
    m_timeStep = timeStep;
    m_delta = delta;
  }

  public IStatus addObservation( final UrlArgument argument, final IObservation obs ) throws SensorException
  {
    final DateRange range = TimeserieUtils.isForecast( obs );
    if( range == null )
      return StatusUtilities.createInfoStatus( "Zeitreihe beinhaltet keinen Vorhersagezeitraum: " + obs.getName()
          + " in Datei " + argument.getUrl().toString() );

    final IAxis dateAxis = ObservationUtilities.findAxisByType( obs.getAxisList(), TimeserieConstants.TYPE_DATE );
    final IAxis valueAxis = ObservationUtilities.findAxisByType( obs.getAxisList(), m_axisType );

    final ITuppleModel values = obs.getValues( null );

    final DoubleComparator dc = new DoubleComparator( m_delta );
    Number maxValue = new Double( Double.MIN_VALUE );
    int maxPos = -1;

    // seek for max value
    for( int i = 0; i < values.getCount(); i++ )
    {
      final Number value = (Number)values.getElement( i, valueAxis );
      if( dc.compare( value, maxValue ) > 0 )
      {
        maxValue = value;
        maxPos = i;
      }
    }

    final Date maxTime = (Date)values.getElement( maxPos, dateAxis );

    // seek for alarm level
    final MetadataList md = obs.getMetadataList();
    final Double[] alarms = new Double[]
    { Double.valueOf( md.getProperty( TimeserieConstants.MD_ALARM_1, "-1" ) ), Double.valueOf( md.getProperty(
        TimeserieConstants.MD_ALARM_2, "-1" ) ), Double.valueOf( md.getProperty( TimeserieConstants.MD_ALARM_3, "-1" ) ), Double
        .valueOf( md.getProperty( TimeserieConstants.MD_ALARM_4, "-1" ) ) };

    String strAlarm = "-";
    double alarm = 0;
    for( int i = 1; i <= alarms.length; i++ )
      if( alarms[i - 1].doubleValue() != -1 && alarms[i - 1].doubleValue() >= maxValue.doubleValue() )
      {
        strAlarm = "AS " + i;
        alarm = alarms[i - 1].doubleValue();
      }

    // fetch values at specific time-positions
    final Calendar cal = Calendar.getInstance();
    cal.setTime( range.getFrom() );

    final Row row = new Row( maxValue, maxTime, strAlarm, alarm );

    while( cal.getTime().compareTo( range.getTo() ) <= 0 )
    {
      final Date time = cal.getTime();
      final int pos = values.indexOf( time, dateAxis );
      if( pos == -1 )
        break;

      final Number value = (Number)values.getElement( pos, valueAxis );

      row.addValue( value );

      m_colHeader.checkAndAddColumn( time );

      cal.add( m_timeUnit, m_timeStep );
    }

    m_map.put( argument, row );

    return Status.OK_STATUS;
  }

  public void writeHeader( final Writer writer, final String separator, final DateFormat df ) throws IOException
  {
    final Date[] dates = m_colHeader.getDates();
    final String[] distances = m_colHeader.getDistances();

    for( int i = 0; i < dates.length; i++ )
    {
      writer.write( df.format( dates[i] ) + " (" + distances[i] + ")" );

      if( i < dates.length - 1 )
        writer.write( separator );
    }
  }

  public void writeRow( final Object key, Writer writer, String sep, DateFormat df, NumberFormat nf )
      throws IOException
  {
    final Row row = (Row)m_map.get( key );

    if( row != null )
      writer.write( row.dumpRow( sep, nf, df ) );
    else
      writer.write( "<keine Daten vorhanden, Zeitreihe möglicherweise ungültig>" );
  }

  public void dispose()
  {
    m_map.clear();
    m_colHeader.dispose();
  }

  /**
   * Represents a row of the sum-up table
   * 
   * @author schlienger
   */
  public static class Row
  {
    private final Number m_maxValue;
    private final Date m_maxTime;
    private final String m_strAlarm;
    private final double m_alarm;
    private final List m_values = new ArrayList();

    public Row( final Number maxValue, final Date maxTime, final String strAlarm, final double alarm )
    {
      m_maxValue = maxValue;
      m_maxTime = maxTime;
      m_strAlarm = strAlarm;
      m_alarm = alarm;
    }

    public void addValue( final Number value )
    {
      m_values.add( value );
    }

    public String dumpRow( final String separator, final NumberFormat nf, final DateFormat df )
    {
      final StringBuffer bf = new StringBuffer();
      bf.append( nf.format( m_maxValue ) ).append( separator ).append( df.format( m_maxTime ) ).append( separator )
          .append( m_strAlarm ).append( separator );

      if( m_strAlarm.length() > 0 )
        bf.append( nf.format( m_alarm ) );

      bf.append( separator );

      for( final Iterator it = m_values.iterator(); it.hasNext(); )
      {
        bf.append( nf.format( it.next() ) );
        if( it.hasNext() )
          bf.append( separator );
      }

      return bf.toString();
    }
  }

  public class ColumnsHeader
  {
    private final Set m_dates = new TreeSet();
    private final List m_hints = new ArrayList();

    public void checkAndAddColumn( final Date d )
    {
      if( m_dates.contains( d ) )
        return;

      final int size = m_dates.size();
      String distance = "";
      if( size == 0 )
        distance = "Ist";
      else
        distance = size * m_timeStep + " " + CalendarUtilities.getAbbreviation( m_timeUnit );

      m_hints.add( distance );

      m_dates.add( d );
    }

    public void dispose()
    {
      m_dates.clear();
      m_hints.clear();
    }

    public String[] getDistances()
    {
      return (String[])m_hints.toArray( new String[m_hints.size()] );
    }

    public Date[] getDates()
    {
      return (Date[])m_dates.toArray( new Date[m_dates.size()] );
    }
  }
}
