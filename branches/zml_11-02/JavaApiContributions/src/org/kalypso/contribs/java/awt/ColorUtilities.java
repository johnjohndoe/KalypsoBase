/** This file is part of Kalypso
 *
 *  Copyright (c) 2008 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.contribs.java.awt;

import java.awt.Color;

import org.kalypso.contribs.java.util.Arrays;

/**
 * Some useful method dealing with java.awt.Color. See also org.kalypso.contribs.java.util.StringUtilities for some
 * other methods using Colors and Strings.
 * 
 * @author schlienger
 */
public class ColorUtilities
{
  /**
   * creates a returns the complementary Color of org. Each component of the color is substracted to 255 and set as
   * component of the new color.
   * 
   * @param org
   * @return new Color
   */
  public static Color createComplementary( final Color org )
  {
    final Color cmp = new Color( 255 - org.getRed(), 255 - org.getGreen(), 255 - org.getBlue() );

    return cmp;
  }

  /**
   * Create a random color. Alpha defaults to 1.
   * 
   * @return a new Color which components have been given by Math.random().
   */
  public static Color random( )
  {
    return new Color( (float) Math.random(), (float) Math.random(), (float) Math.random() );
  }

  /**
   * Create a random color with the given alpha value
   * 
   * @return a new Color which components have been given by Math.random().
   */
  public static Color random( final float alpha )
  {
    return new Color( (float) Math.random(), (float) Math.random(), (float) Math.random(), alpha );
  }

  /**
   * Create a color similar to the given one, but distant of the given distance. The color components from the original
   * color are derived as many times as distance. Thus, if distance is 0, the original color is returned. The same alpha
   * value is kept between the original color and the newly created one.
   * <p>
   * Note to developer: ameliorate the algorithm for finding derivate color
   */
  public static Color derivateColor( final Color c, final int distance )
  {
    if( distance == 0 )
      return c;

    // get color components
    final int a = c.getAlpha();
    final int[] rgb = { c.getRed(), c.getGreen(), c.getBlue() };

    // test if components are all 0 or all 255
    final int[] range = { 0, 255 };
    for( final int element : range )
    {
      if( rgb[0] == element && rgb[1] == element && rgb[2] == element )
      {
        rgb[0] = (int) (Math.random() * 255);
        rgb[1] = (int) (Math.random() * 255);
        rgb[2] = (int) (Math.random() * 255);
      }
    }

    final int pos = Arrays.indexOfMin( rgb );

    // derivate color
    for( int i = 0; i < distance; i++ )
    {
      rgb[pos] += 51;

      if( rgb[pos] > 255 )
        rgb[pos] = rgb[pos] - 255;
    }

    return new Color( rgb[0], rgb[1], rgb[2], a );
  }

  /**
   * Creates a color from the given color with the indicated alpha value.
   * 
   * @param color
   * @param alpha
   *          0 - 255
   * @return transparent color
   */
  public static Color createTransparent( final Color color, final int alpha )
  {
    return new Color( color.getRed(), color.getGreen(), color.getBlue(), alpha );
  }

  /**
   * Creates a color from the given color with the indicated opacity.
   * 
   * @param opacity
   *          0.0 - 1.0: Corresponds to alpha value from 0 to 255.
   */
  public static Color createTransparent( final Color color, final double opacity )
  {
    final int alpha = (int) Math.round( opacity * 255.0 );
    final int red = color.getRed();
    final int green = color.getGreen();
    final int blue = color.getBlue();
    return new Color( red, green, blue, alpha );
  }

  /**
   * Creates a new color with the given opacity.<br>
   * If the color already has an alpha value, the alpha is multiplied by the igven opacity.
   * 
   * @param opacity
   *          0.0 - 1.0: Corresponds to alpha value from 0 to 255.
   */
  public static Color applyOpacity( final Color color, final double opacity )
  {
    final double currentAlpha = color.getAlpha() / 255.0;
    final double changedAlpha = currentAlpha * opacity;

    final int alpha = (int) (changedAlpha * 255.0);
    final int red = color.getRed();
    final int green = color.getGreen();
    final int blue = color.getBlue();
    return new Color( red, green, blue, alpha );
  }

  public static Color interpolateLinear( final Color color1, final Color color2, final double factor )
  {
    final float[] hsba1 = new float[4];
    final float[] hsba2 = new float[4];

    Color.RGBtoHSB( color1.getRed(), color1.getGreen(), color1.getBlue(), hsba1 );
    hsba1[3] = color1.getAlpha();

    Color.RGBtoHSB( color2.getRed(), color2.getGreen(), color2.getBlue(), hsba2 );
    hsba2[3] = color2.getAlpha();

    final double[] hsba = new double[4];
    for( int i = 0; i < hsba.length; i++ )
      hsba[i] = hsba1[i] + (hsba2[i] - hsba1[i]) * factor;

    final Color hsbColor = Color.getHSBColor( (float) hsba[0], (float) hsba[1], (float) hsba[2] );
    return new Color( hsbColor.getRed(), hsbColor.getGreen(), hsbColor.getBlue(), (int) hsba[3] );
  }

  public static Color decodeWithAlpha( final String color )
  {
    if( color.length() > 8 )
    {
      final Long l = Long.decode( color );
      final int r = (int) ((l >> 24) & 0xFF);
      final int g = (int) ((l >> 16) & 0xFF);
      final int b = (int) ((l >> 8) & 0xFF);
      final int a = (int) (l & 0xFF);

      return new Color( r, g, b, a );
    }

    return Color.decode( color );
  }
}
