/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 *
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always.
 *
 * If you intend to use this software in other ways than in kalypso
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree,
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.model.feature;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypsodeegree.model.feature.IFeaturePropertyHandler;

/**
 * Factory which creates {@link org.kalypsodeegree.model.feature.IFeaturePropertyHandler}s.
 * <p>
 * Is a singleton, use {@link #getInstance()} to get an instance of this class.
 * </p>
 *
 * @author Gernot Belger
 */
public final class FeaturePropertyHandlerFactory
{
  private static FeaturePropertyHandlerFactory INSTANCE = new FeaturePropertyHandlerFactory();

  public static FeaturePropertyHandlerFactory getInstance( )
  {
    return INSTANCE;
  }

  /* Non-static starts here */

  private final Map<IFeatureType, IFeaturePropertyHandler> m_handlers = new ConcurrentHashMap<>();

  /**
   * Return a handler for the given feature type.<br/>
   * Caches already created handlers.
   */
  public synchronized IFeaturePropertyHandler getHandler( final IFeatureType featureType )
  {
    // very often called method - removed containsKey call, since
    // get method returns null if the key is not present
    final IFeaturePropertyHandler existingHandler = m_handlers.get( featureType );
    if( existingHandler != null )
      return existingHandler;

    final IFeaturePropertyHandler newHandler = createHandler( featureType );

    m_handlers.put( featureType, newHandler );

    return newHandler;
  }

  private IFeaturePropertyHandler createHandler( final IFeatureType featureType )
  {
    return new AdvancedFeaturePropertyHandler( featureType );
  }
}