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
package org.kalypso.ogc.sensor.filter.filters.interval;

import java.util.Calendar;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.filter.filters.interval.IntervalFilter.MODE;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;

/**
 * @author doemming
 */
public class Interval
{
  /**
   * IMPORTANT: do not change numbering of statuses
   */

  /**
   * <pre>
   *      |--other--|
   *                      |-----this------|
   * </pre>
   */
  public static final int STATUS_INTERSECTION_NONE_BEFORE = 0;

  /**
   * <pre>
   *                             |--other--|
   *  |-----this------|
   * </pre>
   */
  public static final int STATUS_INTERSECTION_NONE_AFTER = 15;

  /**
   * <pre>
   *  |--other--|
   *  |-----this------|
   * </pre>
   */
  public static final int STATUS_INTERSECTION_START = 4;

  /**
   * <pre>
   *                  |--other--|
   *  |-----this------|
   * </pre>
   */
  public static final int STATUS_INTERSECTION_END = 7;

  /**
   * <pre>
   *          |--other--|
   *  |-----this------|
   * </pre>
   */
  public static final int STATUS_INTERSECTION_INSIDE = 5;

  /**
   * <pre>
   *   |--------other---------|
   *  |-----this------|
   * </pre>
   */
  public static final int STATUS_INTERSECTION_ARROUND = 6;

  final Calendar m_start;

  final Calendar m_end;

  private TupleModelDataSet[] m_dataSets;

  // FIME: why do we have a Double/Integer constructor at all? The entries are not checked for null, so we could just
  // use primitive arrays
  public Interval( final Calendar start, final Calendar end, final TupleModelDataSet[] dataSets )
  {
    m_start = start;
    m_end = end;

    m_dataSets = new TupleModelDataSet[ArrayUtils.getLength( dataSets )];
    for( int index = 0; index < ArrayUtils.getLength( dataSets ); index++ )
    {
      final TupleModelDataSet dataSet = dataSets[index];

      m_dataSets[index] = dataSet.clone();
    }
  }

  private Interval( final Calendar start, final Calendar end )
  {
    this( start, end, new TupleModelDataSet[] {} );
  }

  public Calendar getEnd( )
  {
    return (Calendar) m_end.clone();
  }

  public Calendar getStart( )
  {
    return (Calendar) m_start.clone();
  }

  public TupleModelDataSet[] getDataSets( )
  {
    return m_dataSets;
  }

  public void setValue( final TupleModelDataSet[] dataSets )
  {
    m_dataSets = TupleModelDataSet.clone( dataSets );
  }

  private long getDurationInMillis( )
  {
    return m_end.getTimeInMillis() - m_start.getTimeInMillis();
  }

  public int calcIntersectionMatrix( final Interval other )
  {
    // REMARK: not using getters for start/end as cloning the calendars is a performance hot spot of this class.

    int result = 0;
    if( m_start.before( other.m_start ) )
      result |= 1;
    if( m_end.before( other.m_end ) )
      result |= 2;
    if( m_start.before( other.m_end ) )
      result |= 4;
    if( m_end.before( other.m_start ) )
      result |= 8;
    return result;
  }

  public boolean intersects( final Interval other )
  {
    final int matrix = calcIntersectionMatrix( other );
    return !(matrix == STATUS_INTERSECTION_NONE_AFTER || matrix == STATUS_INTERSECTION_NONE_BEFORE);
  }

  public Interval getIntersection( final Interval other, final MODE mode )
  {
    final int matrix = calcIntersectionMatrix( other );
    final Interval result = calcIntersectionInterval( matrix, other );

    // calculate interval values;
    final double factor = calcFactorIntersect( result, mode );

    final TupleModelDataSet[] dataSets = TupleModelDataSet.clone( getDataSets() );

    for( final TupleModelDataSet dataSet : dataSets )
    {
      final Object value = dataSet.getValue();
      if( value instanceof Number )
      {
        final Number number = (Number) value;
        dataSet.setValue( number.doubleValue() * factor );
      }

      /* Bugfix: empty intervals never get a status */
      if( result.getDurationInMillis() == 0 )
      {
        dataSet.setStatus( KalypsoStati.BIT_OK );
      }
    }

    result.setValue( dataSets );

    return result;
  }

  private Interval calcIntersectionInterval( final int matrix, final Interval other )
  {
    switch( matrix )
    {
      // REMARK: not using getters for start/end as cloning the calendars is a performance hot spot of this class.

      case STATUS_INTERSECTION_START:
        return new Interval( m_start, other.m_end );

      case STATUS_INTERSECTION_END:
        return new Interval( other.m_start, m_end );

      case STATUS_INTERSECTION_INSIDE:
        return new Interval( other.m_start, other.m_end );

      case STATUS_INTERSECTION_ARROUND:
        return new Interval( m_start, m_end );

      case STATUS_INTERSECTION_NONE_BEFORE:
      case STATUS_INTERSECTION_NONE_AFTER:
        return null;

      default:
        return null;
    }
  }

  public void merge( final Interval sourceInterval, final Interval intersection, final MODE mode )
  {
    final double factor = calcFactorMerge( intersection, mode );

    final TupleModelDataSet[] interSectionDataSets = intersection.getDataSets();
    for( final TupleModelDataSet intersectionDataSet : interSectionDataSets )
    {
      final TupleModelDataSet myDataSet = findDataSet( intersectionDataSet.getValueAxis() );

      // m_value[i] += factor * intersection.getValue()[i];
      final Object value1 = myDataSet.getValue();
      final Object value2 = intersectionDataSet.getValue();

      myDataSet.setValue( ((Number) value1).doubleValue() + factor * ((Number) value2).doubleValue() );
      myDataSet.setStatus( myDataSet.getStatus() | intersectionDataSet.getStatus() );

      if( !intersection.getStart().equals( intersection.getEnd() ) )
        myDataSet.setSource( mergeSources( sourceInterval, myDataSet.getSource(), intersectionDataSet.getSource() ) );
    }
  }

  private TupleModelDataSet findDataSet( final IAxis valueAxis )
  {
    for( final TupleModelDataSet dataSet : m_dataSets )
    {
      if( AxisUtils.isEqual( dataSet.getValueAxis(), valueAxis ) )
        return dataSet;

    }

    return null;
  }

  private double calcFactorIntersect( final Interval other, final MODE mode )
  {
    switch( mode )
    {
      case eSum:
        /* If target interval length is 0; factor is 0 (the empty sum) */
        final long durationInMillis = getDurationInMillis();
        if( durationInMillis == 0 )
          return 0.0;

        return (double) other.getDurationInMillis() / (double) durationInMillis;

      case eIntensity:
      default:
        return 1d;
    }
  }

  private double calcFactorMerge( final Interval other, final MODE mode )
  {
    switch( mode )
    {
      case eSum:
        return 1.0;
      case eIntensity:
      default:
        return (double) other.getDurationInMillis() / (double) getDurationInMillis();
    }
  }

  @Override
  public String toString( )
  {
    final StringBuffer result = new StringBuffer();
    result.append( Messages.getString( "org.kalypso.ogc.sensor.filter.filters.Intervall.0" ) + m_start.getTime().toString() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    result.append( Messages.getString( "org.kalypso.ogc.sensor.filter.filters.Intervall.2" ) + m_end.getTime().toString() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    result.append( Messages.getString( "org.kalypso.ogc.sensor.filter.filters.Intervall.4" ) + getDurationInMillis() + Messages.getString( "org.kalypso.ogc.sensor.filter.filters.Intervall.5" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    final TupleModelDataSet[] dataSets = getDataSets();
    for( final TupleModelDataSet dataSet : dataSets )
    {
      result.append( Messages.getString( "org.kalypso.ogc.sensor.filter.filters.Intervall.6" ) ); //$NON-NLS-1$

      final Object value = dataSet.getValue();
      if( value != null )
      {
        result.append( "  " + value ); //$NON-NLS-1$
      }
    }
    result.append( "\n" ); //$NON-NLS-1$

    for( final TupleModelDataSet dataSet : dataSets )
    {
      result.append( Messages.getString( "org.kalypso.ogc.sensor.filter.filters.Intervall.6" ) ); //$NON-NLS-1$

      final Integer status = Integer.valueOf( dataSet.getStatus() );
      if( status != null )
      {
        result.append( "  " + status ); //$NON-NLS-1$
      }
    }
    result.append( "\n" ); //$NON-NLS-1$

    return result.toString();
  }

  private String mergeSources( final Interval other, final String sourceBase, final String otherSource )
  {
    if( isSame( other ) )
      return otherSource;

    return IntervalSourceHandler.mergeSourceReference( sourceBase, otherSource );
  }

  private boolean isSame( final Interval other )
  {
    final DateRange myRange = new DateRange( getStart().getTime(), getEnd().getTime() );
    final DateRange otherRange = new DateRange( other.getStart().getTime(), other.getEnd().getTime() );

    return myRange.compareTo( otherRange ) == 0;
  }
}