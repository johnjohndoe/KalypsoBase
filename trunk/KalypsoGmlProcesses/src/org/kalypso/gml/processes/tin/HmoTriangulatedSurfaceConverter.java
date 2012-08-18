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

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.java.util.PropertiesUtilities;
import org.kalypso.gml.processes.KalypsoGmlProcessesPlugin;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;
import org.kalypsodeegree_impl.model.geometry.GM_TriangulatedSurface_Impl;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.bce.gis.io.hmo.HMOReader;
import com.bce.gis.io.hmo.HMOReader.ITriangleReceiver;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author Holger Albert
 */
public class HmoTriangulatedSurfaceConverter extends AbstractTriangulatedSurfaceConverter
{
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

      // FIXME: check: is crs in source??! Probably we get srs from outside!
      final Properties properties = PropertiesUtilities.collectProperties( sourceLocation.getQuery(), "&", "=", null ); //$NON-NLS-1$ //$NON-NLS-2$

      final String sourceSRS = properties.getProperty( "srs" ); //$NON-NLS-1$

      final String targetSRS = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();

      final GM_TriangulatedSurface gmSurface = new GM_TriangulatedSurface_Impl( targetSRS );

      // FIXME: why so complicated?
      final URL hmoLocation = new URL( sourceLocation.getProtocol() + ":" + sourceLocation.getPath() ); //$NON-NLS-1$

      final ITriangleReceiver receiver = new ITriangleReceiver()
      {
        @Override
        public void add( final Coordinate c0, final Coordinate c1, final Coordinate c2 )
        {
          try
          {
            addTriangle( gmSurface, c0, c1, c2, sourceSRS );
          }
          catch( final Exception e )
          {
            e.printStackTrace();
          }
        }
      };

      /* read hmo */
      // FIXME: stream never closed
      final Reader r = new InputStreamReader( hmoLocation.openStream() );
      final HMOReader hmoReader = new HMOReader();
      hmoReader.read( r, receiver );

      /* Monitor. */
      monitor.done();

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

  static void addTriangle( final GM_TriangulatedSurface surface, final Coordinate c0, final Coordinate c1, final Coordinate c2, final String sourceSRS ) throws Exception
  {
    final GM_Position p0 = JTSAdapter.wrap( c0 );
    final GM_Position p1 = JTSAdapter.wrap( c1 );
    final GM_Position p2 = JTSAdapter.wrap( c2 );

    /* Transform into srs of surface */
    final String targetSRS = surface.getCoordinateSystem();

    final GM_Position t0 = p0.transform( sourceSRS, targetSRS );
    final GM_Position t1 = p1.transform( sourceSRS, targetSRS );
    final GM_Position t2 = p2.transform( sourceSRS, targetSRS );

    // For shape export we need a clockwise orientation.
    // The algorithm that delivers the nodes is the nodes to the eater is thinking counter-clockwise.
    // For that reason we switch the positions order to get that clockwise orientation.
    final GM_Triangle gmTriangle = GeometryFactory.createGM_Triangle( t0, t1, t2, targetSRS );
    surface.add( gmTriangle );
  }
}