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
package org.kalypso.commons.java.util;

import java.awt.Color;
import java.awt.Font;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.kalypso.commons.internal.i18n.Messages;

/**
 * Utilities around the String class.
 *
 * @author schlienger
 */
public final class StringUtilities
{
  /** no alignment, as is (used in spanOverLines) */
  public final static int ALIGNMENT_NONE = 0;

  /** left alignment (used in spanOverLines) */
  public final static int ALIGNMENT_LEFT = 1;

  /** right alignment (used in spanOverLines) */
  public final static int ALIGNMENT_RIGHT = 2;

  /**
   * Not intended to be instanciated
   */
  private StringUtilities( )
  {
    // empty
  }

  /**
   * Converts a String into a Color.
   * <p>
   * String has to have the following format: "R;G;B[;A]"
   * <p>
   * with R, G, B being the Red, Green, Blue components of the color and expressed as integers in the range (0 - 255).
   * with A optional, being the alpha composite value in the range (0 - 255).
   *
   * @param s
   * @throws IllegalArgumentException
   *             if s is null
   */
  public static Color stringToColor( final String s ) throws IllegalArgumentException
  {
    if( s == null )
      throw new IllegalArgumentException( Messages.getString("org.kalypso.commons.java.util.StringUtilities.0") ); //$NON-NLS-1$

    final String[] sc = s.split( ";" ); //$NON-NLS-1$

    if( sc.length == 3 )
      return new Color( Integer.parseInt( sc[0] ), Integer.parseInt( sc[1] ), Integer.parseInt( sc[2] ) );

    if( sc.length == 4 )
      return new Color( Integer.parseInt( sc[0] ), Integer.parseInt( sc[1] ), Integer.parseInt( sc[2] ), Integer.parseInt( sc[3] ) );

    throw new IllegalArgumentException( Messages.getString("org.kalypso.commons.java.util.StringUtilities.2") + s ); //$NON-NLS-1$
  }

  /**
   * Converts a Color into a String.
   * <p>
   * String will have same format as specified in {@link StringUtilities#stringToColor(String)}
   *
   * @param c
   * @throws IllegalArgumentException
   *             if color is null
   */
  public static String colorToString( final Color c )
  {
    if( c == null )
      throw new IllegalArgumentException( Messages.getString("org.kalypso.commons.java.util.StringUtilities.1") ); //$NON-NLS-1$

    final StringBuffer buf = new StringBuffer();

    buf.append( c.getRed() ).append( ";" ).append( c.getGreen() ).append( ";" ).append( c.getBlue() ); //$NON-NLS-1$ //$NON-NLS-2$

    // alpha component is optional
    if( c.getAlpha() != 255 )
      buf.append( ";" ).append( c.getAlpha() ); //$NON-NLS-1$

    return buf.toString();
  }

  /**
   * Converts a String to a Font.
   *
   * <pre>
   *
   *
   *
   *
   *
   *         FontName;FontStyle;FontSize
   *
   *
   *
   *
   *
   * </pre>
   *
   * @param s
   * @throws IllegalArgumentException
   *             if s is null
   */
  public static Font stringToFont( final String s )
  {
    if( s == null )
      throw new IllegalArgumentException( Messages.getString("org.kalypso.commons.java.util.StringUtilities.3") ); //$NON-NLS-1$

    final String[] sc = s.split( ";" ); //$NON-NLS-1$

    if( sc.length != 3 )
      throw new IllegalArgumentException( Messages.getString("org.kalypso.commons.java.util.StringUtilities.4") ); //$NON-NLS-1$

    final Font f = new Font( sc[0], Integer.parseInt( sc[1] ), Integer.parseInt( sc[2] ) );

    return f;
  }

  /**
   * Converts a font to a string. Format is defined in {@link StringUtilities#stringToFont(String)}
   *
   * @param f
   * @throws IllegalArgumentException
   *             if f is null
   */
  public static String fontToString( final Font f )
  {
    if( f == null )
      throw new IllegalArgumentException( Messages.getString("org.kalypso.commons.java.util.StringUtilities.5") ); //$NON-NLS-1$

    final StringBuffer buf = new StringBuffer();

    buf.append( f.getName() ).append( ";" ).append( f.getStyle() ).append( ";" ).append( f.getSize() ); //$NON-NLS-1$ //$NON-NLS-2$

    return buf.toString();
  }

  /**
   * Replacement per Pattern-Matching
   *
   * @param sourceValue
   * @param replaceProperties
   * @return string
   */
  public static String replaceAll( final String sourceValue, final Properties replaceProperties )
  {
    String newString = sourceValue;

    for( final Entry<Object, Object> entry : replaceProperties.entrySet() )
    {
      final String key = entry.getKey().toString();
      final String value = entry.getValue().toString();

      newString = newString.replaceAll( key, value );
    }

    return newString;
  }

  /**
   * Spans a string onto lines, thus it inserts NEWLINE chars at lineLength + 1 for each line. It firsts removes all
   * existing NEWLINE chars.
   *
   * @param str
   *            the string to span
   * @param lineLength
   *            the number of chars one line must have
   * @param keepWords
   *            when true words are not cut at the end of the line but rather postponed on the next line
   * @return newly spaned string or null if str is null
   */
  public static String spanOverLines( final String str, final int lineLength, final boolean keepWords, final int alignment )
  {
    if( str == null )
      return null;

    if( lineLength == 0 )
      return str;

    str.replaceAll( "\\n", "" ); //$NON-NLS-1$ //$NON-NLS-2$

    final StringBuffer bf = new StringBuffer();
    int i = 0;
    while( true )
    {
      if( i + lineLength > str.length() )
      {
        String line = str.substring( i, str.length() );
        if( alignment == StringUtilities.ALIGNMENT_LEFT )
          line = StringUtils.stripStart( line, null );
        if( alignment == StringUtilities.ALIGNMENT_RIGHT )
        {
          line = StringUtils.stripEnd( line, null );
          line = StringUtils.leftPad( line, lineLength );
        }
        bf.append( line );
        break;
      }

      int curLineLength = lineLength;
      if( keepWords && !Character.isWhitespace( str.charAt( i + lineLength - 2 ) ) && !Character.isWhitespace( str.charAt( i + lineLength - 1 ) )
          && !Character.isWhitespace( str.charAt( i + lineLength ) ) )
      {
        curLineLength = lineLength - 3;
        while( (curLineLength > 0) && !Character.isWhitespace( str.charAt( i + curLineLength ) ) )
          curLineLength--;

        if( curLineLength == 0 )
          curLineLength = lineLength;
        if( curLineLength != lineLength )
          curLineLength++;
      }

      String line = str.substring( i, i + curLineLength );
      if( alignment == StringUtilities.ALIGNMENT_LEFT )
        line = StringUtils.stripStart( line, null );
      if( alignment == StringUtilities.ALIGNMENT_RIGHT )
      {
        line = StringUtils.stripEnd( line, null );
        line = StringUtils.leftPad( line, lineLength );
      }

      bf.append( line ).append( System.getProperty( "line.separator" ) ); //$NON-NLS-1$

      i = i + curLineLength;
    }

    return bf.toString();
  }

  /**
   * removes last letter of an string literal (see perl, ruby, etc.)
   */
  public static String chomp( final String s )
  {
    return s.substring( 0, s.length() - 1 );
  }

  /**
   * removes all whitespaces at beginning and end of an string (see perl, ruby, etc)
   */
  public static String chop( final String s )
  {
    return s.trim();
  }

  public static boolean isEqual( final String s1, final String s2 )
  {
    if( s1 == null || s2 == null )
      return false;

    return s1.equals( s2 );
  }

  public static boolean isEqualIgnoreCase( final String s1, final String s2 )
  {
    if( s1 == null || s2 == null )
      return false;

    return s1.equalsIgnoreCase( s2 );
  }

  /**
   * Fast splitting of a string using apache commons.<br/>
   * Also maxes performance by checking if the separator is really only a char.
   */
  public static String[] splitString( final String value, final String separator )
  {
    /* For max performance, we prefer the character-split */
    if( separator.length() == 1 )
      return StringUtils.split( value, separator.charAt( 0 ) );

    return StringUtils.split( value, separator );
  }

  public static String replaceString( final String string, final String oldString, final String newString )
  {
    if( oldString.length() == 1 && newString.length() == 1 )
      return string.replace( oldString.charAt( 0 ), newString.charAt( 0 ) );

    return string.replace( oldString, newString );
  }

}
