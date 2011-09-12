/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 * 
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always. 
 * 
 * If you intend to use this software in other ways than in kalypso 
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree, 
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */

package org.kalypsodeegree_impl.io.shpapi;

import java.util.LinkedList;
import java.util.List;

import org.kalypsodeegree.model.geometry.ByteUtils;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_CurveSegment;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_SurfacePatch;
import org.kalypsodeegree_impl.model.geometry.GM_PositionOrientation;
import org.kalypsodeegree_impl.model.geometry.GM_PositionOrientation.TYPE;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/**
 * Class representing a two dimensional ESRI Polygon <BR>
 * 
 * @version 16.08.2000
 * @author Andreas Poth
 */

public class SHPPolygon implements ISHPParts
{
  private final SHPPolyLine m_rings;

  /**
   * constructor: recieves a stream <BR>
   */
  public SHPPolygon( final byte[] recBuf )
  {
    m_rings = new SHPPolyLine( recBuf );
  }

  /**
   * constructor: recieves an array of arrays of GM_Points <BR>
   */
  public SHPPolygon( final GM_SurfacePatch[] surfacePatch )
  {
    final String crs = surfacePatch[0].getCoordinateSystem();
    
    final GM_Curve[] curves = orientCurves( surfacePatch, crs );

    m_rings = new SHPPolyLine( curves );
  }

  static GM_Curve[] orientCurves( final GM_SurfacePatch[] surfacePatch, final String crs )
  {
    final List<GM_Curve> curveList = new LinkedList<GM_Curve>();
    for( final GM_SurfacePatch element : surfacePatch )
    {
      try
      {
        final GM_Position[] exteriorRing = element.getExteriorRing();
        // TODO: real necessary? why not also force positive orientation for interior rings below?
        final GM_Position[] positions = GM_PositionOrientation.orient( exteriorRing, TYPE.NEGATIV );

        final GM_CurveSegment cs = GeometryFactory.createGM_CurveSegment( positions, crs );
        curveList.add( GeometryFactory.createGM_Curve( cs ) );

        final GM_Position[][] interiorRings = element.getInteriorRings();
        if( interiorRings != null )
        {
          final GM_Curve[] rings = GeometryFactory.createGM_Curve( interiorRings, crs );
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

  /**
   * method: writeSHPPolygon(byte[] bytearray, int start) <BR>
   */
  @Override
  public byte[] writeShape( )
  {
    int offset = ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH;
    final byte[] byteArray = new byte[offset + size()];

    final ISHPPoint[][] points = m_rings.getPoints();
    double xmin = points[0][0].getX();
    double xmax = points[0][0].getX();
    double ymin = points[0][0].getY();
    double ymax = points[0][0].getY();

    // write shape type identifier
    ByteUtils.writeLEInt( byteArray, offset, ShapeConst.SHAPE_TYPE_POLYGON );

    offset += 4;
    // save offset of the bounding box
    final int tmp1 = offset;

    // increment offset with size of the bounding box
    offset += (4 * 8);

    // write numRings
    ByteUtils.writeLEInt( byteArray, offset, getNumParts() );
    offset += 4;
    // write numpoints
    ByteUtils.writeLEInt( byteArray, offset, getNumPoints() );
    offset += 4;

    // save offset of the list of offsets for each polyline
    int tmp2 = offset;

    // increment offset with numRings
    offset += (4 * getNumParts());

    int count = 0;
    for( final ISHPPoint[] point : points )
    {
      // stores the index of the i'th part
      ByteUtils.writeLEInt( byteArray, tmp2, count );
      tmp2 += 4;

      // write the points of the i'th part and calculate bounding box
      for( final ISHPPoint element : point )
      {
        // number of the current point
        count++;

        // calculate bounding box
        if( element.getX() > xmax )
        {
          xmax = element.getX();
        }
        else if( element.getX() < xmin )
        {
          xmin = element.getX();
        }

        if( element.getY() > ymax )
        {
          ymax = element.getY();
        }
        else if( element.getY() < ymin )
        {
          ymin = element.getY();
        }

        // write x-coordinate
        ByteUtils.writeLEDouble( byteArray, offset, element.getX() );
        offset += 8;

        // write y-coordinate
        ByteUtils.writeLEDouble( byteArray, offset, element.getY() );
        offset += 8;
      }
    }

    // jump back to the offset of the bounding box
    offset = tmp1;

    // write bounding box to the byte array
    ByteUtils.writeLEDouble( byteArray, offset, xmin );
    offset += 8;
    ByteUtils.writeLEDouble( byteArray, offset, ymin );
    offset += 8;
    ByteUtils.writeLEDouble( byteArray, offset, xmax );
    offset += 8;
    ByteUtils.writeLEDouble( byteArray, offset, ymax );

    return byteArray;
  }

  /**
   * returns the polygon shape size in bytes <BR>
   */
  @Override
  public int size( )
  {
    return 44 + getNumParts() * 4 + getNumPoints() * 16;
  }

  @Override
  public String toString( )
  {
    return "WKBPOLYGON" + " numRings: " + getNumParts();
  }

  @Override
  public SHPEnvelope getEnvelope( )
  {
    return m_rings.getEnvelope();
  }

  @Override
  public int getNumParts( )
  {
    return m_rings.getNumParts();
  }

  @Override
  public int getNumPoints( )
  {
    return m_rings.getNumPoints();
  }

  public SHPPolyLine getRings( )
  {
    return m_rings;
  }

  /**
   * @see org.kalypsodeegree_impl.io.shpapi.ISHPParts#getPoints()
   */
  @Override
  public ISHPPoint[][] getPoints( )
  {
    return m_rings.getPoints();
  }
}