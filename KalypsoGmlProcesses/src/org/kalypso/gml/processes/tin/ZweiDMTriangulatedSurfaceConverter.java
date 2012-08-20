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
package org.kalypso.gml.processes.tin;

import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.gml.processes.KalypsoGmlProcessesPlugin;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;
import org.kalypsodeegree_impl.model.geometry.GM_TriangulatedSurface_Impl;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.bce.gis.io.zweidm.IPolygonWithName;
import com.bce.gis.io.zweidm.ISmsModel;
import com.bce.gis.io.zweidm.SmsElement;
import com.bce.gis.io.zweidm.SmsParser;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author Holger Albert
 */
public class ZweiDMTriangulatedSurfaceConverter extends AbstractTriangulatedSurfaceConverter
{
  public final String m_sourceSrs;

  public ZweiDMTriangulatedSurfaceConverter( final String sourceSrs )
  {
    m_sourceSrs = sourceSrs;
  }

  @Override
  public GM_TriangulatedSurface convert( final URL sourceLocation, IProgressMonitor monitor ) throws CoreException
  {
    /* Monitor. */
    if( monitor == null )
      monitor = new NullProgressMonitor();

    try
    {
      /* Monitor. */
      monitor.beginTask( "Copying input data", 100 );
      monitor.subTask( "Copying input data..." );

      /* Coordinate system of the 2DM file. */
      final int sourceSrid = JTSAdapter.toSrid( m_sourceSrs );

      /* Read the input data. */
      final SmsParser parser = new SmsParser( sourceSrid );
      parser.parse( sourceLocation, new SubProgressMonitor( monitor, 50 ) );

      /* Monitor. */
      monitor.subTask( "Creating triangulated surface..." );

      /* Create the triangulated surface. */
      final GM_TriangulatedSurface gmSurface = new GM_TriangulatedSurface_Impl( m_sourceSrs );

      /* Add the triangles. */
      final ISmsModel model = parser.getModel();
      final List<SmsElement> elementList = model.getElementList();
      for( final SmsElement smsElement : elementList )
      {
        final IPolygonWithName surface = smsElement.toSurface();
        final Polygon polygon = surface.getPolygon();
        final LineString exteriorRing = polygon.getExteriorRing();
        final Coordinate[] coordinates = exteriorRing.getCoordinates();

        /* HINT: Triangle, Points 0,1,2,0. */
        if( coordinates.length == 4 )
        {
          addTriangle( gmSurface, coordinates[0], coordinates[1], coordinates[2], m_sourceSrs );
          continue;
        }

        /* HINT: Rectangle, Points 0,1,2,3,0. */
        if( coordinates.length == 5 )
        {
          addTriangle( gmSurface, coordinates[0], coordinates[1], coordinates[2], m_sourceSrs );
          addTriangle( gmSurface, coordinates[2], coordinates[3], coordinates[0], m_sourceSrs );
          continue;
        }

        throw new IllegalStateException( String.format( "Expected 4 or 5 coordinates, got %d...", coordinates.length ) );
      }

      /* Monitor. */
      monitor.worked( 50 );

      return gmSurface;
    }
    catch( final Exception ex )
    {
      throw new CoreException( new Status( IStatus.ERROR, KalypsoGmlProcessesPlugin.PLUGIN_ID, ex.getLocalizedMessage(), ex ) );
    }
    finally
    {
      /* Monitor. */
      monitor.done();
    }
  }
}