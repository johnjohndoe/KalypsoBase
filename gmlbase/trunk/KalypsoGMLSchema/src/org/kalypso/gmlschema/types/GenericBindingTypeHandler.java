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
import java.text.ParseException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.namespace.QName;

import ogc31.www.opengis.net.gml.LocationPropertyType;

import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.java.net.IUrlResolver;
import org.kalypso.jwsdp.JaxbUtilities;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * a generic typehandler based on binding<br>
 * 
 * @author doemming
 */
public class GenericBindingTypeHandler implements IMarshallingTypeHandler
{
  private final JAXBContext m_jaxbContext;

  private final QName m_typeQName;

  private final Class m_valueClass;

  private final boolean m_isGeometry;

  private final QName m_valueQName;

  /**
   * example: ( NS.GML3, "LocationPropertyType" ), new QName( NS.GML3, "location" ), LocationPropertyType.class) <br>
   * type is GML3:LocationPropertyType<br>
   * xmlTagName is GML3:location<br>
   * representation in gml is:<br>
   * <em>
   * <feature> 
   *    <prop>
   *      <location/>   // serialized by bindung for LocationPropertyType
   *    </prop>
   * </feature>
   * </em>
   * 
   * @param isGeometry
   * @param xmlTypeQName
   *          name of xmltype
   * @param xmlTagQName
   *          name of entity that will occure in xml instance document
   */
  public GenericBindingTypeHandler( JAXBContext jaxbContext, QName xmlTypeQName, QName xmlTagQName, Class valueClass, boolean isGeometry )
  {
    m_jaxbContext = jaxbContext;
    m_valueQName = xmlTagQName;
    m_valueClass = valueClass;
    m_isGeometry = isGeometry;
    m_typeQName = xmlTypeQName;
  }

  public void marshal( QName propQName, Object value, ContentHandler contentHandler, LexicalHandler lexicalHandler, URL context ) throws TypeRegistryException
  {
    // memory to xml
    try
    {
      final String namespaceURI = propQName.getNamespaceURI();
      final String localPart = propQName.getLocalPart();
      final String qNameString = propQName.getPrefix() + ":" + localPart;
      contentHandler.startElement( namespaceURI, localPart, qNameString, null );
      final Marshaller marshaller = JaxbUtilities.createMarshaller( m_jaxbContext );
      final JAXBElement<Object> jaxElement = new JAXBElement<Object>( m_valueQName, (Class<Object>) value.getClass(), value );
      marshaller.marshal( jaxElement, contentHandler );
      contentHandler.endElement( namespaceURI, localPart, qNameString );
    }
    catch( Exception e )
    {
      throw new TypeRegistryException( e );
    }
  }

  public void unmarshal( XMLReader xmlReader, IUrlResolver urlResolver, UnMarshallResultEater marshalResultEater ) throws TypeRegistryException
  {
    // xml to memory
    try
    {
      final Unmarshaller unmarshaller;
      unmarshaller = m_jaxbContext.createUnmarshaller();
      final UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();
      final UnmarshalResultProvider provider = new UnmarshalResultProvider()
      {
        public Object getResult( ) throws IllegalStateException, JAXBException
        {
          try
          {
            unmarshallerHandler.endDocument();
          }
          catch( SAXException e )
          {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          return unmarshallerHandler.getResult();
        }
      };
      final BindingUnmarshalingContentHandler tmpContentHandler = new BindingUnmarshalingContentHandler( unmarshallerHandler, provider, marshalResultEater );
      tmpContentHandler.startDocument();
      xmlReader.setContentHandler( tmpContentHandler );
    }
    catch( Exception e )
    {
      throw new TypeRegistryException( e );
    }
  }

  /**
   * @see org.kalypso.gmlschema.types.ITypeHandler#getValueClass()
   */
  public Class getValueClass( )
  {
    return m_valueClass;
  }

  /**
   * @see org.kalypso.gmlschema.types.ITypeHandler#getTypeName()
   */
  public QName getTypeName( )
  {
    return m_typeQName;
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
    return m_typeQName.getLocalPart();
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#cloneObject(java.lang.Object)
   */
  public Object cloneObject( Object objectToClone ) throws CloneNotSupportedException
  {
    try
    {
      final Unmarshaller unmarshaller = m_jaxbContext.createUnmarshaller();
      final UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();
      final Marshaller marshaller = m_jaxbContext.createMarshaller();
      marshaller.marshal( marshaller, unmarshallerHandler );
      return unmarshallerHandler.getResult();
    }
    catch( JAXBException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
      throw new CloneNotSupportedException();
    }
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#parseType(java.lang.String)
   */
  public Object parseType( String text ) throws ParseException
  {
    throw new UnsupportedOperationException();
  }
}
