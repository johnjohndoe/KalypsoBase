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
package org.kalypso.util.themes;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoFeatureTypeStyle;
import org.kalypso.ogc.gml.IKalypsoStyle;
import org.kalypso.ogc.gml.IKalypsoTheme;

/**
 * This class provides functions for {@link org.kalypso.ogc.gml.IKalypsoTheme}s.
 * 
 * @author Holger Albert
 */
public class ThemeUtilities
{
  /**
   * This constant defines the theme property, used to configure the background color.
   */
  public static final String THEME_PROPERTY_BACKGROUND_COLOR = "background_color";

  /**
   * The constructor.
   */
  private ThemeUtilities( )
  {
  }

  public static Color checkBackgroundColor( final Display display, final String backgroundColorProperty )
  {
    final String[] backgroundColor = StringUtils.split( backgroundColorProperty, ";" );
    if( backgroundColor != null && backgroundColor.length == 3 )
    {
      final Integer r = NumberUtils.parseQuietInteger( backgroundColor[0] );
      final Integer g = NumberUtils.parseQuietInteger( backgroundColor[1] );
      final Integer b = NumberUtils.parseQuietInteger( backgroundColor[2] );
      if( r != null && g != null && b != null )
        return new Color( display, r.intValue(), g.intValue(), b.intValue() );
    }

    return null;
  }

  public static IKalypsoTheme findFirstVisible( final IKalypsoTheme[] themes )
  {
    for( final IKalypsoTheme theme : themes )
    {
      /* Return the first visible theme. */
      if( theme.isVisible() )
        return theme;

      /* If the current theme is not visible, check the next one. */
    }

    /* No visible theme was found. */
    return null;
  }

  /**
   * Finds the 0-based index of the style in the theme's style list.
   * 
   * @return -1, if the style does not belong to the given theme.
   */
  public static int indexOfStyle( final IKalypsoFeatureTheme theme, final IKalypsoFeatureTypeStyle style )
  {
    final IKalypsoStyle[] styles = theme.getStyles();
    for( int i = 0; i < styles.length; i++ )
    {
      if( styles[i] == style )
        return i;
    }

    return -1;
  }
}