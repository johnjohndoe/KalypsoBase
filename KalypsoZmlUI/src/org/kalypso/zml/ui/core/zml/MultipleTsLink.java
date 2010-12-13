/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.zml.ui.core.zml;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Dirk Kuch
 */
public class MultipleTsLink
{
  Set<TSLinkWithName> m_links = new LinkedHashSet<TSLinkWithName>();

  private final String m_identifier;

  public MultipleTsLink( final String identifier )
  {
    m_identifier = identifier;
  }

  public String getIdentifier( )
  {
    return m_identifier;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof MultipleTsLink )
    {
      final MultipleTsLink other = (MultipleTsLink) obj;

      final EqualsBuilder builder = new EqualsBuilder();
      builder.append( getIdentifier(), other.getIdentifier() );

      final TSLinkWithName[] links = getLinks();
      final TSLinkWithName[] otherLinks = other.getLinks();
      if( links.length != otherLinks.length )
        return false;

      final String[] set = getSortedLinks( links );
      final String[] otherSet = getSortedLinks( otherLinks );

      for( int i = 0; i < links.length; i++ )
      {
        builder.append( set[i], otherSet[i] );
      }

      return builder.isEquals();
    }

    return super.equals( obj );
  }

  private String[] getSortedLinks( final TSLinkWithName[] links )
  {
    final Set<String> identifiers = new TreeSet<String>();
    for( final TSLinkWithName link : links )
    {
      identifiers.add( link.getIdentifier() );
    }

    return identifiers.toArray( new String[] {} );
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode( )
  {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append( MultipleTsLink.class.getName() );
    builder.append( getIdentifier() );

    return builder.toHashCode();
  }

  public void add( final TSLinkWithName link )
  {
    m_links.add( link );
  }

  public TSLinkWithName[] getLinks( )
  {
    return m_links.toArray( new TSLinkWithName[] {} );
  }
}
