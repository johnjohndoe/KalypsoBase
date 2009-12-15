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

import org.kalypso.repository.IRepository;
import org.kalypso.repository.IRepositoryItem;
import org.kalypso.repository.RepositoryException;

/**
 * @author Dirk Kuch
 */
public final class RepositoryItemUtlis
{
  private RepositoryItemUtlis( )
  {
    
  }

  public static boolean isModel( final IRepositoryItem item )
  {
    if( !item.getIdentifier().contains( "HVZ_Modelle_" ) )
      return false;

    return true;
  }

  /**
   * Return only the item-id part of the full id. If id is null, it returns null.
   * 
   * @return the id part of the item. This is pretty much straightforward since the convention (as defined in the
   *         IRepositoryItem interface) specifies that an Item's identifier should be build using the repository id +
   *         item specific id (follows URL specification): repository-id://item-id-part
   */
  public static String getItemId( final String fullId )
  {
    if( fullId == null )
      return null;

    final int ix = fullId.indexOf( "://" ); //$NON-NLS-1$
    if( ix == -1 )
      throw new IllegalArgumentException( "Identifier does not follow the URL-rule: " + fullId ); //$NON-NLS-1$

    return fullId.substring( ix + 3 );
  }

  public static String resolveDestinationId( final IRepositoryItem baseItem, final IRepository destinationRepository )
  {
    return replaceIdentifier( baseItem.getIdentifier(), destinationRepository.getIdentifier() );
  }

  /**
   * replace repository identifier in {@value itemIdentifier} with {@value repositoryIdentifier}
   */
  public static String replaceIdentifier( final String itemIdentifier, String repositoryIdentifier )
  {
    final String repository = RepositoryUtils.getRepositoryId( itemIdentifier );
    if( !repositoryIdentifier.endsWith( "://" ) ) //$NON-NLS-1$
      repositoryIdentifier = String.format( "%s://", repositoryIdentifier ); //$NON-NLS-1$

    return String.format( "%s%s", repositoryIdentifier, itemIdentifier.substring( repository.length() ) ); //$NON-NLS-1$
  }

  public static String getParentItemId( final String identifier )
  {
    final int index = identifier.lastIndexOf( "." ); //$NON-NLS-1$
    if( index == -1 )
    {
      return RepositoryUtils.getRepositoryId( identifier );
    }

    return identifier.substring( 0, index );
  }

  public static String resolveItemIdPart( final String identifier ) throws RepositoryException
  {
    int index = identifier.lastIndexOf( "." ); //$NON-NLS-1$
    if( index == -1 )
    {
      index = identifier.indexOf( "://" ); //$NON-NLS-1$
      if( index == -1 )
        throw new RepositoryException( String.format( "Couldn't resolve item name from identifier: %s", identifier ) ); //$NON-NLS-1$

      return identifier.substring( index + 3 );
    }

    return identifier.substring( index + 1 );
  }

  public static String resolveItemName( final IRepositoryItem item ) throws RepositoryException
  {
    if( item instanceof IRepository )
      return item.getIdentifier();

    String base = "";

    IRepositoryItem parent = item.getParent();
    if( parent != null )
      base += resolveItemName( parent );

    if( base.endsWith( "/" ) || base.endsWith( "." ) )
      base += item.getName();
    else
      base += "." + item.getName();

    return base;
  }

  /**
   * @return "plain" item id without "protocol" (the original source, like zml-proxy://, datastore://)
   */
  public static String getPlainId( final String identifier )
  {
    String[] parts = identifier.split( "://" );

    return parts[parts.length - 1];
  }
}
