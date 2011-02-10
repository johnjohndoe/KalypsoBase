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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.OverlayIcon;

/**
 * @author Dirk Kuch
 */
@SuppressWarnings("restriction")
public final class ZmlTableImageMerger
{
  private static final ImageRegistry IMAGE_REGISTRY = new ImageRegistry();

  private static final RGB RGB_WHITE = new RGB( 255, 255, 255 );

  private static final ImageData IMG_ADDITIONAL = new ImageData( ZmlTableImageMerger.class.getResourceAsStream( "icons/additional.png" ) );

  private final Set<ZmlTableImage> m_images = new LinkedHashSet<ZmlTableImage>();

  protected final int m_numberOfIcons;

  protected final Point m_iconSize = new Point( 16, 16 );

  public ZmlTableImageMerger( final int numberOfIcons )
  {
    m_numberOfIcons = numberOfIcons;
  }

  public void addImage( final ZmlTableImage image )
  {
    m_images.add( image );
  }

  public Image createImage( final Display display )
  {
    if( m_images.size() == 0 )
      return null;

    final ZmlTableImage[] images = m_images.toArray( new ZmlTableImage[] {} );
    final String imageReference = buildImageReference( images );
    final Image registered = IMAGE_REGISTRY.get( imageReference );
    if( registered != null )
      return registered;

    final Point size = getSize();

    ImageData base = new ImageData( 1, 1, 1, new PaletteData( new RGB[] { RGB_WHITE } ) );
    base.transparentPixel = base.palette.getPixel( RGB_WHITE );

    for( int index = 0; index < m_numberOfIcons; index++ )
    {
      if( index >= images.length )
        break;

      final int i = index;
      final ZmlTableImage tile = images[index];

      final OverlayIcon overlay = new OverlayIcon( ImageDescriptor.createFromImageData( base ), ImageDescriptor.createFromImage( tile.getIcon() ), size )
      {
        /**
         * @see org.eclipse.ui.internal.OverlayIcon#drawTopRight(org.eclipse.jface.resource.ImageDescriptor)
         */
        @Override
        protected void drawTopRight( final ImageDescriptor ov )
        {
          if( ov == null )
            return;

          drawImage( ov.getImageData(), getX( i ), 0 );
        }
      };

      base = overlay.getImageData();
    }

    if( images.length > m_numberOfIcons )
    {
      final OverlayIcon overlay = new OverlayIcon( ImageDescriptor.createFromImageData( base ), ImageDescriptor.createFromImageData( IMG_ADDITIONAL ), size )
      {
        /**
         * @see org.eclipse.ui.internal.OverlayIcon#drawTopRight(org.eclipse.jface.resource.ImageDescriptor)
         */
        @Override
        protected void drawTopRight( final ImageDescriptor ov )
        {
          drawImage( ov.getImageData(), getX( m_numberOfIcons ) + m_iconSize.x, 0 );
        }
      };

      base = overlay.getImageData();
    }

    final Image image = new Image( display, base );
    IMAGE_REGISTRY.put( imageReference, image );

    return image;
  }

  private String buildImageReference( final ZmlTableImage[] images )
  {
    final StringBuffer buffer = new StringBuffer();
    for( final ZmlTableImage image : images )
    {
      buffer.append( image.getHref() );
      buffer.append( ";" );
    }

    return StringUtils.chomp( buffer.toString() );
  }

  private Point getSize( )
  {
    return new Point( m_numberOfIcons * m_iconSize.x + m_iconSize.x, m_iconSize.y );
  }

  protected int getX( final int index )
  {
    return m_iconSize.x * index;
  }

}
