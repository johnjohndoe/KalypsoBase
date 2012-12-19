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

  public static double getSumOfIntervals( final Interval[] intervals )
  {
    double sum = 0.0;
    for( final Interval interval2 : intervals )
    {
      final Interval interval = interval2;
      if( !interval.isEmptyInterval() )
      {
        final double width = interval.getWidth();
        sum += width;
      }
    }

    return sum;
  }

  public static Interval[] difference( final Interval[] intervals, final Interval[] diffIntervals )
  {
    /* Memory for the results. */
    /* There will be new intervals for each diff interval. */
    /* We start with the original, though. */
    Interval[] results = intervals;

    /* Make the difference with all given diff intervals. */
    for( final Interval diffInterval : diffIntervals )
    {
      /* Make the difference with the last results and the diff interval. */
      /* The returned intervals will be the new results. */
      results = difference( results, diffInterval );
    }

    return results;
  }

  public static Interval[] difference( final Interval[] intervals, final Interval diffInterval )
  {
    /* Memory for the results. */
    final List<Interval> results = new ArrayList<>();

    /* Go through all intervals. */
    for( final Interval interval : intervals )
    {
      /* Make the difference with the diff interval. */
      final Interval[] differences = interval.difference( diffInterval );
      for( final Interval difference2 : differences )
      {
        /* Get the difference. */
        final Interval difference = difference2;
        if( !difference.isEmptyInterval() )
          results.add( difference );
      }
    }

    return results.toArray( new Interval[] {} );
  }
}