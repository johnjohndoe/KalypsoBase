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
package org.kalypso.gmlschema.types;

import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.namespace.QName;

import org.kalypso.commons.bind.JaxbUtilities;
import org.kalypso.gmlschema.GMLSchemaException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * a generic typehandler based on binding<br>
 * 
 * @author doemming
 */
public class GenericBindingTypeHandler implements IMarshallingTypeHandler
{
  private final JAXBContextProvider m_jaxbContextProvider;

  private final QName m_xmlTypeQName;

  private final Class< ? > m_valueClass;

  private final boolean m_isGeometry;

  private final QName m_xmlTagQName;

  private final boolean m_considerSurroundingElement;

  private final boolean m_forceElementTagOnMarshall;

  private Attributes m_atts = null;

  /**
   * @see #GenericBindingTypeHandler(JAXBContext, QName, QName, Class, boolean, false)
   */
  public GenericBindingTypeHandler( final JAXBContextProvider jaxbContextProvider, final QName xmlTypeQName, final QName xmlTagQName, final Class< ? > valueClass, final boolean isGeometry )
  {
    this( jaxbContextProvider, xmlTypeQName, xmlTagQName, valueClass, isGeometry, false, false );
  }

  /**
   * example: ( NS.GML3, "LocationPropertyType" ), new QName( NS.GML3, "location" ), LocationPropertyType.class) <br>
   * type is GML3:LocationPropertyType<br>
   * xmlTagName is GML3:location<br>
   * representation in gml is:<br>
   * <em>
   * <feature> 
   *    <prop> // also serialized by binding if considerSurroundingElement is true
   *      <location/>   // serialized by binding for LocationPropertyType
   *    </prop> // binding unmarshaller always stops here
   * </feature>
   * </em>
   * 
   * @param isGeometry
   * @param xmlTypeQName
   *            name of xmltype
   * @param xmlTagQName
   *            name of entity that will occure in xml instance document
   * @param considerSurroundingElement
   *            if true, the surrounding element defined by xmlTagQName is taken into account when unmarshalling by
   *            explicitely calling startElement on the binding unmarshaller. It allows to parse complex types which are
   *            not subtypes of PropertyType.
   */
  public GenericBindingTypeHandler( final JAXBContextProvider jaxbContextProvider, final QName xmlTypeQName, final QName xmlTagQName, final Class< ? > valueClass, final boolean isGeometry, final boolean considerSurroundingElement, final boolean forceElementTagOnMarshall )
  {
    m_jaxbContextProvider = jaxbContextProvider;
    m_xmlTagQName = xmlTagQName;
    m_valueClass = valueClass;
    m_isGeometry = isGeometry;
    m_xmlTypeQName = xmlTypeQName;
    m_considerSurroundingElement = considerSurroundingElement;
    m_forceElementTagOnMarshall = forceElementTagOnMarshall;
  }

  public void marshal( final QName propQName, final Object value, final XMLReader xmlReader, final URL context, final String gmlVersion ) throws SAXException
  {
    final JAXBElement<Object> jaxElement = JaxbUtilities.createJaxbElement( m_xmlTagQName, value );
    marshal( propQName, jaxElement, xmlReader, gmlVersion );
  }

  public <T> void marshal( final QName propQName, final JAXBElement<T> element, final XMLReader xmlReader, final String gmlVersion ) throws SAXException
  {
    try
    {
      // memory to xml
      final ContentHandler contentHandler = xmlReader.getContentHandler();

      final String namespaceURI = propQName.getNamespaceURI();
      final String localPart = propQName.getLocalPart();
      final String qNameString = propQName.getPrefix() + ":" + localPart;

      if( m_forceElementTagOnMarshall )
        contentHandler.startElement( namespaceURI, localPart, qNameString, new AttributesImpl() );

      final JAXBContext jaxbContext = m_jaxbContextProvider.getJaxBContextForGMLVersion( gmlVersion );
      final Marshaller marshaller = JaxbUtilities.createMarshaller( jaxbContext );

      marshaller.marshal( element, contentHandler );

      if( m_forceElementTagOnMarshall )
        contentHandler.endElement( namespaceURI, localPart, qNameString );
    }
    catch( final JAXBException e )
    {
      throw new SAXException( e );
    }
  }

  public void unmarshal( final XMLReader xmlReader, final URL context, final UnmarshallResultEater marshalResultEater, final String gmlVersion ) throws TypeRegistryException
  {
    // TODO: instead of the passing considerSurrounding to the UnmarshalContentprovider
    // just call startElement/endElement in that case

    // xml to memory
    try
    {
      final JAXBContext jaxbContext = m_jaxbContextProvider.getJaxBContextForGMLVersion( gmlVersion );
      final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      final UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();
      final UnmarshalResultProvider provider = new UnmarshalResultProvider()
      {
        public Object getResult( ) throws GMLSchemaException
        {
          try
          {
            unmarshallerHandler.endDocument();
            return unmarshallerHandler.getResult();
          }
          catch( final Exception e )
          {
            throw new GMLSchemaException( e );
          }
        }
      };
      final BindingUnmarshalingContentHandler tmpContentHandler = new BindingUnmarshalingContentHandler( unmarshallerHandler, provider, marshalResultEater, m_considerSurroundingElement, gmlVersion );
      tmpContentHandler.startDocument();
      xmlReader.setContentHandler( tmpContentHandler );

      // simulate start of element since SAX Parser is already one element too deep
      if( m_considerSurroundingElement )
        tmpContentHandler.startElement( m_xmlTagQName.getNamespaceURI(), m_xmlTagQName.getLocalPart(), m_xmlTagQName.toString(), m_atts );
    }
    catch( final Exception e )
    {
      throw new TypeRegistryException( e );
    }
  }

  /**
   * @see org.kalypso.gmlschema.types.ITypeHandler#getValueClass()
   */
  public Class< ? > getValueClass( )
  {
    return m_valueClass;
  }

  /**
   * @see org.kalypso.gmlschema.types.ITypeHandler#getTypeName()
   */
  public QName getTypeName( )
  {
    return m_xmlTypeQName;
  }

  /**
   * @see org.kalypso.gmlschema.types.ITypeHandler#isGeometry()
   */
  public boolean isGeometry( )
  {
    return m_isGeometry;
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#getShortname()
   */
  public String getShortname( )
  {
    return m_xmlTypeQName.getLocalPart();
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#cloneObject(java.lang.Object)
   */
  public Object cloneObject( final Object objectToClone, final String gmlVersion ) throws CloneNotSupportedException
  {
    try
    {
      final JAXBContext jaxBContext = m_jaxbContextProvider.getJaxBContextForGMLVersion( gmlVersion );
      final Unmarshaller unmarshaller = jaxBContext.createUnmarshaller();
      final UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();
      final Marshaller marshaller = jaxBContext.createMarshaller();
      marshaller.marshal( objectToClone, unmarshallerHandler );
      return unmarshallerHandler.getResult();
    }
    catch( final JAXBException e )
    {
      throw new CloneNotSupportedException( e.getLocalizedMessage() );
    }
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#parseType(java.lang.String)
   */
  public Object parseType( final String text )
  {
    throw new UnsupportedOperationException();
  }

  public void setAttributes( final Attributes atts )
  {
    m_atts = atts;
  }
}
