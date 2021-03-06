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
package org.kalypso.zml.core.base;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.core.runtime.Assert;

/**
 * @author Dirk Kuch
 */
public class MultipleTsLink implements IMultipleZmlSourceElement
{
  private final Set<IndexedTsLink> m_links = new LinkedHashSet<>();

  private final String m_identifier;

  public MultipleTsLink( final String identifier )
  {
    Assert.isNotNull( identifier );

    m_identifier = identifier;
  }

  @Override
  public String getIdentifier( )
  {
    return m_identifier;
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof MultipleTsLink )
    {
      final MultipleTsLink other = (MultipleTsLink) obj;

      final EqualsBuilder builder = new EqualsBuilder();
      builder.append( getIdentifier(), other.getIdentifier() );

      final TSLinkWithName[] links = getSources();
      final TSLinkWithName[] otherLinks = other.getSources();
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
    final Set<String> identifiers = new TreeSet<>();
    for( final TSLinkWithName link : links )
    {
      identifiers.add( link.getIdentifier() );
    }

    return identifiers.toArray( new String[] {} );
  }

  @Override
  public int hashCode( )
  {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append( MultipleTsLink.class.getName() );
    builder.append( getIdentifier() );

    final TSLinkWithName[] links = getSources();
    for( final TSLinkWithName link : links )
    {
      builder.append( link.getHref() );
    }

    return builder.toHashCode();
  }

  @Override
  public void add( final IZmlSourceElement link )
  {
    m_links.add( (IndexedTsLink) link );
  }

  @Override
  public IndexedTsLink[] getSources( )
  {
    return m_links.toArray( new IndexedTsLink[] {} );
  }

  public boolean isIgnoreType( final String[] currentIgnoreTypes )
  {
    if( currentIgnoreTypes == null )
      return false;

    final String identifier = getIdentifier();
    if( StringUtils.isEmpty( identifier ) )
      return false;

    final String type = getType();

    return ArrayUtils.contains( currentIgnoreTypes, type );
  }

  @Override
  public String getType( )
  {
    return ZmlSourceElements.getType( this );
  }

  @Override
  public String toString( )
  {
    return String.format( "multiple link(%d) - %s", m_links.size(), m_identifier ); //$NON-NLS-1$
  }
}
