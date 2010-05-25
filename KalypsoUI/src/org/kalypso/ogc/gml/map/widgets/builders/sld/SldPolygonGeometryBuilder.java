/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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

import java.awt.Graphics;
import java.net.URL;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.widgets.advanced.utils.SLDPainter;
import org.kalypsodeegree.graphics.sld.LineSymbolizer;
import org.kalypsodeegree.graphics.sld.PolygonSymbolizer;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Coordinate;
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
  public SldPolygonGeometryBuilder( final IMapPanel panel, final URL[] slds )
  {
    super( panel, slds );
  }

  public SldPolygonGeometryBuilder( final IMapPanel panel )
  {
    super( panel );
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.builders.IGeometryBuilder#finish()
   */
  @Override
  public GM_Object finish( ) throws Exception
  {
    final Polygon polygone = getGeometry( getCoordinates()[0] );

    return JTSAdapter.wrap( polygone, getCrs() );
  }

  protected Polygon getGeometry( final Coordinate... additional )
  {
    final GeometryFactory factory = JTSAdapter.jtsFactory;

    Coordinate[] coordinates = getCoordinates();
    coordinates = (Coordinate[]) ArrayUtils.addAll( coordinates, additional );

    final LinearRing linearRing = factory.createLinearRing( coordinates );
    final Polygon polygon = factory.createPolygon( linearRing, new LinearRing[] {} );

    return polygon;
  }

  @Override
  public void paint( final Graphics g, final GeoTransform projection, final Point current ) throws CoreException
  {
    final GeometryFactory factory = JTSAdapter.jtsFactory;
    final SLDPainter painter = new SLDPainter( projection, getCrs() );

    if( size() == 1 )
    {
      final LineString lineString = factory.createLineString( new Coordinate[] { getCoordinates()[0], current.getCoordinate() } );

      final Symbolizer symbolizer = getSymbolizer( LineSymbolizer.class );
      painter.paint( g, symbolizer, lineString );

    }
    else if( size() > 1 )
    {
      final Polygon polygon = getGeometry( current.getCoordinate(), getCoordinates()[0] );

      final Symbolizer symbolizer = getSymbolizer( PolygonSymbolizer.class );
      painter.paint( g, symbolizer, polygon );
    }
  }

}
