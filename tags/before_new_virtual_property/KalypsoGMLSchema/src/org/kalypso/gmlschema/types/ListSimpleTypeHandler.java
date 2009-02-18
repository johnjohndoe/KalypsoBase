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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * Type handler for
 * 
 * @author Gernot Belger
 */
public class ListSimpleTypeHandler implements IMarshallingTypeHandler
{
  private final XsdBaseContentHandler m_contentHandler = new XsdBaseContentHandler( this, null );

  private final ISimpleMarshallingTypeHandler m_baseTypeHandler;

  public ListSimpleTypeHandler( final IMarshallingTypeHandler baseTypeHandler )
  {
    if( !(baseTypeHandler instanceof ISimpleMarshallingTypeHandler) )
      throw new IllegalArgumentException( "List may be used only on simple types. Not on complex types: " + baseTypeHandler );

    m_baseTypeHandler = (ISimpleMarshallingTypeHandler) baseTypeHandler;
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#cloneObject(java.lang.Object, java.lang.String)
   */
  public Object cloneObject( final Object objectToClone, final String gmlVersion ) throws CloneNotSupportedException
  {
    if( objectToClone == null )
      return null;

    final List<Object> list = (List<Object>) objectToClone;
    final List<Object> clonedList = new ArrayList<Object>( list.size() );

    for( final Object object : list )
      clonedList.add( m_baseTypeHandler.cloneObject( object, gmlVersion ) );

    return clonedList;
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#getShortname()
   */
  public String getShortname( )
  {
    return m_baseTypeHandler.getShortname();
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#marshal(javax.xml.namespace.QName, java.lang.Object,
   *      org.xml.sax.ContentHandler, org.xml.sax.ext.LexicalHandler, java.net.URL, java.lang.String)
   */
  public void marshal( final QName propQName, final Object value, final ContentHandler contentHandler, final LexicalHandler lexicalHandler, final URL context, final String gmlVersion ) throws TypeRegistryException
  {
    final char[] spaceChar = new char[] { ' ' };

    try
    {
      final String namespaceURI = propQName.getNamespaceURI();
      final String localPart = propQName.getLocalPart();
      final String qNameString = propQName.getPrefix() + ":" + localPart;
      contentHandler.startElement( namespaceURI, localPart, qNameString, null );

      final List<Object> list = (List<Object>) value;
      for( final Iterator<Object> iter = list.iterator(); iter.hasNext(); )
      {
        final Object element = iter.next();

        final String xmlString = m_baseTypeHandler.convertToXMLString( element );
        contentHandler.characters( xmlString.toCharArray(), 0, xmlString.length() );

        if( iter.hasNext() )
          contentHandler.characters( spaceChar, 0, 1 );
      }

      contentHandler.endElement( namespaceURI, localPart, qNameString );
    }
    catch( final Exception e )
    {
      throw new TypeRegistryException( e );
    }
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#unmarshal(org.xml.sax.XMLReader, java.net.URL,
   *      org.kalypso.gmlschema.types.UnMarshallResultEater, java.lang.String)
   */
  public void unmarshal( XMLReader xmlReader, URL context, UnMarshallResultEater marshalResultEater, String gmlVersion ) throws TypeRegistryException
  {
    try
    {
      // REMARK: We had a small performance and memory problem here, because each time the method
      // was called a content handler (and severel other classes) where instantiated.
      // But this method is called quite often!
      // We now resuse the same content handler. This is safe, because a simle type never contains any other types.
      m_contentHandler.setMarshalResultEater( marshalResultEater, true );

      xmlReader.setContentHandler( m_contentHandler );
    }
    catch( final Exception e )
    {
      throw new TypeRegistryException( e );
    }
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#parseType(java.lang.String)
   */
  public Object parseType( final String text ) throws ParseException
  {
    if( text == null )
      return null;

    final List<Object> list = new ArrayList<Object>();

    final String[] strings = text.split( " +" );
    for( final String string : strings )
    {
      final Object object = m_baseTypeHandler.parseType( string );
      list.add( object );
    }

    return list;
  }

  /**
   * @see org.kalypso.gmlschema.types.ITypeHandler#getTypeName()
   */
  public QName getTypeName( )
  {
    return m_baseTypeHandler.getTypeName();
  }

  /**
   * @see org.kalypso.gmlschema.types.ITypeHandler#getValueClass()
   */
  public Class getValueClass( )
  {
    return List.class;
  }

  /**
   * @see org.kalypso.gmlschema.types.ITypeHandler#isGeometry()
   */
  public boolean isGeometry( )
  {
    return m_baseTypeHandler.isGeometry();
  }

}
