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
package org.kalypso.zml.ui.table.commands.menu.spline;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.kalypso.ogc.sensor.DateRange;

import com.mxgraph.util.mxSpline;
import com.mxgraph.util.mxSpline1D;

/**
 * @author Dirk Kuch
 */
public final class Splines
{
  Map<Date, Double> m_values = new TreeMap<>();

  private final DateRange m_dateRange;

// private mxSpline m_spline;

  public Splines( final DateRange dateRange )
  {
    m_dateRange = dateRange;
  }

  /**
   * @return date as system ticks in hours (minus offset from the starting date)
   */
  public double convertDate( final Date date )
  {
    return toSystemHours( date ) - getOffset();
  }

  public Date convertDate( final double dx )
  {
    final Long time = fromSystemHours( dx + getOffset() );
    final Date date = new Date( time );

    return date;
  }

  private Long fromSystemHours( final double value )
  {
    final Double v = value * 1000.0;

    return v.longValue();
  }

  private Double getOffset( )
  {
    return toSystemHours( m_dateRange.getFrom() );
  }

  private Double toSystemHours( final Date date )
  {
    return date.getTime() / 1000.0;
  }

  public void apply( final mxSpline spline )
  {
    final mxSpline1D splineX = spline.getSplineX();
    final mxSpline1D splineY = spline.getSplineY();

    for( double i = 0.0; i <= 1.0; i += 0.001 )
    {
      final double x = splineX.getValue( i );
      final double y = splineY.getValue( i );

      final Date date = convertDate( x );
      m_values.put( date, y );
    }
  }

  public Double getValue( final Date date )
  {
    double diff = Double.MAX_VALUE;
    Date index = null;
    final Set<Date> keys = m_values.keySet();
    for( final Date key : keys )
    {
      final double d = Math.abs( date.getTime() - key.getTime() );
      if( d < diff )
      {
        diff = d;
        index = key;
      }
    }

    final Double hashed = m_values.get( index );
    return hashed;
  }

}
