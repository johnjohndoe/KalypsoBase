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
package org.kalypso.ogc.gml.map.utilities;

import java.awt.Point;

import org.kalypso.jts.SnapUtilities;
import org.kalypso.jts.SnapUtilities.SNAP_TYPE;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Utility class for map operations.
 * 
 * @author Holger Albert
 */
public final class MapUtilities
{
  private MapUtilities( )
  {
  }

  /**
   * Snaps the given AWT-Point to a given geometry, if it lies into a specified radius.
   * 
   * @param mapPanel
   *          The MapPanel of the map.
   * @param p
   *          The AWT-Point which should be snapped.
   * @param radiusPx
   *          This radius will be converted to a world coord radius. Within this circle, the AWT-Point is beeing
   *          snapped.
   * @param type
   *          This type of snapping will be used. {@link SNAP_TYPE}
   * @return The GM_Point snapped on the geometry.
   */
  public static GM_Point snap( final IMapPanel mapPanel, final GM_Object geometry, final Point p, final int radiusPx, final SNAP_TYPE type ) throws GM_Exception
  {
    /* Transform the point to a GM_Point. */
    final GM_Point point = MapUtilities.transform( mapPanel, p );
    if( point == null )
      return null;

    return snap( mapPanel, geometry, point, radiusPx, type );
  }

  /**
   * Snaps the given GM_Point to a given geometry, if it lies into a specified radius.
   * 
   * @param mapPanel
   *          The MapPanel of the map.
   * @param point
   *          The GM_Point which should be snapped.
   * @param radiusPx
   *          This radius will be converted to a world coord radius. Within this circle, the GM_Point is beeing snapped.
   * @param type
   *          This type of snapping will be used. {@link SNAP_TYPE}
   * @return The GM_Point snapped on the geometry.
   */
  public static GM_Point snap( final IMapPanel mapPanel, final GM_Object geometry, final GM_Point point, final int radiusPx, final SNAP_TYPE type ) throws GM_Exception
  {
    /* Get the JTS geometry. */
    final Geometry geometryJTS = JTSAdapter.export( geometry );
    final com.vividsolutions.jts.geom.Point pointJTS = (com.vividsolutions.jts.geom.Point) JTSAdapter.export( point );

    final double buffer = MapUtilities.calculateWorldDistance( mapPanel, point, radiusPx );
    final com.vividsolutions.jts.geom.Point snapPoint = snap( geometryJTS, pointJTS, type, buffer );
    if( snapPoint != null )
    {
      final GM_Point myPoint = (GM_Point) JTSAdapter.wrap( snapPoint );
      myPoint.setCoordinateSystem( point.getCoordinateSystem() );

      return myPoint;
    }

    return null;
  }

  public static com.vividsolutions.jts.geom.Point snap( final Geometry geometryJTS, final com.vividsolutions.jts.geom.Point pointJTS, final SNAP_TYPE type, final double buffer )
  {
    /* Buffer the point. */
    final Geometry pointBuffer = pointJTS.buffer( buffer );
    if( !pointBuffer.intersects( geometryJTS ) )
      return null;

    if( geometryJTS instanceof com.vividsolutions.jts.geom.Point )
    {
      final com.vividsolutions.jts.geom.Point snapPoint = SnapUtilities.snapPoint( pointJTS );
      return snapPoint;
    }
    else if( geometryJTS instanceof LineString )
    {
      final com.vividsolutions.jts.geom.Point snapPoint = SnapUtilities.snapLine( (LineString) geometryJTS, pointBuffer, type );

      return snapPoint;
    }
    else if( geometryJTS instanceof Polygon )
    {
      final com.vividsolutions.jts.geom.Point snapPoint = SnapUtilities.snapPolygon( (Polygon) geometryJTS, pointBuffer, type );

      return snapPoint;
    }

    return null;
  }

  /**
   * This method transforms the AWT-Point to a GM_Point.
   * 
   * @param mapPanel
   *          The MapPanel of the map.
   * @param p
   *          The AWT-Point.
   */
  public static GM_Point transform( final IMapPanel mapPanel, final Point p )
  {
    if( p == null )
      return null;

    final GeoTransform projection = mapPanel.getProjection();
    final IMapModell mapModell = mapPanel.getMapModell();
    if( mapModell == null || projection == null )
      return null;

    String coordinatesSystem = mapModell.getCoordinatesSystem();
    if( coordinatesSystem == null )
    {
      coordinatesSystem = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
    }

    final double x = p.getX();
    final double y = p.getY();

    return GeometryFactory.createGM_Point( projection.getSourceX( x ), projection.getSourceY( y ), coordinatesSystem );
  }

  /**
   * This method transforms the GM_Point to an AWT-Point.
   * 
   * @param mapPanel
   *          The MapPanel of the map.
   * @param p
   *          The GM_Point.
   */
  public static Point retransform( final IMapPanel mapPanel, final GM_Point p )
  {
    final GeoTransform projection = mapPanel.getProjection();

    final double x = p.getX();
    final double y = p.getY();

    return new Point( (int) projection.getDestX( x ), (int) projection.getDestY( y ) );
  }

  /**
   * This method transforms the GM_Point to an AWT-Point.
   * 
   * @param mapPanel
   *          The MapPanel of the map.
   * @param p
   *          The GM_Point.
   */
  public static Point retransform( final IMapPanel mapPanel, final GM_Position position )
  {
    final GeoTransform projection = mapPanel.getProjection();

    final double x = position.getX();
    final double y = position.getY();

    return new Point( (int) projection.getDestX( x ), (int) projection.getDestY( y ) );
  }

  /**
   * This function transforms a distance in pixel to the world distance.
   * 
   * @param mapPanel
   *          The MapPanel of the map.
   * @param reference
   *          The reference point.
   * @param distancePx
   *          The distance to be calculated.
   * @return The distance in the world coords.
   */
  public static double calculateWorldDistance( final IMapPanel mapPanel, final GM_Point reference, final int distancePx )
  {
    final Point point = MapUtilities.retransform( mapPanel, reference );
    point.x = point.x + distancePx;

    final GM_Point destination = MapUtilities.transform( mapPanel, point );
    return destination.getX() - reference.getX();
  }

  /**
   * This function transforms a distance in pixel to the world distance.
   * 
   * @param mapPanel
   *          The MapPanel of the map.
   * @param distancePx
   *          The distance in pixel to be calculated.
   * @return The distance in the world coordinates system.
   */
  public static double calculateWorldDistance( final IMapPanel mapPanel, final int distancePx )
  {
    final GM_Position minPosition = mapPanel.getBoundingBox().getMin();
    final GM_Point reference = GeometryFactory.createGM_Point( minPosition.getX(), minPosition.getY(), mapPanel.getMapModell().getCoordinatesSystem() );

    return MapUtilities.calculateWorldDistance( mapPanel, reference, distancePx );
  }

  /**
   * This function sets the map scale, if different from the given map panel.
   * 
   * @param scale
   *          The new map scale.
   */
  public static void setMapScale( final IMapPanel mapPanel, final double scale )
  {
    /* Get the current map scale. */
    final double mapScale = mapPanel.getCurrentScale();

    /* If it is the same as before, don't change anything. */
    if( mapScale == scale )
      return;

    // TODO Transform to EPSG4XXX like in the function where the scale is calculated.
    // Then calculate the new BBOX.
    // Then retransform to the original CS.

    /* Get the current extent. */
    final GM_Envelope extent = mapPanel.getBoundingBox();

    /* Get the current displayed distance (meter). */
    final double width = extent.getWidth();
    final double height = extent.getHeight();

    /* Calculate the center of the extent (coordinates). */
    double x = extent.getMin().getX();
    double y = extent.getMin().getY();
    x = x + width / 2;
    y = y + height / 2;

    /* Calculate the new extent. */
    final double newWidth = (width / mapScale) * scale;
    final double newHeight = (height / mapScale) * scale;

    final double newX = x - newWidth / 2;
    final double newY = y - newHeight / 2;

    /* Create the new extent. */
    final GM_Envelope newExtent = GeometryFactory.createGM_Envelope( newX, newY, newX + newWidth, newY + newHeight, extent.getCoordinateSystem() );

    /* Set the new extent. */
    mapPanel.setBoundingBox( newExtent );
  }

}