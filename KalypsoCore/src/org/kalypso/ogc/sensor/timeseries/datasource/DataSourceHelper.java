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
package org.kalypso.ogc.sensor.timeseries.datasource;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dirk Kuch
 */
public class DataSourceHelper
{

  /**
   * @param reference
   *          filter://smeFilterClass?source_1=blub&source_2=blub2&...
   * @return [blub, blub2]
   */
  public static String[] getSources( final String reference )
  {
    final String[] referenceParts = reference.split( "\\?" );
    if( referenceParts.length != 2 )
      return new String[] {};

    final List<String> sources = new ArrayList<String>();

    final String[] parts = referenceParts[1].split( "\\&" );
    for( final String part : parts )
    {
      final String[] sourceParts = part.split( "\\=" );
      if( sourceParts.length == 1 )
        sources.add( IDataSourceItem.SOURCE_UNKNOWN );
      else
        sources.add( sourceParts[1] );
    }

    return sources.toArray( new String[] {} );
  }
}
