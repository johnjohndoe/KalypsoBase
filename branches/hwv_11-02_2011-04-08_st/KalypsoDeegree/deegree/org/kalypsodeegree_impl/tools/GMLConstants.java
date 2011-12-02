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
package org.kalypsodeegree_impl.tools;

import javax.xml.namespace.QName;

import org.kalypso.commons.xml.NS;

/** 
 * 
 * @author Felipe Maximino
 *
 */
public final class GMLConstants
{
  public static final QName QN_GEOMETRY = new QName( NS.GML3, "_Geometry" );

  public static final QName QN_SURFACE = new QName( NS.GML3, "_Surface" );

  public static final QName QN_POLYGON = new QName( NS.GML3, "Polygon" );

  public static final QName QN_POINT = new QName( NS.GML3, "Point" );

  public static final QName QN_LINE_STRING = new QName( NS.GML3, "LineString" );

  public static final QName QN_CURVE = new QName( NS.GML3, "_Curve" );

  public static final QName QN_MULTI_POINT = new QName( NS.GML3, "MultiPoint" );

  public static final QName QN_POINT_MEMBER = new QName( NS.GML3, "pointMember" );

  public static final QName QN_POINT_MEMBERS = new QName( NS.GML3, "pointMembers" );

  public static final QName QN_POLYGON_MEMBER = new QName( NS.GML3, "polygonMember" );

  public static final QName QN_MULTI_LINE_STRING = new QName( NS.GML3, "MultiLineString" );

  public static final QName QN_MULTI_CURVE = new QName( NS.GML3, "MultiCurve" );

  public static final QName QN_MULTI_POLYGON = new QName( NS.GML3, "MultiPolygon" );


  public static final QName QN_LOCATION = new QName( NS.GML3, "location" );

  public static final QName QN_DIRECTION = new QName( NS.GML3, "direction" );

  public static final QName QN_POS = new QName( NS.GML3, "pos" );

  public static final QName QN_POS_LIST = new QName( NS.GML3, "posList" );

  public static final QName QN_COORDINATES = new QName( NS.GML3, "coordinates" );

  public static final QName QN_COORD = new QName( NS.GML3, "coord" );

  public static final String DEFAULT_CS = ",";

  public static final String DEFAULT_TS = " ";

  public static final String DEFAULT_DECIMAL = ".";

  public static final QName QN_LINEAR_RING = new QName( NS.GML3, "LinearRing" );

}
