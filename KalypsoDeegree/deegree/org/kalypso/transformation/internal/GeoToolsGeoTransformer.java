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
package org.kalypso.transformation.internal;

import org.geotools.geometry.GeneralDirectPosition;
import org.kalypso.transformation.Debug;
import org.kalypso.transformation.transformer.GeoTransformerException;
import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.model.geometry.GM_AbstractSurfacePatch;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 * This geo transformer uses geo tools.
 * 
 * @author Holger Albert
 */
public class GeoToolsGeoTransformer implements IGeoTransformer
{
  /**
   * The coordinate system, into which the transformations should be done.
   */
  private final String m_targetCRS;

  /**
   * @param targetCRS
   *          The coordinate system, into which the transformations should be done.
   */
  public GeoToolsGeoTransformer( final String targetCRS )
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
    try
    {
      if( position == null )
        return null;

      if( sourceCRS == null || sourceCRS.equalsIgnoreCase( m_targetCRS ) )
        return position;

      // TODO: performance bottle neck -> we should cache the math transforms!

      /* Get the coordinate systems. */
      final CoordinateReferenceSystem sourceCoordinateSystem = GeoTransformerFactory.CRS_CACHE.getCRS( sourceCRS );
      final CoordinateReferenceSystem targetCoordinateSystem = GeoTransformerFactory.CRS_CACHE.getCRS( m_targetCRS );

      /* Get the transformation. */
      final MathTransform transformation = GeoTransformerFactory.CRS_CACHE.getTransform( sourceCRS, m_targetCRS );

      /* Debug. */
      Debug.TRANSFORM.printf( "POS: %s to %s\n", sourceCRS, m_targetCRS );

      /* Create the source position. */
      final GeneralDirectPosition sourcePt = new GeneralDirectPosition( sourceCoordinateSystem );
      sourcePt.setOrdinate( 0, position.getX() );
      sourcePt.setOrdinate( 1, position.getY() );
      if( sourcePt.getDimension() == 3 )
        sourcePt.setOrdinate( 2, position.getZ() );

      /* Create the target position. */
      final GeneralDirectPosition targetPt = new GeneralDirectPosition( targetCoordinateSystem );

      /* Transform. */
      transformation.transform( sourcePt, targetPt );

      return GeometryFactory.createGM_Position( targetPt.getOrdinate( 0 ), targetPt.getOrdinate( 1 ), targetPt.getDimension() == 3 ? targetPt.getOrdinate( 2 ) : position.getZ() );
    }
    catch( MismatchedDimensionException | IndexOutOfBoundsException | TransformException e )
    {
      throw new GeoTransformerException( e );
    }
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T extends GM_Object> T transform( final T geometry ) throws GeoTransformerException
  {
    if( geometry == null )
      return null;

    final String sourceCRS = geometry.getCoordinateSystem();
    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( m_targetCRS ) )
      return geometry;

    if( geometry instanceof GM_Point )
      return (T)transform( (GM_Point)geometry, sourceCRS, m_targetCRS );

    return (T)geometry.transform( m_targetCRS );
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
  public <T extends GM_AbstractSurfacePatch> T transform( final T surfacePatch ) throws GeoTransformerException
  {
    if( surfacePatch == null )
      return null;

    final String sourceCRS = surfacePatch.getCoordinateSystem();
    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( m_targetCRS ) )
      return surfacePatch;

    return (T)surfacePatch.transform( m_targetCRS );
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
    try
    {
      /* If the coordinate systems are the same, do not transform. */
      if( sourceCRS == null || sourceCRS.equalsIgnoreCase( targetCRS ) )
        return point;

      /* Get the coordinate systems. */
      final CoordinateReferenceSystem sourceCoordinateSystem = GeoTransformerFactory.CRS_CACHE.getCRS( sourceCRS );
      final CoordinateReferenceSystem targetCoordinateSystem = GeoTransformerFactory.CRS_CACHE.getCRS( targetCRS );

      /* Get the transformation. */
      final MathTransform transformation = GeoTransformerFactory.CRS_CACHE.getTransform( sourceCRS, targetCRS );

      /* Debug. */
      Debug.TRANSFORM.printf( "POS: %s to %s\n", sourceCRS, targetCRS );

      /* Create the source position. */
      final GeneralDirectPosition sourcePt = new GeneralDirectPosition( sourceCoordinateSystem );
      sourcePt.setOrdinate( 0, point.getX() );
      sourcePt.setOrdinate( 1, point.getY() );
      if( sourcePt.getDimension() == 3 )
        sourcePt.setOrdinate( 2, point.getZ() );

      /* Create the target position. */
      final GeneralDirectPosition targetPt = new GeneralDirectPosition( targetCoordinateSystem );

      /* Transform. */
      transformation.transform( sourcePt, targetPt );

      return GeometryFactory.createGM_Point( targetPt.getOrdinate( 0 ), targetPt.getOrdinate( 1 ), targetPt.getDimension() == 3 ? targetPt.getOrdinate( 2 ) : point.getZ(), targetCRS );
    }
    catch( MismatchedDimensionException | IndexOutOfBoundsException | TransformException e )
    {
      throw new GeoTransformerException( e );
    }
  }
}