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

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.io.sax.marshaller.GmlElementXmlReader;
import org.kalypsodeegree_impl.io.sax.marshaller.MarshallerInputSource;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Factory class to wrap from binding geometries to GM_Object geometries and visa versa
 *
 * @author doemming
 */
public class AdapterValueToBinding implements AdapterValueToGMLBinding
{
  @Override
  public Element wrapToElement( final GM_Object geometry ) throws TransformerException
  {
    final TransformerFactory tf = TransformerFactory.newInstance();
    final Transformer transformer = tf.newTransformer();

    final InputSource is = new MarshallerInputSource<>( geometry );

    final XMLReader reader = new GmlElementXmlReader();

    final DOMResult domResult = new DOMResult();
    transformer.transform( new SAXSource( reader, is ), domResult );

    return (Element) domResult.getNode();
  }
}