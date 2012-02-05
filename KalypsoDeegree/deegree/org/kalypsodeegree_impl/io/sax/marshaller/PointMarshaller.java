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

import org.kalypsodeegree.model.geometry.GM_Point;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A marshaller for gml:Point
 * <p>
 * The point positions are marshalled as a gml:pos element. The default values for the gml:coordinates are: tuple
 * separator: " " coordinate separator: "," decimal indicator: "." These values can be changed by the set methods:
 * {@link setCoordinatesSeparator} {@link setTuplesSeparator} {@link setDecimalIndicator}
 * 
 * @author Felipe Maximino
 */
public class PointMarshaller extends GeometryMarshaller<GM_Point>
{
  private static final String TAG_POINT = "Point";

  private final PosMarshaller m_posMarshaller;

  public PointMarshaller( final XMLReader reader )
  {
    this( reader, null );
  }

  public PointMarshaller( final XMLReader reader, final GM_Point point )
  {
    super( reader, TAG_POINT, point );

    m_posMarshaller = new PosMarshaller( reader );
  }

  @Override
  protected void doMarshallContent( final GM_Point marshalledObject ) throws SAXException
  {
    m_posMarshaller.setPosition( marshalledObject.getPosition() );
    m_posMarshaller.marshall();
  }

  public void setPoint( final GM_Point point )
  {
    setMarshalledObject( point );
  }
}
