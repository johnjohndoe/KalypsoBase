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
package org.kalypso.shape;

/**
 * Availabel shape types of a shape file.
 * 
 * @author Gernot Belger
 */
public enum ShapeType
{
  NULL(0, "Null"),
  POINT(1, "Point"),
  POLYLINE(3, "PolyLine"),
  POLYGON(5, "Polygon"),
  MULTIPOINT(8, "MultiPoint"),
  POINTZ(11, "PointZ"),
  POLYLINEZ(13, "PolyLineZ"),
  POLYGONZ(15, "PolygonZ"),
  MULTIPOINTZ(18, "MultiPointZ");

// "PointM";
// "PolyLineM";
// "PolygonM";
// "MultiPointM";
// "MultiPatch";

  private final int m_shpValue;

  private final String m_label;

  private ShapeType( final int shpValue, final String label )
  {
    m_shpValue = shpValue;
    m_label = label;
  }

  public int getType( )
  {
    return m_shpValue;
  }

  public String getLabel( )
  {
    return m_label;
  }

  /**
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString( )
  {
    return String.format( "%s (%d)", m_label, m_shpValue );
  }

  public static ShapeType valueOf( final int type )
  {
    final ShapeType[] values = values();
    for( final ShapeType shapeType : values )
    {
      if( shapeType.getType() == type )
        return shapeType;
    }

    throw new IllegalArgumentException( String.format( "Unknown shape type %s", type ) );
  }
}
