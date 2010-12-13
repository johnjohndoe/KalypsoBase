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

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;

import org.xml.sax.XMLReader;

/**
 * Type handler for list-simple types.
 *
 * @author Gernot Belger
 */
public class ListSimpleTypeHandler implements ISimpleMarshallingTypeHandler<List< ? >>
{
  private final ISimpleMarshallingTypeHandler<Object> m_baseTypeHandler;

  @SuppressWarnings("unchecked")
  public ListSimpleTypeHandler( final IMarshallingTypeHandler baseTypeHandler )
  {
    if( !(baseTypeHandler instanceof ISimpleMarshallingTypeHandler) )
      throw new IllegalArgumentException( "List may be used only on simple types. Not on complex types: " + baseTypeHandler ); //$NON-NLS-1$

    m_baseTypeHandler = (ISimpleMarshallingTypeHandler<Object>) baseTypeHandler;
  }

  /**
   * @see org.kalypso.gmlschema.types.ISimpleMarshallingTypeHandler#convertToJavaValue(java.lang.String)
   */
  @Override
  public List< ? > convertToJavaValue( final String xmlString )
  {
    if( xmlString == null )
      return null;

    final List<Object> list = new ArrayList<Object>();

    final StringTokenizer st = new StringTokenizer( xmlString );    
    while ( st.hasMoreTokens() )
    {
      final String token = st.nextToken();
      final Object object = m_baseTypeHandler.convertToJavaValue( token );
      list.add( object );  
    }

    return list;
  }

  /**
   * @see org.kalypso.gmlschema.types.ISimpleMarshallingTypeHandler#convertToXMLString(java.lang.Object)
   */
  @Override
  public String convertToXMLString( final List< ? > value )
  {
    final StringBuffer result = new StringBuffer();
    for( final Iterator< ? > iter = value.iterator(); iter.hasNext(); )
    {
      final Object element = iter.next();

      final String xmlString = m_baseTypeHandler.convertToXMLString( element );
      result.append( xmlString );

      if( iter.hasNext() )
        result.append( ' ' );
    }

    return result.toString();
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#cloneObject(java.lang.Object, java.lang.String)
   */
  @Override
  public Object cloneObject( final Object objectToClone, final String gmlVersion ) throws CloneNotSupportedException
  {
    if( objectToClone == null )
      return null;

    final List< ? > list = (List< ? >) objectToClone;
    final List<Object> clonedList = new ArrayList<Object>( list.size() );

    for( final Object object : list )
      clonedList.add( m_baseTypeHandler.cloneObject( object, gmlVersion ) );

    return clonedList;
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#getShortname()
   */
  @Override
  public String getShortname( )
  {
    return m_baseTypeHandler.getShortname();
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#marshal(java.lang.Object, org.xml.sax.XMLReader,
   *      java.net.URL, java.lang.String)
   */
  @Override
  public void marshal( final Object value, final XMLReader reader, final URL context, final String gmlVersion )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void unmarshal( final XMLReader reader, final URL context, final UnmarshallResultEater marshalResultEater, final String gmlVersion )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#parseType(java.lang.String)
   */
  @Override
  public Object parseType( final String text )
  {
    return convertToJavaValue( text );
  }

  /**
   * @see org.kalypso.gmlschema.types.ITypeHandler#getTypeName()
   */
  @Override
  public QName getTypeName( )
  {
    return m_baseTypeHandler.getTypeName();
  }

  /**
   * @see org.kalypso.gmlschema.types.ITypeHandler#getValueClass()
   */
  @Override
  public Class< ? > getValueClass( )
  {
    return List.class;
  }

  /**
   * @see org.kalypso.gmlschema.types.ITypeHandler#isGeometry()
   */
  @Override
  public boolean isGeometry( )
  {
    return m_baseTypeHandler.isGeometry();
  }

}
