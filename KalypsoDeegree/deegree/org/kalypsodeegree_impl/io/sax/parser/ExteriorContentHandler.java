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
package org.kalypsodeegree_impl.io.sax.parser;

import javax.xml.namespace.QName;

import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.types.IGmlContentHandler;
import org.xml.sax.XMLReader;

/**
 * A content handler which parses a gml:exterior element.<br>
 * 
 * @author Gernot Belger
 * @author Felipe Maximino
 */
public class ExteriorContentHandler extends AbstractRingPropertyContentHandler
{
  public static final String ELEMENT_EXTERIOR = "exterior";

  public static final QName QNAME_EXTERIOR = new QName( NS.GML3, ELEMENT_EXTERIOR );

  public ExteriorContentHandler( final XMLReader reader, final IGmlContentHandler parent, final IRingHandler exteriorHandler, final String defaultSrs )
  {
    super( reader, QNAME_EXTERIOR, parent, exteriorHandler, defaultSrs, 1 );
  }
}