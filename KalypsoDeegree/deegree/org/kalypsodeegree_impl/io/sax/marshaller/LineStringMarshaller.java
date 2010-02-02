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
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author Felipe Maximino
 * 
 * A gml:LineString Marshaller. It marshalls the line string specifying a gml:posList
 * for its positions.
 * 
 */
public class LineStringMarshaller extends GeometryMarshaller<GM_Curve>
{
  public final static String TAG_LINE_STRING = "LineString";

  public LineStringMarshaller( XMLReader xmlReader, GM_Curve lineString )
  {
    super( xmlReader, TAG_LINE_STRING, lineString );
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.marshaller.AbstractMarshaller#doMarshall()
   */
  @Override
  protected void doMarshall( ) throws SAXException
  {
    try
    {
      new PosListMarshaller( m_xmlReader, m_marshalledObject.getAsLineString().getPositions() ).marshall();
    }
    catch( GM_Exception e)
    {
      throw new SAXException( "Error when marshalling a gml:LineString: " + e.getMessage() );
    }
  }
}
