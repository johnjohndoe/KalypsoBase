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

import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A marshaller for gml:polygonPatch
 * 
 * @author Felipe Maximino
 */
public class PolygonPatchMarshaller extends SurfacePatchMarshaller<GM_Polygon>
{
  private static final String TAG_POLYGON_PATCH = "PolygonPatch";

  public PolygonPatchMarshaller( final XMLReader reader )
  {
    this( reader, null );
  }

  public PolygonPatchMarshaller( final XMLReader reader, final String surfaceCrs )
  {
    super( reader, TAG_POLYGON_PATCH, surfaceCrs );
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.marshaller.AbstractMarshaller#doMarshall(java.lang.Object)
   */
  @Override
  protected void doMarshallContent( final GM_Polygon marshalledObject ) throws SAXException
  {
    final GM_Position[] exteriorRing = marshalledObject.getExteriorRing();
    new ExteriorMarshaller( getXMLReader(), exteriorRing ).marshall();

    final GM_Position[][] interiorRings = marshalledObject.getInteriorRings();
    if( interiorRings != null )
    {
      for( final GM_Position[] interior : interiorRings )
        new InteriorMarshaller( getXMLReader(), interior ).marshall();
    }
  }
}
