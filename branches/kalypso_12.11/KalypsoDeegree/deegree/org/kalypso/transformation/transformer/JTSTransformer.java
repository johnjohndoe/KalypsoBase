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
package org.kalypso.transformation.transformer;

import org.apache.commons.lang3.ObjectUtils;
import org.geotools.geometry.GeneralDirectPosition;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Helper that transforms JTS coordinates, based on SRID's.
 * 
 * @author Gernot Belger
 */
public class JTSTransformer
{
  private final int m_targetSRID;

  private MathTransform m_transformation;

  private GeneralDirectPosition m_sourcePt;

  private GeneralDirectPosition m_targetPt;

  public JTSTransformer( final int sourceSRID, final int targetSRID ) throws FactoryException
  {
    this( JTSAdapter.toSrs( sourceSRID ), JTSAdapter.toSrs( targetSRID ) );
  }

  public JTSTransformer( final String sourceCRS, final String targetCRS ) throws FactoryException
  {
    try
    {
      m_targetSRID = JTSAdapter.toSrid( targetCRS );

      final CoordinateReferenceSystem sourceCoordinateSystem = GeoTransformerFactory.CRS_CACHE.getCRS( sourceCRS );
      final CoordinateReferenceSystem targetCoordinateSystem = GeoTransformerFactory.CRS_CACHE.getCRS( targetCRS );

      if( ObjectUtils.equals( sourceCRS, targetCRS ) )
      {
        m_transformation = null;
        m_sourcePt = null;
        m_targetPt = null;
      }
      else
      {
        m_transformation = GeoTransformerFactory.CRS_CACHE.getTransform( sourceCRS, targetCRS );
        m_sourcePt = new GeneralDirectPosition( sourceCoordinateSystem );
        m_targetPt = new GeneralDirectPosition( targetCoordinateSystem );
      }
    }
    catch( final GeoTransformerException e )
    {
      throw new FactoryException( e );
    }
  }

  public synchronized Coordinate transform( final Coordinate sourceCrd ) throws TransformException
  {
    if( sourceCrd == null )
      return null;

    if( m_transformation == null )
      return sourceCrd;

    m_sourcePt.setOrdinate( 0, sourceCrd.x );
    m_sourcePt.setOrdinate( 1, sourceCrd.y );
    if( m_sourcePt.getDimension() == 3 )
      m_sourcePt.setOrdinate( 2, sourceCrd.z );

    m_transformation.transform( m_sourcePt, m_targetPt );

    final double targetZ = m_targetPt.getDimension() == 3 ? m_targetPt.getOrdinate( 2 ) : sourceCrd.z;
    return new Coordinate( m_targetPt.getOrdinate( 0 ), m_targetPt.getOrdinate( 1 ), targetZ );
  }

  public Coordinate[] transform( final Coordinate[] coordinates ) throws TransformException
  {
    if( coordinates == null )
      return null;

    final Coordinate[] transformedCoordinates = new Coordinate[coordinates.length];

    for( int i = 0; i < transformedCoordinates.length; i++ )
      transformedCoordinates[i] = transform( coordinates[i] );

    return transformedCoordinates;
  }

  public int getTargetSRID( )
  {
    return m_targetSRID;
  }

  public <G extends Geometry> G transform( final G geometry ) throws TransformException
  {
    if( geometry == null )
      return null;

    final Geometry transformed = transformRaw( geometry );

    // Factory sets srid to source id, so we need to change it now
    transformed.setSRID( getTargetSRID() );

    final Class<G> type = (Class<G>)geometry.getClass();
    return type.cast( transformed );
  }

  private Geometry transformRaw( final Geometry geometry ) throws TransformException
  {
    if( geometry instanceof Point )
      return transformPoint( (Point)geometry );

    if( geometry instanceof LinearRing )
      return transformLinearRing( (LinearRing)geometry );

    if( geometry instanceof Polygon )
      return transformPolygon( (Polygon)geometry );

    throw new UnsupportedOperationException();
  }

  private Polygon transformPolygon( final Polygon polygon ) throws TransformException
  {
    if( polygon == null )
      return null;

    /* transform shell */
    final LinearRing transformedShell = transformLinearRing( (LinearRing)polygon.getExteriorRing() );

    /* transform interior rings */
    final int numInteriorRing = polygon.getNumInteriorRing();
    final LinearRing[] transformedHoles = new LinearRing[numInteriorRing];
    for( int i = 0; i < transformedHoles.length; i++ )
      transformedHoles[i] = transformLinearRing( (LinearRing)polygon.getInteriorRingN( i ) );

    /* create new geometry */
    final GeometryFactory factory = polygon.getFactory();
    final Polygon transformedPolygone = factory.createPolygon( transformedShell, transformedHoles );

    // Factory sets srid to source id, so we need to change it now
    transformedPolygone.setSRID( getTargetSRID() );

    return transformedPolygone;
  }

  private LinearRing transformLinearRing( final LinearRing ring ) throws TransformException
  {
    if( ring == null )
      return null;

    final Coordinate[] coordinates = ring.getCoordinates();
    final Coordinate[] transformedCoordinates = transform( coordinates );

    /* create new geometry */
    final GeometryFactory factory = ring.getFactory();
    final LinearRing transformedRing = factory.createLinearRing( transformedCoordinates );

    // Factory sets srid to source id, so we need to change it now
    transformedRing.setSRID( getTargetSRID() );

    return transformedRing;
  }

  private Point transformPoint( final Point point ) throws TransformException
  {
    if( point == null )
      return null;

    final Coordinate transformed = transform( point.getCoordinate() );

    final Point transformedPoint = point.getFactory().createPoint( transformed );

    // Factory sets srid to source id, so we need to change it now
    transformedPoint.setSRID( getTargetSRID() );

    return transformedPoint;
  }
}