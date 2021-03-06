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

import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A marshaller for gml:TriangulatedSurfaces
 * 
 * @author Gernot Belger
 * @author Felipe Maximino - Refaktoring
 */
public class TriangulatedSurfaceMarshaller extends AbstractSurfaceMarshaller<GM_TriangulatedSurface>
{
  private static final String TAG_TRIANGULATED_SURFACE = "TriangulatedSurface";

  private final TrianglePatchesMarshaller m_patchesMarshaller;

  public TriangulatedSurfaceMarshaller( final XMLReader reader, final String defaultCRS )
  {
    super( reader, TAG_TRIANGULATED_SURFACE );

    m_patchesMarshaller = new TrianglePatchesMarshaller( reader, defaultCRS );
  }

  @Override
  protected void doMarshallContent( final GM_TriangulatedSurface marshalledObject ) throws SAXException
  {
    m_patchesMarshaller.marshall( marshalledObject );
  }

  public void marshallTriangle( final GM_Triangle triangle ) throws SAXException
  {
    final SurfacePatchMarshaller<GM_Triangle> triangleMarshaller = m_patchesMarshaller.createPatchMarshaller();
    triangleMarshaller.marshall( triangle );
  }

  public void startSurface( final GM_TriangulatedSurface surface, final Attributes atts ) throws SAXException
  {
    startMarshalling( surface, atts );
    m_patchesMarshaller.startMarshalling( surface );
  }

  public void endSurface( ) throws SAXException
  {
    m_patchesMarshaller.endMarshalling();
    endMarshalling();
  }
}
