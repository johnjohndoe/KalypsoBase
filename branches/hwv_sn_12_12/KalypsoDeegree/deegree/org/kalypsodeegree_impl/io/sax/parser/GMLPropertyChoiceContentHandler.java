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

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.types.AbstractGmlContentHandler;
import org.kalypso.gmlschema.types.IGmlContentHandler;
import org.kalypsodeegree_impl.io.sax.parser.geometrySpec.IGeometrySpecification;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * When a feature can be specified using more than one property, this content handler can be set as delegate, and the
 * properties that can specify the feature must be registered. This content handler then delegates the parsing to a
 * child content handler that parses an specific sub-element.
 * 
 * @author Felipe Maximino
 */
public class GMLPropertyChoiceContentHandler extends AbstractGmlContentHandler
{
  private final IGeometrySpecification m_spec;

  private final String m_defaultSrs;

  private final IGmlContentHandler m_receiver;

  public GMLPropertyChoiceContentHandler( final XMLReader reader, final IGmlContentHandler parentContentHandler, final IGmlContentHandler receiver, final String defaultSrs, final IGeometrySpecification spec )
  {
    super( reader, parentContentHandler );

    m_receiver = receiver;
    m_defaultSrs = defaultSrs;
    m_spec = spec;
  }

  @Override
  public void endElement( final String uri, final String localName, final String name ) throws SAXException
  {
    final GMLElementContentHandler parentContentHandler = (GMLElementContentHandler) getParentContentHandler();
    if( parentContentHandler.getLocalName().equals( localName ) )
    {
      activateParent();
      parentContentHandler.endElement( uri, localName, name );
    }
  }

  @Override
  public void startElement( final String uri, final String localName, final String name, final Attributes atts ) throws SAXException
  {
    final IGmlContentHandler delegate = findDelegate( new QName( uri, localName ) );

    if( delegate == null )
      throwSAXParseException( "Unexpected start element: %s - %s -  %s", uri, localName, name );

    delegate.activate();
    delegate.startElement( uri, localName, name, atts );
  }

  private IGmlContentHandler findDelegate( final QName property )
  {
    return m_spec.getHandler( property, getXMLReader(), this, m_receiver, m_defaultSrs );
  }
}
