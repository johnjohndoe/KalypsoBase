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

import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.CRS;
import org.kalypso.transformation.Debug;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_SurfacePatch;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

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
  private String m_targetCRS;

  /**
   * The constructor.
   * 
   * @param targetCRS
   *          The coordinate system, into which the transformations should be done.
   */
  public GeoToolsGeoTransformer( String targetCRS )
  {
    m_targetCRS = targetCRS;
  }

  /**
   * @see org.kalypso.transformation.transformer.IGeoTransformer#getTarget()
   */
  @Override
  public String getTarget( )
  {
    return m_targetCRS;
  }

  /**
   * @see org.kalypso.transformation.transformer.IGeoTransformer#transform(org.kalypsodeegree.model.geometry.GM_Position,
   *      java.lang.String)
   */
  @Override
  public GM_Position transform( GM_Position position, String sourceCRS ) throws Exception
  {
    if( position == null )
      return null;

    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( m_targetCRS ) )
      return position;

    return transform( position, sourceCRS, m_targetCRS );
  }

  /**
   * @see org.kalypso.transformation.transformer.IGeoTransformer#transform(org.kalypsodeegree.model.geometry.GM_Object)
   */
  @Override
  public GM_Object transform( GM_Object geometry ) throws Exception
  {
    if( geometry == null )
      return null;

    String sourceCRS = geometry.getCoordinateSystem();
    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( m_targetCRS ) )
      return geometry;

    if( geometry instanceof GM_Point )
      return transform( ((GM_Point) geometry), sourceCRS, m_targetCRS );

    return geometry.transform( m_targetCRS );
  }

  /**
   * @see org.kalypso.transformation.transformer.IGeoTransformer#transform(org.kalypsodeegree.model.geometry.GM_Envelope)
   */
  @Override
  public GM_Envelope transform( GM_Envelope envelope ) throws Exception
  {
    if( envelope == null )
      return null;

    String sourceCRS = envelope.getCoordinateSystem();
    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( m_targetCRS ) )
      return envelope;

    GM_Position min = transform( envelope.getMin(), sourceCRS );
    GM_Position max = transform( envelope.getMax(), sourceCRS );

    return GeometryFactory.createGM_Envelope( min, max, m_targetCRS );
  }

  /**
   * @see org.kalypso.transformation.transformer.IGeoTransformer#transform(org.kalypsodeegree.model.geometry.GM_SurfacePatch)
   */
  @Override
  public GM_SurfacePatch transform( GM_SurfacePatch surfacePatch ) throws Exception
  {
    if( surfacePatch == null )
      return null;

    String sourceCRS = surfacePatch.getCoordinateSystem();
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
  private GM_Point transform( GM_Point point, String sourceCRS, String targetCRS ) throws Exception
  {
    /* If the coordinate systems are the same, do not transform. */
    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( targetCRS ) )
      return point;

    /* Get the coordinate systems. */
    CoordinateReferenceSystem sourceCoordinateSystem = CRS.decode( sourceCRS );
    CoordinateReferenceSystem targetCoordinateSystem = CRS.decode( targetCRS );

    /* Get the transformation. */
    MathTransform transformation = CRS.findMathTransform( sourceCoordinateSystem, targetCoordinateSystem );

    /* Debug. */
    Debug.TRANSFORM.printf( "POS: %s to %s\n", sourceCRS, targetCRS );

    /* Create the source position. */
    GeneralDirectPosition sourcePt = new GeneralDirectPosition( sourceCoordinateSystem );
    sourcePt.setOrdinate( 0, point.getX() );
    sourcePt.setOrdinate( 1, point.getY() );
    if( sourcePt.getDimension() == 3 )
      sourcePt.setOrdinate( 2, point.getZ() );

    /* Create the target position. */
    GeneralDirectPosition targetPt = new GeneralDirectPosition( targetCoordinateSystem );

    /* Transform. */
    transformation.transform( sourcePt, targetPt );

    return GeometryFactory.createGM_Point( targetPt.getOrdinate( 0 ), targetPt.getOrdinate( 1 ), (targetPt.getDimension() == 3) ? targetPt.getOrdinate( 2 ) : point.getZ(), targetCRS );
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
  private GM_Position transform( GM_Position pos, String sourceCRS, String targetCRS ) throws Exception
  {
    /* If the coordinate systems are the same, do not transform. */
    if( sourceCRS == null || sourceCRS.equalsIgnoreCase( targetCRS ) )
      return pos;

    /* Get the coordinate systems. */
    CoordinateReferenceSystem sourceCoordinateSystem = CRS.decode( sourceCRS );
    CoordinateReferenceSystem targetCoordinateSystem = CRS.decode( targetCRS );

    /* Get the transformation. */
    MathTransform transformation = CRS.findMathTransform( sourceCoordinateSystem, targetCoordinateSystem );

    /* Debug. */
    Debug.TRANSFORM.printf( "POS: %s to %s\n", sourceCRS, targetCRS );

    /* Create the source position. */
    GeneralDirectPosition sourcePt = new GeneralDirectPosition( sourceCoordinateSystem );
    sourcePt.setOrdinate( 0, pos.getX() );
    sourcePt.setOrdinate( 1, pos.getY() );
    if( sourcePt.getDimension() == 3 )
      sourcePt.setOrdinate( 2, pos.getZ() );

    /* Create the target position. */
    GeneralDirectPosition targetPt = new GeneralDirectPosition( targetCoordinateSystem );

    /* Transform. */
    transformation.transform( sourcePt, targetPt );

    return GeometryFactory.createGM_Position( targetPt.getOrdinate( 0 ), targetPt.getOrdinate( 1 ), (targetPt.getDimension() == 3) ? targetPt.getOrdinate( 2 ) : pos.getZ() );
  }
}