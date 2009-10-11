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
package org.kalypso.contribs.eclipse.jface.resource;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

/**
 * An image descriptor that creates an image from another image in the disabled state.
 * 
 * @author Gernot Belger
 */
public class DisabledImageDescriptor extends ImageDescriptor
{
  private final Image m_image;

  public DisabledImageDescriptor( final Image image )
  {
    this( image, SWT.IMAGE_DISABLE );
  }

  /**
   * @param flag
   *          Flag which will be forwarded to the image copy constructor. Only {@link SWT#IMAGE_DISABLE} and
   *          {@link SWT#IMAGE_GRAY} are supported.
   * @see Image#Image(org.eclipse.swt.graphics.Device, Image, int)
   */
  public DisabledImageDescriptor( final Image image, final int flag )
  {
    m_image = image;

    Assert.isTrue( flag == SWT.IMAGE_GRAY | flag == SWT.IMAGE_DISABLE );
  }

  /**
   * @see org.eclipse.jface.resource.ImageDescriptor#getImageData()
   */
  @Override
  public ImageData getImageData( )
  {
    // REMARK: a bit heavy to create/destroy the image here, but else the implementation will be much more difficult.
    final Image image = new Image( m_image.getDevice(), m_image, SWT.IMAGE_DISABLE );
    final ImageData imageData = image.getImageData();
    image.dispose();
    return imageData;
  }

}
