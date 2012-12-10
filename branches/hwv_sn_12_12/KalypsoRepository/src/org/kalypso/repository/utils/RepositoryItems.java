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
package org.kalypso.repository.utils;

import org.kalypso.repository.IDataSourceItem;
import org.kalypso.repository.IRepository;
import org.kalypso.repository.IRepositoryItem;

/**
 * @author Dirk Kuch
 */
public final class RepositoryItems
{
  private RepositoryItems( )
  {
  }

  /**
   * @return "plain" item id without "protocol" (the original source, like zml-proxy://, datastore://)
   */
  public static String getPlainId( final String identifier )
  {
    final int indexOf = identifier.indexOf( "://" );
    if( indexOf == -1 )
      return identifier;

    final String protocol = identifier.substring( 0, indexOf + 3 );

    if( IDataSourceItem.FILTER_SOURCE.equals( protocol ) )
      return identifier;

    return identifier.substring( indexOf + 3 );
  }

  /**
   * @return "protocol" of the given item id (like 'zml-proxy', 'datastore')
   */
  public static String getProtocol( final String identifier )
  {
    final String[] parts = identifier.split( ":" ); //$NON-NLS-1$
    return parts[0];
  }

  public static boolean isPlainId( final String identifier )
  {
    if( identifier.startsWith( IDataSourceItem.FILTER_SOURCE ) )
      return true;

    return !identifier.contains( "://" ); //$NON-NLS-1$
  }

  public static boolean isRepositoryItem( final String identifier )
  {
    return identifier.contains( "://" ); //$NON-NLS-1$
  }

  /**
   * replace repository identifier in {@value itemIdentifier} with {@value repositoryIdentifier}
   */
  public static String replaceIdentifier( final String itemIdentifier, final String repositoryIdentifier )
  {
    String base = repositoryIdentifier;

    final String repository = Repositories.getRepositoryId( itemIdentifier );
    if( !base.endsWith( "://" ) ) //$NON-NLS-1$
      base = String.format( "%s://", base ); //$NON-NLS-1$

    return String.format( "%s%s", base, itemIdentifier.substring( repository.length() ) ); //$NON-NLS-1$
  }

  public static String resolveDestinationId( final IRepositoryItem baseItem, final IRepository destinationRepository )
  {
    return replaceIdentifier( baseItem.getIdentifier(), destinationRepository.getIdentifier() );
  }

  public static boolean equals( final IRepositoryItem item1, final IRepositoryItem item2 )
  {
    return getPlainId( item1.getIdentifier() ).equalsIgnoreCase( getPlainId( item2.getIdentifier() ) );
  }

  public static int countParts( final String identifier )
  {
    final String[] parts = identifier.split( "\\." ); //$NON-NLS-1$

    return parts.length;
  }

}
