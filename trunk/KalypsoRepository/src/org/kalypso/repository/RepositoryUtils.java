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
package org.kalypso.repository;


/**
 * RepositoryUtils provides some utility methods in a static way
 * 
 * @author schlienger (24.05.2005)
 */
public class RepositoryUtils
{
  private RepositoryUtils( )
  {
    // no instanciation
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

  /**
   * looks for an equivalent item in an different repository
   */
  public static IRepositoryItem findEquivalentItem( final IRepositoryItem baseItem, final IRepository destinationRepository ) throws RepositoryException
  {
    final String id = resolveDestinationId( baseItem, destinationRepository );

    return destinationRepository.findItem( id );
  }

  /**
   * looks for an equivalent item in an different repository
   */
  public static IRepositoryItem findEquivalentItem( final String id, final IRepository repository ) throws RepositoryException
  {
    final String identifier = replaceIdentifier( id, repository.getIdentifier() );

    return repository.findItem( identifier );
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
    final String repository = getRepositoryId( itemIdentifier );
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

}
