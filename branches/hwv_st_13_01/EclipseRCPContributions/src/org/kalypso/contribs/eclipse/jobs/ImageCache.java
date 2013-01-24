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
package org.kalypso.contribs.eclipse.jobs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * A cache of buffered images. {@link BufferedImage}s can be akquired and released via the cache. <br>
 * Released images are retained within the cache (up to a maximal number of images) for later reuse.
 * 
 * @author Gernot Belger
 */
public class ImageCache
{
  private final Queue<WeakReference<BufferedImage>> m_queue;

  private final int m_maxSize;

  private final Color m_backgroundColor;

  private final boolean m_clearOnCreate;

  /**
   * @param maxSize
   *          Maximal number of images retained for reuse.
   * @param backgroundColor
   *          Reused images get cleared with this color before reuse.
   * @param clearOnCreate
   *          if <code>false</code>, the image is only filled when reused.
   */
  public ImageCache( final int maxSize, final Color backgroundColor, final boolean clearOnCreate )
  {
    m_maxSize = maxSize;
    m_backgroundColor = backgroundColor;
    m_clearOnCreate = clearOnCreate;
    m_queue = new ArrayDeque<WeakReference<BufferedImage>>( m_maxSize );
  }

  public synchronized void release( final BufferedImage image )
  {
    if( m_queue.size() == m_maxSize || !m_queue.offer( new WeakReference<BufferedImage>( image ) ) )
      image.flush();

    // System.out.println( String.format( "ImageCaches holds %d images", m_queue.size() ) );
  }

  public synchronized BufferedImage akquire( final Point size )
  {
    final WeakReference<BufferedImage> ref = m_queue.poll();
    final BufferedImage image = ref == null ? null : ref.get();
    if( image != null )
    {
      if( image.getWidth() == size.x && image.getHeight() == size.y )
        return clearImage( image );
      else
        image.flush();
    }

    // System.out.println( "ImageCache creating image" );
    final BufferedImage newImage = new BufferedImage( size.x, size.y, BufferedImage.TYPE_INT_ARGB );
    if( m_clearOnCreate )
      return clearImage( newImage );
    return newImage;
  }

  private BufferedImage clearImage( final BufferedImage image )
  {
    if( m_backgroundColor == null )
      return image;

    Graphics2D g2 = null;
    try
    {
      // Hm, is there no other way to clear the image?
      g2 = image.createGraphics();
      g2.setColor( m_backgroundColor );
      g2.setBackground( m_backgroundColor );
// g2.fillRect( 0, 0, image.getWidth(), image.getHeight() );
      g2.clearRect( 0, 0, image.getWidth(), image.getHeight() );

      // System.out.println( String.format( "ImageCaches holds %d images", m_queue.size() ) );
      return image;
    }
    finally
    {
      if( g2 != null )
        g2.dispose();
    }
  }

  public void clear( )
  {
    m_queue.clear();
  }

}
