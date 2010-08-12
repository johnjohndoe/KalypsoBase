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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.kalypso.commons.java.util.StringUtilities;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.filter.filters.interval.IntervalFilter.MODE;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHelper;
import org.kalypso.ogc.sensor.timeseries.datasource.IDataSourceItem;

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

  private int[] m_status;

  private double[] m_value;

  private String[] m_sources;

  public Interval( final Calendar start, final Calendar end, final double[] value, final int[] status, final String[] sources )
  {
    m_sources = sources;
    m_start = (Calendar) start.clone();
    m_end = (Calendar) end.clone();
    m_status = status.clone();
    m_value = value.clone();
  }

  private Interval( final Calendar start, final Calendar end )
  {
    m_start = (Calendar) start.clone();
    m_end = (Calendar) end.clone();
    m_status = null;
    m_value = null;
    m_sources = null;
  }

  public Interval( final Calendar start, final Calendar end, final Double[] values, final Integer[] status, final String[] sources )
  {
    m_start = start;
    m_end = end;

    m_status = new int[status.length];
    for( int i = 0; i < status.length; i++ )
    {
      m_status[i] = status[i].intValue();
    }

    m_value = new double[values.length];
    for( int i = 0; i < values.length; i++ )
    {
      m_value[i] = values[i].doubleValue();
    }

    m_sources = sources;
  }

  public Calendar getEnd( )
  {
    return (Calendar) m_end.clone();
  }

  public Calendar getStart( )
  {
    return (Calendar) m_start.clone();
  }

  public int[] getStatus( )
  {
    return m_status.clone();
  }

  public void setStatus( final int[] status )
  {
    m_status = status.clone();
  }

  public double[] getValue( )
  {
    return m_value.clone();
  }

  public String[] getSources( )
  {
    return m_sources.clone();
  }

  public void setValue( final double[] value )
  {
    m_value = value.clone();
  }

  private long getDurationInMillis( )
  {
    return m_end.getTimeInMillis() - m_start.getTimeInMillis();
  }

  public int calcIntersectionMatrix( final Interval other )
  {
    int result = 0;
    if( getStart().before( other.getStart() ) )
      result |= 1;
    if( getEnd().before( other.getEnd() ) )
      result |= 2;
    if( getStart().before( other.getEnd() ) )
      result |= 4;
    if( getEnd().before( other.getStart() ) )
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
    final Interval result;
    final int matrix = calcIntersectionMatrix( other );
    switch( matrix )
    {
      case STATUS_INTERSECTION_START:
        result = new Interval( getStart(), other.getEnd() );
        break;

      case STATUS_INTERSECTION_END:
        result = new Interval( other.getStart(), getEnd() );
        break;

      case STATUS_INTERSECTION_INSIDE:
        result = new Interval( other.getStart(), other.getEnd() );
        break;

      case STATUS_INTERSECTION_ARROUND:
        result = new Interval( getStart(), getEnd() );
        break;

      case STATUS_INTERSECTION_NONE_BEFORE:
      case STATUS_INTERSECTION_NONE_AFTER:
        return null;
      default:
        return null;
    }

    // calculate interval values;
    final double[] values = getValue();
    final double factor = calcFactorIntersect( result, mode );

    for( int i = 0; i < values.length; i++ )
    {
      values[i] = factor * values[i];
    }

    result.setValue( values );

    final int[] status = getStatus();
    /* Bugfix: empty intervals never get a status */
    if( result.getDurationInMillis() == 0 )
    {
      for( int i = 0; i < status.length; i++ )
      {
        status[i] = KalypsoStati.BIT_OK;
      }
    }

    result.setStatus( status );

    final String[] sources = getSources();
    for( int i = 0; i < sources.length; i++ )
    {
      final String source = sources[i];

      /* Faktor != 1: "verschmiert?source=Prio_X" */
      if( factor != 1.0 )
      {
        final String reference = String.format( "filter://%s?source_1=%s", IntervalFilter.class.getName(), source );
        sources[i] = reference;
      }
    }

    result.setSources( sources );

    return result;
  }

  private void setSources( final String[] sources )
  {
    m_sources = sources.clone();
  }

  public void merge( final Interval other, final MODE mode )
  {
    final double factor = calcFactorMerge( other, mode );
    for( int i = 0; i < other.getValue().length; i++ )
    {
      m_value[i] += factor * other.getValue()[i];
    }

    for( int i = 0; i < other.getStatus().length; i++ )
    {
      m_status[i] |= other.getStatus()[i];
    }

    final String[] otherSources = other.getSources();
    for( int i = 0; i < otherSources.length; i++ )
    {
      final String reference = mergeSourceReference( m_sources[i], otherSources[i] );
      m_sources[i] = reference;
    }

  }

  private String mergeSourceReference( final String base, final String other )
  {
    // - wenn undefiniert: quelle kopieren
    // - wenn schon definiert: "verschimiert": nach ? kombinieren

    if( IDataSourceItem.SOURCE_UNKNOWN.equalsIgnoreCase( base ) )
      return other;
    else if( base.startsWith( "filter://" ) )
    {
      final Set<String> sources = new LinkedHashSet<String>();
      Collections.addAll( sources, DataSourceHelper.getSources( base ) );

      if( other.startsWith( "filter://" ) )
        Collections.addAll( sources, DataSourceHelper.getSources( other ) );
      else if( !IDataSourceItem.SOURCE_UNKNOWN.equals( other ) )
        sources.add( other );

      final StringBuffer buffer = new StringBuffer();
      buffer.append( String.format( "filter://%s?", IntervalFilter.class.getName() ) );

      final String[] sourceArray = sources.toArray( new String[] {} );
      for( int i = 0; i < sourceArray.length; i++ )
      {
        buffer.append( String.format( "source_%d=%s&", i, sourceArray[i] ) );
      }

      return StringUtilities.chomp( buffer.toString() );
    }

    throw new IllegalStateException();
  }

  private double calcFactorIntersect( final Interval other, final MODE mode )
  {
    switch( mode )
    {
      case eSum:
        /* If target interval length is 0; factor is 0 (the empty sum) */
        final long durationInMillis = getDurationInMillis();
        if( durationInMillis == 0 )
          return 0d;

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
        return 1d;
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
    if( m_value != null )
    {
      result.append( Messages.getString( "org.kalypso.ogc.sensor.filter.filters.Intervall.6" ) ); //$NON-NLS-1$
      for( final double element : m_value )
      {
        result.append( "  " + element ); //$NON-NLS-1$
      }

      result.append( "\n" ); //$NON-NLS-1$
    }
    if( m_status != null )
    {
      result.append( Messages.getString( "org.kalypso.ogc.sensor.filter.filters.Intervall.9" ) ); //$NON-NLS-1$
      for( final int status : m_status )
      {
        result.append( "  " + status ); //$NON-NLS-1$
      }

      result.append( "\n" ); //$NON-NLS-1$
    }
    return result.toString();
  }
}