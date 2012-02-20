/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.contribs.eclipse.swt.graphics;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

/**
 * Helper class for deriving fonts from other fonts.
 * 
 * @author Gernot Belger, Holger Albert
 */
public final class FontUtilities
{
  private FontUtilities( )
  {
  }

  /**
   * This function copies the font, setting a new height and adding the style constants. It sets the height to the new
   * font. It adjusts the style of the original font by the style, before setting to the new font.
   * 
   * @param device
   *          The device.
   * @param font
   *          The original font.
   * @param height
   *          The height of the new font.
   * @param style
   *          The style of the original font and this will be set to the new font.
   * @return The new font.
   */
  public static Font changeHeightAndStyle( final Device device, final Font font, final int height, final int style )
  {
    /* The new font data. */
    final List<FontData> fontData = new ArrayList<FontData>();

    /* Create the new font data. */
    final FontData[] elements = font.getFontData();
    for( final FontData element : elements )
      fontData.add( new FontData( element.getName(), height, element.getStyle() | style ) );

    /* Create and return the new font. */
    return new Font( device, fontData.toArray( new FontData[] {} ) );
  }

  public static Font changeHeightAndStyleByFactor( final Device device, final Font font, final double heightFactor, final int style )
  {
    /* The new font data. */
    final List<FontData> fontData = new ArrayList<FontData>();

    /* Create the new font data. */
    final FontData[] elements = font.getFontData();
    for( final FontData element : elements )
      fontData.add( new FontData( element.getName(), (int) (element.getHeight() * heightFactor), element.getStyle() | style ) );

    /* Create and return the new font. */
    return new Font( device, fontData.toArray( new FontData[] {} ) );
  }
}