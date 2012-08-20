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
package org.kalypso.gml.processes.tin;

import com.bce.gis.io.hmo.HMOReader.ITriangleReceiver;
import com.bce.gis.io.zweidm.IPolygonWithName;
import com.bce.gis.io.zweidm.SmsElement;
import com.bce.gis.io.zweidm.SmsModel;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author Holger Albert
 */
public class TriangulatedSurfaceSmsModel extends SmsModel
{
  public TriangulatedSurfaceSmsModel( final int srid, final ITriangleReceiver receiver )
  {
    super( srid );
  }

  /**
   * @see com.bce.gis.io.zweidm.SmsModel#addElement(java.lang.String, int, java.lang.Integer[], int)
   */
  @Override
  public void addElement( final String lineString, final int id, final Integer[] nodeIds, final int roughnessClassID )
  {
    final SmsElement smsElement = new SmsElement( this, id, nodeIds );
    final IPolygonWithName surface = smsElement.toSurface();
    final Polygon polygon = surface.getPolygon();
    final LineString exteriorRing = polygon.getExteriorRing();
    final Coordinate[] coordinates = exteriorRing.getCoordinates();

    // TODO
  }
}