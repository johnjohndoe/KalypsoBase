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
package de.openali.odysseus.chart.framework.util.resource;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;

/**
 * @author burtscher1
 */
public class ImageRegistry extends AbstractResourceRegistryFactory<ImageDescriptor, Image>
{

  private final Map<ImageDescriptor, Image> m_imageMap = new HashMap<ImageDescriptor, Image>();

  /**
   * Registry and factory object which can be used to load images from urls and store them; the images must not be
   * disposed by the caller
   */
  public ImageRegistry( )
  {

  }

// /**
// * @return the image loaded from the url or null, if the image could not be loaded
// */
// public Image getImage( Device dev, ImageDescriptor id )
// {
// Image img = m_imageMap.get( id );
// if( img == null )
// img = m_imageMap.put( id, img );
// return img;
// }

  /**
   * @see de.openali.odysseus.chart.framework.util.resource.AbstractResourceRegistry#createValue(org.eclipse.swt.graphics.Device,
   *      java.lang.Object)
   */
  @Override
  protected Image createResource( Device dev, ImageDescriptor descriptor )
  {
    return descriptor.createImage( dev );
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.resource.AbstractResourceRegistry#disposeValue(java.lang.Object)
   */
  @Override
  protected void disposeResource( Image value )
  {
    value.dispose();

  }
}
