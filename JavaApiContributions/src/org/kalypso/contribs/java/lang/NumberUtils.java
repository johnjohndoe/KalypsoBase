/*--------------- Kalypso-Header --------------------------------------------------------------------

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

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.contribs.java.lang;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;

import org.eclipse.core.runtime.Assert;

/**
 * Utility class for Number parsing etc.
 * 
 * @author belger
 */
public final class NumberUtils
{
  private NumberUtils( )
  {
    // do not instantiate
  }

  public static final boolean isInteger( final String string )
  {
    final Integer integer = parseQuietInteger( string );
    if( integer == null )
      return false;

    return true;
  }

  public static final boolean isDouble( final String string )
  {
    return !Double.isNaN( parseQuietDouble( string ) );
  }

  /**
   * Parses a string as double.<br>
   * The decimal separator may be one of '.' or ','.
   */
  public static final double parseDouble( final String string ) throws NumberFormatException
  {
    return Double.parseDouble( string.replace( ',', '.' ) );
  }

  public static final float parseFloat( final String string )
  {
    return Float.parseFloat( string.replace( ',', '.' ) );
  }

  /**
   * Parses a string as {@link BigDecimal}.<br>
   * The decimal separator may be one of '.' or ','.
   */
  public static final BigDecimal parseBigDecimal( final String string ) throws NumberFormatException
  {
    return new BigDecimal( string.replace( ',', '.' ) );
  }

  /**
   * Similar to {@link #parseQuietInteger(String)} but returns an int.<br/>
   * If the value cannot be parse, the default value is returned.
   */
  public static final int parseQuietInt( final String value, final int errorValue )
  {
    final Integer integer = parseQuietInteger( value );
    if( integer == null )
      return errorValue;

    return integer.intValue();
  }

  /**
   * Tries to parse an integer, if fails returns null
   */
  public static final Integer parseQuietInteger( final String string )
  {
    try
    {
      return new Integer( Integer.parseInt( string ) );
    }
    catch( final NumberFormatException e )
    {
      return null;
    }
  }

  /**
   * Tries to parse a double, if fails, returns {@link java.lang.Double#NaN} <br>
   * The decimal separator may be one of '.' or ','.
   */
  public static final double parseQuietDouble( final String string )
  {
    try
    {
      return Double.parseDouble( string.replace( ',', '.' ) );
    }
    catch( final Exception e )
    {
      return Double.NaN;
    }
  }

  /**
   * Tries to parse a {@link BigDecimal}.<br>
   * The decimal separator may be one of '.' or ','.
   * 
   * @return A new BigDecimal parsed from the indicated string. <code>null</code>, if the string is not parseable.
   * @see BigDecimal
   */
  public static final BigDecimal parseQuietDecimal( final String string )
  {
    try
    {
      return new BigDecimal( string.replace( ',', '.' ) );
    }
    catch( final NumberFormatException e )
    {
      return null;
    }
  }

  /**
   * Tries to parse a {@link BigDecimal} from a part of a string and additionally sets the indicated scale.<br>
   * The decimal separator may be one of '.' or ','.
   * 
   * @param line
   *          The string from which to parse the decimal.
   * @param beginIndex
   *          the beginning index, inclusive.
   * @param endIndex
   *          the ending index, exclusive.
   * @param scale
   *          The scale to set on the parsed decimal. If rounding is necessary the {@link BigDecimal#ROUND_HALF_UP} method is used.
   * @return A new BigDecimal parsed from the indicated substring scaled to the givern scale. <code>null</code>, if the
   *         substring is not parseable as BigDecimal or if the given string is too short.
   * @throws IllegalArgumentException
   *           If <code>beginIndex</code> is not less than <code>endIndex</code>.
   * @see BigDecimal
   * @see BigDecimal#setScale(int, int)
   * @see String#substring(int, int)
   */
  public static final BigDecimal parseQuietDecimal( final String line, final int beginIndex, final int endIndex, final int scale )
  {
    Assert.isLegal( beginIndex < endIndex );

    if( line.length() < endIndex )
      return null;

    final String substring = line.substring( beginIndex, endIndex ).trim();

    try
    {
      return new BigDecimal( substring.replace( ',', '.' ) ).setScale( scale, BigDecimal.ROUND_HALF_UP );
    }
    catch( final NumberFormatException e )
    {
      return null;
    }
  }

  /**
   * TODO: What is the difference to: <code>Integer.parseInt()</code>?<br/>
   * Makes no sense: Simply use {@link Integer#valueOf(String)} instead.
   */
  @Deprecated
  public static int toInteger( final String value ) throws ParseException
  {
    final NumberFormat instance = NumberFormat.getInstance();
    final Number number = instance.parse( value );
    return number.intValue();
  }

  /**
   * Returns the next bigger {@link BigDecimal} with the same scale.
   * 
   * @see BigDecimal
   */
  public static BigDecimal increment( final BigDecimal decimal )
  {
    return decimal.add( decimal.ulp() );
  }

  /**
   * Returns the next smaller {@link BigDecimal} with the same scale.
   * 
   * @see BigDecimal
   */
  public static BigDecimal decrement( final BigDecimal decimal )
  {
    return decimal.subtract( decimal.ulp() );
  }

  /**
   * returns the rounded up to by sigFigs defined decimal positions double value from given double the setting of scale
   * or using of {@link BigDecimal#round(MathContext)} with different scaled numbers gives not exact solution.
   */
  public static double getRoundedToSignificant( final Double value, final int sigFigs )
  {
    final long longValue = value.longValue();
    BigDecimal bigDecimal = BigDecimal.valueOf( value );
    final BigDecimal bigDecimalLong = BigDecimal.valueOf( longValue );
    final BigDecimal bigDecimalFloat = bigDecimal.subtract( bigDecimalLong );
    bigDecimal = bigDecimalFloat.setScale( sigFigs, RoundingMode.HALF_UP ).add( bigDecimalLong );
    return bigDecimal.doubleValue();
  }
}