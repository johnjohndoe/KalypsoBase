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
package org.kalypso.contribs.java.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Dirk Kuch
 */
public final class QueryUtilities
{
  private QueryUtilities( )
  {
  }

  public static Map<String, String> parse( final String query )
  {
    final Map<String, String> result = new HashMap<String, String>();
    if( query == null || query.isEmpty() )
      return result;

    final String[] parts;
    if( query.contains( "?" ) ) //$NON-NLS-1$
    {
      final String[] splitted = query.split( "\\?" ); //$NON-NLS-1$
      parts = splitted[1].split( "&" ); //$NON-NLS-1$
    }
    else
      parts = query.split( "&" ); //$NON-NLS-1$

    for( final String param : parts )
    {
      if( param.length() == 0 )
        continue;

      final String[] paramParts = param.split( "=", 2 ); //$NON-NLS-1$
      if( paramParts.length != 2 )
        throw new IllegalArgumentException( "URL contains incorect query part: " + param );

      final String key = paramParts[0];
      final String value = paramParts[1];
      result.put( key, value );
    }

    return result;
  }

  public static String addParameter( String query, Map<String, String> parameter )
  {
    List<String> allParameter = new ArrayList<String>();
    for( Entry<String, String> entry : parameter.entrySet() )
    {
      String oneParameter = String.format( "%s=%s", entry.getValue(), entry.getKey() );
      allParameter.add( oneParameter );
    }

    String joinedParameter = StringUtils.join( allParameter.toArray( new String[] {} ), "&" );

    if( query.contains( "?" ) )
      return String.format( "%s&%s", query, joinedParameter );

    return String.format( "%s?%s", query, joinedParameter );
  }
}
