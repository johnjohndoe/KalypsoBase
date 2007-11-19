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
package org.kalypso.gml.processes.constDelaunay;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.io.StreamGobbler;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_LineString;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree.model.geometry.GM_SurfacePatch;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.opengis.cs.CS_CoordinateSystem;

/**
 * @author Thomas Jung
 */
public class ConstraintDelaunayHelper
{
  public static final long PROCESS_TIMEOUT = 50000;

  /**
   * writes out a polyfile with linestrings for the console program Triangle.exe
   */
  @SuppressWarnings("unchecked")
  public static CS_CoordinateSystem writePolyFileForLinestrings( final OutputStream polyStream, final List list, final PrintWriter simLog )
  {
    final List<GM_LineString> breaklines = new ArrayList<GM_LineString>( list.size() );
    int totalPointCount = 0;
    int totalSegmentCount = 0;

    CS_CoordinateSystem crs = null;

    for( final Object geoObject : list )
    {
      if( geoObject instanceof GM_Curve )
      {
        try
        {
          final GM_Curve curve = (GM_Curve) geoObject;

          if( crs == null )
            crs = curve.getCoordinateSystem();

          final GM_LineString lineString;
          lineString = curve.getAsLineString();
          breaklines.add( lineString );
          totalPointCount += lineString.getNumberOfPoints();
          totalSegmentCount += lineString.getNumberOfPoints() - 1;
        }
        catch( final GM_Exception e )
        {
          e.printStackTrace();
          simLog.println( "Bad geometry object:  " + geoObject + " - " + e.getLocalizedMessage() );
        }
      }
      else
      {
        simLog.println( "Geometry object cannot be handled:  " + geoObject );
      }
    }

    final PrintWriter writer = new PrintWriter( new OutputStreamWriter( polyStream ) );
    writer.print( totalPointCount );
    writer.println( " 2 1 0" ); // Dimension 2, Value-Dimension 1, Border Markers 0
    int pointIndex = 1;
    for( final GM_LineString breakline : breaklines )
    {
      for( int i = 0; i < breakline.getNumberOfPoints(); i++ )
      {
        final GM_Position pos = breakline.getPositionAt( i );
        writer.print( pointIndex++ );
        writer.print( ' ' );
        writer.print( pos.getX() );
        writer.print( ' ' );
        writer.print( pos.getY() );
        writer.print( ' ' );
        writer.print( pos.getZ() );
        writer.println();
      }
    }

    writer.print( totalSegmentCount );
    writer.println( " 0" ); // Border Markers 0

    int segmentIndex = 1;
    // we reuse pointindex and iterate through the segments in the same way as before
    pointIndex = 1;
    for( final GM_LineString breakline : breaklines )
    {
      for( int i = 0; i < breakline.getNumberOfPoints() - 1; i++ )
      {
        writer.print( segmentIndex++ );
        writer.print( ' ' );
        writer.print( pointIndex++ );
        writer.print( ' ' );
        writer.print( pointIndex );
        writer.println();
      }

      // increase pointIndex one more, because we have one segment less than points per line string
      pointIndex++;
    }

    writer.println( "0" );
    writer.println( "" );

    writer.flush();

    return crs;
  }

  /**
   * writes out a polyfile of polygons for the console program Triangle.exe
   */
  @SuppressWarnings("unchecked")
  public static org.eclipse.core.runtime.IStatus writePolyFileForPolygon( final OutputStream polyStream, final GM_Position[] posArray )
  {
    final PrintWriter writer = new PrintWriter( new OutputStreamWriter( polyStream ) );
    writer.print( posArray.length );
    writer.println( " 2 1 0" ); // Dimension 2, Value-Dimension 1, Border Markers 0

    // nodes
    int pointIndex = 1;
    for( final GM_Position pos : posArray )
    {
      writer.print( pointIndex++ );
      writer.print( ' ' );
      writer.print( pos.getX() );
      writer.print( ' ' );
      writer.print( pos.getY() );
      writer.print( ' ' );
      writer.print( pos.getZ() );
      writer.println();
    }

    writer.print( 1 ); // totalSegmentCount, right now we have 1 polygon.
    writer.println( " 0" ); // Border Markers 0

    // element
    int segmentIndex = 1;
    pointIndex = 1;
    for( int i = 0; i < posArray.length - 1; i++ )
    {
      writer.print( segmentIndex++ );
      writer.print( ' ' );
      writer.print( pointIndex++ );
      writer.print( ' ' );
      writer.print( pointIndex );
      writer.println();
    }

    writer.println( "0" );
    writer.println( "" );

    writer.flush();

    return Status.OK_STATUS;
  }

  @SuppressWarnings("unchecked")
  public static final List<GM_Surface> parseTriangleElementOutput( final BufferedReader eleReader, final CS_CoordinateSystem crs, final GM_Position[] points ) throws IOException, GM_Exception
  {
    List<GM_Surface> surfaces = new ArrayList<GM_Surface>();

    eleReader.readLine(); // ignore first line
    while( eleReader.ready() )
    {
      final String string = eleReader.readLine();
      if( string == null )
        break;

      if( string.startsWith( "#" ) )
        continue;

      final StringTokenizer tokenizer = new StringTokenizer( string );
      tokenizer.nextToken(); // ele id - ignore
      final int p1 = Integer.parseInt( tokenizer.nextToken() );
      final int p2 = Integer.parseInt( tokenizer.nextToken() );
      final int p3 = Integer.parseInt( tokenizer.nextToken() );

      final GM_Position[] triangle = new GM_Position[] { points[p1], points[p2], points[p3], points[p1] };

      final GM_Surface surface = GeometryFactory.createGM_Surface( triangle, null, null, crs );

      surfaces.add( surface );

    }
    return surfaces;
  }

  public static GM_Position[] parseTriangleNodeOutput( final BufferedReader nodeReader ) throws IOException
  {
    // ignore first line, we don't check the file format
    final String firstLine = nodeReader.readLine();
    final StringTokenizer firstTokenizer = new StringTokenizer( firstLine );
    final int pointCount = Integer.parseInt( firstTokenizer.nextToken() );

    final GM_Position[] points = new GM_Position[pointCount + 1];

    while( nodeReader.ready() )
    {
      final String string = nodeReader.readLine();
      if( string == null )
        break;

      if( string.startsWith( "#" ) )
        continue;

      final StringTokenizer tokenizer = new StringTokenizer( string );
      final int id = Integer.parseInt( tokenizer.nextToken() );
      final double x = Double.parseDouble( tokenizer.nextToken() );
      final double y = Double.parseDouble( tokenizer.nextToken() );
      final double z = Double.parseDouble( tokenizer.nextToken() );

      final GM_Position position = GeometryFactory.createGM_Position( x, y, z );
      points[id] = position;
    }
    return points;
  }

  public static StringBuffer createTriangleCommand( final File polyfile )
  {
    final StringBuffer cmd = new StringBuffer( "cmd /c triangle.exe -c -p" );

    final Double qualityMinAngle = 5.00;

    if( qualityMinAngle != null )
    {
      // at this point no quality meshing because it produces interpolation errors end zero-value points

      // cmd.append( "-q" );
      // cmd.append( qualityMinAngle.doubleValue() );
    }

    cmd.append( ' ' );
    cmd.append( polyfile.getName() );
    return cmd;
  }

  public static void execTriangle( final PrintWriter pwSimuLog, final File tempDir, final StringBuffer cmd ) throws IOException, CoreException, InterruptedException
  {
    pwSimuLog.append( "Triangle.exe wird ausgeführt..." );

    final long lTimeout = PROCESS_TIMEOUT;

    final Process exec = Runtime.getRuntime().exec( cmd.toString(), null, tempDir );

    final InputStream errorStream = exec.getErrorStream();
    final InputStream inputStream = exec.getInputStream();

    final StreamGobbler error = new StreamGobbler( errorStream, "ERROR_STREAM", false );
    final StreamGobbler input = new StreamGobbler( inputStream, "INPUT_STREAM", false );

    error.start();
    input.start();

    int timeRunning = 0;

    /* It is running until the job has finished or the timeout of 5 minutes is reached. */
    while( true )
    {
      try
      {
        exec.exitValue();
        break;
      }
      catch( final RuntimeException e )
      {
        /* The process has not finished. */
      }

      if( timeRunning >= lTimeout )
      {
        exec.destroy();
        throw new CoreException( StatusUtilities.createErrorStatus( "Es wurde das Timeout erreicht." ) );
      }

      /* Wait a few millisec, before continuing. */
      Thread.sleep( 100 );
      timeRunning = timeRunning + 100;
    }
  }

  public static GM_Triangle[] convertToTriangles( final GM_Surface<GM_SurfacePatch> surface, CS_CoordinateSystem crs )
  {
    final List<GM_Triangle> triangleList = new LinkedList<GM_Triangle>();
    for( GM_SurfacePatch surfacePatch : surface )
    {
      GM_Position[] exterior = surfacePatch.getExteriorRing();
      GM_Position[][] interior = surfacePatch.getInteriorRings();
      GM_Triangle[] triangles = createGM_Triangles( exterior, interior, crs );
      if( triangles != null )
      {
        for( GM_Triangle triangle : triangles )
        {
          triangleList.add( triangle );
        }
      }
    }
    return triangleList.toArray( new GM_Triangle[triangleList.size()] );
  }

  /**
   * converts an array of {@link GM_Position} into a list of {@link GM_Triangle}. If there are more than 3 positions in
   * the array the positions gets triangulated by Triangle.exe The positions must build a closed polygon.
   */
  private static GM_Triangle[] createGM_Triangles( GM_Position[] exterior, GM_Position[][] interior, CS_CoordinateSystem crs )
  {
    // check if pos arrays are closed polygons

    // first, check the exterior ring. If it is not closed or has less than 4 positions cancel operation.
    if( checkForPolygon( exterior ) == false )
      throw new UnsupportedOperationException( "Geometry Utilities: could not convert a non-closed geometry object into a triangle." );

    final GM_Position[][] interiorPolygons = getClosedPolygons( interior );

    // if there are more than 3 corner positions triangulate the pos array.
    if( exterior.length > 4 )
    {
      return triangulatePolygon( exterior, interiorPolygons, crs );
    }
    return null;
  }

  /**
   * checks if the positions are defining closed polygons and returns the valid positions. Non-closing arrays will be
   * ignored.
   */
  public static GM_Position[][] getClosedPolygons( GM_Position[][] rings )
  {
    // check the rings. If there are non-valid rings, ignore them.
    final List<GM_Position[]> ringList = new LinkedList<GM_Position[]>();
    if( rings != null )
    {
      for( GM_Position[] inRing : rings )
      {
        if( checkForPolygon( inRing ) == true )
          ringList.add( inRing );
      }
    }
    if( ringList.size() == 0 )
      return null;

    return ringList.toArray( new GM_Position[ringList.size()][] );
  }

  /**
   * checks if the given positions are defining a closed polygon.
   */
  public static boolean checkForPolygon( GM_Position[] ring )
  {
    // check the ring.
    if( ring != null )
    {
      // check number of positions
      if( ring.length >= 4 )
      {
        if( ring[0].equals( ring[ring.length - 1] ) )
        {
          return true;
        }
      }
    }
    return false;
  }

  public static GM_Triangle[] triangulatePolygon( GM_Position[] exterior, GM_Position[][] interiorPolygons, final CS_CoordinateSystem crs )
  {
    BufferedReader nodeReader = null;
    BufferedReader eleReader = null;
    PrintWriter pwSimuLog;
    final List<GM_Triangle> triangles = new LinkedList<GM_Triangle>();

    try
    {
      pwSimuLog = new PrintWriter( System.out );

      final File tempDir = FileUtilities.createNewTempDir( "Triangle" );
      final File polyfile = new File( tempDir, "input.poly" );

      BufferedOutputStream strmPolyInput = null;
      strmPolyInput = new BufferedOutputStream( new FileOutputStream( polyfile ) );
      IStatus writeStatus = writePolyFileForPolygon( strmPolyInput, exterior );
      strmPolyInput.close();

      if( writeStatus != Status.OK_STATUS )
        return null;

      // create command
      final StringBuffer cmd = createTriangleCommand( polyfile );

      // start Triangle
      execTriangle( pwSimuLog, tempDir, cmd );

      // prepare the polygon for output

      // start triangulation

      // get the triangle list
      final File nodeFile = new File( tempDir, "input.1.node" );
      final File eleFile = new File( tempDir, "input.1.ele" );

      if( !nodeFile.exists() || !eleFile.exists() )
      {
        pwSimuLog.append( "Fehler beim Ausführen von triangle.exe" );
        pwSimuLog.append( "Stellen Sie sicher, dass triangle.exe über die Windows PATH-Variable auffindbar ist." );
        pwSimuLog.append( "Fehler: triangle.exe konnte nicht ausgeführt werden." );
        return null;
      }

      nodeReader = new BufferedReader( new InputStreamReader( new FileInputStream( nodeFile ) ) );
      eleReader = new BufferedReader( new InputStreamReader( new FileInputStream( eleFile ) ) );

      final GM_Position[] points = parseTriangleNodeOutput( nodeReader );

      final List<GM_Surface> elements = parseTriangleElementOutput( eleReader, crs, points );

      for( final GM_Surface<GM_SurfacePatch> element : elements )
      {
        for( final GM_SurfacePatch surfacePatch : element )
        {
          final GM_Position[] ring = surfacePatch.getExteriorRing();
          triangles.add( GeometryFactory.createGM_Triangle( ring[0], ring[1], ring[2], crs ) );
        }
      }
      return triangles.toArray( new GM_Triangle[triangles.size()] );
    }
    catch( Exception e )
    {
      e.printStackTrace();
      return null;
    }
  }
}
