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
package org.kalypso.ogc.gml.map.widgets.builders.sld;

import java.net.URL;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author Dirk Kuch
 */
public class SldPolygonGeometryBuilder extends AbstractSldGeometryBuilder implements ISldGeometryBuilder
{
  public SldPolygonGeometryBuilder( final IMapPanel panel, final URL... slds )
  {
    super( panel, slds );
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.builders.IGeometryBuilder#finish()
   */
  @Override
  public GM_Object finish( ) throws Exception
  {
    final Geometry polygon = finishJts();

    return JTSAdapter.wrap( polygon, getCrs() );
  }

  @Override
  public Geometry finishJts( )
  {
    final Geometry polygon = getGeometry( getCoordinates()[0] );

    return polygon;
  }

  protected Geometry getGeometry( final Coordinate... additional )
  {
    final GeometryFactory factory = JTSAdapter.jtsFactory;

    Coordinate[] coordinates = getCoordinates();
    coordinates = (Coordinate[]) ArrayUtils.addAll( coordinates, additional );

    if( coordinates.length < 2 )
      return null;
    else if( coordinates.length == 2 )
    {
      final LineString lineString = factory.createLineString( coordinates );

      return lineString;
    }
    else
    {
      coordinates = (Coordinate[]) ArrayUtils.add( coordinates, coordinates[0] );

      final LinearRing linearRing = factory.createLinearRing( coordinates );
      final Polygon polygon = factory.createPolygon( linearRing, new LinearRing[] {} );

      return polygon;
    }
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.builders.sld.AbstractSldGeometryBuilder#buildGeometry(com.vividsolutions.jts.geom.Point)
   */
  @Override
  protected Geometry buildGeometry( final Point current )
  {
    if( current == null )
      return null;

    return getGeometry( current.getCoordinate() );
  }

}
