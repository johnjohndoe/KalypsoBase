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

import java.util.Date;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

/**
 * Helper class that guesses the timestep from a given timeseries.
 * 
 * @author Gernot Belger
 */
class TimestepGuesser
{
  private final Multiset<Period> m_timesteps = HashMultiset.create();

  private final ITupleModel m_timeseries;

  private final int m_guessFromNumberOfTimesteps;

  public TimestepGuesser( final ITupleModel timeseries, final int guessFromNumberOfTimesteps )
  {
    m_timeseries = timeseries;
    m_guessFromNumberOfTimesteps = guessFromNumberOfTimesteps;
  }

  public Period execute( ) throws SensorException
  {
    final int testSteps = getTestSteps();

    final IAxis dateAxis = AxisUtils.findDateAxis( m_timeseries.getAxes() );
    if( dateAxis == null )
      throw new IllegalArgumentException( "Argument must be a timeseries" ); //$NON-NLS-1$

    for( int i = 0; i < testSteps - 1; i++ )
    {
      final Date date1 = (Date) m_timeseries.get( i, dateAxis );
      final Date date2 = (Date) m_timeseries.get( i + 1, dateAxis );

      final Period timestep = new Period( new DateTime( date1 ), new DateTime( date2 ) );
      m_timesteps.add( timestep );
    }

    /* Find maximum */
    final SortedMap<Integer, Period> orderedByCount = new TreeMap<>();

    final Set<Entry<Period>> entrySet = m_timesteps.entrySet();
    for( final Entry<Period> entry : entrySet )
    {
      final int count = entry.getCount();
      final Period element = entry.getElement();
      orderedByCount.put( count, element );
    }

    if( orderedByCount.isEmpty() )
      return null;

    final Integer maxCount = orderedByCount.lastKey();
    return orderedByCount.get( maxCount );
  }

  private int getTestSteps( ) throws SensorException
  {
    final int size = m_timeseries.size();

    if( m_guessFromNumberOfTimesteps == -1 )
      return size;

    /* Prevent timeseries is smaller then number of test steps */
    return Math.min( size, m_guessFromNumberOfTimesteps );
  }
}