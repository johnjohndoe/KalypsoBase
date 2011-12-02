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
import org.kalypsodeegree_impl.io.sax.parser.CoordContentHandler;
import org.kalypsodeegree_impl.io.sax.parser.CoordinatesContentHandler;
import org.kalypsodeegree_impl.io.sax.parser.ICoordinatesHandler;
import org.kalypsodeegree_impl.io.sax.parser.IPositionHandler;
import org.kalypsodeegree_impl.io.sax.parser.PosContentHandler;
import org.kalypsodeegree_impl.io.sax.parser.PosListContentHandler;
import org.kalypsodeegree_impl.tools.GMLConstants;
import org.xml.sax.XMLReader;

/**
 * @author Felipe Maximino
 */
public class LinearRingSpecification implements IGeometrySpecification
{
  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.geometrySpec.IGeometrySpecification#getHandler(javax.xml.namespace.QName,
   *      org.xml.sax.XMLReader, org.kalypso.gmlschema.types.IGmlContentHandler,
   *      org.kalypso.gmlschema.types.IGmlContentHandler, java.lang.String)
   */
  @Override
  public IGmlContentHandler getHandler( final QName property, final XMLReader reader, final IGmlContentHandler parent, final IGmlContentHandler receiver, final String defaultSrs )
  {
    if( GMLConstants.QN_POS.equals( property ) )
      return new PosContentHandler( reader, parent, (IPositionHandler) receiver, defaultSrs );

    /* gml:posList */
    if( GMLConstants.QN_POS_LIST.equals( property ) )
      return new PosListContentHandler( reader, parent, (IPositionHandler) receiver, defaultSrs );

    /* gml:coordinates for gml:LinearRing is deprecated with GML Version 3.1.0 */
    if( GMLConstants.QN_COORDINATES.equals( property ) )
      return new CoordinatesContentHandler( reader, parent, (ICoordinatesHandler) receiver, defaultSrs );

    /* gml:coord for gml:LinearRing is deprecated with GML Version 3.0 */
    if( GMLConstants.QN_COORD.equals( property ) )
      return new CoordContentHandler( reader, parent, (ICoordinatesHandler) receiver, defaultSrs );

    return null;
  }
}
