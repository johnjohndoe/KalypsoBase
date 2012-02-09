/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.sensor.deegree;

import java.net.URL;

import javax.xml.namespace.QName;

import org.kalypso.contribs.java.lang.reflect.ClassUtilities;
import org.kalypso.gmlschema.types.IGmlContentHandler;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler2;
import org.kalypso.gmlschema.types.UnmarshallResultEater;
import org.kalypso.zml.obslink.ObjectFactory;
import org.kalypso.zml.obslink.TimeseriesLinkFeatureProperty;
import org.kalypso.zml.obslink.TimeseriesLinkType;
import org.kalypsodeegree_impl.gml.schema.schemata.DeegreeUrlCatalog;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author belger
 */
public class ObservationLinkHandler implements IMarshallingTypeHandler2
{
  public static final QName TYPE_NAME = new QName( DeegreeUrlCatalog.NAMESPACE_ZML_OBSLINK, "TimeseriesLink" ); //$NON-NLS-1$

  public static final QName PROPERTY_NAME = new QName( DeegreeUrlCatalog.NAMESPACE_ZML_OBSLINK, ClassUtilities.getOnlyClassName( TimeseriesLinkFeatureProperty.class ) );

  private final static ObjectFactory m_factory = new ObjectFactory();

  @Override
  public Class< ? > getValueClass( )
  {
    return TimeseriesLinkType.class;
  }

  @Override
  public QName getTypeName( )
  {
    return PROPERTY_NAME;
  }

  @Override
  public IGmlContentHandler createContentHandler( final XMLReader reader, final IGmlContentHandler parentContentHandler, final UnmarshallResultEater resultEater )
  {
    return new ObslinkContentHandler( reader, parentContentHandler, resultEater );
  }

  @Override
  public void marshal( final Object value, final XMLReader reader, final URL context, final String gmlVersion ) throws SAXException
  {
    new ObservationLinkMarshaller( reader ).marshall( (TimeseriesLinkType) value );
  }

  @Override
  public void unmarshal( final XMLReader reader, final URL context, final UnmarshallResultEater marshalResultEater, final String gmlVersion )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object cloneObject( final Object objectToClone, final String gmlVersion )
  {
    final TimeseriesLinkType link = (TimeseriesLinkType) objectToClone;
    final TimeseriesLinkType clone = m_factory.createTimeseriesLinkType();
    clone.setActuate( link.getActuate() );
    clone.setArcrole( link.getArcrole() );
    clone.setHref( link.getHref() );
    clone.setLinktype( link.getLinktype() );
    clone.setRole( link.getRole() );
    clone.setShow( link.getShow() );
    clone.setTimeaxis( link.getTimeaxis() );
    clone.setTitle( link.getTitle() );
    clone.setType( link.getType() );
    clone.setValueaxis( link.getValueaxis() );
    return clone;
  }

  @Override
  public Object parseType( final String text )
  {
    final org.kalypso.zml.obslink.ObjectFactory factory = new org.kalypso.zml.obslink.ObjectFactory();
    final TimeseriesLinkType link = factory.createTimeseriesLinkType();
    link.setHref( text );
    return link;
  }

  @Override
  public boolean isGeometry( )
  {
    return false;
  }
}