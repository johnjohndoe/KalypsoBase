/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 *
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always.
 *
 * If you intend to use this software in other ways than in kalypso
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree,
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.tools;


/**
 * this is a collection of some methods that extends the functionallity of the sun-java string class.
 */
public class StringExtend
{
  /**
   * replaces occurences of a string fragment within a string by a new string.
   *
   * @param target
   *          is the original string
   * @param from
   *          is the string to be replaced
   * @param to
   *          is the string which will used to replace
   * @param all
   *          if it's true all occurences of the string to be replaced will be replaced. else only the first occurence
   *          will be replaced.
   */
  public static String replace( final String target, final String from, final String to, final boolean all )
  {
    int start = target.indexOf( from );

    if( start == -1 )
    {
      return target;
    }

    final int lf = from.length();
    final char[] targetChars = target.toCharArray();
    final StringBuffer buffer = new StringBuffer( target.length() );
    int copyFrom = 0;

    while( start != -1 )
    {
      buffer.append( targetChars, copyFrom, start - copyFrom );
      buffer.append( to );
      copyFrom = start + lf;
      start = target.indexOf( from, copyFrom );

      if( !all )
      {
        start = -1;
      }
    }

    buffer.append( targetChars, copyFrom, targetChars.length - copyFrom );

    return buffer.toString();
  }
}