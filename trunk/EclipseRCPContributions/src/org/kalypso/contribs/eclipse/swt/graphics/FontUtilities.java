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

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

/**
 * Helper class for deriving fonts from other fonts.<br>
 * Keeps track of created fonts and disposes theses if this class is disposed.<br>
 * 
 * @author Gernot Belger
 */
public class FontUtilities
{
  private final Collection<Font> m_disposeFonts = new LinkedList<Font>();

  public void dispose()
  {
    for( final Font font : m_disposeFonts )
      font.dispose();
  }

  public Font createChangedFontData( final FontData[] fontData, final int heightOffset, final int styleOffset,
      final Device device )
  {
    for( final FontData element : fontData )
    {
      element.setHeight( element.getHeight() + heightOffset );
      element.setStyle( element.getStyle() | styleOffset );
    }

    final Font font = new Font( device, fontData );
    m_disposeFonts.add( font );

    return font;
  }
}
