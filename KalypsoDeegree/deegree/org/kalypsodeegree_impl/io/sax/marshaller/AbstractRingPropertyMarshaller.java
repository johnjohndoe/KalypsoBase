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
 * FIXME: instead of marshalling a GM_Ring we marshall GM_Positions[]: this is because the GM_SurfacePath hold just
 * GM_Position[]'s as interior and exterior rings.<br/>
 * We need to change the GM_SurfacePath classes.<br/>
 * elements marshallers.
 * 
 * @author Felipe Maximino
 */
public abstract class AbstractRingPropertyMarshaller extends AbstractMarshaller<GM_Position[]>
{
  public AbstractRingPropertyMarshaller( final XMLReader reader, final String tag, final GM_Position[] ring )
  {
    super( reader, tag, ring );
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.marshaller.AbstractMarshaller#doMarshall(java.lang.Object)
   */
  @Override
  protected void doMarshallContent( final GM_Position[] marshalledObject ) throws SAXException
  {
    new LinearRingMarshaller( getXMLReader(), marshalledObject ).marshall();
  }
}
