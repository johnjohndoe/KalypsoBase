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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.commons.java.util.StringUtilities;
import org.kalypso.repository.IRepository;
import org.kalypso.repository.IRepositoryItem;
import org.kalypso.repository.RepositoryException;

/**
 * @author Dirk Kuch
 */
public final class RepositoryItemUtils
{
  private static final int PARAMETER_START_BORDER = 4;

  public static final String ZRXP_ITEM_IDENTIFIER = "Zrxp_";

  public static final int ZRXP_PRIORITY_ITEM_OFFSET = 10;

  private RepositoryItemUtils( )
  {
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
   * @return parameter from identifier
   */
  public static String getParameterType( final String identifier )
  {
    final String[] parts = identifier.split( "\\." );
    if( parts.length < PARAMETER_START_BORDER )
      return null;

    final StringBuffer parameter = new StringBuffer();
    for( int i = PARAMETER_START_BORDER - 1; i < parts.length; i++ )
    {
      parameter.append( parts[i] + "." );
    }

    return StringUtilities.chomp( parameter.toString() );
  }

  /**
   * @param qualified
   *          means:
   * 
   *          <pre>
   * some zml-proxy://[1].[2].[3].[4] will be qualified / interpreted as followed
   *             ------- 1 ------ --- 2 ----- - 3 -- - 4 -
   * zml-proxy://HVZ_Modelle_Elbe.Elbe_Prio_1.560051.W
   * will be classified as
   * zml-proxy://[HVZ_Modelle_Elbe].[Elbe_Prio_1].[560051].[W]
   * and the parent is
   * zml-proxy://[HVZ_Modelle_Elbe].[Elbe_Prio_1].[560051]
   *             ------- 1 ------ --- 2 ----- - 3 -- --- 4 ---
   * zml-proxy://HVZ_Modelle_Elbe.Elbe_Prio_1.560051.W.Prognose
   * will be classified as
   * zml-proxy://[HVZ_Modelle_Elbe].[Elbe_Prio_1].[560051].[W.Prognose]
   * and the parent is
   * zml-proxy://[HVZ_Modelle_Elbe].[Elbe_Prio_1].[560051]
   * </pre>
   */
  public static String getParentItemId( final String identifier, final int qualified )
  {
    final String[] parts = getQualifiedItemParts( identifier, qualified );
    if( parts.length == 1 )
      return RepositoryUtils.getRepositoryId( identifier );

    String parent = "";
    for( int i = 0; i < parts.length - 1; i++ )
    {
      parent += parts[i] + ".";
    }

    return StringUtilities.chomp( parent );
  }

  public static String getParentItemId( final String identifier )
  {
    final String[] parts = identifier.split( "\\." );
    if( parts.length == 1 )
      return RepositoryUtils.getRepositoryId( identifier );

    String parent = "";
    for( int i = 0; i < parts.length - 1; i++ )
    {
      parent += parts[i] + ".";
    }

    return StringUtilities.chomp( parent );
  }

  /**
   * @return "plain" item id without "protocol" (the original source, like zml-proxy://, datastore://)
   */
  public static String getPlainId( final String identifier )
  {
    final String[] parts = identifier.split( "://" );

    /**
     * sometime an identifier looks like:<br/>
     * <br/>
     * filter://org.kalypso.ogc.sensor.filter.filters.interval.IntervalFilter?source_0=
     * datastore://HVZ_Modelle_Saale.Saale_Prio_1.41524.N
     */
    if( parts.length > 2 )
    {
      final StringBuffer plain = new StringBuffer();

      for( int i = 0; i < parts.length; i++ )
      {
        if( i == parts.length - 1 )
          plain.append( parts[i] + "://" );
        else
          plain.append( parts[i] + "://" );
      }

      return plain.toString();
    }

    return parts[parts.length - 1];
  }

  /**
   * @return "protocol" of the given item id (like 'zml-proxy', 'datastore')
   */
  public static String getProtocol( final String identifier )
  {
    final String[] parts = identifier.split( ":" );
    return parts[0];
  }

  /**
   * see {@link RepositoryItemUtlis.resolveItemIdPart}
   */
  public static String[] getQualifiedItemParts( final String identifier, final int qualified )
  {
    final List<String> partsQualified = new ArrayList<String>();

    String concat = "";

    final String[] parts = identifier.split( "\\." );
    for( int i = 0; i < parts.length; i++ )
    {
      if( i < qualified - 1 )
      {
        partsQualified.add( parts[i] );
      }
      else
      {
        concat += parts[i] + ".";
      }
    }
    if( !concat.isEmpty() )
      partsQualified.add( StringUtilities.chomp( concat ) );

    return partsQualified.toArray( new String[] {} );
  }

  public static String getStationKennziffer( final IRepositoryItem item )
  {
    return getStationKennziffer( item.getIdentifier() );
  }

  public static String getStationKennziffer( final String identifier )
  {
    final String[] parts = identifier.split( "\\." );
    if( parts.length >= 3 )
      return parts[2];

    return null;
  }

  public static String getModel( final IRepositoryItem item )
  {
    return getModel( item.getIdentifier() );
  }

  private static String getModel( final String identifier )
  {
    final String[] parts = getPlainId( identifier ).split( "\\." );
    if( !ArrayUtils.isEmpty( parts ) )
    {
      return parts[0];
    }

    return null;
  }

  public static boolean isEqualStationKennziffer( final String id1, final String id2 )
  {
    final String station1 = getStationKennziffer( id1 );
    final String station2 = getStationKennziffer( id2 );

    return StringUtilities.isEqualIgnoreCase( station1, station2 );
  }

  public static boolean isEqualType( final String id1, final String id2 )
  {
    final String type1 = getParameterType( id1 );
    final String type2 = getParameterType( id2 );

    return StringUtilities.isEqualIgnoreCase( type1, type2 );
  }

  /**
   * @return wiski://HVZ_Modelle_Elbe.Elbe_Prio_1 -> will return true
   */
  public static boolean isGroupItem( final IRepositoryItem item )
  {
    final String identifier = item.getIdentifier();
    final String[] parts = identifier.split( "\\." );
    if( parts.length == 2 )
      return true;

    return false;
  }

  /**
   * @return wiski://HVZ_Modelle_Elbe.Elbe_Prio_1 -> will return true
   */
  public static boolean isModelItem( final IRepositoryItem item )
  {
    final String identifier = item.getIdentifier();
    final String[] parts = identifier.split( "\\." );
    if( parts.length == 1 )
      return true;

    return false;
  }

  public static boolean isPlainId( final String identifier )
  {
    return !identifier.contains( "\\:" );
  }

  public static boolean isPrognose( final IRepositoryItem item )
  {
    final String identifier = item.getIdentifier();

    /**
     * the group has to be prognose, not the station value itselfs
     */
    final String[] parts = identifier.split( "\\." );
    if( parts.length > 2 )
    {
      return parts[1].toLowerCase().contains( "_prog_" );
    }

    return false;
  }

  public static boolean isRepositoryItem( final String identifier )
  {
    return identifier.contains( "://" );
  }

  public static boolean isVirtual( final String identifier )
  {
    return identifier.toLowerCase().contains( ".virtuell." ); //$NON-NLS-N$
  }

  public static boolean isZrxpItem( final IRepositoryItem item )
  {
    return isZrxpItem( item.getIdentifier() );
  }

  public static boolean isZrxpItem( final String identifier )
  {
    return identifier.contains( ZRXP_ITEM_IDENTIFIER );
  }

  /**
   * replace repository identifier in {@value itemIdentifier} with {@value repositoryIdentifier}
   */
  public static String replaceIdentifier( final String itemIdentifier, final String repositoryIdentifier )
  {
    String base = repositoryIdentifier;

    final String repository = RepositoryUtils.getRepositoryId( itemIdentifier );
    if( !base.endsWith( "://" ) ) //$NON-NLS-1$
      base = String.format( "%s://", base ); //$NON-NLS-1$

    return String.format( "%s%s", base, itemIdentifier.substring( repository.length() ) ); //$NON-NLS-1$
  }

  public static String resolveDestinationId( final IRepositoryItem baseItem, final IRepository destinationRepository )
  {
    return replaceIdentifier( baseItem.getIdentifier(), destinationRepository.getIdentifier() );
  }

  public static String resolveItemIdPart( final String identifier, final int qualified )
  {
    final String[] parts = getQualifiedItemParts( identifier, qualified );
    final String part = parts[parts.length - 1];

    return getPlainId( part );
  }

  public static String resolveItemIdPart( final String identifier )
  {
    final String[] parts = identifier.split( "\\." );
    final String part = parts[parts.length - 1];

    return getPlainId( part );
  }

  public static String resolveItemName( final IRepositoryItem item ) throws RepositoryException
  {
    if( item instanceof IRepository )
      return item.getIdentifier();

    String base = "";

    final IRepositoryItem parent = item.getParent();
    if( parent != null )
      base += resolveItemName( parent );

    if( base.endsWith( "/" ) || base.endsWith( "." ) )
      base += item.getName();
    else
      base += "." + item.getName();

    return base;
  }

  /**
   * @param parameterUrlParts
   *          assumption an identifier of a parameter consists of 'x' parts
   */
  public static boolean isParameterItem( final IRepositoryItem item, final int parameterUrlParts ) throws RepositoryException
  {
    if( isVirtual( item ) )
    {
      final String identifier = item.getIdentifier();
      final String[] parts = identifier.split( "\\." );

      return parts.length == parameterUrlParts;
    }

    if( !ArrayUtils.isEmpty( item.getChildren() ) )
      return false;

    final String identifier = item.getIdentifier();
    final String[] parts = identifier.split( "\\." );

    return parts.length == parameterUrlParts;
  }

  public static boolean isVirtual( final IRepositoryItem item )
  {
    return isVirtual( item.getIdentifier() );
  }

  public static boolean equals( final IRepositoryItem item1, final IRepositoryItem item2 )
  {
    return getPlainId( item1.getIdentifier() ).equalsIgnoreCase( getPlainId( item2.getIdentifier() ) );
  }

  public static int countParts( final String identifier )
  {
    final String[] parts = identifier.split( "\\." );

    return parts.length;
  }

}
