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
package org.kalypso.model.wspm.ui.view.table.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.kalypso.model.wspm.core.gml.classifications.ICodeClass;
import org.kalypso.model.wspm.core.gml.classifications.IRoughnessClass;
import org.kalypso.model.wspm.core.gml.classifications.IVegetationClass;

/**
 * @author Holger Albert
 */
public class FavoritesUtilities
{
  private FavoritesUtilities( )
  {
  }

  public static void updateDialogSettings( final IDialogSettings dialogSettings, final String name )
  {
    if( dialogSettings == null || StringUtils.isEmpty( name ) )
      return;

    if( dialogSettings.get( name ) == null )
    {
      dialogSettings.put( name, 1 );
      return;
    }

    final int amount = dialogSettings.getInt( name );
    dialogSettings.put( name, amount + 1 );
  }

  public static FavoriteItem[] findMostUsedItems( final String[] names, final IDialogSettings dialogSettings )
  {
    if( dialogSettings == null )
      return new FavoriteItem[] {};

    final List<FavoriteItem> allItems = new ArrayList<>();
    for( final String name : names )
    {
      if( dialogSettings.get( name ) == null )
        continue;

      final int amount = dialogSettings.getInt( name );
      allItems.add( new FavoriteItem( name, amount ) );
    }

    Collections.sort( allItems );

    final List<FavoriteItem> mostUsedItems = new ArrayList<>();
    for( final FavoriteItem item : allItems )
    {
      mostUsedItems.add( item );
      if( mostUsedItems.size() == 5 )
        break;
    }

    return mostUsedItems.toArray( new FavoriteItem[] {} );
  }

  public static String[] getNames( final IRoughnessClass[] roughnessClasses )
  {
    final List<String> names = new ArrayList<>();

    for( final IRoughnessClass roughnessClass : roughnessClasses )
      names.add( roughnessClass.getName() );

    return names.toArray( new String[] {} );
  }

  public static String[] getNames( final IVegetationClass[] vegetationClasses )
  {
    final List<String> names = new ArrayList<>();

    for( final IVegetationClass vegetationClass : vegetationClasses )
      names.add( vegetationClass.getName() );

    return names.toArray( new String[] {} );
  }

  public static String[] getNames( final ICodeClass[] codeClasses )
  {
    final List<String> names = new ArrayList<>();

    for( final ICodeClass codeClass : codeClasses )
      names.add( codeClass.getName() );

    return names.toArray( new String[] {} );
  }
}