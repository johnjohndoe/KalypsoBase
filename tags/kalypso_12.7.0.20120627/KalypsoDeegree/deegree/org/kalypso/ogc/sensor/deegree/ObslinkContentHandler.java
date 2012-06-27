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
package org.kalypso.ogc.sensor.deegree;

import org.apache.commons.lang3.StringUtils;
import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.types.IGmlContentHandler;
import org.kalypso.gmlschema.types.UnmarshallResultEater;
import org.kalypso.zml.obslink.TimeseriesLinkType;
import org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * @author Gernot Belger
 */
public class ObslinkContentHandler extends GMLElementContentHandler
{
  private final UnmarshallResultEater m_resultEater;

  public ObslinkContentHandler( final XMLReader reader, final IGmlContentHandler parent, final UnmarshallResultEater resultEater )
  {
    super( reader, ObservationLinkHandler.TYPE_NAME.getNamespaceURI(), ObservationLinkHandler.TYPE_NAME.getLocalPart(), parent );

    m_resultEater = resultEater;
  }

  @Override
  protected void doEndElement( final String uri, final String localName, final String name )
  {
  }

  @Override
  public void handleUnexpectedEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    /* Happens if element was empty */
    activateParent();
    getParentContentHandler().endElement( uri, localName, name );
  }

  @Override
  protected void doStartElement( final String uri, final String localName, final String name, final Attributes atts ) throws SAXParseException
  {
    final TimeseriesLinkType link = new TimeseriesLinkType();

    // String value = atts.getValue( NS.XLINK, "linktype" ); //$NON-NLS-1$
    // String value = atts.getValue( NS.XLINK, "timeaxis" ); //$NON-NLS-1$
    // String value = atts.getValue( NS.XLINK, "valueaxis" ); //$NON-NLS-1$

    final String href = atts.getValue( NS.XLINK, "href" ); //$NON-NLS-1$
    if( !StringUtils.isEmpty( href ) )
      link.setHref( href );

    final String actuate = atts.getValue( NS.XLINK, "actuate" ); //$NON-NLS-1$
    if( !StringUtils.isEmpty( actuate ) )
      link.setActuate( actuate );

    final String arcrole = atts.getValue( NS.XLINK, "arcrole" ); //$NON-NLS-1$
    if( !StringUtils.isEmpty( arcrole ) )
      link.setArcrole( arcrole );

    final String role = atts.getValue( NS.XLINK, "role" ); //$NON-NLS-1$
    if( !StringUtils.isEmpty( role ) )
      link.setRole( role );

    final String show = atts.getValue( NS.XLINK, "show" ); //$NON-NLS-1$
    if( !StringUtils.isEmpty( show ) )
      link.setShow( show );

    final String title = atts.getValue( NS.XLINK, "title" ); //$NON-NLS-1$
    if( !StringUtils.isEmpty( title ) )
      link.setTitle( title );

    final String type = atts.getValue( NS.XLINK, "type" ); //$NON-NLS-1$
    if( !StringUtils.isEmpty( type ) )
      link.setType( type );

    m_resultEater.unmarshallSuccesful( link );
  }
}
