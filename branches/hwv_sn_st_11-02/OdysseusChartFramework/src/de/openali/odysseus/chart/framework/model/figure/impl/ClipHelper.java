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
package de.openali.odysseus.chart.framework.model.figure.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author Gernot Belger
 */
public class ClipHelper
{
  private final GeometryFactory m_gf = new GeometryFactory();

  private final Rectangle m_clipRect;

  public ClipHelper( final Rectangle clipRect )
  {
    m_clipRect = clipRect;
  }

  /**
   * Considers the given points as a LineString and intersects it with the current clip rectangle.<br/>
   * 
   * @param Possible
   *          multiple sub-line-strings of the original line string. The whole thing, if no clip was defined.
   */
  public Point[][] clipAsLine( final Point[] points )
  {
    if( m_clipRect == null || points.length == 0 )
      return new Point[][] { points };

    final Polygon clipPolygon = getClipPolygon();

    // Using JTS to intersect graphics
    final Coordinate[] crds = new Coordinate[points.length];

    for( int i = 0; i < crds.length; i++ )
      crds[i] = new Coordinate( points[i].x, points[i].y );

    // special case: we got only one single point
    if( crds.length == 1 )
    {
      final com.vividsolutions.jts.geom.Point singlePoint = m_gf.createPoint( crds[0] );
      if( clipPolygon.contains( singlePoint ) )
        return new Point[][]{ points };
      else
        return new Point[][]{};
    }
    
    final LineString lineString = m_gf.createLineString( crds );


    final Geometry clippedPoints = lineString.intersection( clipPolygon );

    final Collection<Point[]> collector = new ArrayList<Point[]>();
    drawGeometry( collector, clippedPoints );
    return collector.toArray( new Point[collector.size()][] );
  }

  private void drawGeometry( final Collection<Point[]> result, final Geometry geom )
  {
    if( geom instanceof GeometryCollection )
    {
      final GeometryCollection collection = (GeometryCollection) geom;
      final int numGeometries = collection.getNumGeometries();
      for( int i = 0; i < numGeometries; i++ )
      {
        final Geometry geometryN = collection.getGeometryN( i );
        drawGeometry( result, geometryN );
      }
    }
    else if( geom instanceof LineString )
    {
      final LineString ls = (LineString) geom;
      final Coordinate[] coordinates = ls.getCoordinates();
      final Point[] points = new Point[coordinates.length];
      for( int i = 0; i < points.length; i++ )
        points[i] = new Point( (int) coordinates[i].x, (int) coordinates[i].y );

      result.add( points );
    }
    else if( geom instanceof com.vividsolutions.jts.geom.Point )
    {
      final com.vividsolutions.jts.geom.Point p = (com.vividsolutions.jts.geom.Point) geom;
      final Point point = new Point( (int) p.getX(), (int) p.getY() );
      result.add( new Point[] { point } );
    }
    else
    {
      System.out.println( "Unknwon: " + geom );
    }
  }

  private Polygon getClipPolygon( )
  {
    final Coordinate[] clipShellPoints = new Coordinate[5];
    clipShellPoints[0] = new Coordinate( m_clipRect.x, m_clipRect.y );
    clipShellPoints[1] = new Coordinate( m_clipRect.x, m_clipRect.y + m_clipRect.height );
    clipShellPoints[2] = new Coordinate( m_clipRect.x + m_clipRect.width, m_clipRect.y + m_clipRect.height );
    clipShellPoints[3] = new Coordinate( m_clipRect.x + m_clipRect.width, m_clipRect.y );
    clipShellPoints[4] = clipShellPoints[0];
    final LinearRing clipShell = m_gf.createLinearRing( clipShellPoints );
    return m_gf.createPolygon( clipShell, null );
  }
}