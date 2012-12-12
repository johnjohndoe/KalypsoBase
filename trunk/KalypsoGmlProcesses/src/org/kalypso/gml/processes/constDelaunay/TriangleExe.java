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
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.KalypsoCommonsExtensions;
import org.kalypso.commons.process.IProcess;
import org.kalypso.commons.process.IProcessFactory;
import org.kalypso.contribs.java.lang.ICancelable;
import org.kalypso.gml.processes.i18n.Messages;
import org.kalypso.transformation.transformer.GeoTransformerException;
import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_LineString;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree.model.geometry.GM_PolygonPatch;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Wrapper around the triangle.exe.
 * 
 * @author Gernot Belger
 */
class TriangleExe
{
  private final String[] m_triangleArgs;

  public TriangleExe( final String[] triangleArgs )
  {
    m_triangleArgs = triangleArgs;
  }

  public GM_Triangle[] triangulate( final GM_Polygon[] boundaries, final GM_Curve[] breaklines, final boolean exportZ )
  {
    final File triangleExe = ConstraintDelaunayHelper.findTriangleExe();
    if( triangleExe == null || !triangleExe.isFile() )
      throw new IllegalStateException( "triangle.exe not found" ); //$NON-NLS-1$

    // FIXME: put into separate class!
    BufferedReader nodeReader = null;
    BufferedReader eleReader = null;
    PrintStream pwSimuLog;

    /* prepare */
    final List<TriangleVertex> nodeList = new ArrayList<>();
    final List<TriangleSegment> segmentList = new ArrayList<>();
    final List<TriangleHole> holeList = new ArrayList<>();

    int i = 0;

    final String crs = boundaries[0].getCoordinateSystem();
    final IGeoTransformer geoTransformer = GeoTransformerFactory.getGeoTransformer( crs );
    for( final GM_Polygon boundary : boundaries )
    {

      final GM_PolygonPatch surfacePatch = boundary.getSurfacePatch();
      {
        // handle the exterior
        int k = i;
        final GM_Position[] exterior = surfacePatch.getExteriorRing();
        for( ; i - k < exterior.length - 2; i++ )
        {
          final GM_Position position = exterior[i - k];
          final TriangleVertex vertex = createVertex( exportZ, position );
          nodeList.add( vertex );
          final TriangleSegment segment = new TriangleSegment( i, i + 1, 1 );
          segmentList.add( segment );
        }
        final GM_Position position = exterior[i - k];
        final TriangleVertex vertex = createVertex( exportZ, position );
        nodeList.add( vertex );
        final TriangleSegment segment = new TriangleSegment( i, k, 1 );
        segmentList.add( segment );
        i++;
      }

      final GM_Position[][] interiorRings = surfacePatch.getInteriorRings();
      if( interiorRings != null )
      {
        for( final GM_Position[] ring : interiorRings )
        {
          // handle the hole
          int j = i;
          for( ; i - j < ring.length - 2; i++ )
          {
            final GM_Position position = ring[i - j];
            final TriangleVertex vertex = createVertex( exportZ, position );
            nodeList.add( vertex );
            final TriangleSegment segment = new TriangleSegment( i, i + 1, 1 );
            segmentList.add( segment );
          }
          final GM_Position position = ring[i - j];
          final TriangleVertex vertex = createVertex( exportZ, position );
          nodeList.add( vertex );
          final TriangleSegment segment = new TriangleSegment( i, j, 1 );
          segmentList.add( segment );

          final Coordinate[] coordinates = JTSAdapter.export( ring );
          final com.vividsolutions.jts.geom.GeometryFactory geometryFactory = new com.vividsolutions.jts.geom.GeometryFactory();
          final Polygon jtsHolePolygon = geometryFactory.createPolygon( geometryFactory.createLinearRing( coordinates ), null );
          final Point interiorPointJTS = jtsHolePolygon.getInteriorPoint();
          try
          {
            final GM_Point interiorPoint = (GM_Point)JTSAdapter.wrap( interiorPointJTS, surfacePatch.getCoordinateSystem() );
            final TriangleHole hole = new TriangleHole( interiorPoint.getPosition() );
            holeList.add( hole );
          }
          catch( final GM_Exception e )
          {
            e.printStackTrace();
          }
          i++;
        }
      }
    }

    if( !ArrayUtils.isEmpty( breaklines ) )
    {
      // handle the breaklines
      for( final GM_Curve rawCurve : breaklines )
      {
        try
        {
          final GM_Curve curve = geoTransformer.transform( rawCurve );
          final GM_LineString lineString = curve.getAsLineString();
          final GM_Position[] positions = lineString.getPositions();
          for( int j = 0; j < positions.length - 1; j++ )
          {
            final GM_Position position = positions[j];
            final TriangleVertex vertex = createVertex( exportZ, position );
            nodeList.add( vertex );
            final TriangleSegment segment = new TriangleSegment( i, i + 1, 0 );
            segmentList.add( segment );
            i++;
          }
          final GM_Position position = positions[positions.length - 1];
          final TriangleVertex vertex = createVertex( exportZ, position );
          nodeList.add( vertex );
          i++;
        }
        catch( final GM_Exception | GeoTransformerException e )
        {
          e.printStackTrace();
        }
      }
    }

    // collect the data
    final TrianglePolyFileData trianglePolyFileData = new TrianglePolyFileData( nodeList, segmentList, holeList );

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

      final List<GM_Triangle> triangles = parseTriangleElementOutput( eleReader, crs, points );
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

  private TriangleVertex createVertex( final boolean exportZ, final GM_Position position )
  {
    if( exportZ )
    {
      return new TriangleVertex( position, position.getZ() );
    }
    else
    {
      final List<Double> emptyList = Collections.emptyList();
      return new TriangleVertex( position, emptyList );
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

    final GM_Position[] points = new GM_Position[pointCount];

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

  public static List<GM_Triangle> parseTriangleElementOutput( final BufferedReader eleReader, final String crs, final GM_Position[] points ) throws IOException
  {
    final List<GM_Triangle> surfaces = new ArrayList<>();

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

      final GM_Triangle surface = GeometryFactory.createGM_Triangle( points[p1], points[p2], points[p3], crs );

      surfaces.add( surface );

    }
    return surfaces;
  }
}