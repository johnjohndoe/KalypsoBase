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

import org.kalypsodeegree.model.geometry.GM_PolygonPatch;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree_impl.model.geometry.GM_Polygon_Impl;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A marshaller for gml:Exterior. It delegates the marshalling to the corresponding gml:LinearRing elements marshallers.
 *
 * @author Felipe Maximino
 */
public class PolygonMarshaller extends GeometryMarshaller<GM_Polygon<GM_PolygonPatch>>
{
  public PolygonMarshaller( final XMLReader reader )
  {
    super( reader, GM_Polygon_Impl.POLYGON_ELEMENT.getLocalPart() );
  }

  @Override
  protected void doMarshallContent( final GM_Polygon<GM_PolygonPatch> marshalledObject ) throws SAXException
  {
    final GM_PolygonPatch polygon = marshalledObject.get( 0 );

    final GM_Position[] exteriorRing = polygon.getExteriorRing();
    new ExteriorMarshaller( getXMLReader() ).marshall( exteriorRing );

    final GM_Position[][] interiorRings = polygon.getInteriorRings();
    if( interiorRings != null )
    {
      for( final GM_Position[] interior : interiorRings )
        new InteriorMarshaller( getXMLReader() ).marshall( interior );
    }
  }
}