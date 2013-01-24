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
package org.kalypso.contribs.java.math;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for operations on intervals.
 * 
 * @author Holger Albert
 */
public class IntervalUtilities
{
  /**
   * The constructor.
   */
  private IntervalUtilities( )
  {
  }

  public static double getSumOfIntervals( Interval[] intervals )
  {
    double sum = 0.0;
    for( int i = 0; i < intervals.length; i++ )
    {
      Interval interval = intervals[i];
      if( !interval.isEmptyInterval() )
      {
        double width = interval.getWidth();
        sum += width;
      }
    }

    return sum;
  }

  public static Interval[] difference( Interval[] intervals, Interval[] diffIntervals )
  {
    /* Memory for the results. */
    /* There will be new intervals for each diff interval. */
    /* We start with the original, though. */
    Interval[] results = intervals;

    /* Make the difference with all given diff intervals. */
    for( int i = 0; i < diffIntervals.length; i++ )
    {
      /* Get the diff interval. */
      Interval diffInterval = diffIntervals[i];

      /* Make the difference with the last results and the diff interval. */
      /* The returned intervals will be the new results. */
      results = difference( results, diffInterval );
    }

    return results;
  }

  public static Interval[] difference( Interval[] intervals, Interval diffInterval )
  {
    /* Memory for the results. */
    List<Interval> results = new ArrayList<Interval>();

    /* Go through all intervals. */
    for( int i = 0; i < intervals.length; i++ )
    {
      /* Get the interval. */
      Interval interval = intervals[i];

      /* Make the difference with the diff interval. */
      Interval[] differences = interval.difference( diffInterval );
      for( int j = 0; j < differences.length; j++ )
      {
        /* Get the difference. */
        Interval difference = differences[j];
        if( !difference.isEmptyInterval() )
          results.add( difference );
      }
    }

    return results.toArray( new Interval[] {} );
  }
}