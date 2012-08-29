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
package org.kalypso.gml.processes.constDelaunay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.datalocation.Location;
import org.kalypso.commons.KalypsoCommonsExtensions;
import org.kalypso.commons.process.IProcess;
import org.kalypso.commons.process.IProcessFactory;
import org.kalypso.contribs.java.lang.ICancelable;
import org.kalypso.gml.processes.i18n.Messages;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_LineString;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree.model.geometry.GM_SurfacePatch;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/**
 * Wrapper around the triangle.exe.
 *
 * @author Gernot Belger
 */
public class TriangleExe
{
  private final String m_crs;

  private final String[] m_triangleArgs;

  // TODO: resue calc core code
  public static File findTriangleExe( )
  {
    final Location installLocation = Platform.getInstallLocation();
    final File installDir = FileUtils.toFile( installLocation.getURL() );
    final File exeDir = new File( installDir, "bin" ); //$NON-NLS-1$
    return new File( exeDir, "triangle.exe" ); //$NON-NLS-1$
  }

  public TriangleExe( final String crs, final String[] triangleArgs )
  {
    m_crs = crs;
    m_triangleArgs = triangleArgs;
  }

  // FIXME: refaktor, this is awful!
  public GM_Triangle[] triangulate( final GM_Position[] exterior, final GM_Curve[] breaklines )
  {
    final File triangleExe = findTriangleExe();
    if( triangleExe == null || !triangleExe.isFile() )
      throw new IllegalStateException( "triangle.exe not found" ); //$NON-NLS-1$

    // FIXME: put into separate class!
    BufferedReader nodeReader = null;
    BufferedReader eleReader = null;
    PrintStream pwSimuLog;

    final List<GM_Triangle> triangles = new LinkedList<GM_Triangle>();

    /* prepare */
    final List<TriangleVertex> nodeList = new LinkedList<TriangleVertex>();
    final List<TriangleSegment> segmentList = new LinkedList<TriangleSegment>();

    int i = 0;

    final List<Double> emptyArgs = Collections.emptyList();

    if( exterior.length >= 3 )
    {

      // handle the polygon
      for( ; i < exterior.length - 2; i++ )
      {
        final TriangleVertex vertex = new TriangleVertex( exterior[i], true, emptyArgs );
        nodeList.add( vertex );
        final TriangleSegment segment = new TriangleSegment( i, i + 1, true );
        segmentList.add( segment );
      }

      final TriangleVertex vertex = new TriangleVertex( exterior[i], true, emptyArgs );
      nodeList.add( vertex );
      final TriangleSegment segment = new TriangleSegment( i, 0, true );
      segmentList.add( segment );
    }

    if( breaklines != null )
    {
      // handle the breaklines
      for( final GM_Curve curve : breaklines )
      {
        i++;
        try
        {
          final GM_LineString lineString = curve.getAsLineString();
          final GM_Position[] positions = lineString.getPositions();
          for( int j = 0; j < positions.length - 2; j++ )
          {
            final TriangleVertex vertex = new TriangleVertex( positions[j], false, emptyArgs );
            nodeList.add( vertex );
            final TriangleSegment segment = new TriangleSegment( i, i + 1, false );
            segmentList.add( segment );
            i++;
          }
          final TriangleVertex vertex = new TriangleVertex( positions[positions.length - 1], false, emptyArgs );
          nodeList.add( vertex );
        }
        catch( final GM_Exception e )
        {
          e.printStackTrace();
        }
      }
    }

    // collect the data
    final TrianglePolyFileData trianglePolyFileData = new TrianglePolyFileData( nodeList, segmentList, null );

    File tempDir = null;
    try
    {
      pwSimuLog = System.out;

      final String polyFileName = "input.poly"; //$NON-NLS-1$

      final String[] args = Arrays.copyOf( m_triangleArgs, m_triangleArgs.length + 2 );
      args[m_triangleArgs.length] = "-p"; //$NON-NLS-1$
      args[m_triangleArgs.length + 1] = polyFileName;
      final IProcess process = KalypsoCommonsExtensions.createProcess( IProcessFactory.DEFAULT_PROCESS_FACTORY_ID, "Triangle", triangleExe.getName(), args );//$NON-NLS-1$

      tempDir = new File( new URL( process.getSandboxDirectory() ).getFile() );
      FileUtils.copyFileToDirectory( triangleExe, tempDir );

      final File polyfile = new File( tempDir, polyFileName );

      // prepare the polygon for output
      final IStatus writeStatus = trianglePolyFileData.writePolyFile( polyfile );

      if( writeStatus != Status.OK_STATUS )
        return null;

      // start Triangle
      process.startProcess( pwSimuLog, System.err, System.in, new ICancelable()
      {

        @Override
        public boolean isCanceled( )
        {
          return false;
        }

        @Override
        public void cancel( )
        {
        }
      } );

      // get the triangle list
      final File nodeFile = new File( tempDir, "input.1.node" ); //$NON-NLS-1$
      final File eleFile = new File( tempDir, "input.1.ele" ); //$NON-NLS-1$

      if( !nodeFile.exists() || !eleFile.exists() )
      {
        pwSimuLog.append( Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayHelper.31" ) ); //$NON-NLS-1$
        pwSimuLog.append( Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayHelper.32" ) ); //$NON-NLS-1$
        pwSimuLog.append( Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayHelper.33" ) ); //$NON-NLS-1$
        return null;
      }

      nodeReader = new BufferedReader( new InputStreamReader( new FileInputStream( nodeFile ) ) );
      eleReader = new BufferedReader( new InputStreamReader( new FileInputStream( eleFile ) ) );

      final GM_Position[] points = parseTriangleNodeOutput( nodeReader );

      final List<GM_Surface< ? extends GM_SurfacePatch>> elements = parseTriangleElementOutput( eleReader, m_crs, points );

      for( final GM_Surface< ? extends GM_SurfacePatch> element : elements )
      {
        for( final GM_SurfacePatch surfacePatch : element )
        {
          final GM_Position[] ring = surfacePatch.getExteriorRing();
          triangles.add( GeometryFactory.createGM_Triangle( ring[0], ring[1], ring[2], m_crs ) );
        }
      }
      return triangles.toArray( new GM_Triangle[triangles.size()] );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      return null;
    }
    finally
    {
      IOUtils.closeQuietly( nodeReader );
      IOUtils.closeQuietly( eleReader );
      if( tempDir != null && tempDir.exists() )
      {
        try
        {
          FileUtils.deleteDirectory( tempDir );
        }
        catch( final IOException e )
        {
          e.printStackTrace();
        }
      }
    }
  }

  public static GM_Position[] parseTriangleNodeOutput( final BufferedReader nodeReader ) throws IOException
  {
    // ignore first line, we don't check the file format
    final String firstLine = nodeReader.readLine();
    final StringTokenizer firstTokenizer = new StringTokenizer( firstLine );
    final int pointCount = Integer.parseInt( firstTokenizer.nextToken() );
    /* final int coordCount = */Integer.parseInt( firstTokenizer.nextToken() );
    final int attCount = Integer.parseInt( firstTokenizer.nextToken() );

    final GM_Position[] points = new GM_Position[pointCount + 1];

    while( nodeReader.ready() )
    {
      final String string = nodeReader.readLine();
      if( string == null )
        break;

      if( string.startsWith( "#" ) ) //$NON-NLS-1$
        continue;

      final StringTokenizer tokenizer = new StringTokenizer( string );
      final int id = Integer.parseInt( tokenizer.nextToken() );
      final double x = Double.parseDouble( tokenizer.nextToken() );
      final double y = Double.parseDouble( tokenizer.nextToken() );
      final double z = attCount > 0 ? Double.parseDouble( tokenizer.nextToken() ) : Double.NaN;

      final GM_Position position = GeometryFactory.createGM_Position( x, y, z );
      points[id] = position;
    }
    return points;
  }

  public static List<GM_Surface< ? extends GM_SurfacePatch>> parseTriangleElementOutput( final BufferedReader eleReader, final String crs, final GM_Position[] points ) throws IOException, GM_Exception
  {
    final List<GM_Surface< ? extends GM_SurfacePatch>> surfaces = new ArrayList<GM_Surface< ? extends GM_SurfacePatch>>();

    eleReader.readLine(); // ignore first line
    while( eleReader.ready() )
    {
      final String string = eleReader.readLine();
      if( string == null )
        break;

      if( string.startsWith( "#" ) ) //$NON-NLS-1$
        continue;

      final StringTokenizer tokenizer = new StringTokenizer( string );
      tokenizer.nextToken(); // ele id - ignore
      final int p1 = Integer.parseInt( tokenizer.nextToken() );
      final int p2 = Integer.parseInt( tokenizer.nextToken() );
      final int p3 = Integer.parseInt( tokenizer.nextToken() );

      final GM_Position[] triangle = new GM_Position[] { points[p1], points[p2], points[p3], points[p1] };

      final GM_Surface< ? extends GM_SurfacePatch> surface = GeometryFactory.createGM_Surface( triangle, null, crs );

      surfaces.add( surface );

    }
    return surfaces;
  }
}