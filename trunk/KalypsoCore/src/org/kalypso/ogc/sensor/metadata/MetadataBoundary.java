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
package org.kalypso.ogc.sensor.metadata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import jregex.Pattern;

import org.kalypso.contribs.java.lang.NumberUtils;

/**
 * @author Dirk Kuch
 */
public final class MetadataBoundary implements IMetadataBoundary
{
  private final String m_type;

  private final double m_value;

  /**
   * @param type
   *          type of the boundary (alarmstufe 3b, niedrigwasser, hochwasser, aso.)
   */
  public MetadataBoundary( final String type, final double value )
  {
    m_type = type;
    m_value = value;
  }

  @Override
  public String getType( )
  {
    return m_type;
  }

  @Override
  public double getValue( )
  {
    return m_value;
  }

  public static String[] findBoundaryKeys( final MetadataList metadata )
  {
    final Set<String> keys = new HashSet<String>();

    final Set<Entry<Object, Object>> entries = metadata.entrySet();
    for( final Entry<Object, Object> entry : entries )
    {
      final String key = entry.getKey().toString();

      if( key.startsWith( BOUNDARY_PREFIX ) )
        keys.add( key );
    }

    return keys.toArray( new String[] {} );
  }

  public static String[] findBoundaryKeys( final MetadataList metadata, final String type )
  {
    final Pattern pattern = new Pattern( BOUNDARY_PREFIX + type + ".*" );
    final List<String> found = new ArrayList<String>();

    final String[] keys = findBoundaryKeys( metadata );
    for( final String key : keys )
    {
      if( pattern.matches( key ) )
        found.add( key );
    }

    return found.toArray( new String[] {} );
  }

  /**
   * @param type
   *          w or q
   * @param boundaryType
   *          alarmstufe or meldegrenze
   */
  public static String[] findBoundaryKeys( final MetadataList metadata, final String type, final String boundaryType )
  {
    final List<String> found = new ArrayList<String>();

    final String[] keys = findBoundaryKeys( metadata, type );
    for( final String key : keys )
    {
      if( key.contains( boundaryType ) )
        found.add( key );
    }

    return found.toArray( new String[] {} );
  }

  public static MetadataBoundary[] getBoundaries( final MetadataList metadata, final String[] keys )
  {
    final List<MetadataBoundary> boundaries = new ArrayList<MetadataBoundary>();

    for( final String key : keys )
    {
      final Object property = metadata.get( key );
      if( property instanceof String )
      {
        final double value = NumberUtils.parseDouble( (String) property );
        boundaries.add( new MetadataBoundary( key, value ) );
      }
    }

    return boundaries.toArray( new MetadataBoundary[] {} );
  }

}
