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

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.operation.valid.IsValidOp;
import com.vividsolutions.jts.operation.valid.TopologyValidationError;

import de.openali.odysseus.chart.framework.util.resource.IPair;
import de.openali.odysseus.chart.framework.util.resource.Pair;

/**
 * FIXME: move to ZmlLineLayer, it does not belong here anymore.
 * 
 * @author Gernot Belger
 */
public class ClipHelper
{
  private final GeometryFactory m_gf = new GeometryFactory();

  private final Rectangle2D m_clipRect;

  public ClipHelper( final Rectangle2D clipRect )
  {
    m_clipRect = clipRect;
  }

  /**
   * Considers the given points as a LineString and intersects it with the current clip rectangle.<br/>
   * 
   * @param Possible
   *          multiple sub-line-strings of the original line string. The whole thing, if no clip was defined.
   */
  @SuppressWarnings("unchecked")
  public IPair<Number, Number>[][] clipAsLine( final IPair<Number, Number>[] points )
  {
    if( m_clipRect == null )
      return new IPair[][] { points };

    if( points.length == 0 )
      return new IPair[][] { points };

    final Polygon clipPolygon = getClipPolygon();

    final Coordinate[] crds = asCoordinates( points );
    if( crds.length == 1 )
    {
      // special case: we got only one single point
      final com.vividsolutions.jts.geom.Point singlePoint = m_gf.createPoint( crds[0] );
      if( clipPolygon.contains( singlePoint ) )
        return new IPair[][] { points };
      else
        return new IPair[][] {};
    }

    if( m_clipRect.getWidth() == 0.0 || m_clipRect.getHeight() == 0.0 )
      return new IPair[][] {};

    final LineString lineString = m_gf.createLineString( crds );

    final IsValidOp clipIsValidOp = new IsValidOp( clipPolygon );
    final IsValidOp lineIsValidOp = new IsValidOp( lineString );
    final TopologyValidationError clipError = clipIsValidOp.getValidationError();
    if( clipError != null )
      System.out.println( clipError );

    final TopologyValidationError lineError = lineIsValidOp.getValidationError();
    if( lineError != null )
      System.out.println( lineError );

    try
    {
      final Geometry clippedPoints = lineString.intersection( clipPolygon );

      final Collection<IPair<Number, Number>[]> collector = new ArrayList<IPair<Number, Number>[]>();
      drawGeometry( collector, clippedPoints );
      return collector.toArray( new IPair[collector.size()][] );
    }
    catch( final TopologyException e )
    {
      e.printStackTrace();
      return new IPair[][] { points };
    }
  }

  protected Coordinate[] asCoordinates( final IPair<Number, Number>[] points )
  {
    final Coordinate[] crds = new Coordinate[points.length];
    for( int i = 0; i < crds.length; i++ )
      crds[i] = new Coordinate( points[i].getDomain().doubleValue(), points[i].getTarget().doubleValue() );
    return crds;
  }

  @SuppressWarnings("unchecked")
  private void drawGeometry( final Collection<IPair<Number, Number>[]> result, final Geometry geom )
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
      final IPair<Number, Number>[] points = new IPair[coordinates.length];
      for( int i = 0; i < points.length; i++ )
        points[i] = new Pair<Number, Number>( coordinates[i].x, coordinates[i].y );

      result.add( points );
    }
    else if( geom instanceof com.vividsolutions.jts.geom.Point )
    {
      final com.vividsolutions.jts.geom.Point p = (com.vividsolutions.jts.geom.Point) geom;
      final IPair<Number, Number> point = new Pair<Number, Number>( p.getX(), p.getY() );
      result.add( new IPair[] { point } );
    }
    else
    {
      System.out.println( "Unknwon: " + geom );
    }
  }

  private Polygon getClipPolygon( )
  {
    final double minX = m_clipRect.getMinX();
    final double minY = m_clipRect.getMinY();
    final double maxX = m_clipRect.getMaxX();
    final double maxY = m_clipRect.getMaxY();

    final Coordinate[] clipShellPoints = new Coordinate[5];
    clipShellPoints[0] = new Coordinate( minX, minY );
    clipShellPoints[1] = new Coordinate( minX, maxY );
    clipShellPoints[2] = new Coordinate( maxX, maxY );
    clipShellPoints[3] = new Coordinate( maxX, minY );
    clipShellPoints[4] = clipShellPoints[0];
    final LinearRing clipShell = m_gf.createLinearRing( clipShellPoints );
    return m_gf.createPolygon( clipShell, null );
  }
}