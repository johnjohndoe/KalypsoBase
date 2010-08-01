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
package org.kalypso.gml;

import org.kalypso.gmlschema.types.AbstractGmlContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A {@link org.xml.sax.ContentHandler} implementation which parses simple content.
 * 
 * @author Andreas von Doemming
 * @author Felipe Maximino - Refaktoring
 */
public class SimpleContentContentHandler extends AbstractGmlContentHandler
{
  private final ISimpleContentHandler m_simpleContentHandler;

  private final StringBuffer m_simpleContent;

  public SimpleContentContentHandler( final XMLReader reader, final ISimpleContentHandler simpleContentHandler )
  {
    super( reader, simpleContentHandler );

    m_simpleContentHandler = simpleContentHandler;
    m_simpleContent = new StringBuffer();
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.DelegatingContentHandler#endElement(java.lang.String, java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void endElement( final String uri, final String localName, final String qName ) throws SAXException
  {
    m_simpleContentHandler.handle( m_simpleContent );
    activateParent();
    getTopLevel().endElement( uri, localName, qName );
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.DelegatingContentHandler#characters(char[], int, int)
   */
  @Override
  public void characters( final char[] ch, final int start, final int length )
  {
    m_simpleContent.append( ch, start, length );
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.DelegatingContentHandler#ignorableWhitespace(char[], int, int)
   */
  @Override
  public void ignorableWhitespace( final char[] ch, final int start, final int length )
  {
    characters( ch, start, length );
  }
}
