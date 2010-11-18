/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypsodeegree_impl.io.sax.parser;

import org.apache.commons.lang.StringUtils;
import org.kalypso.contribs.org.xml.sax.AttributesUtilities;
import org.xml.sax.Attributes;

/**
 * Common helper code for gml sax parsing.
 * 
 * @author Gernot Belger
 * @author Felipe Maximino
 */
public final class ContentHandlerUtils
{
  private ContentHandlerUtils( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" );
  }

  private static String parseStringFromAttributes( final Attributes attributes, final String attribute, final String defaultValue )
  {
    final String value = AttributesUtilities.getAttributeValue( attributes, "", attribute, null );
    if( value == null )
      return defaultValue;

    return value;
  }

  /** Get srs from attributes, fallback to default crs */
  public static String parseSrsFromAttributes( final Attributes attributes, final String defaultSrs )
  {
    return parseStringFromAttributes( attributes, "srsName", defaultSrs );
  }

  public static String parseCsFromAttributes( final Attributes attributes, final String defaultCs )
  {
    return parseStringFromAttributes( attributes, "cs", defaultCs );
  }

  public static String parseTsFromAttributes( final Attributes attributes, final String defaultTs )
  {
    return parseStringFromAttributes( attributes, "ts", defaultTs );
  }

  public static String parseDecimalFromAttributes( final Attributes attributes, final String defaultDecimal )
  {
    return parseStringFromAttributes( attributes, "decimal", defaultDecimal );
  }

  public static Integer parseSrsDimensionFromAttributes( final Attributes attributes )
  {
    return AttributesUtilities.getAttributeIntegerValue( attributes, "", "srsDimension", null );
  }

  public static Integer parseCountFromAttributes( final Attributes attributes )
  {
    return AttributesUtilities.getAttributeIntegerValue( attributes, "", "count", null );
  }

  public static double[] parseDoublesString( final String text )
  {
    String[] split = StringUtils.split( text );
    double[] result = new double[split.length];
    for( int i = 0; i < result.length; i++ )
      result[i] = Double.valueOf( split[i] );
    return result;
  }
}
