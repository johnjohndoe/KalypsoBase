/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.gmlschema.types;

import javax.xml.bind.JAXBElement;

import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.GMLSchemaException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * A contentHandler used for unmarshalling processes based on binding framework
 * 
 * @author doemming
 */
public class BindingUnmarshalingContentHandler implements ContentHandler
{
  private static final String BOX_STRING = "box"; //$NON-NLS-1$

  private int m_level;

  private final ContentHandler m_unmarshallerHandler;

  private final UnmarshallResultEater m_unmarshalResultEater;

  private final UnmarshalResultProvider m_unmarshallResultProvider;

  private final String m_gmlVersion;

  private final boolean m_isGML2;

  /**
   * @param unmarshallerHandler
   *          the real binding unmarshalling content handler that will be wrapped
   * @param unmarshallResultProvider
   *          provider that will provide the result after unmarshalling
   * @param unmarshalResultEater
   *          will be feeded with the result of unmarshalling
   */
  public BindingUnmarshalingContentHandler( final ContentHandler unmarshallerHandler, final UnmarshalResultProvider unmarshallResultProvider, final UnmarshallResultEater unmarshalResultEater, final String gmlVersion )
  {
    m_unmarshallerHandler = unmarshallerHandler;
    m_unmarshallResultProvider = unmarshallResultProvider;
    m_unmarshalResultEater = unmarshalResultEater;
    m_gmlVersion = gmlVersion;
    m_isGML2 = m_gmlVersion.startsWith( "2" ); //$NON-NLS-1$
  }

  /**
   * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
   *      org.xml.sax.Attributes)
   */
  public void startElement( final String uri, String local, String qname, final Attributes atts ) throws SAXException
  {
    // hack:
    // deegree-bug gazetteer provides gml:box instead of gml:Box
    // (doemming)
    if( m_isGML2 && NS.GML3.equals( uri ) )
    {
      // hack for loading invalid gml2 polygons, do not remove (doemming)
      if( "exterior".equals( local ) ) //$NON-NLS-1$
      {
        local = "outerBoundaryIs"; //$NON-NLS-1$
        qname = qname.replaceFirst( "exterior", "outerBoundaryIs" ); //$NON-NLS-1$ //$NON-NLS-2$
      }
      // hack for loading invalid gml2 polygons, do not remove (doemming)
      else if( "interior".equals( local ) ) //$NON-NLS-1$
      {
        local = "innerBoundaryIs"; //$NON-NLS-1$
        qname = qname.replaceFirst( "interior", "innerBoundaryIs" ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
    else if( NS.GML2.equals( uri ) && BOX_STRING.equals( local ) )
    {
      local = "Box"; //$NON-NLS-1$
      qname = qname.replaceFirst( BOX_STRING, "Box" ); //$NON-NLS-1$
    }
    m_level++;
    // System.out.println( indent() + "<" + qname + ">" );

    try
    {
      m_unmarshallerHandler.startElement( uri, local, qname, atts );
    }
    catch( final NullPointerException e )
    {
      e.printStackTrace();

      throw new SAXException( "Element could not be processed: " + qname ); //$NON-NLS-1$
    }
  }

  @SuppressWarnings("unchecked")
  public void endElement( final String uri, String local, String qname ) throws SAXException
  {
    
    if( m_isGML2 && NS.GML3.equals( uri ) )
    {
      // hack for loading invalid gml2 polygons, do not remove (doemming)
      if( "exterior".equals( local ) ) //$NON-NLS-1$
      {
        local = "outerBoundaryIs"; //$NON-NLS-1$
        qname = qname.replaceFirst( "exterior", "outerBoundaryIs" ); //$NON-NLS-1$ //$NON-NLS-2$
      }
      // hack for loading invalid gml2 polygons, do not remove (doemming)
      if( "interior".equals( local ) ) //$NON-NLS-1$
      {
        local = "innerBoundaryIs"; //$NON-NLS-1$
        qname = qname.replaceFirst( "interior", "innerBoundaryIs" ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
    else if( NS.GML2.equals( uri ) && BOX_STRING.equals( local ) )
    {
      local = "Box"; //$NON-NLS-1$
      qname = qname.replaceFirst( BOX_STRING, "Box" ); //$NON-NLS-1$
    }
    
    // System.out.println( indent() + "</" + qname + ">" );
    m_level--;
    
    m_unmarshallerHandler.endElement( uri, local, qname );

    if( m_level < 0 )
    {
      try
      {
        final Object result = m_unmarshallResultProvider.getResult();
        final JAXBElement<Object> jaxBElement = (JAXBElement<Object>) result;
        if( jaxBElement != null )
          m_unmarshalResultEater.unmarshallSuccesful( jaxBElement.getValue() );
        else
          m_unmarshalResultEater.unmarshallSuccesful( null );
      }
      catch( final GMLSchemaException e )
      {
        throw new SAXException( e );
      }
    }
  }

  public void characters( final char[] ch, final int start, final int length ) throws SAXException
  {
    if( m_level > 0 )
      m_unmarshallerHandler.characters( ch, start, length );
  }

  public void ignorableWhitespace( final char[] ch, final int start, final int len ) throws SAXException
  {
    if( m_level > 0 )
      m_unmarshallerHandler.ignorableWhitespace( ch, start, len );
  }

  public void processingInstruction( final String target, final String data ) throws SAXException
  {
    if( m_level > 0 )
      m_unmarshallerHandler.processingInstruction( target, data );
  }

  public void startDocument( ) throws SAXException
  {
    m_unmarshallerHandler.startDocument();
  }

  public void endDocument( ) throws SAXException
  {
    m_unmarshallerHandler.endDocument();
  }

  public void startPrefixMapping( final String prefix, final String uri ) throws SAXException
  {
    m_unmarshallerHandler.startPrefixMapping( prefix, uri );
  }

  public void endPrefixMapping( final String prefix ) throws SAXException
  {
    m_unmarshallerHandler.endPrefixMapping( prefix );
  }

  public void setDocumentLocator( final Locator locator )
  {
    m_unmarshallerHandler.setDocumentLocator( locator );
  }

  public void skippedEntity( final String name ) throws SAXException
  {
    m_unmarshallerHandler.skippedEntity( name );
  }
}
