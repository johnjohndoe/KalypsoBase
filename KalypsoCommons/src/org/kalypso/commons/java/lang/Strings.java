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

import jregex.Pattern;
import jregex.RETokenizer;

import org.apache.commons.lang.StringUtils;

/**
 * @author Dirk Kuch
 */
public final class Strings
{
  private Strings( )
  {
  }

  public static boolean isEmpty( final String string )
  {
    if( StringUtils.isEmpty( string ) )
      return true;

    return StringUtils.isEmpty( string.trim() );
  }

  public static boolean isNotEmpty( final String string )
  {
    return !isEmpty( string );
  }

  /**
   * @deprecated removes last letter of an string literal (see perl, ruby, etc.)
   */
  @Deprecated
  public static String chomp( final String s )
  {
    return StringUtils.chomp( s );
  }

  /**
   * @deprecated removes all white spaces at beginning and end of an string (see perl, ruby, etc)
   */
  @Deprecated
  public static String chop( final String s )
  {
    return StringUtils.chop( s );
  }

  @Deprecated
  public static boolean isEqual( final String s1, final String s2 )
  {
    return StringUtils.equals( s1, s2 );
  }

  @Deprecated
  public static boolean isEqualIgnoreCase( final String s1, final String s2 )
  {
    return StringUtils.equalsIgnoreCase( s1, s2 );
  }

  public static String tokenize( final String string, final Pattern... patterns )
  {
    String working = string;
    for( final Pattern pattern : patterns )
    {
      final RETokenizer tokenizer = new RETokenizer( pattern, working );
      if( tokenizer.hasMore() )
        working = tokenizer.nextToken();
    }

    if( working.equals( string ) )
      return null;

    return working;
  }
}
