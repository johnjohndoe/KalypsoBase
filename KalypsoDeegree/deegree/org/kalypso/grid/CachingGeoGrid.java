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
package org.kalypso.grid;

import java.awt.Point;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * @author belger
 */
public class CachingGeoGrid extends AbstractDelegatingGeoGrid
{
  private LoadingCache<Point, Double> m_cache;

  public CachingGeoGrid( final IGeoGrid delegate )
  {
    super( delegate, true );
  }

  private LoadingCache<Point, Double> getCache( )
  {
    synchronized( this )
    {
      if( m_cache == null )
      {
        final CacheLoader<Point, Double> loader = new CacheLoader<Point, Double>()
        {
          @Override
          public Double load( final Point tuple ) throws Exception
          {
            return getDelegate().getValue( tuple.x, tuple.y );
          }
        };

        m_cache = CacheBuilder.newBuilder().maximumSize( 10 * 1024 * 1024 ).build( loader );
      }
    }

    return m_cache;
  }

  @Override
  public void dispose( )
  {
    super.dispose();

    synchronized( this )
    {
      if( m_cache != null )
        m_cache.cleanUp();
      m_cache = null;
    }
  }

  @Override
  public double getValue( final int x, final int y ) throws GeoGridException
  {
    try
    {
      return getCache().get( new Point( x, y ) );
    }
    catch( final ExecutionException e )
    {
      throw new GeoGridException( "Failed to access cached grid value", e ); //$NON-NLS-1$
    }
  }
}