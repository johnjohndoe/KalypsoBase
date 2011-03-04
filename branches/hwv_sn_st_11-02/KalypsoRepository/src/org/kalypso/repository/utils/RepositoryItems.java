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

import jregex.Pattern;
import jregex.RETokenizer;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.kalypso.repository.IRepository;
import org.kalypso.repository.IRepositoryItem;

/**
 * @author Dirk Kuch
 */
public final class RepositoryItems
{
  private static final int PARAMETER_START_BORDER = 4;

  public static final String ZRXP_ITEM_IDENTIFIER = "Zrxp_"; //$NON-NLS-1$

  private static final Pattern ZRXP_ITEM_PATTERN = new Pattern( "^[\\w\\d_]+\\.Zrxp_.*" ); //$NON-NLS-1$

  public static final int ZRXP_PRIORITY_ITEM_OFFSET = 10;

  private RepositoryItems( )
  {
  }

  /**
   * @return parameter from identifier
   */
  public static String getParameterType( final String identifier )
  {
    final String[] parts = identifier.split( "\\." ); //$NON-NLS-1$
    if( parts.length < PARAMETER_START_BORDER )
      return null;

// final StringBuffer parameter = new StringBuffer();
// for( int i = PARAMETER_START_BORDER - 1; i < parts.length; i++ )
// {
// parameter.append( parts[i] + "." );
// }
//
// return StringUtilities.chomp( parameter.toString() );

    // becaus q.prognose is type of q!
    return parts[PARAMETER_START_BORDER - 1];
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

    String parent = ""; //$NON-NLS-1$
    for( int i = 0; i < parts.length - 1; i++ )
    {
      parent += parts[i] + "."; //$NON-NLS-1$
    }

    return StringUtils.chop( parent );
  }

  private static final Pattern PATTERN_PARENT_ITEM_TOKINZER = new Pattern( "\\.[\\w\\d_]*$" ); //$NON-NLS-1$

  public static String getParentItemId( final String identifier )
  {
    final RETokenizer tokenizer = new RETokenizer( PATTERN_PARENT_ITEM_TOKINZER, identifier );

    return tokenizer.nextToken();
  }

  private static final Pattern PATTERN_PLAIN_ID_TOKENIZER = new Pattern( ".*\\://" ); //$NON-NLS-1$

  /**
   * @return "plain" item id without "protocol" (the original source, like zml-proxy://, datastore://)
   */
  public static String getPlainId( final String identifier )
  {
    if( isPlainId( identifier ) )
      return identifier;

    if( PATTERN_PLAIN_ID_TOKENIZER.matches( identifier ) )
      return ""; // "plain id" of an IRepository

    final RETokenizer tokenizer = new RETokenizer( PATTERN_PLAIN_ID_TOKENIZER, identifier );

    return tokenizer.nextToken();
  }

  /**
   * @return "protocol" of the given item id (like 'zml-proxy', 'datastore')
   */
  public static String getProtocol( final String identifier )
  {
    final String[] parts = identifier.split( ":" ); //$NON-NLS-1$
    return parts[0];
  }

  /**
   * see {@link RepositoryItemUtlis.resolveItemIdPart}
   */
  public static String[] getQualifiedItemParts( final String identifier, final int qualified )
  {
    final List<String> partsQualified = new ArrayList<String>();

    String concat = ""; //$NON-NLS-1$

    final String[] parts = identifier.split( "\\." ); //$NON-NLS-1$
    for( int i = 0; i < parts.length; i++ )
    {
      if( i < qualified - 1 )
      {
        partsQualified.add( parts[i] );
      }
      else
      {
        concat += parts[i] + "."; //$NON-NLS-1$
      }
    }
    if( !concat.isEmpty() )
      partsQualified.add( StringUtils.chop( concat ) );

    return partsQualified.toArray( new String[] {} );
  }

  public static String getStationKennziffer( final IRepositoryItem item )
  {
    return getStationKennziffer( item.getIdentifier() );
  }

  public static String getStationKennziffer( final String identifier )
  {
    final String[] parts = identifier.split( "\\." ); //$NON-NLS-1$
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
    final String[] parts = getPlainId( identifier ).split( "\\." ); //$NON-NLS-1$
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

    return StringUtils.equalsIgnoreCase( station1, station2 );
  }

  public static boolean isEqualType( final String id1, final String id2 )
  {
    final String type1 = getParameterType( id1 );
    final String type2 = getParameterType( id2 );

    return StringUtils.equalsIgnoreCase( type1, type2 );
  }

  public static boolean isStationItem( final String identifier )
  {
    final String[] parts = identifier.split( "\\." ); //$NON-NLS-1$

    return parts.length == 3;
  }

  /**
   * @return wiski://HVZ_Modelle_Elbe.Elbe_Prio_1 -> will return true
   */
  public static boolean isGroupItem( final IRepositoryItem item )
  {
    final String identifier = item.getIdentifier();
    final String[] parts = identifier.split( "\\." ); //$NON-NLS-1$

    return parts.length == 2;
  }

  /**
   * @return wiski://HVZ_Modelle_Elbe -> will return true
   */
  public static boolean isModelItem( final IRepositoryItem item )
  {
    final String identifier = item.getIdentifier();
    final String[] parts = identifier.split( "\\." ); //$NON-NLS-1$

    return parts.length == 1;
  }

  public static boolean isPlainId( final String identifier )
  {
    return !identifier.contains( "://" ); //$NON-NLS-1$
  }

  public static boolean isForecast( final IRepositoryItem item )
  {
    return isForecast( item.getIdentifier() );
  }

  private static final Pattern PATTERN_IS_FORECAST = new Pattern( "[\\w\\d_]+\\.[\\w\\d]+_Prog_[\\w\\d_]+(\\..*)?" ); //$NON-NLS-1$

  public static boolean isForecast( final String identifier )
  {
    /**
     * the group has to be forecast, not the station value itself's
     */
    // like: HVZ_Modelle_Bode.Bode_Prog_Neu_Pegel.579040
    final String plain = getPlainId( identifier );

    return PATTERN_IS_FORECAST.matches( plain );
  }

  public static boolean isRepositoryItem( final String identifier )
  {
    return identifier.contains( "://" ); //$NON-NLS-1$
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
    return ZRXP_ITEM_PATTERN.matches( getPlainId( identifier ) );
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
    final String[] parts = identifier.split( "\\." ); //$NON-NLS-1$
    final String part = parts[parts.length - 1];

    return getPlainId( part );
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
    final String[] parts = identifier.split( "\\." ); //$NON-NLS-1$

    return parts.length;
  }

}
