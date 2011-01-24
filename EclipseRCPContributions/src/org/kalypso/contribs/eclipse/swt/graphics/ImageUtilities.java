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
package org.kalypso.contribs.eclipse.swt.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;

/**
 * This class contains functions for dealing with SWT images.
 * 
 * @author Holger Albert
 */
public class ImageUtilities
{
  /**
   * The constructor.
   */
  private ImageUtilities( )
  {
  }

  /**
   * This code is copied from <code>http://www.eclipse.org/swt/snippets/</code>.
   * 
   * @param srcData
   *          The original image.
   * @param direction
   *          The direction. See {@link SWT#LEFT} for -90∞, {@link SWT#RIGHT} for 90∞ and {@link SWT#DOWN} for 180∞.
   * @return The rotated image.
   * @see http://www.eclipse.org/swt/snippets/
   */
  public static ImageData rotate( ImageData srcData, int direction )
  {
    int bytesPerPixel = srcData.bytesPerLine / srcData.width;
    int destBytesPerLine = (direction == SWT.DOWN) ? srcData.width * bytesPerPixel : srcData.height * bytesPerPixel;
    byte[] newData = new byte[(direction == SWT.DOWN) ? srcData.height * destBytesPerLine : srcData.width * destBytesPerLine];
    int width = 0, height = 0;
    for( int srcY = 0; srcY < srcData.height; srcY++ )
    {
      for( int srcX = 0; srcX < srcData.width; srcX++ )
      {
        int destX = 0, destY = 0, destIndex = 0, srcIndex = 0;
        switch( direction )
        {
          case SWT.LEFT: // left 90 degrees
            destX = srcY;
            destY = srcData.width - srcX - 1;
            width = srcData.height;
            height = srcData.width;
            break;
          case SWT.RIGHT: // right 90 degrees
            destX = srcData.height - srcY - 1;
            destY = srcX;
            width = srcData.height;
            height = srcData.width;
            break;
          case SWT.DOWN: // 180 degrees
            destX = srcData.width - srcX - 1;
            destY = srcData.height - srcY - 1;
            width = srcData.width;
            height = srcData.height;
            break;
        }

        destIndex = (destY * destBytesPerLine) + (destX * bytesPerPixel);
        srcIndex = (srcY * srcData.bytesPerLine) + (srcX * bytesPerPixel);
        System.arraycopy( srcData.data, srcIndex, newData, destIndex, bytesPerPixel );
      }
    }

    // destBytesPerLine is used as scanlinePad to ensure that no padding is required
    return new ImageData( width, height, srcData.depth, srcData.palette, srcData.scanlinePad, newData );
  }
}