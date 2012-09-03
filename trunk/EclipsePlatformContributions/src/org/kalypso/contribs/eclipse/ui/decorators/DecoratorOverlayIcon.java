/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
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
package org.kalypso.contribs.eclipse.ui.decorators;

import java.util.Arrays;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * An DecoratorOverlayIcon consists of a main icon and several adornments.
 * <p>
 * Copy / Paste of the {@link org.eclipse.ui.internal.decorators.DecoratorOverlayIcon} class, in order to make it
 * public.
 * </p>
 */
public class DecoratorOverlayIcon extends CompositeImageDescriptor
{
  // the base image
  private final Image m_base;

  // the overlay images
  private final ImageDescriptor[] m_overlays;

  // the size
  private final Point m_size;

  /**
   * OverlayIcon constructor.
   * 
   * @param baseImage
   *          the base image
   * @param overlaysArray
   *          the overlay images
   * @param sizeValue
   *          the size
   */
  public DecoratorOverlayIcon( final Image baseImage, final ImageDescriptor[] overlaysArray, final Point sizeValue )
  {
    m_base = baseImage;
    m_overlays = overlaysArray;
    m_size = sizeValue;
  }

  /**
   * Draw the overlays for the reciever.
   */
  protected void drawOverlays( final ImageDescriptor[] overlaysArray )
  {
    for( int i = 0; i < m_overlays.length; i++ )
    {
      final ImageDescriptor overlay = overlaysArray[i];
      if( overlay == null )
        continue;
      ImageData overlayData = overlay.getImageData();
      // Use the missing descriptor if it is not there.
      if( overlayData == null )
        overlayData = ImageDescriptor.getMissingImageDescriptor().getImageData();
      switch( i )
      {
        case 0:
          drawImage( overlayData, 0, 0 );
          break;
        case 1:
          drawImage( overlayData, m_size.x - overlayData.width, 0 );
          break;
        case 2:
          drawImage( overlayData, 0, m_size.y - overlayData.height );
          break;
        case 3:
          drawImage( overlayData, m_size.x - overlayData.width, m_size.y - overlayData.height );
          break;
      }
    }
  }

  @Override
  public boolean equals( final Object o )
  {
    if( !(o instanceof DecoratorOverlayIcon) )
      return false;
    final DecoratorOverlayIcon other = (DecoratorOverlayIcon) o;
    return m_base.equals( other.m_base ) && Arrays.equals( m_overlays, other.m_overlays );
  }

  @Override
  public int hashCode( )
  {
    int code = m_base.hashCode();
    for( final ImageDescriptor overlay : m_overlays )
    {
      if( overlay != null )
        code ^= overlay.hashCode();
    }
    return code;
  }

  @Override
  protected void drawCompositeImage( final int width, final int height )
  {
    final ImageDescriptor underlay = m_overlays[4];
    if( underlay != null )
      drawImage( underlay.getImageData(), 0, 0 );
    drawImage( m_base.getImageData(), 0, 0 );
    drawOverlays( m_overlays );
  }

  @Override
  protected Point getSize( )
  {
    return m_size;
  }
}
