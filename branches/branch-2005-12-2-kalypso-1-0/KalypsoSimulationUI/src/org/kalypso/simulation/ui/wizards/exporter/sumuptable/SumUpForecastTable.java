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
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
 * Used for creating an hydrological summary table for a list of timeseries with forecast information
 * 
 * @author schlienger
 */
public class SumUpForecastTable
{
  private static final String cNoAlarm = "-";
  private final String m_axisType;
  private final double m_delta;
  private final int m_timeUnit;
  private final int m_timeStep;
  private final ColumnsHeader m_colHeader = new ColumnsHeader();
  private final Map m_map = new HashMap();
  private Date m_firstDate;

  public SumUpForecastTable( final String axisType, final int timeUnit, final int timeStep, final double delta )
  {
    m_axisType = axisType;
    m_timeUnit = timeUnit;
    m_timeStep = timeStep;
    m_delta = delta;
    
    // initialise firstDate with null, used as a marker when adding observations
    m_firstDate = null;
  }

  public IStatus addObservation( final UrlArgument argument, final IObservation obs ) throws SensorException
  {
    final DateRange range = TimeserieUtils.isForecast( obs );
    if( range == null )
      return StatusUtilities.createInfoStatus( "Die Zeitreihe <" + obs.getName()
          + "> beinhaltet keinen Vorhersagezeitraum. Datei: " + argument.getUrl().toString() );

    final IAxis dateAxis = ObservationUtilities.findAxisByType( obs.getAxisList(), TimeserieConstants.TYPE_DATE );
    final IAxis valueAxis;
    try
    {
      valueAxis = ObservationUtilities.findAxisByType( obs.getAxisList(), m_axisType );
    }
    catch( final NoSuchElementException e )
    {
      return StatusUtilities.createWarningStatus( "Zeitreihe kann nicht berücksichtigt werden: "
          + e.getLocalizedMessage() + ". Siehe URL: " + argument.getUrl() );
    }

    final ITuppleModel values = obs.getValues( null );

    if( values.getCount() == 0 )
      throw new SensorException( "Keine Daten vorhanden in: " + argument.getUrl() );

    final DoubleComparator dc = new DoubleComparator( m_delta );
    Number maxValue = new Double( -Double.MAX_VALUE );
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

    String strAlarm = cNoAlarm;
    double alarm = 0;
    for( int i = 1; i <= alarms.length; i++ )
    {
      if( alarms[i - 1].doubleValue() != -1 )
      {
        try
        {
          // value is automatically converted if we use another axis than
          // the alarm-level default axis
          final Double alarmValue = TimeserieUtils.convertAlarmLevel( obs, m_axisType, alarms[i - 1], maxTime );
          
          // if a alarmValue is zero (mostly because of unsufficient W/Q) then it won't be considered valid       
          if( maxValue.doubleValue() >= alarmValue.doubleValue() && alarmValue.doubleValue() > 0.0 )
          {
            strAlarm = "AS " + i;
            alarm = alarmValue.doubleValue();
          }
        }
        catch( final Exception ignored ) // generic for simplicity
        {
          // empty
        }
      }
    }

    // fetch values at specific time-positions
    final Calendar cal = Calendar.getInstance();
    
    // we use firstDate as a marker, as soon as an observation has been added,
    // its first date that was found is then used for all other subsequent
    // observations that are added
    if( m_firstDate != null && range.getFrom().before( m_firstDate ) )
      cal.setTime( m_firstDate );
    else
      cal.setTime( range.getFrom() );

    final Row row = new Row( maxValue, maxTime, strAlarm, alarm );

    boolean notFoundYet = true;

    while( cal.getTime().compareTo( range.getTo() ) <= 0 )
    {
      final Date time = cal.getTime();
      final int pos = values.indexOf( time, dateAxis );
      if( pos != -1 )
      {
        if( m_firstDate == null )
          m_firstDate = time;
        
        m_colHeader.checkAndAddColumn( time, m_timeStep, m_timeUnit );

        final Number value = (Number)values.getElement( pos, valueAxis );
        row.addValue( time, value );

        notFoundYet = false;
      }

      if( notFoundYet )
        cal.add( m_timeUnit, 1 );
      else
        cal.add( m_timeUnit, m_timeStep );
    }

    m_map.put( argument, row );

    return Status.OK_STATUS;
  }

  public Date[] writeHeader( final Writer writer, final String separator, final DateFormat df ) throws IOException
  {
    final String unit = "[" + TimeserieUtils.getUnit( m_axisType ) + "]";

    writer.write( "Max " + unit );
    writer.write( separator );
    writer.write( "Eintrittszeit" );
    writer.write( separator );
    writer.write( "Überschrittene Alarmstufe" );
    writer.write( separator );
    writer.write( "Richtwert " + unit );
    writer.write( separator );

    final Date[] dates = m_colHeader.getDates();
    final String[] distances = m_colHeader.getDistances();

    for( int i = 0; i < dates.length; i++ )
    {
      writer.write( df.format( dates[i] ) + " (" + distances[i] + ")" );

      if( i < dates.length - 1 )
        writer.write( separator );
    }

    return dates;
  }

  public void writeRow( final Object key, final Writer writer, final String sep, final DateFormat df,
      final NumberFormat nf, final Date[] dates ) throws IOException
  {
    final Row row = (Row)m_map.get( key );

    writer.write( sep );

    if( row != null )
      writer.write( row.dumpRow( sep, nf, df, dates ) );
    else
      writer.write( "<keine Daten vorhanden, Zeitreihe enthält die erforderliche Datenachse nicht oder ist möglicherweise ungültig>" );
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
    private final Map m_values = new HashMap();

    public Row( final Number maxValue, final Date maxTime, final String strAlarm, final double alarm )
    {
      m_maxValue = maxValue;
      m_maxTime = maxTime;
      m_strAlarm = strAlarm;
      m_alarm = alarm;
    }

    public void addValue( final Date time, final Number value )
    {
      m_values.put( time, value );
    }

    public String dumpRow( final String separator, final NumberFormat nf, final DateFormat df, final Date[] dates )
    {
      final StringBuffer bf = new StringBuffer();
      bf.append( nf.format( m_maxValue ) ).append( separator ).append( df.format( m_maxTime ) ).append( separator )
          .append( m_strAlarm ).append( separator );

      // if no Alarmstufe has been set (m_strAlarm == cNoAlarm) then cNoAlarm should be set as alarmValue
      if( m_strAlarm.length() > 0 && !cNoAlarm.equals(m_strAlarm))
        bf.append( nf.format( m_alarm ) );
      else
        bf.append (cNoAlarm);

      bf.append( separator );

      for( int i = 0; i < dates.length; i++ )
      {
        final Number value = (Number)m_values.get( dates[i] );

        if( value != null )
          bf.append( nf.format( value ) );

        if( i < dates.length - 1 )
          bf.append( separator );
      }

      return bf.toString();
    }
  }

  public class ColumnsHeader
  {
    private final Set m_dates = new TreeSet();
    private final List m_hints = new ArrayList();

    public void checkAndAddColumn( final Date d, final int timeStep, final int timeUnit )
    {
      if( m_dates.contains( d ) )
        return;

      final int size = m_dates.size();
      String distance = "";
      if( size == 0 )
        distance = " Beginn";
      else
        distance = size * timeStep + " " + CalendarUtilities.getAbbreviation( timeUnit );

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
