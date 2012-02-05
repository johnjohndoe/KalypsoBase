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

import org.kalypsodeegree.model.geometry.GM_Position;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * FIXME: Should marshall GM_Ring's instead of GM_Position[]s<br/>
 * A marshaller for gml:LinearRing. It delegates the marshalling to the corresponding gml:pos elements marshallers.
 *
 * @author Felipe Maximino
 */
public class LinearRingMarshaller extends AbstractMarshaller<GM_Position[]>
{
  public static final String TAG_LINEAR_RING = "LinearRing"; //$NON-NLS-1$

  public LinearRingMarshaller( final XMLReader reader )
  {
    super( reader, TAG_LINEAR_RING );
  }

  @Override
  protected void doMarshallContent( final GM_Position[] marshalledObject ) throws SAXException
  {
    new PosListMarshaller( getXMLReader() ).marshall( marshalledObject );

// final PosMarshaller positionMarshaller = new PosMarshaller( getXMLReader() );
// for( final GM_Position position : marshalledObject )
// {
// positionMarshaller.setPosition( position );
// positionMarshaller.marshall();
// }
  }
}
