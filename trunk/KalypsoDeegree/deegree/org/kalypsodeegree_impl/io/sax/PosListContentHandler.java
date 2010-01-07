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
package org.kalypsodeegree_impl.io.sax;

import java.util.ArrayList;
import java.util.List;

import org.deegree.model.crs.UnknownCRSException;
import org.kalypso.commons.xml.NS;
import org.kalypso.transformation.CRSHelper;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
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
  
  private IPositionHandler m_positionHandler;
  
  public PosListContentHandler( final IPositionHandler positionHandler, final String defaultSrs, final XMLReader xmlReader )
  {
    super( NS.GML3, ELEMENT_POSLIST, xmlReader, defaultSrs, positionHandler );
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

    m_positionHandler.handleElement( posList, m_srs );
  }

  /**
   * checks if there is a proper srsName and srsDimension set for this posList.
   * Returns the dimension of the CRS
   */
  private Integer checkCRSAndGetCRSDimension( final Attributes attributes )
  {
    m_srs = ContentHandlerUtils.parseSrsFromAttributes( attributes, m_defaultSrs );
    m_srsDimension = ContentHandlerUtils.parseSrsDimensionFromAttributes( attributes );
    m_count = ContentHandlerUtils.parseCountFromAttributes( attributes );
    
    Integer dimension = findDimension();
    
    /*
     * !!HACK if it's still null, we will have to guess it: 2
     */
    if( dimension == null)
    {
      dimension = 2;
    }
    
    return dimension;
  }
  
  private Integer findDimension()
  {
    try
    {
      if( m_srsDimension == null )
      {
        if ( m_srs != null)
        {
          return CRSHelper.getDimension( m_srs );
        }
        else if ( m_defaultSrs != null )
        {
          return CRSHelper.getDimension( m_defaultSrs );
        }
      }
      else
      {
        return m_srsDimension;
      }
    }
    catch( UnknownCRSException e )
    {
      e.printStackTrace();
    }
    
    return null;
  }
  
  private GM_Position[] endPosList( ) throws SAXParseException
  {
    final String coordsString = m_coordBuffer == null ? "" : m_coordBuffer.toString().trim();
    m_coordBuffer = null;    
    
    final List<Double> doubles = (List<Double>) m_positionHandler.parseType( coordsString );  
    
    final int coordsSize = doubles.size();
    
    verifyCoordsSize( coordsSize, coordsString );
    
    return createPositions(doubles, coordsSize);    
  }
  
  private void verifyCoordsSize( final int coordsSize, final String coordsString ) throws SAXParseException
  { 
    if ( coordsSize % m_checkedCrsDimension != 0)
    {
      throw new SAXParseException( "The number of coords in posList( " + coordsSize +" ) element doesn't respect the srsDimension attribute: " + m_checkedCrsDimension + " in " + coordsString, m_locator );
    }

    if (m_count != null && coordsSize != m_count )
    {
      throw new SAXParseException( "The number of coords in posList ( " + coordsSize +" ) element doesn't respect the count attribute: " + m_count + " in " + coordsString, m_locator );
    }
  }
  
  private GM_Position[] createPositions( final List<Double> doubles, final int coordsSize )
  { 
    List<GM_Position> positions = new ArrayList<GM_Position>( coordsSize );
    if( m_checkedCrsDimension == 2)
    {
      for( int i = 0; i < coordsSize;)
      { 
        positions.add( GeometryFactory.createGM_Position( doubles.get( i++ ), doubles.get( i++ ) ) );
      }
    }
    else
    {
      for( int i = 0; i < coordsSize;)
      { 
        positions.add( GeometryFactory.createGM_Position( doubles.get( i++ ), doubles.get( i++ ), doubles.get( i++ ) ) );
      }
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
