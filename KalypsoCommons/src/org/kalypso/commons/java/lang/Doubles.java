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
package org.kalypso.commons.java.lang;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Dirk Kuch
 */
public final class Doubles
{
  private Doubles( )
  {
  }

  public static Double min( final Double... doubles )
  {
    Double min = Double.MAX_VALUE;
    for( final Double value : doubles )
    {
      if( Objects.isNull( value ) )
        continue;

      min = Math.min( min, value );
    }

    if( Double.MAX_VALUE == min )
      return null;

    return min;
  }

  public static Double max( final Double... doubles )
  {
    Double max = -Double.MAX_VALUE;
    for( final Double value : doubles )
    {
      if( Objects.isNull( value ) )
        continue;

      max = Math.max( max, value );
    }

    if( -Double.MAX_VALUE == max )
      return null;

    return max;
  }

  /**
   * @return first non null double value. Double.NaN is filtered too
   */
  public static Double firstNonNull( final Double... numbers )
  {
    for( final Double number : numbers )
    {
      if( Objects.isNull( number ) )
        continue;
      else if( Double.isNaN( number ) )
        continue;

      return number;
    }

    return null;
  }

  public static boolean isNullOrInfinite( final Number... numbers )
  {
    final Double[] doubles = new Double[numbers.length];
    for( int i = 0; i < doubles.length; i++ )
    {
      if( numbers[i] == null )
        doubles[i] = Double.NaN;
      else if( numbers[i] instanceof Double )
        doubles[i] = (Double)numbers[i];
      else
        doubles[i] = numbers[i].doubleValue();
    }

    return isNullOrInfinite( doubles );
  }

  /**
   * Returns <code>true</code>, if one of the given doubles if <code>null</code>, NaN, or Infinite.
   */
  public static boolean isNullOrInfinite( final Double... numbers )
  {
    if( ArrayUtils.isEmpty( numbers ) )
      return true;

    for( final Double number : numbers )
    {
      if( Objects.isNull( number ) )
        return true;

      if( !com.google.common.primitives.Doubles.isFinite( number ) )
        return true;
    }

    return false;
  }

  // FIXME bad name!
  public static boolean isNaN( final Double... numbers )
  {
    if( ArrayUtils.isEmpty( numbers ) )
      return true;

    for( final Double number : numbers )
    {
      // TODO: bad! this not was the method name promises!
      if( Objects.isNull( number ) )
        return true;

      if( Double.isNaN( number ) )
        return true;
    }

    return false;
  }
}
