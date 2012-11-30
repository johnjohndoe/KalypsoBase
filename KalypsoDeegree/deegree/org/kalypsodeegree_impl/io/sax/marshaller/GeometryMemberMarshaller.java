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
package org.kalypsodeegree_impl.io.sax.marshaller;

import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_MultiCurve;
import org.kalypsodeegree.model.geometry.GM_MultiGeometry;
import org.kalypsodeegree.model.geometry.GM_MultiPoint;
import org.kalypsodeegree.model.geometry.GM_MultiSurface;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_PolyhedralSurface;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A marshaller for gml:curveMember. It delegates the marshalling to the gml:LineString marshaller.
 *
 * @author Gernot Belger
 */
public class GeometryMemberMarshaller extends AbstractMarshaller<GM_Object>
{
  public GeometryMemberMarshaller( final XMLReader reader )
  {
    super( reader, GM_MultiGeometry.MEMBER_GEOMETRY.getLocalPart() );
  }

  @Override
  protected void doMarshallContent( final GM_Object marshalledObject ) throws SAXException
  {
    @SuppressWarnings("unchecked")
    final GeometryMarshaller<GM_Object> marshaller = (GeometryMarshaller<GM_Object>) createMarshaller( getXMLReader(), marshalledObject );
    marshaller.marshall( marshalledObject );
  }

  public static GeometryMarshaller< ? extends GM_Object> createMarshaller( final XMLReader xmlReader, final GM_Object marshalledObject )
  {
    if( marshalledObject instanceof GM_Point )
      return new PointMarshaller( xmlReader );

    if( marshalledObject instanceof GM_Curve )
      return new LineStringMarshaller( xmlReader );

    if( marshalledObject instanceof GM_TriangulatedSurface )
      return new TriangulatedSurfaceMarshaller( xmlReader, marshalledObject.getCoordinateSystem() );

    if( marshalledObject instanceof GM_PolyhedralSurface )
      return new PolyhedralSurfaceMarshaller( xmlReader );

    if( marshalledObject instanceof GM_Polygon )
      return new PolygonMarshaller( xmlReader );

    if( marshalledObject instanceof GM_MultiGeometry )
      return new MultiGeometryMarshaller( xmlReader );

    if( marshalledObject instanceof GM_MultiPoint )
      return new MultiPointMarshaller( xmlReader );

    if( marshalledObject instanceof GM_MultiCurve )
      return new MultiCurveMarshaller( xmlReader );

    if( marshalledObject instanceof GM_MultiSurface )
      return new MultiSurfaceMarshaller( xmlReader );

    final String message = String.format( "Unable to serialize geometry in MultiGeometry: %s", marshalledObject );
    throw new UnsupportedOperationException( message );
  }
}