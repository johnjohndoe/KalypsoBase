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
package org.kalypso.zml.ui.table.provider;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * @author Dirk Kuch
 */
public final class ZmlTableHeaderIconProvider
{
  private static final Image IMG_ADDITIONAL = new Image( null, ZmlTableHeaderIconProvider.class.getResourceAsStream( "icons/additional.png" ) );

  Set<Image> m_images = new LinkedHashSet<Image>();

  private final int m_numberOfIcons;

  private final Point m_iconSize;

  public ZmlTableHeaderIconProvider( final int numberOfIcons, final Point iconSize )
  {
    m_numberOfIcons = numberOfIcons;
    m_iconSize = iconSize;
  }

  public void addImage( final Image image )
  {
    m_images.add( image );
  }

  public Image createImage( final Display display )
  {
    // size = number of icons + one place holder image ('+' icon, for "not all icons are displayed");
    final ImageData data = new ImageData( m_numberOfIcons * m_iconSize.x + m_iconSize.x, m_iconSize.y, 32, new PaletteData( new RGB[] { display.getSystemColor( SWT.COLOR_WHITE ).getRGB(),
        display.getSystemColor( SWT.COLOR_BLACK ).getRGB() } ) );
    data.transparentPixel = data.palette.getPixel( new RGB( 255, 255, 255 ) );

    final Image image = new Image( display, data );
    final Image[] images = m_images.toArray( new Image[] {} );

    final GC gc = new GC( image );

    for( int index = 0; index < m_numberOfIcons; index++ )
    {
      if( index >= images.length )
        break;

      final Image tile = images[index];
      gc.drawImage( tile, getX( index ), 0 );
    }

    if( images.length > m_numberOfIcons )
    {
      gc.drawImage( IMG_ADDITIONAL, getX( m_numberOfIcons ) + m_iconSize.x, 0 );
    }

    gc.dispose();

    return image;
  }

  private int getX( final int index )
  {
    return m_iconSize.x * index;
  }
}
