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
package org.kalypsodeegree_impl.io.sax.marshaller;

import org.kalypsodeegree.model.geometry.GM_PolygonPatch;
import org.kalypsodeegree.model.geometry.GM_PolyhedralSurface;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A marshaller for gml:PolyhedralSurfaces
 * 
 * @author Gernot Belger
 * @author Felipe Maximino - Refaktoring
 */
public class PolyhedralSurfaceMarshaller extends AbstractSurfaceMarshaller<GM_PolyhedralSurface<GM_PolygonPatch>>
{
  private static final String TAG_POLYHEDRAL_SURFACE = "PolyhedralSurface"; //$NON-NLS-1$

  public PolyhedralSurfaceMarshaller( final XMLReader reader )
  {
    super( reader, TAG_POLYHEDRAL_SURFACE );
  }

  @Override
  protected void doMarshallContent( final GM_PolyhedralSurface<GM_PolygonPatch> marshalledObject ) throws SAXException
  {
    final PolygonPatchesMarshaller patchesMarshaller = new PolygonPatchesMarshaller( getXMLReader(), marshalledObject.getCoordinateSystem() );
    patchesMarshaller.marshall( marshalledObject );
  }
}
