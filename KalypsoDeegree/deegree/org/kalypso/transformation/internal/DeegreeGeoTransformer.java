/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import javax.vecmath.Point3d;

import org.deegree.crs.components.Unit;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.projections.ProjectionUtils;
import org.deegree.crs.transformations.CRSTransformation;
import org.deegree.model.crs.UnknownCRSException;
import org.kalypso.transformation.Debug;
import org.kalypso.transformation.transformer.GeoTransformerException;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_SurfacePatch;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/**
 * This geo transformer uses deegree.
 *
 * @author Holger Albert
 */
public class DeegreeGeoTransformer implements IGeoTransformer
{
  /**
   * The coordinate system, into which the transformations should be done.
   */
  private final String m_targetCRS;

  /**
   * @param targetCRS
   *          The coordinate system, into which the transformations should be done.
   */
  public DeegreeGeoTransformer( final String targetCRS )
  {
    m_targetCRS = targetCRS;
  }

  @Override
  public String getTarget( )
  {
    return m_targetCRS;
  }

  @Override
  public GM_Position transform( final GM_Position position, final String sourceCRS ) throws GeoTransformerException
  {
    if( position == null )
      return null;

    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( m_targetCRS ) )
      return position;

    return transform( position, sourceCRS, m_targetCRS );
  }

  @Override
  public GM_Object transform( final GM_Object geometry ) throws GeoTransformerException
  {
    if( geometry == null )
      return null;

    final String sourceCRS = geometry.getCoordinateSystem();
    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( m_targetCRS ) )
      return geometry;

    if( geometry instanceof GM_Point )
      return transform( (GM_Point) geometry, sourceCRS, m_targetCRS );

    return geometry.transform( m_targetCRS );
  }

  @Override
  public GM_Envelope transform( final GM_Envelope envelope ) throws GeoTransformerException
  {
    if( envelope == null )
      return null;

    final String sourceCRS = envelope.getCoordinateSystem();
    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( m_targetCRS ) )
      return envelope;

    final GM_Position min = transform( envelope.getMin(), sourceCRS );
    final GM_Position max = transform( envelope.getMax(), sourceCRS );

    return GeometryFactory.createGM_Envelope( min, max, m_targetCRS );
  }

  @Override
  public GM_SurfacePatch transform( final GM_SurfacePatch surfacePatch ) throws GeoTransformerException
  {
    if( surfacePatch == null )
      return null;

    final String sourceCRS = surfacePatch.getCoordinateSystem();
    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( m_targetCRS ) )
      return surfacePatch;

    return (GM_SurfacePatch) surfacePatch.transform( m_targetCRS );
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
  private GM_Point transform( final GM_Point point, final String sourceCRS, final String targetCRS ) throws GeoTransformerException
  {
    /* If the coordinate systems are the same, do not transform. */
    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( targetCRS ) )
      return point;

    /* Get the transformation. */
    final CRSTransformation transformation = getTransformation( sourceCRS, targetCRS );

    /* Get the coordinate systems. */
    final CoordinateSystem sourceCoordinateSystem = transformation.getSourceCRS();
    final CoordinateSystem targetCoordinateSystem = transformation.getTargetCRS();

    /* Debug. */
    Debug.TRANSFORM.printf( "POINT: %s to %s\n", sourceCoordinateSystem.getIdentifier(), targetCoordinateSystem.getIdentifier() );

    /* Normalize points to fit in -180:180 and -90:90 if they are in degrees. */
    double geoX = point.getX();
    double geoY = point.getY();
    final double geoZ = point.getZ();
    if( sourceCoordinateSystem.getUnits().equals( Unit.RADIAN ) )
    {
      geoX = ProjectionUtils.normalizeLongitude( Math.toRadians( geoX ) );
      geoY = ProjectionUtils.normalizeLatitude( Math.toRadians( geoY ) );
    }

    /* Transform. */
    final Point3d coords = new Point3d( geoX, geoY, geoZ );
    final Point3d newCoords = transformation.doTransform( coords );

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
    return GeometryFactory.createGM_Point( newCoords.x, newCoords.y, targetCoordinateSystem.getDimension() == 3 ? newCoords.z : point.getZ(), targetCoordinateSystem.getIdentifier() );
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
  private GM_Position transform( final GM_Position pos, final String sourceCRS, final String targetCRS ) throws GeoTransformerException
  {
      /* If the coordinate systems are the same, do not transform. */
      if( sourceCRS == null || sourceCRS.equalsIgnoreCase( targetCRS ) )
        return pos;

      /* Get the transformation. */
      final CRSTransformation transformation = getTransformation( sourceCRS, targetCRS );

      /* Get the coordinate systems. */
      final CoordinateSystem sourceCoordinateSystem = transformation.getSourceCRS();
      final CoordinateSystem targetCoordinateSystem = transformation.getTargetCRS();

      /* Debug. */
      Debug.TRANSFORM.printf( "POS: %s to %s\n", sourceCoordinateSystem.getIdentifier(), targetCoordinateSystem.getIdentifier() );

      /* Normalize points to fit in -180:180 and -90:90 if they are in degrees. */
      double geoX = pos.getX();
      double geoY = pos.getY();
      final double geoZ = pos.getZ();
      if( sourceCoordinateSystem.getUnits().equals( Unit.RADIAN ) )
      {
        geoX = ProjectionUtils.normalizeLongitude( Math.toRadians( geoX ) );
        geoY = ProjectionUtils.normalizeLatitude( Math.toRadians( geoY ) );
      }

      /* Transform. */
      final Point3d coords = new Point3d( geoX, geoY, geoZ );
      final Point3d newCoords = transformation.doTransform( coords );

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
      return GeometryFactory.createGM_Position( newCoords.x, newCoords.y, targetCoordinateSystem.getDimension() == 3 ? newCoords.z : pos.getZ() );
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
  private CRSTransformation getTransformation( final String sourceCRS, final String targetCRS ) throws GeoTransformerException
  {
    try
    {
    final CachedTransformationFactory transformationFactory = CachedTransformationFactory.getInstance();

    return transformationFactory.createFromCoordinateSystems( sourceCRS, targetCRS );
    }
    catch( UnknownCRSException | TransformationException e )
    {
      throw new GeoTransformerException( e );
    }
  }
}