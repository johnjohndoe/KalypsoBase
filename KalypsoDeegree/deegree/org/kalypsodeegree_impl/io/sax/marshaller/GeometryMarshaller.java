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

import org.kalypso.commons.xml.NS;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * An abstract marshaller for Geometries.
 * <p>
 * A geometry has the characteristic to have its attributes srsDimension and srsName to be marshalled *
 * 
 * @author Felipe Maximino
 */
public abstract class GeometryMarshaller<T extends GM_Object> extends AbstractMarshaller<T>
{
  public GeometryMarshaller( final XMLReader reader, final String tag, final T object )
  {
    super( reader, tag, object );
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.marshaller.AbstractMarshaller#startMarshalling()
   */
  @Override
  protected void startMarshalling( ) throws SAXException
  {
    final String crs = getMarshalledObject().getCoordinateSystem();
    final int srsDimension = getMarshalledObject().getCoordinateDimension();

    final AttributesImpl atts = new AttributesImpl();
    if( crs != null )
      atts.addAttribute( "", "srsName", "srsName", "CDATA", crs );

    if( srsDimension != -1 )
      atts.addAttribute( "", "srsDimension", "srsDimension", "decimal", String.valueOf( srsDimension ) );

    final ContentHandler contentHandler = getXMLReader().getContentHandler();
    contentHandler.startElement( NS.GML3, getTag(), getQName(), atts );
  }
}
