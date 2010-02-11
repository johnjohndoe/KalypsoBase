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

import org.kalypso.gmlschema.types.IMarshallingTypeHandler2;
import org.kalypso.gmlschema.types.UnmarshallResultEater;
import org.kalypsodeegree.model.geometry.GM_MultiCurve;
import org.kalypsodeegree_impl.io.sax.marshaller.MultiLineStringMarshaller;
import org.kalypsodeegree_impl.io.sax.parser.MultiLineStringContentHandler;
import org.kalypsodeegree_impl.tools.GMLConstants;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

/**
 * @author Felipe Maximino
 *
 */
public class MultiLineStringHandler implements IMarshallingTypeHandler2
{
  private static final QName QNAME_TYPE = GMLConstants.QN_MULTI_LINE_STRING;

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler2#createContentHandler(org.xml.sax.XMLReader, org.xml.sax.ContentHandler, org.kalypso.gmlschema.types.UnmarshallResultEater, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  public ContentHandler createContentHandler( XMLReader xmlReader, ContentHandler parentContentHandler, UnmarshallResultEater resultEater )
  {
    return new MultiLineStringContentHandler( resultEater, parentContentHandler, xmlReader );
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#cloneObject(java.lang.Object, java.lang.String)
   */
  @Override
  public Object cloneObject( Object objectToClone, String gmlVersion ) throws CloneNotSupportedException
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
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#marshal(java.lang.Object, org.xml.sax.XMLReader, java.net.URL, java.lang.String)
   */
  @Override
  public void marshal( Object value, XMLReader xmlReader, URL context, String gmlVersion )
  { 
    new MultiLineStringMarshaller( xmlReader, (GM_MultiCurve) value );
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#parseType(java.lang.String)
   */
  @Override
  public Object parseType( String text )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#unmarshal(org.xml.sax.XMLReader, java.net.URL, org.kalypso.gmlschema.types.UnmarshallResultEater, java.lang.String)
   */
  @Override
  public void unmarshal( XMLReader xmlReader, URL context, UnmarshallResultEater marshalResultEater, String gmlVersion )
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
    return GM_MultiCurve.class; 
  }

  /**
   * @see org.kalypso.gmlschema.types.ITypeHandler#isGeometry()
   */
  @Override
  public boolean isGeometry( )
  {
    return true;
  }
}
