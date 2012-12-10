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
package org.kalypso.zml.core.base;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author Dirk Kuch
 */
public class MultipleTsLinkBuilder
{
  private final TimeserieFeatureProperty[] m_properties;

  private final IMultipleTsLinkBuilderSource m_delegate;

  public MultipleTsLinkBuilder( final IMultipleTsLinkBuilderSource delegate, final TimeserieFeatureProperty[] properties )
  {
    m_delegate = delegate;
    m_properties = properties;
  }

  public MultipleTsLink[] build( )
  {
    if( m_properties == null )
      return new MultipleTsLink[] {};

    final TSLinkWithName[] links = m_delegate.getLinks();
    final Map<String, MultipleTsLink> map = new LinkedHashMap<String, MultipleTsLink>();

    for( int index = 0; index < links.length; index++ )
    {
      final TSLinkWithName link = links[index];

      final String identifier = link.getIdentifier();
      MultipleTsLink multiple = map.get( identifier );
      if( multiple == null )
      {
        multiple = new MultipleTsLink( identifier );
        map.put( identifier, multiple );
      }

      multiple.add( new IndexedTsLink( link, index ) );

    }

    return map.values().toArray( new MultipleTsLink[] {} );
  }
}