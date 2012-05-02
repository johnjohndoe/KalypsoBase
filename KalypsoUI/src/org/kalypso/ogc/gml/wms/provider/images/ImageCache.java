/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.ogc.gml.wms.provider.images;

import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.resource.ImageDescriptor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;


/**
 * Loads image in background and returns a missing image until the real image is loaded.<br/>
 * TODO: move to more common place or use an existing solution
 * 
 * @author Gernot Belger
 */
class ImageCache
{
  private final ImageCacheLoader m_loader = new ImageCacheLoader( this );

  private final LoadingCache<URL, ImageDescriptor> m_cache = CacheBuilder.newBuilder().maximumSize( 255 ).expireAfterWrite( 60, TimeUnit.MINUTES ).build( m_loader );

  public void dispose( )
  {
    m_loader.dispose();
    m_cache.invalidateAll();
  }

  public synchronized ImageDescriptor getImage( final URL onlineResource )
  {
    try
    {
      return m_cache.get( onlineResource );
    }
    catch( final ExecutionException e )
    {
      // will not happen, we do not throw an exception
      return null;
    }
  }

  synchronized void imageLoaded( final URL onlineResource, final ImageDescriptor image )
  {
    m_cache.put( onlineResource, image );
  }
}