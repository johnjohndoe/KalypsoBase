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
package org.kalypso.util.themes.text;

import java.util.Locale;
import java.util.Properties;

import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.ogc.gml.ThemeUtilities;
import org.kalypso.util.themes.position.PositionUtilities;

/**
 * This class provides functions for {@link org.kalypso.ogc.gml.IKalypsoTheme}s.
 * 
 * @author Holger Albert
 */
public class TextUtilities
{
  /**
   * This constant defines the theme property, used to configure the text, which should be shown.
   */
  public static final String THEME_PROPERTY_TEXT = "text"; //$NON-NLS-1$

  /**
   * This constant defines the theme property, used to configure the font size of the text.
   */
  public static final String THEME_PROPERTY_FONT_SIZE = "font_size"; //$NON-NLS-1$

  /**
   * This constant defines the theme property, used to configure the transparency of the background.
   */
  public static final String THEME_PROPERTY_TRANSPARENCY = "transparency"; //$NON-NLS-1$

  /**
   * The constructor.
   */
  private TextUtilities( )
  {
  }

  public static String checkText( final String textProperty )
  {
    // TODO Perhaps we want to validate the text?
    return textProperty;
  }

  public static int checkFontSize( final String fontSizeProperty )
  {
    final Integer fontSize = NumberUtils.parseQuietInteger( fontSizeProperty );
    if( fontSize != null && fontSize.intValue() > 0 )
      return fontSize.intValue();

    return -1;
  }

  public static boolean checkTransparency( final String transparencyProperty )
  {
    if( transparencyProperty != null && transparencyProperty.length() > 0 )
      return Boolean.parseBoolean( transparencyProperty );

    return false;
  }

  /**
   * This function returns a properties object, containing all serialized default text properties.
   * 
   * @return A properties object, containing all serialized default text properties.
   */
  public static Properties getDefaultProperties( )
  { /* Create the properties object. */
    final Properties properties = new Properties();

    /* Serialize the properties. */
    final String horizontalProperty = String.format( Locale.PRC, "%d", PositionUtilities.RIGHT ); //$NON-NLS-1$
    final String verticalProperty = String.format( Locale.PRC, "%d", PositionUtilities.BOTTOM ); //$NON-NLS-1$
    final String backgroundColorProperty = String.format( Locale.PRC, "%d;%d;%d", 255, 255, 255 ); //$NON-NLS-1$
    final String textProperty = ""; //$NON-NLS-1$
    final String fontSizeProperty = "-1"; //$NON-NLS-1$
    final String transparencyProperty = "false"; //$NON-NLS-1$

    /* Add the properties. */
    properties.put( PositionUtilities.THEME_PROPERTY_HORIZONTAL_POSITION, horizontalProperty );
    properties.put( PositionUtilities.THEME_PROPERTY_VERTICAL_POSITION, verticalProperty );
    properties.put( ThemeUtilities.THEME_PROPERTY_BACKGROUND_COLOR, backgroundColorProperty );
    properties.put( THEME_PROPERTY_TEXT, textProperty );
    properties.put( THEME_PROPERTY_FONT_SIZE, fontSizeProperty );
    properties.put( THEME_PROPERTY_TRANSPARENCY, transparencyProperty );

    return properties;
  }
}