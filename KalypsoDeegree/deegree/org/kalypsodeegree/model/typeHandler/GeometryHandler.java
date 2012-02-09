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

import org.kalypso.gmlschema.types.IGmlContentHandler;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler2;
import org.kalypso.gmlschema.types.UnmarshallResultEater;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.io.sax.marshaller.GeometryMarshaller;
import org.kalypsodeegree_impl.io.sax.marshaller.GeometryMemberMarshaller;
import org.kalypsodeegree_impl.io.sax.parser.GeometryContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author Felipe Maximino
 */
public class GeometryHandler implements IMarshallingTypeHandler2
{
  @Override
  public IGmlContentHandler createContentHandler( final XMLReader reader, final IGmlContentHandler parentContentHandler, final UnmarshallResultEater resultEater )
  {
    return new GeometryContentHandler( reader, resultEater, parentContentHandler, KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );
  }

  @Override
  public Object cloneObject( final Object objectToClone, final String gmlVersion ) throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException();
  }

  @Override
  public void marshal( final Object value, final XMLReader reader, final URL context, final String gmlVersion ) throws SAXException
  {
    final GeometryMarshaller<GM_Object> marshaller = (GeometryMarshaller<GM_Object>) GeometryMemberMarshaller.createMarshaller( reader, (GM_Object) value );
    marshaller.marshall( (GM_Object) value );
  }

  @Override
  public Object parseType( final String text )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void unmarshal( final XMLReader reader, final URL context, final UnmarshallResultEater marshalResultEater, final String gmlVersion )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public QName getTypeName( )
  {
    return GM_Object.GEOMETRY_ELEMENT;
  }

  @Override
  public Class< ? > getValueClass( )
  {
    return GM_Object.class;
  }

  @Override
  public boolean isGeometry( )
  {
    return true;
  }
}
