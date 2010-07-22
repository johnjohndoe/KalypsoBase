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

import java.util.ArrayList;

import javax.vecmath.Point3d;

import org.deegree.crs.components.Unit;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.projections.ProjectionUtils;
import org.deegree.crs.transformations.CRSTransformation;
import org.kalypso.transformation.Debug;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/**
 * This class provides some functions for the GeoTransformer.
 * 
 * @author Holger Albert
 */
public class DeegreeTransformUtilities
{
  /**
   * The constructor.
   */
  private DeegreeTransformUtilities( )
  {
  }

  /**
   * This function transformes a point.
   * 
   * @param point
   *          The point, which should be transformed.
   * @param sourceCRS
   *          The coordinate system of the source.
   * @param targetCRS
   *          The coordinate system of the target.
   * @return The transformed point.
   */
  public static GM_Point transform( GM_Point point, String sourceCRS, String targetCRS ) throws Exception
  {
    /* If the coordinate systems are the same, do not transform. */
    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( targetCRS ) )
      return point;

    /* Get the transformation. */
    CRSTransformation transformation = getTransformation( sourceCRS, targetCRS );

    /* Get the coordinate systems. */
    CoordinateSystem sourceCoordinateSystem = transformation.getSourceCRS();
    CoordinateSystem targetCoordinateSystem = transformation.getTargetCRS();

    /* Debug. */
    Debug.TRANSFORM.printf( "POINT: %s to %s\n", sourceCoordinateSystem.getIdentifier(), targetCoordinateSystem.getIdentifier() );

    /* Normalize points to fit in -180:180 and -90:90 if they are in degrees. */
    double geoX = point.getX();
    double geoY = point.getY();
    double geoZ = point.getZ();
    if( sourceCoordinateSystem.getUnits().equals( Unit.RADIAN ) )
    {
      geoX = ProjectionUtils.normalizeLongitude( Math.toRadians( geoX ) );
      geoY = ProjectionUtils.normalizeLatitude( Math.toRadians( geoY ) );
    }

    /* Transform. */
    Point3d coords = new Point3d( geoX, geoY, geoZ );
    Point3d newCoords = transformation.doTransform( coords );

    /* Convert back to degrees, if necessary. */
    if( targetCoordinateSystem.getUnits().equals( Unit.RADIAN ) )
    {
      newCoords.x = Math.toDegrees( newCoords.x );
      newCoords.y = Math.toDegrees( newCoords.y );
    }

    // REMARK: here we have to write a z-value in any case!
    // We only have to check if the z value was transformed because of a 3d transformation
    // (therefore the check for dimensions)
    // We either put the old z value or the transformed value
    return GeometryFactory.createGM_Point( newCoords.x, newCoords.y, (targetCoordinateSystem.getDimension() == 3) ? newCoords.z : point.getZ(), targetCoordinateSystem.getIdentifier() );
  }

  /**
   * This function transforms a position.
   * 
   * @param pos
   *          The position, which should be transformed.
   * @param sourceCRS
   *          The coordinate system of the source.
   * @param targetCRS
   *          The coordinate system of the target.
   * @return The transformed position.
   */
  public static GM_Position transform( GM_Position pos, String sourceCRS, String targetCRS ) throws Exception
  {
    /* If the coordinate systems are the same, do not transform. */
    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( targetCRS ) )
      return pos;

    /* Get the transformation. */
    CRSTransformation transformation = getTransformation( sourceCRS, targetCRS );

    /* Get the coordinate systems. */
    CoordinateSystem sourceCoordinateSystem = transformation.getSourceCRS();
    CoordinateSystem targetCoordinateSystem = transformation.getTargetCRS();

    /* Debug. */
    Debug.TRANSFORM.printf( "POS: %s to %s\n", sourceCoordinateSystem.getIdentifier(), targetCoordinateSystem.getIdentifier() );

    /* Normalize points to fit in -180:180 and -90:90 if they are in degrees. */
    double geoX = pos.getX();
    double geoY = pos.getY();
    double geoZ = pos.getZ();
    if( sourceCoordinateSystem.getUnits().equals( Unit.RADIAN ) )
    {
      geoX = ProjectionUtils.normalizeLongitude( Math.toRadians( geoX ) );
      geoY = ProjectionUtils.normalizeLatitude( Math.toRadians( geoY ) );
    }

    /* Transform. */
    Point3d coords = new Point3d( geoX, geoY, geoZ );
    Point3d newCoords = transformation.doTransform( coords );

    /* Convert back to degrees, if necessary. */
    if( targetCoordinateSystem.getUnits().equals( Unit.RADIAN ) )
    {
      newCoords.x = Math.toDegrees( newCoords.x );
      newCoords.y = Math.toDegrees( newCoords.y );
    }

    // REMARK: here we have to write a z-value in any case!
    // We only have to check if the z value was transformed because of a 3d transformation
    // (therefore the check for dimensions)
    // We either put the old z value or the transformed value
    return GeometryFactory.createGM_Position( newCoords.x, newCoords.y, (targetCoordinateSystem.getDimension() == 3) ? newCoords.z : pos.getZ() );
  }

  /**
   * This function transforms an array of positions.
   * 
   * @param pos
   *          The array of positions.
   * @param sourceCRS
   *          The coordinate system of the source.
   * @param targetCRS
   *          The coordinate system of the target.
   * @return The array of transformed positions.
   */
  public static GM_Position[] transform( GM_Position[] pos, String sourceCRS, String targetCRS ) throws Exception
  {
    ArrayList<GM_Position> newPos = new ArrayList<GM_Position>();

    for( GM_Position po : pos )
      newPos.add( transform( po, sourceCRS, targetCRS ) );

    return newPos.toArray( new GM_Position[] {} );
  }

  /**
   * This function returns a transformation for a source coordinate system.
   * 
   * @param sourceCRS
   *          The source coordinate system.
   * @param targetCRS
   *          The coordinate system, into which the transformations should be done.
   * @return The transformation from the source coordinate system to the GeoTransformers target coordinate system.
   */
  private static CRSTransformation getTransformation( String sourceCRS, String targetCRS ) throws Exception
  {
    CachedTransformationFactory transformationFactory = CachedTransformationFactory.getInstance();
    CRSTransformation transformation = transformationFactory.createFromCoordinateSystems( sourceCRS, targetCRS );

    return transformation;
  }
}