/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
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
