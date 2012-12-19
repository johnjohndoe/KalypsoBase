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
package org.kalypso.shape.deegree;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.kalypso.shape.ShapeDataException;
import org.kalypso.shape.ShapeType;
import org.kalypso.shape.geometry.ISHPGeometry;
import org.kalypso.shape.geometry.SHPNullShape;
import org.kalypso.shape.geometry.SHPPoint;
import org.kalypso.shape.geometry.SHPPointm;
import org.kalypso.shape.geometry.SHPPointz;
import org.kalypso.shape.tools.JTS2SHP;
import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.model.geometry.GM_AbstractSurfacePatch;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * @author Gernot Belger
 */
public class GM_Object2Shape
{
  private final ShapeType m_shapeType;

  private final IGeoTransformer m_transformer;

  public GM_Object2Shape( final ShapeType shapeType, final String coordinateSystem )
  {
    m_shapeType = shapeType;
    m_transformer = GeoTransformerFactory.getGeoTransformer( coordinateSystem );
  }

  public ISHPGeometry convert( final GM_Object geom ) throws ShapeDataException
  {
    if( geom == null )
      return new SHPNullShape();

    final GM_Object transformedGeom = getTransformedGeom( geom );

    switch( m_shapeType )
    {
      case NULL:
        return new SHPNullShape();

      case POINT:
      {
        final GM_Point point = (GM_Point)transformedGeom.getAdapter( GM_Point.class );
        if( point == null )
          return null;
        else
          return new SHPPoint( point.getX(), point.getY() );
      }

      case POLYLINE:
      {
        final GM_Curve[] curves = (GM_Curve[])transformedGeom.getAdapter( GM_Curve[].class );
        if( ArrayUtils.isEmpty( curves ) )
          return new SHPNullShape();

        final Coordinate[][] lines = asLines( curves );

        return JTS2SHP.toPolyline( lines );
      }

      case POLYGON:
      {
        final GM_AbstractSurfacePatch[] surfacePatches = (GM_AbstractSurfacePatch[])transformedGeom.getAdapter( GM_AbstractSurfacePatch[].class );
        if( ArrayUtils.isEmpty( surfacePatches ) )
          return new SHPNullShape();

        final Coordinate[][] curves = orientCurves( surfacePatches );
        if( ArrayUtils.isEmpty( curves ) )
          return new SHPNullShape();

        return JTS2SHP.toPolygon( curves );
      }

      case POINTZ:
      {
        final GM_Point point = (GM_Point)transformedGeom.getAdapter( GM_Point.class );
        if( point == null )
          return null;
        else
          return new SHPPointz( point.getX(), point.getY(), point.getZ(), Double.NaN );
      }

      case POLYLINEZ:
      {
        final GM_Curve[] curves = (GM_Curve[])transformedGeom.getAdapter( GM_Curve[].class );
        if( ArrayUtils.isEmpty( curves ) )
          return new SHPNullShape();

        final Coordinate[][] lines = asLines( curves );

        return JTS2SHP.toPolylineZ( lines );
      }

      case POLYGONZ:
      {
        final GM_AbstractSurfacePatch[] surfacePatches = (GM_AbstractSurfacePatch[])transformedGeom.getAdapter( GM_AbstractSurfacePatch[].class );
        if( ArrayUtils.isEmpty( surfacePatches ) )
          return new SHPNullShape();

        final Coordinate[][] curves = orientCurves( surfacePatches );
        return JTS2SHP.toPolygonZ( curves );
      }

      case MULTIPOINT:
        // TODO

        // /**
        // * constructor: recieves an array of gm_points
        // */
        // public SHPMultiPoint( final GM_MultiPoint multipoint )
        // {
        // double xmin = multipoint.getEnvelope().getMin().getX();
        // double xmax = multipoint.getEnvelope().getMax().getX();
        // double ymin = multipoint.getEnvelope().getMin().getY();
        // double ymax = multipoint.getEnvelope().getMax().getY();
        //
        // m_points = new SHPPoint[multipoint.getSize()];
        // for( int i = 0; i < multipoint.getSize(); i++ )
        // {
        // m_points[i] = new SHPPoint( multipoint.getPointAt( i ) );
        // if( m_points[i].getX() > xmax )
        // {
        // xmax = m_points[i].getX();
        // }
        // else if( m_points[i].getX() < xmin )
        // {
        // xmin = m_points[i].getX();
        // }
        // if( m_points[i].getY() > ymax )
        // {
        // ymax = m_points[i].getY();
        // }
        // else if( m_points[i].getY() < ymin )
        // {
        // ymin = m_points[i].getY();
        // }
        // }
        //
        // m_envelope = new SHPEnvelope( xmin, xmax, ymax, ymin );
        // }
        break;

      case MULTIPOINTZ:
        // TODO

        // /**
        // * constructor: recieves an array of gm_points
        // */
        // public SHPMultiPointz( final GM_MultiPoint multipointz )
        // {
        // double xmin = multipointz.getEnvelope().getMin().getX();
        // double xmax = multipointz.getEnvelope().getMax().getX();
        // double ymin = multipointz.getEnvelope().getMin().getY();
        // double ymax = multipointz.getEnvelope().getMax().getY();
        // double zmin = multipointz.getEnvelope().getMin().getZ();
        // double zmax = multipointz.getEnvelope().getMax().getZ();
        //
        // final int numPoints = multipointz.getSize();
        // m_pointsz = new SHPPointz[numPoints];
        // for( int i = 0; i < multipointz.getSize(); i++ )
        // {
        // m_pointsz[i] = new SHPPointz( multipointz.getPointAt( i ).getPosition() );
        // if( m_pointsz[i].getX() > xmax )
        // {
        // xmax = m_pointsz[i].getX();
        // }
        // else if( m_pointsz[i].getX() < xmin )
        // {
        // xmin = m_pointsz[i].getX();
        // }
        // if( m_pointsz[i].getY() > ymax )
        // {
        // ymax = m_pointsz[i].getY();
        // }
        // else if( m_pointsz[i].getY() < ymin )
        // {
        // ymin = m_pointsz[i].getY();
        // }
        // if( m_pointsz[i].getZ() > zmax )
        // {
        // zmax = m_pointsz[i].getZ();
        // }
        // else if( m_pointsz[i].getZ() < zmin )
        // {
        // zmin = m_pointsz[i].getZ();
        // }
        // }
        //
        // m_zrange = new SHPZRange( zmin, zmax );
        // m_envelope = new SHPEnvelope( xmin, xmax, ymin, ymax );
        // }
        break;

      case POINTM:
      {
        final GM_Point point = (GM_Point)transformedGeom.getAdapter( GM_Point.class );
        if( point == null )
          return null;
        else
          return new SHPPointm( point.getX(), point.getY(), point.getZ() );
      }

      case POLYLINEM:
      {
        final GM_Curve[] curves = (GM_Curve[])transformedGeom.getAdapter( GM_Curve[].class );
        if( ArrayUtils.isEmpty( curves ) )
          return new SHPNullShape();

        final Coordinate[][] lines = asLines( curves );

        return JTS2SHP.toPolylineM( lines );
      }

      case POLYGONM:
      {
        final GM_AbstractSurfacePatch[] surfacePatches = (GM_AbstractSurfacePatch[])transformedGeom.getAdapter( GM_AbstractSurfacePatch[].class );
        if( ArrayUtils.isEmpty( surfacePatches ) )
          return new SHPNullShape();

        final Coordinate[][] curves = orientCurves( surfacePatches );
        return JTS2SHP.toPolygonM( curves );
      }

      case MULTIPOINTM:
        // TODO
        break;
    }

    throw new IllegalStateException( "Unknown shape type: " + m_shapeType );
  }

  private Coordinate[][] asLines( final GM_Curve[] curves ) throws ShapeDataException
  {
    try
    {
      final Coordinate[][] lines = new Coordinate[curves.length][];
      for( int i = 0; i < lines.length; i++ )
      {
        final LineString line = (LineString)JTSAdapter.export( curves[i] );
        lines[i] = line.getCoordinates();
      }
      return lines;
    }
    catch( final GM_Exception e )
    {
      throw new ShapeDataException( "Failed to export curve", e ); //$NON-NLS-1$
    }
  }

  private GM_Object getTransformedGeom( final GM_Object geom2 ) throws ShapeDataException
  {
    try
    {
      return m_transformer.transform( geom2 );
    }
    catch( final Exception e )
    {
      throw new ShapeDataException( "Failed to project geometry", e );
    }
  }

  private static Coordinate[][] orientCurves( final GM_AbstractSurfacePatch[] surfacePatch )
  {
    final List<Coordinate[]> curveList = new LinkedList<>();
    for( final GM_AbstractSurfacePatch element : surfacePatch )
    {
      /* Outer rings are clockwise */
      final LinearRing exteriorRing = JTSAdapter.exportAsRing( element.getExteriorRing() );
      final Coordinate[] exteriorPoses = exteriorRing.getCoordinates();
      if( CGAlgorithms.isCCW( exteriorPoses ) )
        ArrayUtils.reverse( exteriorPoses );

      curveList.add( exteriorPoses );

      final GM_Position[][] interiorRings = element.getInteriorRings();
      if( interiorRings != null )
      {
        for( final GM_Position[] interiorRing : interiorRings )
        {
          final LinearRing interiorJTSRing = JTSAdapter.exportAsRing( interiorRing );
          final Coordinate[] interiorPoses = interiorJTSRing.getCoordinates();
          if( !CGAlgorithms.isCCW( interiorPoses ) )
            ArrayUtils.reverse( interiorPoses );

          curveList.add( interiorPoses );
        }
      }
    }

    return curveList.toArray( new Coordinate[curveList.size()][] );
  }

  public ShapeType getShapeType( )
  {
    return m_shapeType;
  }

  public ISHPGeometry convert( final GM_AbstractSurfacePatch patch ) throws ShapeDataException
  {
    if( patch == null )
      return new SHPNullShape();

    final GM_AbstractSurfacePatch transformedPatch = getTransformedPatch( patch );

    final GM_AbstractSurfacePatch[] patches = new GM_AbstractSurfacePatch[] { transformedPatch };

    // FIXME: do not orient curves for poyline?!

    final Coordinate[][] curves = orientCurves( patches );
    if( curves == null )
      return new SHPNullShape();

    switch( m_shapeType )
    {
      case POLYLINE:
        return JTS2SHP.toPolyline( curves );

      case POLYGON:
        return JTS2SHP.toPolygon( curves );

      case POLYLINEZ:
        return JTS2SHP.toPolylineZ( curves );

      case POLYGONZ:
        return JTS2SHP.toPolygonZ( curves );

      case POLYLINEM:
        return JTS2SHP.toPolylineM( curves );

      case POLYGONM:
        return JTS2SHP.toPolygonM( curves );

      default:
        throw new IllegalStateException( "Illegal shape type for patch: " + m_shapeType );
    }
  }

  private GM_AbstractSurfacePatch getTransformedPatch( final GM_AbstractSurfacePatch patch ) throws ShapeDataException
  {
    try
    {
      return m_transformer.transform( patch );
    }
    catch( final Exception e )
    {
      throw new ShapeDataException( "Failed to project geometry", e );
    }
  }

  public String getCoordinateSystem( )
  {
    return m_transformer.getTarget();
  }
}