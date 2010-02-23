/*
 * --------------- Kalypso-Header --------------------------------------------
 * 
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 * 
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 * 
 * and
 * 
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Contact:
 * 
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 * 
 * ------------------------------------------------------------------------------------
 */
package org.kalypso.repository.utils;

import org.kalypso.repository.IRepository;
import org.kalypso.repository.IRepositoryItem;
import org.kalypso.repository.IRepositoryRegistry;
import org.kalypso.repository.KalypsoRepository;
import org.kalypso.repository.RepositoryException;




/**
 * RepositoryUtils provides some utility methods in a static way
 * 
 * @author schlienger (24.05.2005)
 */
public final class RepositoryUtils
{
  private RepositoryUtils( )
  {
    // utility class - no instance of class is allowed
  }

  /**
   * Return the repository-id:// part of the itemId. If itemId is null, this method returns null.
   * 
   * @return the id of the repository the given item belongs to. This is pretty much straightforward since the
   *         convention (as defined in the IRepositoryItem interface) specifies that an Item's identifier should be
   *         build using the repository id + item specific id (follows URL specification): repository-id://item-id-part
   */
  public static String getRepositoryId( final String fullId )
  {
    if( fullId == null )
      return null;

    final int ix = fullId.indexOf( "://" ); //$NON-NLS-1$
    if( ix == -1 )
      throw new IllegalArgumentException( "Identifier does not follow the URL-rule: " + fullId ); //$NON-NLS-1$

    return fullId.substring( 0, ix + 3 );
  }

  /**
   * looks for an equivalent item in an different repository
   */
  public static IRepositoryItem findEquivalentItem( final IRepository destinationRepository, final IRepositoryItem baseItem ) throws RepositoryException
  {
    final String id = RepositoryItemUtlis.resolveDestinationId( baseItem, destinationRepository );

    return destinationRepository.findItem( id );
  }

  /**
   * looks for an equivalent item in an different repository
   */
  public static IRepositoryItem findEquivalentItem( final IRepository repository, final String id ) throws RepositoryException
  {
    final String identifier = RepositoryItemUtlis.replaceIdentifier( id, repository.getIdentifier() );

    return repository.findItem( identifier );
  }

  public static boolean continueSearch( final String baseIdendifier, final String lookingFor )
  {
    final String[] baseParts = baseIdendifier.split( "\\." );
    final String[] lookingForParts = lookingFor.split( "\\." );

    int count = lookingForParts.length;
    if( baseParts.length < lookingForParts.length )
      count = baseParts.length;
    else if( baseParts.length > lookingForParts.length )
      return false;

    for( int i = 0; i < count; i++ )
    {

        if( !baseParts[i].equals( lookingForParts[i] ) )
          return false;
    }

    return true;
  }

  public static IRepository findRegisteredRepository( final String itemIdentifier )
  {
    final String protocol = RepositoryItemUtlis.getProtocol( itemIdentifier );
    final IRepositoryRegistry repositoryRegistry = KalypsoRepository.getDefault().getRepositoryRegistry();

    return repositoryRegistry.getRepository( protocol );
  }

}
