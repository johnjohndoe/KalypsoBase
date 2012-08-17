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
package org.kalypso.gml.processes.converters;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.java.util.PropertiesUtilities;
import org.kalypso.gml.processes.KalypsoGmlProcessesPlugin;
import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;
import org.kalypsodeegree_impl.model.geometry.GM_TriangulatedSurface_Impl;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.bce.gis.io.hmo.HMOReader;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * @author Holger Albert
 */
public class HmoTriangulatedSurfaceConverter extends AbstractTriangulatedSurfaceConverter
{
  public HmoTriangulatedSurfaceConverter( )
  {
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

      final Properties properties = PropertiesUtilities.collectProperties( sourceLocation.getQuery(), "&", "=", null ); //$NON-NLS-1$ //$NON-NLS-2$
      final String crs = properties.getProperty( "srs" ); //$NON-NLS-1$
      final GM_TriangulatedSurface gmSurface = new GM_TriangulatedSurface_Impl( crs );

      final IGeoTransformer transformer = GeoTransformerFactory.getGeoTransformer( KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );
      final URL hmoLocation = new URL( sourceLocation.getProtocol() + ":" + sourceLocation.getPath() ); //$NON-NLS-1$
      final HMOReader hmoReader = new HMOReader( new GeometryFactory() );
      final Reader r = new InputStreamReader( hmoLocation.openStream() );
      final LinearRing[] rings = hmoReader.read( r );

      int count = 1;
      for( final LinearRing ring : rings )
      {
        /* Monitor. */
        monitor.subTask( String.format( "Importing triangles: %d / %d", count++, rings.length ) );

        final List<GM_Point> pointList = new LinkedList<GM_Point>();
        for( int i = 0; i < ring.getNumPoints() - 1; i++ )
        {
          final GM_Object object = JTSAdapter.wrap( ring.getPointN( i ), crs );
          final GM_Point point = (GM_Point) object;
          point.setCoordinateSystem( crs );
          pointList.add( (GM_Point) transformer.transform( point ) );
        }

        addPoints( gmSurface, pointList );
      }

      /* Monitor. */
      monitor.worked( 100 );

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

  private void addPoints( final GM_TriangulatedSurface gmSurface, final List<GM_Point> pointList ) throws GM_Exception
  {
    final String crs = pointList.get( 0 ).getCoordinateSystem();
    final GM_Position pos[] = new GM_Position[3];
    for( int i = 0; i < pointList.size(); i++ )
    {
      final GM_Point point = pointList.get( i );
      pos[i] = org.kalypsodeegree_impl.model.geometry.GeometryFactory.createGM_Position( point.getX(), point.getY(), point.getZ() );
    }

    // For shape export we need a clockwise orientation.
    // The algorithm that delivers the nodes is the nodes to the eater is thinking counter-clockwise.
    // For that reason we switch the positions order to get that clockwise orientation.
    final GM_Triangle gmTriangle = org.kalypsodeegree_impl.model.geometry.GeometryFactory.createGM_Triangle( pos[2], pos[1], pos[0], crs );
    if( gmTriangle != null )
      gmSurface.add( gmTriangle );
  }
}