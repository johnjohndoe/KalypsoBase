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
package org.kalypso.ogc.gml;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.contribs.java.lang.NumberUtils;

/**
 * This class provides functions for {@link org.kalypso.ogc.gml.IKalypsoTheme}s.
 *
 * @author Holger Albert
 */
public final class ThemeUtilities
{
  // FIXME: move the constant ot the other constants in IKalypsoTheme
  /**
   * This constant defines the theme property, used to configure the background color.
   */
  public static final String THEME_PROPERTY_BACKGROUND_COLOR = "background_color"; //$NON-NLS-1$

  private ThemeUtilities( )
  {
    throw new UnsupportedOperationException();
  }

  public static RGB checkBackgroundColor( final String backgroundColorProperty )
  {
    final String[] backgroundColor = StringUtils.split( backgroundColorProperty, ";" ); //$NON-NLS-1$
    if( backgroundColor != null && backgroundColor.length == 3 )
    {
      final Integer r = NumberUtils.parseQuietInteger( backgroundColor[0] );
      final Integer g = NumberUtils.parseQuietInteger( backgroundColor[1] );
      final Integer b = NumberUtils.parseQuietInteger( backgroundColor[2] );
      if( r != null && g != null && b != null )
        return new RGB( r.intValue(), g.intValue(), b.intValue() );
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

  /**
   * Returns the value of the theme property {@link IKalypsoTheme#PROPERTY_DELETEABLE}.
   */
  public static boolean isDeletable( final IKalypsoTheme theme )
  {
    final String deleteableStr = theme.getProperty( IKalypsoTheme.PROPERTY_DELETEABLE, Boolean.toString( false ) );
    return Boolean.parseBoolean( deleteableStr );
  }
}