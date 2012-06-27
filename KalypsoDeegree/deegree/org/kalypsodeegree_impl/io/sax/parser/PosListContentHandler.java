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
package org.kalypsodeegree_impl.io.sax.parser;

import java.util.ArrayList;
import java.util.List;

import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.types.IGmlContentHandler;
import org.kalypso.transformation.CRSHelper;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.io.sax.parser.IPositionHandler.PositionsWithSrs;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * A content handler that parses a gml:posList element.<br>
 *
 * @author Felipe Maximino
 */
public class PosListContentHandler extends GMLElementContentHandler
{
  public static final String ELEMENT_POSLIST = "posList";

  private StringBuffer m_coordBuffer = new StringBuffer();

  private String m_srs;

  private Integer m_srsDimension;

  private Integer m_count;

  private Integer m_checkedCrsDimension;

  private final IPositionHandler m_positionHandler;

  public PosListContentHandler( final XMLReader reader, final IGmlContentHandler parent, final IPositionHandler positionHandler, final String defaultSrs )
  {
    super( reader, NS.GML3, ELEMENT_POSLIST, defaultSrs, parent );

    m_positionHandler = positionHandler;
  }

  @Override
  public void doStartElement( final String uri, final String localName, final String name, final Attributes attributes )
  {
    m_checkedCrsDimension = checkCRSAndGetCRSDimension( attributes );
  }

  /**
   * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void doEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    final GM_Position[] posList = endPosList();

    m_positionHandler.handle( new PositionsWithSrs( posList, m_srs ) );
  }

  /**
   * checks if there is a proper srsName and srsDimension set for this posList. Returns the dimension of the CRS
   */
  private Integer checkCRSAndGetCRSDimension( final Attributes attributes )
  {
    m_srs = ContentHandlerUtils.parseSrsFromAttributes( attributes, getDefaultSrs() );
    m_srsDimension = ContentHandlerUtils.parseSrsDimensionFromAttributes( attributes );
    m_count = ContentHandlerUtils.parseCountFromAttributes( attributes );

    Integer dimension = findDimension();

    /* if it's still null, we use a default value: 2 */
    if( dimension == null )
    {
      dimension = 2;
    }

    return dimension;
  }

  /**
   * Simple heuristic to get the dimension of the posList element
   */
  private Integer findDimension( )
  {
    if( m_srsDimension == null )
    {
      if( m_srs != null )
      {
        return CRSHelper.getDimension( m_srs );
      }
      else if( getDefaultSrs() != null )
      {
        return CRSHelper.getDimension( getDefaultSrs() );
      }
    }
    else
    {
      return m_srsDimension;
    }

    return null;
  }

  private GM_Position[] endPosList( ) throws SAXParseException
  {
    final String coordsString = m_coordBuffer == null ? "" : m_coordBuffer.toString().trim();
    m_coordBuffer = null;

    final double[] doubles = ContentHandlerUtils.parseDoublesString( coordsString );

    final int coordsSize = doubles.length;

    verifyCoordsSize( coordsSize, coordsString );

    return createPositions( doubles, coordsSize );
  }

  private void verifyCoordsSize( final int coordsSize, final String coordsString ) throws SAXParseException
  {
    /* Special case: the empty geometry, no dimension check possible, i.e. it is always valid. */
    if( coordsSize == 0 )
      return;

    if( coordsSize % m_checkedCrsDimension != 0 )
      throwSAXParseException( "The number of coords in posList( " + coordsSize + " ) element doesn't respect the srsDimension attribute: " + m_checkedCrsDimension + " in " + coordsString );

    if( m_count != null && coordsSize != m_count )
      throwSAXParseException( "The number of coords in posList ( " + coordsSize + " ) element doesn't respect the count attribute: " + m_count + " in " + coordsString );
  }

  private GM_Position[] createPositions( final double[] doubles, final int coordsSize )
  {
    final List<GM_Position> positions = new ArrayList<GM_Position>( coordsSize );
    if( m_checkedCrsDimension == 2 )
    {
      for( int i = 0; i < coordsSize; )
        positions.add( GeometryFactory.createGM_Position( doubles[i++], doubles[i++] ) );
    }
    else
    // dimension = 3
    {
      for( int i = 0; i < coordsSize; )
        positions.add( GeometryFactory.createGM_Position( doubles[i++], doubles[i++], doubles[i++] ) );
    }

    return positions.toArray( new GM_Position[positions.size()] );
  }

  /**
   * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
   */
  @Override
  public void characters( final char[] ch, final int start, final int length )
  {
    if( m_coordBuffer == null )
      m_coordBuffer = new StringBuffer();

    m_coordBuffer.append( ch, start, length );
  }
}
