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
package org.kalypso.ogc.gml.map.widgets.advanced.edit.delegates;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.kalypso.ogc.gml.map.widgets.advanced.edit.IAdvancedEditWidgetGeometry;
import org.kalypsodeegree.model.feature.Feature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.TopologyException;

/**
 * @author Dirk Kuch
 */
public class DelegateHelper
{
  public static IAdvancedEditWidgetGeometry findUnderlyingGeometry( final Map<Geometry, Feature> geometries, final Point point )
  {
    final Set<Entry<Geometry, Feature>> entries = geometries.entrySet();
    for( final Entry<Geometry, Feature> entry : entries )
    {
      try
      {
        final Geometry geometry = entry.getKey();
        final Geometry intersection = geometry.intersection( point );

        if( !intersection.isEmpty() )
          return new IAdvancedEditWidgetGeometry()
          {
            @Override
            public Point getCurrentPoint( )
            {
              return point;
            }

            @Override
            public Feature getFeature( )
            {
              return entry.getValue();
            }

            @Override
            public Geometry getUnderlyingGeometry( )
            {
              return geometry;
            }
          };
      }
      catch( final TopologyException e )
      {
        // nothing to do
        // System.out.println( "JTS TopologyException" );
      }
    }

    return null;
  }

}
