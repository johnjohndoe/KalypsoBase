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
package org.kalypso.ogc.gml;

import java.util.concurrent.ExecutionException;

import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * A cache for results of the method {@link IKalypsoFeatureTheme#getFeatureListVisible(org.kalypsodeegree.model.geometry.GM_Envelope)}.
 * 
 * @author Gernot Belger
 */
class VisibleFeaturesCache
{
  /** Marker for full extent (map does not support <code>null</code> keys). */
  static final GM_Envelope FULL_EXTENT = GeometryFactory.createGM_Envelope( 0, 0, 0, 0, null );

  private final LoadingCache<GM_Envelope, FeatureList> m_cache;

  VisibleFeaturesCache( final KalypsoFeatureTheme theme )
  {
    final CacheLoader<GM_Envelope, FeatureList> cacheLoader = new CacheLoader<GM_Envelope, FeatureList>()
    {
      @Override
      public FeatureList load( final GM_Envelope input )
      {
        if( input == FULL_EXTENT )
          return theme.calculateFeatureListVisible( null );
        else
          return theme.calculateFeatureListVisible( input );
      }
    };

    m_cache = CacheBuilder.newBuilder().weakKeys().maximumSize( 33 ).build( cacheLoader );
  }

  void clear( )
  {
    m_cache.invalidateAll();
  }

  public FeatureList getVisibleFeatures( final GM_Envelope searchEnvelope )
  {
    try
    {
      if( searchEnvelope == null )
        return m_cache.get( FULL_EXTENT );
      else
        return m_cache.get( searchEnvelope );
    }
    catch( final ExecutionException e )
    {
      e.printStackTrace();
      return null;
    }
  }
}