/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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

import javax.xml.namespace.QName;

import org.apache.commons.lang3.ArrayUtils;
import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.types.IGmlContentHandler;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.io.sax.parser.IPositionHandler.PositionsWithSrs;
import org.kalypsodeegree_impl.io.sax.parser.geometrySpec.IGeometrySpecification;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * @author Gernot Belger
 */
public class EnvelopeContentHandler extends GMLElementContentHandler implements IGeometrySpecification
{
  private final IPositionHandler m_positionHandler = new IPositionHandler()
  {
    @Override
    public void handle( final PositionsWithSrs pws ) throws SAXParseException
    {
      addPosition( pws );
    }
  };

  private final ICoordinatesHandler m_coordHandler = new ICoordinatesHandler()
  {
    @Override
    public void handle( final List<Double[]> element ) throws SAXParseException
    {
      addCoordinate( element );
    }
  };

  private final IEnvelopeHandler m_envelopeHandler;

  private String m_srs;

  private List<GM_Position> m_positions;

  public EnvelopeContentHandler( final XMLReader reader, final IGmlContentHandler parentContentHandler, final IEnvelopeHandler envelopeHandler )
  {
    super( reader, NS.GML3, GM_Envelope.ENVELOPE_ELEMENT.getLocalPart(), parentContentHandler ); //$NON-NLS-1$

    m_envelopeHandler = envelopeHandler;
  }

  @Override
  protected void doStartElement( final String uri, final String localName, final String name, final Attributes atts )
  {
    m_srs = ContentHandlerUtils.parseSrsFromAttributes( atts, getDefaultSrs() );
    m_positions = new ArrayList<>( 2 );

    final GMLPropertyChoiceContentHandler choiceContentHandler = new GMLPropertyChoiceContentHandler( getXMLReader(), this, this, m_srs, this );
    choiceContentHandler.activate();
  }

  @Override
  protected void doEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    if( m_positions == null || m_positions.size() != 2 )
      throwSAXParseException( "Not enough coorindates in envelope" );

    final GM_Envelope envelope = GeometryFactory.createGM_Envelope( m_positions.get( 0 ), m_positions.get( 1 ), m_srs );

    m_envelopeHandler.handle( envelope );

    m_srs = null;
    m_positions = null;
  }

  @Override
  public IGmlContentHandler getHandler( final QName property, final XMLReader reader, final IGmlContentHandler parent, final IGmlContentHandler receiver, final String defaultSrs ) throws SAXParseException
  {
    if( GM_Envelope.PROPERTY_LOWER_CORNER.equals( property ) )
      return new DirectPositionTypeContentHandler( reader, parent, m_positionHandler, defaultSrs, GM_Envelope.PROPERTY_LOWER_CORNER.getLocalPart() );

    if( GM_Envelope.PROPERTY_UPPER_CORNER.equals( property ) )
      return new DirectPositionTypeContentHandler( reader, parent, m_positionHandler, defaultSrs, GM_Envelope.PROPERTY_UPPER_CORNER.getLocalPart() );

    if( GM_Envelope.PROPERTY_COORD.equals( property ) )
      return new CoordContentHandler( reader, parent, m_coordHandler, defaultSrs );

    if( GM_Envelope.PROPERTY_POS.equals( property ) )
      return new DirectPositionTypeContentHandler( reader, parent, m_positionHandler, defaultSrs, GM_Envelope.PROPERTY_POS.getLocalPart() );

    if( GM_Envelope.PROPERTY_COORDINATES.equals( property ) )
      return new CoordinatesContentHandler( reader, parent, m_coordHandler, defaultSrs );

    throwSAXParseException( "Unknown property found: %s", property );
    return null;
  }

  protected void addCoordinate( final List<Double[]> positions ) throws SAXParseException
  {
    for( final Double[] position : positions )
    {
      final GM_Position pos = GeometryFactory.createGM_Position( ArrayUtils.toPrimitive( position ) );
      addPosition( new PositionsWithSrs( pos, m_srs ) );
    }
  }

  protected void addPosition( final PositionsWithSrs pws ) throws SAXParseException
  {
    if( m_positions.size() == 2 )
      throwSAXParseException( "Too many coordinates in envelope" ); //$NON-NLS-1$

    final String srs = pws.getSrs();

    try
    {
      final GM_Position[] positions = pws.getPositions();
      for( final GM_Position position : positions )
      {
        final GM_Position transformedPosition = position.transform( srs, m_srs );
        m_positions.add( transformedPosition );
      }
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      throwSAXParseException( e, "Failed to transform coordinates" ); //$NON-NLS-1$
    }
  }
}