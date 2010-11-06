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
package org.kalypsodeegree.model.typeHandler;

import java.net.URL;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.property.IPropertyMarshallingTypeHandler;
import org.kalypso.gmlschema.types.IGmlContentHandler;
import org.kalypso.gmlschema.types.IValueHandler;
import org.kalypso.gmlschema.types.UnmarshallResultEater;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.io.sax.marshaller.PointMemberMarshaller;
import org.kalypsodeegree_impl.io.sax.parser.IPointHandler;
import org.kalypsodeegree_impl.io.sax.parser.PointMemberContentHandler;
import org.kalypsodeegree_impl.tools.GMLConstants;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A handler for gml:pointMember property.
 * 
 * @author Felipe Maximino
 */
public class PointMemberHandler implements IPropertyMarshallingTypeHandler
{
  private static QName QNAME_TYPE = GMLConstants.QN_POINT_MEMBER;

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler2#createContentHandler(org.xml.sax.XMLReader,
   *      org.xml.sax.ContentHandler, org.kalypso.gmlschema.types.UnmarshallResultEater, java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  public IGmlContentHandler createContentHandler( final XMLReader reader, final IGmlContentHandler parentContentHandler, final UnmarshallResultEater resultEater )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyMarshallingTypeHandler#createContentHandler(org.xml.sax.XMLReader,
   *      org.xml.sax.ContentHandler, org.kalypso.gmlschema.types.IPropertyValueHandler, java.lang.String)
   */
  @Override
  public IGmlContentHandler createContentHandler( final XMLReader reader, final IGmlContentHandler parentContentHandler, final IValueHandler handler, final String defaultSrs )
  {
    return new PointMemberContentHandler( reader, parentContentHandler, (IPointHandler) handler, defaultSrs );
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#cloneObject(java.lang.Object, java.lang.String)
   */
  @Override
  public Object cloneObject( final Object objectToClone, final String gmlVersion ) throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException();
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#getShortname()
   */
  @Override
  public String getShortname( )
  {
    return QNAME_TYPE.getLocalPart();
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#marshal(java.lang.Object, org.xml.sax.XMLReader,
   *      java.net.URL, java.lang.String)
   */
  @Override
  public void marshal( final Object value, final XMLReader reader, final URL context, final String gmlVersion ) throws SAXException
  {
    final PointMemberMarshaller marshaller = new PointMemberMarshaller( reader );
    marshaller.setMember( (GM_Point) value );
    marshaller.marshall();
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#parseType(java.lang.String)
   */
  @Override
  public Object parseType( final String text )
  {
    return null;
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#unmarshal(org.xml.sax.XMLReader, java.net.URL,
   *      org.kalypso.gmlschema.types.UnmarshallResultEater, java.lang.String)
   */
  @Override
  public void unmarshal( final XMLReader reader, final URL context, final UnmarshallResultEater marshalResultEater, final String gmlVersion )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypso.gmlschema.types.ITypeHandler#getTypeName()
   */
  @Override
  public QName getTypeName( )
  {
    return QNAME_TYPE;
  }

  /**
   * @see org.kalypso.gmlschema.types.ITypeHandler#getValueClass()
   */
  @Override
  public Class< ? > getValueClass( )
  {
    return null;
  }

  /**
   * @see org.kalypso.gmlschema.types.ITypeHandler#isGeometry()
   */
  @Override
  public boolean isGeometry( )
  {
    return false;
  }
}
