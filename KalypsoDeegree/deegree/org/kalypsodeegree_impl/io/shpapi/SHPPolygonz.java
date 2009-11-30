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

import org.kalypsodeegree.model.geometry.ByteUtils;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_SurfacePatch;

/**
 * Class representing a three dimensional ESRI Polygonz <BR>
 * 
 * @version 26.01.2007
 * @author Thomas Jung
 */

public class SHPPolygonz implements ISHPParts
{
  private final SHPPolyLinez m_rings;

  /**
   * constructor: recieves a stream <BR>
   */
  public SHPPolygonz( final byte[] recBuf )
  {
    m_rings = new SHPPolyLinez( recBuf );
  }

  /**
   * constructor: recieves an array of arrays of GM_Points <BR>
   */
  public SHPPolygonz( final GM_SurfacePatch[] surfacePatch )
  {
    final String crs = surfacePatch[0].getCoordinateSystem();
    final GM_Curve[] curves = SHPPolygon.orientCurves( surfacePatch, crs );

    m_rings = new SHPPolyLinez( curves );
  }

  /**
   * method: writeSHPPolygonz(byte[] bytearray, int start) <BR>
   */
  public byte[] writeShape( )
  {
    if( m_rings == null )
      return null;

    int offset = ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH;
    final byte[] bytearray = new byte[offset + size()];

    int byteposition;

    final ISHPPoint[][] pointsZ = m_rings.getPoints();

    final ISHPPoint point = pointsZ[0][0];

    double xmin = point.getX();
    double xmax = point.getX();
    double ymin = point.getY();
    double ymax = point.getY();
    double zmin = point.getZ();
    double zmax = point.getZ();
    // write shape type identifier
    ByteUtils.writeLEInt( bytearray, offset, ShapeConst.SHAPE_TYPE_POLYGONZ );

    offset += 4;
    // save offset of the bounding box
    final int tmp1 = offset;

    // increment offset with size of the bounding box
    offset += (4 * 8);

    // write numRings
    ByteUtils.writeLEInt( bytearray, offset, m_rings.getNumParts() );
    offset += 4;
    // write numpoints
    ByteUtils.writeLEInt( bytearray, offset, getNumPoints() );
    offset += 4;

    // save offset of the list of offsets for each polyline
    int tmp2 = offset;

    // increment offset with numRings
    offset += (4 * m_rings.getNumParts());

    int count = 0;
    for( final ISHPPoint[] element : pointsZ )
    {
      // stores the index of the i'th part
      ByteUtils.writeLEInt( bytearray, tmp2, count );
      tmp2 += 4;

      // write the points of the i'th part and calculate bounding box
      for( final ISHPPoint element2 : element )
      {
        // number of the current point
        count++;

        // calculate bounding box
        if( element2.getX() > xmax )
        {
          xmax = element2.getX();
        }
        else if( element2.getX() < xmin )
        {
          xmin = element2.getX();
        }

        if( element2.getY() > ymax )
        {
          ymax = element2.getY();
        }
        else if( element2.getY() < ymin )
        {
          ymin = element2.getY();
        }

        if( element2.getZ() > zmax )
        {
          zmax = element2.getZ();
        }
        else if( element2.getZ() < zmin )
        {
          zmin = element2.getZ();
        }

        // write x-coordinate
        ByteUtils.writeLEDouble( bytearray, offset, element2.getX() );
        offset += 8;

        // write y-coordinate
        ByteUtils.writeLEDouble( bytearray, offset, element2.getY() );
        offset += 8;

        // write z-coordinate
        // jump to the z-values
        byteposition = ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH + 44 + (4 * m_rings.getNumParts()) + (m_rings.getNumPoints() * 16) + 16 + ((count - 1) * 8);
        ByteUtils.writeLEDouble( bytearray, byteposition, element2.getZ() );
      }
    }
    // jump back to the offset of the bounding box
    offset = tmp1;

    // write bounding box to the byte array
    ByteUtils.writeLEDouble( bytearray, offset, xmin );
    offset += 8;
    ByteUtils.writeLEDouble( bytearray, offset, ymin );
    offset += 8;
    ByteUtils.writeLEDouble( bytearray, offset, xmax );
    offset += 8;
    ByteUtils.writeLEDouble( bytearray, offset, ymax );

    // write z-range
    // jump to the z-range byte postition
    byteposition = ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH + 44 + (4 * getNumParts()) + (getNumPoints() * 16);
    // write z-range to the byte array
    ByteUtils.writeLEDouble( bytearray, byteposition, zmin );
    offset += 8;
    ByteUtils.writeLEDouble( bytearray, byteposition, zmax );

    return bytearray;
  }

  /**
   * returns the polygonz shape size in bytes <BR>
   */
  public int size( )
  {
    return 44 + getNumParts() * 4 + getNumPoints() * 16 + 16 + (8 * getNumPoints());
  }

  @Override
  public String toString( )
  {
    return "WKBPOLYGON" + " numRings: " + m_rings.getNumParts();
  }

  public SHPEnvelope getEnvelope( )
  {
    return m_rings.getEnvelope();
  }

  public int getNumParts( )
  {
    return m_rings.getNumParts();
  }

  public int getNumPoints( )
  {
    return m_rings.getNumPoints();
  }

  /**
   * @see org.kalypsodeegree_impl.io.shpapi.ISHPParts#getPoints()
   */
  @Override
  public ISHPPoint[][] getPoints( )
  {
    return m_rings.getPoints();
  }

  public SHPPolyLinez getRings( )
  {
    return m_rings;
  }

  public SHPZRange getZrange( )
  {
    return m_rings.getZrange();
  }

}