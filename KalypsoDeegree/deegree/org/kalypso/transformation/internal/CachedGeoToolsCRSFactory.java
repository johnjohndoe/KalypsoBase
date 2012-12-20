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
package org.kalypso.transformation.internal;

import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.tuple.Pair;
import org.geotools.referencing.CRS;
import org.kalypso.transformation.transformer.GeoTransformerException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Cache for geotools crs and transformations
 * 
 * @author Gernot Belger
 */
public class CachedGeoToolsCRSFactory
{
  private final LoadingCache<String, CoordinateReferenceSystem> m_crsCache = CacheBuilder.newBuilder().maximumSize( 100 ).build( new CacheLoader<String, CoordinateReferenceSystem>()
  {
    @Override
    public CoordinateReferenceSystem load( final String srsName ) throws Exception
    {
      return CRS.decode( srsName );
    }
  } );

  private final LoadingCache<Pair<String, String>, MathTransform> m_transformCache = CacheBuilder.newBuilder().maximumSize( 100 ).build( new CacheLoader<Pair<String, String>, MathTransform>()
  {
    @Override
    public MathTransform load( final Pair<String, String> fromTo ) throws Exception
    {
      final String sourceSRS = fromTo.getLeft();
      final String targetSRS = fromTo.getRight();

      final CoordinateReferenceSystem sourceCoordinateSystem = getCRS( sourceSRS );
      final CoordinateReferenceSystem targetCoordinateSystem = getCRS( targetSRS );

      /* Get the transformation. */
      return CRS.findMathTransform( sourceCoordinateSystem, targetCoordinateSystem );
    }
  } );

  public CoordinateReferenceSystem getCRS( final String srsName ) throws GeoTransformerException
  {
    try
    {
      return m_crsCache.get( srsName );
    }
    catch( final ExecutionException e )
    {
      throw new GeoTransformerException( e );
    }
  }

  public MathTransform getTransform( final String sourceSRS, final String targetSRS ) throws GeoTransformerException
  {
    try
    {
      return m_transformCache.get( Pair.of( sourceSRS, targetSRS ) );
    }
    catch( final ExecutionException e )
    {
      throw new GeoTransformerException( e );
    }
  }
}
