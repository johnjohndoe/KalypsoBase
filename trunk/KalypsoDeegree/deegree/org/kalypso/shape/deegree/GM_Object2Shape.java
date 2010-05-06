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

import org.kalypso.shape.ShapeConst;
import org.kalypso.shape.geometry.ISHPGeometry;
import org.kalypso.shape.geometry.SHPNullShape;
import org.kalypso.shape.geometry.SHPPoint;
import org.kalypso.shape.geometry.SHPPointz;
import org.kalypso.shape.geometry.SHPPolyLine;
import org.kalypso.shape.geometry.SHPPolyLinez;
import org.kalypso.shape.geometry.SHPPolygon;
import org.kalypso.shape.geometry.SHPPolygonz;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_CurveSegment;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_SurfacePatch;
import org.kalypsodeegree_impl.model.geometry.GM_PositionOrientation;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.kalypsodeegree_impl.model.geometry.GM_PositionOrientation.TYPE;

/**
 * @author Gernot Belger
 */
public class GM_Object2Shape
{
  private final byte m_shapeType;

  public GM_Object2Shape( final byte shapeType )
  {
    m_shapeType = shapeType;
  }

  public ISHPGeometry convert( final GM_Object geom )
  {
    if( geom == null )
      return new SHPNullShape();

    switch( m_shapeType )
    {
      case ShapeConst.SHAPE_TYPE_NULL:
        return new SHPNullShape();

      case ShapeConst.SHAPE_TYPE_POINT:
      {
        final GM_Point point = (GM_Point) geom.getAdapter( GM_Point.class );
        if( point == null )
          return null;
        else
          return new SHPPoint( point );
      }

      case ShapeConst.SHAPE_TYPE_POLYLINE:
      {
        final GM_Curve[] curves = (GM_Curve[]) geom.getAdapter( GM_Curve[].class );
        if( curves == null )
          return null;
        else
          return new SHPPolyLine( curves );
      }

      case ShapeConst.SHAPE_TYPE_POLYGON:
      {
        final GM_SurfacePatch[] surfacePatches = (GM_SurfacePatch[]) geom.getAdapter( GM_SurfacePatch[].class );
        if( surfacePatches == null )
          return null;
        else
        {
          final GM_Curve[] curves = orientCurves( surfacePatches );
          return new SHPPolygon( new SHPPolyLine( curves ) );
        }
      }

      case ShapeConst.SHAPE_TYPE_POINTZ:
      {
        final GM_Point point = (GM_Point) geom.getAdapter( GM_Point.class );
        if( point == null )
          return null;
        else
          return new SHPPointz( point.getX(), point.getY(), point.getZ(), 0.0 );
      }

      case ShapeConst.SHAPE_TYPE_POLYLINEZ:
      {
        final GM_Curve[] curves = (GM_Curve[]) geom.getAdapter( GM_Curve[].class );
        if( curves == null )
          return null;
        else
          return new SHPPolyLinez( curves );
      }

      case ShapeConst.SHAPE_TYPE_POLYGONZ:
      {
        final GM_SurfacePatch[] surfacePatches = (GM_SurfacePatch[]) geom.getAdapter( GM_SurfacePatch[].class );
        if( surfacePatches == null )
          return null;
        else
        {
          final GM_Curve[] curves = orientCurves( surfacePatches );
          return new SHPPolygonz( new SHPPolyLinez( curves ) );
        }
      }

      case ShapeConst.SHAPE_TYPE_MULTIPOINT:
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

      case ShapeConst.SHAPE_TYPE_MULTIPOINTZ:
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
    }

    throw new IllegalStateException( "Unknown shape type: " + m_shapeType );
  }

  private static GM_Curve[] orientCurves( final GM_SurfacePatch[] surfacePatch )
  {
    final List<GM_Curve> curveList = new LinkedList<GM_Curve>();
    for( final GM_SurfacePatch element : surfacePatch )
    {
      try
      {
        final GM_Position[] exteriorRing = element.getExteriorRing();
        // TODO: real necessary? why not also force positive orientation for interior rings below?
        final GM_Position[] positions = GM_PositionOrientation.orient( exteriorRing, TYPE.NEGATIV );

        final GM_CurveSegment cs = GeometryFactory.createGM_CurveSegment( positions, element.getCoordinateSystem() );
        curveList.add( GeometryFactory.createGM_Curve( cs ) );

        final GM_Position[][] interiorRings = element.getInteriorRings();
        if( interiorRings != null )
        {
          final GM_Curve[] rings = GeometryFactory.createGM_Curve( interiorRings, element.getCoordinateSystem() );
          if( rings != null )
          {
            for( final GM_Curve ring : rings )
              curveList.add( ring );
          }
        }
      }
      catch( final Exception e )
      {
        System.out.println( "SHPPolygon::" + e );
      }
    }
    return curveList.toArray( new GM_Curve[curveList.size()] );
  }

  public byte getShapeType( )
  {
    return m_shapeType;
  }

}
