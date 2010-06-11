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
package org.kalypso.contribs.java.math;

/**
 * A closed interval on the real number line.
 * 
 * @author Holger Albert
 */
public class Interval
{
  /**
   * The minimum value of the interval.
   */
  private double m_min;

  /**
   * The maximum value of the interval.
   */
  private double m_max;

  /**
   * The constructor.
   */
  public Interval( )
  {
    this( 0.0, 0.0 );
  }

  /**
   * The constructor.
   * 
   * @param interval
   *          The values will be copied from this interval.
   */
  public Interval( Interval interval )
  {
    this( interval.getMin(), interval.getMax() );
  }

  /**
   * The constructor.
   * 
   * @param min
   *          The minimum value of the interval.
   * @param max
   *          The maximum value of the interval.
   */
  public Interval( double min, double max )
  {
    m_min = min;
    m_max = max;
    if( min > max )
    {
      m_min = max;
      m_max = min;
    }
  }

  /**
   * This function returns the minimum value of the interval.
   * 
   * @return The minimum value of the interval.
   */
  public double getMin( )
  {
    return m_min;
  }

  /**
   * This function returns the maximum value of the interval.
   * 
   * @return The maximum value of the interval.
   */
  public double getMax( )
  {
    return m_max;
  }

  /**
   * This function returns the width of this interval.
   * 
   * @return The width of this interval.
   */
  public double getWidth( )
  {
    return m_max - m_min;
  }

  public boolean overlaps( Interval interval )
  {
    return overlaps( interval.getMin(), interval.getMax() );
  }

  public boolean overlaps( double min, double max )
  {
    if( m_min > max || m_max < min )
      return false;

    return true;
  }

  public boolean contains( Interval interval )
  {
    return contains( interval.getMin(), interval.getMax() );
  }

  public boolean contains( double min, double max )
  {
    return (min >= m_min && max <= m_max);
  }

  public Interval[] difference( Interval interval )
  {
    return difference( interval.getMin(), interval.getMax() );
  }

  public Interval[] difference( double min, double max )
  {
    /* Only if this and the given interval overlaps each other in one way, we can do the difference operation. */
    if( !overlaps( min, max ) )
      return new Interval[] { new Interval( m_min, m_max ) };

    /* This interval lies completely within the given interval, the result is the empty interval. */
    if( m_min >= min && m_max <= max )
      return new Interval[] { new Interval( 0.0, 0.0 ) };

    /* The given interval lies completely within this interval, the result are two intervals. */
    if( m_min < min && m_max > max )
      return new Interval[] { new Interval( m_min, min ), new Interval( max, m_max ) };

    /* .|------this------|..... */
    /* ..............|---|..... */
    /* ..................|---|. */
    /* ................|---|... */
    if( max >= m_max )
      return new Interval[] { new Interval( m_min, min ) };

    /* .....|------this------|. */
    /* .....|---|.............. */
    /* .|---|.................. */
    /* ...|---|................ */
    if( min <= m_min )
      return new Interval[] { new Interval( max, m_max ) };

    throw new IllegalStateException( "Wrong state doing the difference operation..." );
  }

  public boolean isEmptyInterval( )
  {
    if( getWidth() == 0.0 )
      return true;

    return false;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return "[" + m_min + ", " + m_max + "]";
  }
}