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

import java.util.HashMap;
import java.util.Map;

import org.kalypsodeegree.model.geometry.GM_Position;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A marshaller for gml:posList
 *
 * @author Felipe Maximino
 */
public class PosListMarshaller extends AbstractMarshaller<GM_Position[]>
{
  public static final String ELEMENT_POS_LIST = "posList"; //$NON-NLS-1$

  private final Map<GM_Position[], Integer> m_dimensionHash = new HashMap<>();

  public PosListMarshaller( final XMLReader reader )
  {
    super( reader, ELEMENT_POS_LIST );
  }

  @Override
  protected Attributes createAttributesForStartElement( final GM_Position[] element )
  {
    final int srsDimension = findSrsDimension( element );

    // FIXED: remember dimension for later
    m_dimensionHash.put( element, srsDimension );

    final AttributesImpl atts = new AttributesImpl();

    MarshallerUtils.addSrsDimensionAttributes( atts, srsDimension );

    return atts;
  }

  private int findSrsDimension( final GM_Position[] positions )
  {
    int srsDim = 0;
    for( final GM_Position pos : positions )
      srsDim = Math.max( srsDim, pos.getCoordinateDimension() );
    return srsDim;
  }

  @Override
  protected void doMarshallContent( final GM_Position[] marshalledObject ) throws SAXException
  {
    final Integer srsDimension = m_dimensionHash.get( marshalledObject );

    for( final GM_Position pos : marshalledObject )
      marshallPosition( pos, srsDimension );
  }

  private void marshallPosition( final GM_Position pos, final int srsDimension ) throws SAXException
  {
    final ContentHandler contentHandler = getXMLReader().getContentHandler();

    final double[] asArray = pos.getAsArray();

    // REMARK: using the globally determined dimension: either all pos have 2 or 3 coordinates.
    // If we write mixed lengths here, we get problems when reading this gml.
    for( int i = 0; i < srsDimension; i++ )
    {
      final double value = i < asArray.length ? asArray[i] : Double.NaN;
      final String dString = Double.toString( value );
      final char[] charArray = dString.toCharArray();
      contentHandler.characters( charArray, 0, charArray.length );
      contentHandler.characters( WHITESPACE, 0, 1 );
    }
  }
}
