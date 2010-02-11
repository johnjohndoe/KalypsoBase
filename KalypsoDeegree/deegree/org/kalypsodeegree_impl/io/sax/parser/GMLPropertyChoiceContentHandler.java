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

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.KalypsoGMLSchemaPlugin;
import org.kalypso.gmlschema.property.IPropertyMarshallingTypeHandler;
import org.kalypso.gmlschema.types.IValueHandler;
import org.kalypsodeegree_impl.tools.GMLConstants;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * When a feature can be specified using more than one property, this content handler can be set as delegate, and
 * the properties that can specify the feature must be registered. 
 * This content handler then delegates the parsing to a child content handler that
 * parses an specific sub-element.   
 * 
 * @author Felipe Maximino
 */
public class GMLPropertyChoiceContentHandler extends DelegatingContentHandler
{
  private Map<QName, ContentHandler> m_registeredProperties;
  
  private String m_defaultSrs;
  
  public GMLPropertyChoiceContentHandler( ContentHandler parentContentHandler, final XMLReader xmlReader, final String defaultSrs )
  {
    super( xmlReader, parentContentHandler );
    
    m_registeredProperties = new HashMap<QName, ContentHandler>();
    m_defaultSrs = defaultSrs;
  }
  
  @Override
  public void endElement( final String uri, final String localName, final String name ) throws SAXException
  { 
    GMLElementContentHandler parentContentHandler = (GMLElementContentHandler) getParentContentHandler();
    
    if( parentContentHandler.getLocalName().equals( localName ) )
    {
      endDelegation();
      parentContentHandler.endElement( uri, localName, name );      
    }
  }

  @Override
  public void startElement( final String uri, final String localName, final String name, final Attributes atts ) throws SAXException
  { 
    ContentHandler delegate = findDelegate( new QName( uri, localName ) );

    if( delegate != null )
    {
      delegate( delegate );
      delegate.startElement( uri, localName, name, atts );      
    }    
    else
    {
      throw new SAXParseException( String.format( "Unexpected start element: %s - %s -  %s", uri, localName, name ), getLocator() );
    }
  }
  
  private ContentHandler findDelegate( final QName qName )
  {
    return m_registeredProperties.get( qName );
  }
  
  private void registerProperty( final QName qName, final ContentHandler contentHandler )
  {
    m_registeredProperties.put( qName, contentHandler );
  }
  
  /**
   * Loads the properties that can specify the geometry given as parameter
   */
  public void loadPropertiesFor( QName geometry ) throws SAXParseException
  {
    try
    {
      IPropertyMarshallingTypeHandler[] allowedProps = KalypsoGMLSchemaPlugin.getDefault().getAllowedProperties( geometry );
      
      for( IPropertyMarshallingTypeHandler handler : allowedProps )
      { 
        registerProperty( handler.getTypeName(), handler.createContentHandler( m_xmlReader, this, (IValueHandler) m_parentContentHandler, m_defaultSrs ) );
      }
    } 
    catch ( Exception e )
    {
      e.printStackTrace();
      throw new SAXParseException( "Could not get properties for " + geometry.toString() +  ": " + e.getMessage(), m_locator );
    }
  }
}
