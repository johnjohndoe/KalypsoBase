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
package org.kalypso.ogc.sensor.timeseries;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.LocalTime;
import org.kalypso.contribs.java.util.CalendarUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
 * Helper class that guesses the timestamp from a given timeseries.<br/>
 * REMARK: This class priorizes times in Winter, and only tests summer times, if no winter times are available at all.
 * All/Most of the old projects have problematic timeseries.
 * 
 * @author Holger Albert
 */
public class TimestampGuesser
{
  /**
   * The tuple model of a timeseries.
   */
  private final ITupleModel m_timeseries;

  /**
   * The number of test steps. -1, if the entire timeseries should be tested.
   */
  private final int m_testSteps;

  /**
   * The constructor.
   * 
   * @param timeseries
   *          The tuple model of a timeseries.
   * @param testSteps
   *          The number of test steps. -1, if the entire timeseries should be tested.
   */
  public TimestampGuesser( final ITupleModel timeseries, final int testSteps )
  {
    m_timeseries = timeseries;
    m_testSteps = testSteps;
  }

  /**
   * This function guesses the timestamp.
   * 
   * @return The timestamp in UTC.
   */
  public LocalTime execute( ) throws SensorException
  {
    /* Used to determine the number of equal timestamps. */
    final Multiset<LocalTime> timestampsDSTWinter = HashMultiset.create();
    final Multiset<LocalTime> timestampsDSTSummer = HashMultiset.create();

    /* Get the number of test steps. */
    final int testSteps = getTestSteps();

    /* Find the date axis. */
    final IAxis dateAxis = AxisUtils.findDateAxis( m_timeseries.getAxes() );
    if( dateAxis == null )
      throw new IllegalArgumentException( "Argument mus tbe a timeseries" ); //$NON-NLS-1$

    /* Collect all timestamps. */
    for( int i = 0; i < testSteps; i++ )
    {
      /* REMARK: We need UTC here. */
      final Date date = (Date) m_timeseries.get( i, dateAxis );
      final Calendar calendar = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) ); //$NON-NLS-1$
      calendar.setTime( date );

      /* Differ between daylight saving winter and summer times. */
      /* Old zml daylight saving summer times are possible broken! */
      final boolean dstWinterTime = CalendarUtilities.isDSTWinterTime( date );

      /* REMARK: The ISO Chronolgy used will have the UTC timezone set. */
      /* REMARK: See the source code of the constructor. */
      final LocalTime timestamp = new LocalTime( calendar.get( Calendar.HOUR_OF_DAY ), calendar.get( Calendar.MINUTE ) );
      if( dstWinterTime )
        timestampsDSTWinter.add( timestamp );
      else
        timestampsDSTSummer.add( timestamp );
    }

    /* We want to use the one, with the most occurences. */
    final LocalTime timestamp = doGuessTimestamp( timestampsDSTWinter );
    if( timestamp != null )
      return timestamp;

    return doGuessTimestamp( timestampsDSTSummer );
  }

  /**
   * This function returns the timestamp with the most occurances.
   * 
   * @param timestamps
   *          The timestamps
   * @return The timestamp with the most occurances.
   */
  private LocalTime doGuessTimestamp( final Multiset<LocalTime> timestamps )
  {
    LocalTime foundTimestamp = null;
    int maxCount = 0;
    for( final LocalTime timestamp : timestamps )
    {
      final int count = timestamps.count( timestamp );
      if( count > maxCount )
      {
        foundTimestamp = timestamp;
        maxCount = count;
      }
    }

    return foundTimestamp;
  }

  /**
   * This function returns the number of test steps.
   * 
   * @return The size of the timeseries if no number of test steps was provided in the constructor. Otherwise it returns
   *         the lower number of either the size of the timeseries or the provided number of test steps.
   */
  private int getTestSteps( ) throws SensorException
  {
    /* Get the size of the timeseries. */
    final int size = m_timeseries.size();
    if( m_testSteps == -1 )
      return size;

    /* Prevent that the timeseries is smaller then number of test steps. */
    return Math.min( size, m_testSteps );
  }
}