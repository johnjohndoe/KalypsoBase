/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 *
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always.
 *
 * If you intend to use this software in other ways than in kalypso
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree,
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.model.geometry;

import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import ogc31.www.opengis.net.gml.DirectPositionType;
import ogc31.www.opengis.net.gml.EnvelopeType;

import org.kalypso.contribs.ogc31.KalypsoOGC31JAXBcontext;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * factory class to wrap from binding geometries to GM_Object geometries and visa versa
 *
 * @author doemming
 */
public class AdapterValueToBinding_GML31 implements AdapterValueToGMLBinding
{
  @Override
  public Object wrapToBinding( final GM_Object geometry )
  {
    throw new UnsupportedOperationException( geometry.getClass().getName() + " is not supported" );
  }

  @Override
  public Element wrapToElement( final GM_Object geometry ) throws GM_Exception
  {
    try
    {
      final Object bindingGeometry = wrapToBinding( geometry );
      final Marshaller marshaller = KalypsoOGC31JAXBcontext.getContext().createMarshaller();
      final DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
      fac.setNamespaceAware( true );
      final DocumentBuilder builder = fac.newDocumentBuilder();
      final Document document = builder.newDocument();
      marshaller.marshal( bindingGeometry, document );
      return document.getDocumentElement();
    }
    catch( final Exception e )
    {
      throw new GM_Exception( "could not marshall to Element", e );
    }
  }

  @Override
  public Object wrapToBinding( final GM_Envelope envelope )
  {
    final GM_Position min = envelope.getMin();
    final GM_Position max = envelope.getMax();

    final EnvelopeType envelopeType = KalypsoOGC31JAXBcontext.GML3_FAC.createEnvelopeType();

    final DirectPositionType lowerCorner = KalypsoOGC31JAXBcontext.GML3_FAC.createDirectPositionType();
    final DirectPositionType upperCorner = KalypsoOGC31JAXBcontext.GML3_FAC.createDirectPositionType();

    final List<Double> lowers = lowerCorner.getValue();
    lowers.clear();
    lowers.add( min.getX() );
    lowers.add( min.getY() );

    final List<Double> uppers = upperCorner.getValue();
    uppers.clear();
    uppers.add( max.getX() );
    uppers.add( max.getY() );

    envelopeType.setLowerCorner( lowerCorner );
    envelopeType.setUpperCorner( upperCorner );

    envelopeType.setSrsName( envelope.getCoordinateSystem() );

    return envelopeType;
  }

  @Override
  public JAXBElement< ? extends Object> createJAXBGeometryElement( final Object geometry )
  {
    throw new UnsupportedOperationException();
  }
}
