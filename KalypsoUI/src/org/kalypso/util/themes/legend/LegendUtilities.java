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
package org.kalypso.util.themes.legend;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.mapmodel.IKalypsoThemeVisitor;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.MatchingIdKalypsoThemePredicate;
import org.kalypso.ogc.gml.mapmodel.visitor.KalypsoThemeVisitor;
import org.kalypso.util.themes.ThemeUtilities;
import org.kalypso.util.themes.position.PositionUtilities;

/**
 * This class provides functions for {@link org.kalypso.ogc.gml.IKalypsoTheme}s.
 * 
 * @author Holger Albert
 */
public class LegendUtilities
{
  /**
   * This constant defines the theme property, used to configure the insets of the legend.
   */
  public static final String THEME_PROPERTY_INSETS = "insets";

  /**
   * This constant defines the theme property, used to configure the ids of the themes, which should be included in the
   * legend.
   */
  public static final String THEME_PROPERTY_THEME_IDS = "theme_ids";

  /**
   * This constant defines the theme property, used to configure the font size of the legend.
   */
  public static final String THEME_PROPERTY_FONT_SIZE = "font_size";

  /**
   * The constructor.
   */
  private LegendUtilities( )
  {
  }

  public static int checkInsets( final String insetsProperty )
  {
    final Integer insets = NumberUtils.parseQuietInteger( insetsProperty );
    if( insets != null && insets.intValue() > 0 )
      return insets.intValue();

    return -1;
  }

  /**
   * This function verifies the ids contained in the themeIdsProperty variable and returns a list of ids, still
   * contained in the map model.
   * 
   * @param mapModel
   *          The map model.
   * @param themeIdsProperty
   *          The theme ids as serialized property, separated by a ';'.
   * @return A list of verified theme ids.
   */
  public static List<String> verifyThemeIds( final IMapModell mapModel, final String themeIdsProperty )
  {
    if( themeIdsProperty != null )
    {
      final List<String> themes = new ArrayList<String>();
      final String[] themeIds = StringUtils.split( themeIdsProperty, ";" );
      for( final String themeId : themeIds )
      {
        final IKalypsoTheme theme = findThemeById( mapModel, themeId );
        if( theme != null )
          themes.add( theme.getId() );
      }

      return themes;
    }

    return null;
  }

  public static int checkFontSize( final String fontSizeProperty )
  {
    final Integer fontSize = NumberUtils.parseQuietInteger( fontSizeProperty );
    if( fontSize != null && fontSize.intValue() > 0 )
      return fontSize.intValue();

    return -1;
  }

  /**
   * This function returns a properties object, containing all serialized default legend properties.
   * 
   * @return A properties object, containing all serialized default legend properties.
   */
  public static Properties getDefaultProperties( )
  { /* Create the properties object. */
    final Properties properties = new Properties();

    /* Serialise the properties. */
    final String horizontalProperty = String.format( Locale.PRC, "%d", PositionUtilities.RIGHT );
    final String verticalProperty = String.format( Locale.PRC, "%d", PositionUtilities.BOTTOM );
    final String backgroundColorProperty = String.format( Locale.PRC, "%d;%d;%d", 255, 255, 255 );
    final String insetsProperty = String.format( Locale.PRC, "%d", 10 );
    final String themeIdsProperty = "";
    final String fontSizeProperty = "-1";

    /* Add the properties. */
    properties.put( PositionUtilities.THEME_PROPERTY_HORIZONTAL_POSITION, horizontalProperty );
    properties.put( PositionUtilities.THEME_PROPERTY_VERTICAL_POSITION, verticalProperty );
    properties.put( ThemeUtilities.THEME_PROPERTY_BACKGROUND_COLOR, backgroundColorProperty );
    properties.put( THEME_PROPERTY_INSETS, insetsProperty );
    properties.put( THEME_PROPERTY_THEME_IDS, themeIdsProperty );
    properties.put( THEME_PROPERTY_FONT_SIZE, fontSizeProperty );

    return properties;
  }

  /**
   * This function returns the theme with the given id, if only one theme with the id exists in the map model.
   * 
   * @param mapModel
   *          The map model.
   * @param id
   *          The id to search for.
   * @return The theme, if ONE theme with the given id exists in the map model.
   */
  public static IKalypsoTheme findThemeById( final IMapModell mapModel, final String id )
  {
    /* Create the visitor. */
    final KalypsoThemeVisitor visitor = new KalypsoThemeVisitor( new MatchingIdKalypsoThemePredicate( id ) );

    /* Search all themes. */
    mapModel.accept( visitor, IKalypsoThemeVisitor.DEPTH_INFINITE );

    /* The found themes. */
    final IKalypsoTheme[] foundThemes = visitor.getFoundThemes();
    if( foundThemes.length != 1 )
      return null;

    return foundThemes[0];
  }
}