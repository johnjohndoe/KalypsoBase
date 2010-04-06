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

import java.util.List;

import org.kalypso.commons.xml.NS;
import org.kalypsodeegree_impl.tools.GMLConstants;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A marshaller for gml:coordinates
 * <p>
 * The default values for separators e decimal indicators are: tuple separator: ", " coordinate separator: " " decimal
 * indicator: "." These values can be changed by the set methods: {@link setCoordinatesSeparator}
 * {@link setTuplesSeparator} {@link setDecimalIndicator}
 * 
 * @author Felipe Maximino
 */
public class CoordinatesMarshaller extends AbstractMarshaller<List<double[]>>
{
  private static final String TAG_COORDINATES = "coordinates";

  /* tuples separator */
  private String m_ts;

  /* coordinates separator */
  private String m_cs;

  /* decimal indicator */
  private String m_decimal;

  public CoordinatesMarshaller( final XMLReader xmlReader )
  {
    this( xmlReader, null );
  }

  public CoordinatesMarshaller( final XMLReader xmlReader, final List<double[]> coords )
  {
    super( xmlReader, TAG_COORDINATES, coords );

    /* separators initialized to default values */
    m_ts = ",";
    m_cs = " ";
    m_decimal = GMLConstants.DEFAULT_DECIMAL;
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.marshaller.AbstractMarshaller#startMarshalling()
   */
  @Override
  public void startMarshalling( ) throws SAXException
  {
    final Attributes atts = createCoordinatesDefaultAttributes();

    final ContentHandler contentHandler = getXmlReader().getContentHandler();
    contentHandler.startElement( NS.GML3, getTag(), getQName(), atts );
  }

  private AttributesImpl createCoordinatesDefaultAttributes( )
  {
    final AttributesImpl atts = new AttributesImpl();
    atts.addAttribute( "", "ts", "ts", "CDATA", m_ts );
    atts.addAttribute( "", "cs", "cs", "CDATA", m_cs );
    atts.addAttribute( "", "decimal", "decimal", "CDATA", m_decimal );

    return atts;
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.marshaller.AbstractMarshaller#doMarshall()
   */
  @Override
  public void doMarshall( ) throws SAXException
  {
    final ContentHandler contentHandler = getXmlReader().getContentHandler();

    final int nTuples = getMarshalledObject().size();
    for( int i = 0; i < nTuples; i++ )
    {
      final double[] tuple = getMarshalledObject().get( i );

      marshallTuple( contentHandler, tuple );

      /* don't write the TS after the last tuple */
      if( i != nTuples - 1 )
      {
        contentHandler.characters( m_ts.toCharArray(), 0, 1 );
      }

    }
  }

  private void marshallTuple( final ContentHandler contentHandler, final double[] tuple ) throws SAXException
  {
    final int nCoordinates = tuple.length;
    for( int i = 0; i < nCoordinates; i++ )
    {
      String dString = Double.toString( tuple[i] );
      /* transform to desired decimal indicator */
      if( !m_decimal.equals( GMLConstants.DEFAULT_DECIMAL ) )
      {
        dString = dString.replace( GMLConstants.DEFAULT_DECIMAL, m_decimal );
      }

      final char[] charArray = dString.toCharArray();

      contentHandler.characters( charArray, 0, charArray.length );

      /* don't write the CS after the last coordinate */
      if( i != nCoordinates - 1 )
      {
        contentHandler.characters( m_cs.toCharArray(), 0, 1 );
      }
    }
  }

  public void setCoordinates( final List<double[]> coordinates )
  {
    setMarshalledObject( coordinates );
  }

  /**
   * sets the desired tuple separator for the marshalling
   */
  public void setTupleSeparator( final String ts )
  {
    m_ts = ts;
  }

  /**
   * sets the desired tuple coordinates separator for the marshalling
   */
  public void setCoordinatesSeparator( final String cs )
  {
    m_cs = cs;
  }

  /**
   * sets the desired decimalIndicator for the marshalling
   */
  public void setDecimalIndicator( final String decimal )
  {
    m_decimal = decimal;
  }
}
