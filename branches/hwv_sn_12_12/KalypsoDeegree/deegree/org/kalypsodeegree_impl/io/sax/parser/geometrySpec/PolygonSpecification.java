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
package org.kalypsodeegree_impl.io.sax.parser.geometrySpec;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.types.IGmlContentHandler;
import org.kalypsodeegree_impl.io.sax.parser.ExteriorContentHandler;
import org.kalypsodeegree_impl.io.sax.parser.IRingHandler;
import org.kalypsodeegree_impl.io.sax.parser.InnerBoundaryContentHandler;
import org.kalypsodeegree_impl.io.sax.parser.InteriorContentHandler;
import org.kalypsodeegree_impl.io.sax.parser.OuterBoundaryContentHandler;
import org.xml.sax.XMLReader;

/**
 * @author Gernot Belger
 */
public class PolygonSpecification implements IGeometrySpecification
{
  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.geometrySpec.IGeometrySpecification#getHandler(javax.xml.namespace.QName,
   *      org.xml.sax.XMLReader, org.kalypso.gmlschema.types.IGmlContentHandler,
   *      org.kalypso.gmlschema.types.IGmlContentHandler, java.lang.String)
   */
  @Override
  public IGmlContentHandler getHandler( final QName property, final XMLReader reader, final IGmlContentHandler parent, final IGmlContentHandler receiver, final String defaultSrs )
  {
    /* gml:exterior */
    if( ExteriorContentHandler.QNAME_EXTERIOR.equals( property ) )
      return new ExteriorContentHandler( reader, parent, (IRingHandler) receiver, defaultSrs );

    /* gml:outerBoundaryIs */
    if( OuterBoundaryContentHandler.QNAME_OUTER_BOUNDARY.equals( property ) )
      return new OuterBoundaryContentHandler( reader, parent, (IRingHandler) receiver, defaultSrs );

    /* gml:interior */
    if( InteriorContentHandler.QNAME_INTERIOR.equals( property ) )
      return new InteriorContentHandler( reader, parent, (IRingHandler) receiver, defaultSrs );

    /* gml:innerBoundaryIs */
    if( InnerBoundaryContentHandler.QNAME_INNER_BOUNDARY.equals( property ) )
      return new InnerBoundaryContentHandler( reader, parent, (IRingHandler) receiver, defaultSrs );

    return null;
  }
}
