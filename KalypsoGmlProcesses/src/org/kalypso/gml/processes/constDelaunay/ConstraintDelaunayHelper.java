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
import org.kalypso.gml.processes.constDelaunay.DelaunayImpl.QuadraticAlgorithm;
import org.kalypso.gml.processes.constDelaunay.DelaunayImpl.TriangulationDT;
import org.kalypso.gml.processes.i18n.Messages;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_LineString;
import org.kalypsodeegree.model.geometry.GM_MultiSurface;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree.model.geometry.GM_SurfacePatch;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/**
 * Helper class for tringle.exe<BR>
 * for more information goto: http://www.cs.cmu.edu/~quake/triangle.html
 * 
 * @author Thomas Jung
 * 
 * extension for creating triangulated surfaces without writing temporary files and using triangle.exe
 * @author ig
 */
public class ConstraintDelaunayHelper
{
  public static final long PROCESS_TIMEOUT = 50000;

  /**
   * writes out a triangle-polyfile with linestrings for the console program Triangle.exe
   */
  @SuppressWarnings({ "unchecked", "deprecation" })
  public static String writePolyFileForLinestrings( final OutputStream polyStream, final List list, final PrintWriter simLog )
  {
    final List<GM_LineString> breaklines = new ArrayList<GM_LineString>( list.size() );
    int totalPointCount = 0;
    int totalSegmentCount = 0;

    String crs = null;

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
          simLog.println( Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayHelper.1" ) + geoObject + " - " + e.getLocalizedMessage() ); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
      else if( geoObject instanceof Feature )
      {
        try
        {
          final Feature feature = (Feature) geoObject;
          final GM_Curve curve = (GM_Curve) feature.getDefaultGeometryProperty();

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
          simLog.println( Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayHelper.1" ) + geoObject + " - " + e.getLocalizedMessage() ); //$NON-NLS-1$ //$NON-NLS-2$
        }

      }
      else
      {
        simLog.println( Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayHelper.3" ) + geoObject ); //$NON-NLS-1$
      }
    }

    final PrintWriter writer = new PrintWriter( new OutputStreamWriter( polyStream ) );

    /* node file */

    /* write header */
    // First line: <# of vertices> <dimension (must be 2)> <# of attributes> <# of boundary markers (0 or 1)>
    writer.print( totalPointCount );
    writer.println( " 2 1 0" ); // Dimension 2, Value-Dimension 1, Border Markers 0 //$NON-NLS-1$

    // Remaining lines: <vertex #> <x> <y> [attributes] [boundary marker]
    int pointIndex = 1;
    writeNodes( breaklines, writer, pointIndex );

    // First line: <# of vertices> <dimension (must be 2)> <# of attributes> <# of boundary markers (0 or 1)>
    writer.print( totalSegmentCount );
    writer.println( " 0" ); // Border Markers 0 //$NON-NLS-1$

    // Following lines: <vertex #> <x> <y> [attributes] [boundary marker]
    final int segmentIndex = 1;
    // we reuse pointindex and iterate through the segments in the same way as before
    pointIndex = 1;
    writeElements( breaklines, writer, pointIndex, segmentIndex );

    // One line: <# of segments> <# of boundary markers (0 or 1)>
    // Following lines: <segment #> <endpoint> <endpoint> [boundary marker]
    writer.println( "0" ); //$NON-NLS-1$
    writer.println( "" ); //$NON-NLS-1$

    // One line: <# of holes>
    // Following lines: <hole #> <x> <y>

    // Optional line: <# of regional attributes and/or area constraints>
    // Optional following lines: <region #> <x> <y> <attribute> <maximum area>

    writer.flush();

    return crs;
  }

  /**
   * writes the elements of the given {@link GM_LineString} to the given {@link PrintWriter}
   */
  protected static void writeElements( final List<GM_LineString> lines, final PrintWriter writer, int pointIndex, int segmentIndex )
  {
    for( final GM_LineString line : lines )
    {
      for( int i = 0; i < line.getNumberOfPoints() - 1; i++ )
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
  }

  /**
   * writes the points of the given {@link GM_LineString} to the given {@link PrintWriter}
   */
  protected static void writeNodes( final List<GM_LineString> lines, final PrintWriter writer, int pointIndex )
  {
    for( final GM_LineString line : lines )
    {
      for( int i = 0; i < line.getNumberOfPoints(); i++ )
      {
        final GM_Position pos = line.getPositionAt( i );
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
  }

  public static IStatus writePolyFile( final BufferedOutputStream polyStream, final TrianglePolyFileData data )
  {
    final PrintWriter writer = new PrintWriter( new OutputStreamWriter( polyStream ) );

    // node header
    writer.print( data.getVertexHeader() );
    writer.println();

    // nodes
    final List<TriangleVertex> nodeList = data.getNodeList();
    for( int i = 0; i < nodeList.size(); i++ )
    {
      final TriangleVertex triangleVertex = nodeList.get( i );
      writer.print( i + " " + triangleVertex.getLine() ); //$NON-NLS-1$
      writer.println();
    }

    // segment header
    writer.print( data.getSegmentHeader() );
    writer.println();

    // segments
    final List<TriangleSegment> segmentList = data.getSegmentList();
    for( int i = 0; i < segmentList.size(); i++ )
    {
      final TriangleSegment segment = segmentList.get( i );
      writer.print( i + " " + segment.getLine() ); //$NON-NLS-1$
      writer.println();

    }

    final List<TriangleHole> holeList = data.getHoleList();
    if( holeList != null && holeList.size() > 0 )
    {

      // holes header
      writer.print( data.getHoleHeader() );
      writer.println();

      // holes
      for( int i = 0; i < holeList.size(); i++ )
      {
        final TriangleHole hole = holeList.get( i );
        writer.print( i + " " + hole.getLine() ); //$NON-NLS-1$
        writer.println();
      }
    }
    else
    {
      writer.print( "0" ); //$NON-NLS-1$
      writer.println();
    }

    writer.flush();

    return Status.OK_STATUS;
  }

  /**
   * writes out a polyfile of polygons for the console program Triangle.exe
   */
  public static IStatus writePolyFileForPolygon( final OutputStream polyStream, final GM_Position[] posArray )
  {
    final PrintWriter writer = new PrintWriter( new OutputStreamWriter( polyStream ) );
    writer.print( posArray.length );
    writer.println( " 2 1 0" ); // Dimension 2, Value-Dimension 1, Border Markers 0 //$NON-NLS-1$

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
    writer.println( " 0" ); // Border Markers 0 //$NON-NLS-1$

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

    writer.println( "0" ); //$NON-NLS-1$
    writer.println( "" ); //$NON-NLS-1$

    writer.flush();

    return Status.OK_STATUS;
  }

  public static final List<GM_Surface<GM_SurfacePatch>> parseTriangleElementOutput( final BufferedReader eleReader, final String crs, final GM_Position[] points ) throws IOException, GM_Exception
  {
    final List<GM_Surface<GM_SurfacePatch>> surfaces = new ArrayList<GM_Surface<GM_SurfacePatch>>();

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

      final GM_Surface<GM_SurfacePatch> surface = GeometryFactory.createGM_Surface( triangle, null, null, crs );

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

      if( string.startsWith( "#" ) ) //$NON-NLS-1$
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
    final StringBuffer cmd = new StringBuffer( "cmd /c triangle.exe -c -p" ); //$NON-NLS-1$

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
    pwSimuLog.append( Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayHelper.21" ) ); //$NON-NLS-1$

    final long lTimeout = PROCESS_TIMEOUT;

    final Process exec = Runtime.getRuntime().exec( cmd.toString(), null, tempDir );

    final InputStream errorStream = exec.getErrorStream();
    final InputStream inputStream = exec.getInputStream();

    final StreamGobbler error = new StreamGobbler( errorStream, "ERROR_STREAM", false ); //$NON-NLS-1$
    final StreamGobbler input = new StreamGobbler( inputStream, "INPUT_STREAM", false ); //$NON-NLS-1$

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
        throw new CoreException( StatusUtilities.createErrorStatus( Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayHelper.24" ) ) ); //$NON-NLS-1$
      }

      /* Wait a few millisec, before continuing. */
      Thread.sleep( 100 );
      timeRunning = timeRunning + 100;
    }
  }

  public static GM_Triangle[] convertToTriangles( final GM_MultiSurface polygonSurface, final String crs ) throws GM_Exception
  {
    return convertToTriangles( polygonSurface, crs, true );
  }

  @SuppressWarnings("unchecked")
  public static GM_Triangle[] convertToTriangles( final GM_MultiSurface polygonSurface, final String crs, boolean pBoolWriteFiles ) throws GM_Exception
  {
    final List<GM_Triangle> triangleList = new LinkedList<GM_Triangle>();

    final GM_Object[] objects = polygonSurface.getAll();
    for( final GM_Object object : objects )
    {
      if( object instanceof GM_Surface )
      {
        final GM_Surface<GM_SurfacePatch> surface = (GM_Surface<GM_SurfacePatch>) object;
        final GM_Triangle[] triangles = convertToTriangles( surface, crs, pBoolWriteFiles );
        for( final GM_Triangle triangle : triangles )
        {
          triangleList.add( triangle );
        }
      }
    }
    return triangleList.toArray( new GM_Triangle[triangleList.size()] );
  }

  public static GM_Triangle[] convertToTriangles( final GM_Surface<GM_SurfacePatch> surface, final String crs ) throws GM_Exception
  {
    return convertToTriangles( surface, crs, true );
  }

  public static GM_Triangle[] convertToTriangles( final GM_Surface<GM_SurfacePatch> pSurface, final String crs, boolean pBoolWriteFiles ) throws GM_Exception
  {
    final List<GM_Triangle> triangleList = new LinkedList<GM_Triangle>();
    boolean lBoolConvex = pSurface.getConvexHull().difference( pSurface ) == null;
    for( final GM_SurfacePatch surfacePatch : pSurface )
    {
      final GM_Position[] exterior = surfacePatch.getExteriorRing();
      final GM_Position[][] interior = surfacePatch.getInteriorRings();
      final GM_Triangle[] lArrTriangles;
      lArrTriangles = createGM_Triangles( exterior, interior, crs, pBoolWriteFiles );

      if( lArrTriangles != null )
      {
        for( final GM_Triangle lTriangle : lArrTriangles )
        {
          if( !lBoolConvex )
          {
            if( !pSurface.contains( lTriangle.getCentroid().getPosition() ) )
            {
              continue;
            }
          }
          if( lTriangle.getOrientation() == -1 )
          {
            triangleList.add( GeometryFactory.createGM_Triangle( lTriangle.getExteriorRing()[0], lTriangle.getExteriorRing()[2], lTriangle.getExteriorRing()[1], crs ) );

          }
          else
          {
            triangleList.add( lTriangle );
          }
        }
      }
    }
    //can be used for reducing of all angles in triangles
    return triangleList.toArray( new GM_Triangle[triangleList.size()] );
  }

  /**
   * converts an array of {@link GM_Position} into a list of {@link GM_Triangle}. If there are more than 3 positions in
   * the array the positions gets triangulated by Triangle.exe The positions must build a closed polygon.
   */
  private static GM_Triangle[] createGM_Triangles( final GM_Position[] exterior, final GM_Position[][] interior, final String crs, boolean pBoolWriteFiles ) throws GM_Exception
  {
    // check if pos arrays are closed polygons

    // first, check the exterior ring. If it is not closed or has less than 4 positions cancel operation.
    if( checkForPolygon( exterior ) == false )
      throw new UnsupportedOperationException( Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayHelper.26" ) ); //$NON-NLS-1$

    final GM_Position[][] interiorPolygons = getClosedPolygons( interior );

    // if there are more than 3 corner positions triangulate the pos array.
    if( exterior.length > 4 )
    {
      return triangulatePolygon( exterior, interiorPolygons, crs, pBoolWriteFiles );
    }
    else
    {
      final GM_Triangle[] tri = new GM_Triangle[1];
      tri[0] = GeometryFactory.createGM_Triangle( exterior[0], exterior[1], exterior[2], crs );
      return tri;
    }
  }

  /**
   * checks if the positions are defining closed polygons and returns the valid positions. Non-closing arrays will be
   * ignored.
   */
  public static GM_Position[][] getClosedPolygons( final GM_Position[][] rings )
  {
    // check the rings. If there are non-valid rings, ignore them.
    final List<GM_Position[]> ringList = new LinkedList<GM_Position[]>();
    if( rings != null )
    {
      for( final GM_Position[] inRing : rings )
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
  public static boolean checkForPolygon( final GM_Position[] ring )
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

  public static GM_Triangle[] triangulatePolygon( final GM_Position[] exterior, final GM_Position[][] interiorPolygons, final String crs )
  {
    return triangulatePolygon( exterior, interiorPolygons, crs, true );
  }

  public static GM_Triangle[] triangulatePolygon( final GM_Position[] exterior, @SuppressWarnings("unused") final GM_Position[][] interiorPolygons, final String crs, boolean pBoolWriteFiles )
  {
    BufferedReader nodeReader = null;
    BufferedReader eleReader = null;
    PrintWriter pwSimuLog;
    final List<GM_Triangle> triangles = new LinkedList<GM_Triangle>();

    /* prepare */
    final List<TriangleVertex> nodeList = new LinkedList<TriangleVertex>();
    final List<TriangleSegment> segmentList = new LinkedList<TriangleSegment>();

    // handle the points
    for( final GM_Position pos : exterior )
    {
      final TriangleVertex vertex = new TriangleVertex( pos, true, pos.getZ() );
      nodeList.add( vertex );
    }

    // handle the polygon
    for( int i = 0; i < exterior.length - 1; i++ )
    {
      final TriangleSegment segment = new TriangleSegment( i, i + 1, true );
      segmentList.add( segment );
    }

    // triangulate without triangle.exe
    if( !pBoolWriteFiles )
    {
      List<GM_Triangle> lListAllResults = new ArrayList<GM_Triangle>();

      GM_Triangle[] lArrTriRes = getTrianglesWithTriangulationDT( exterior, crs );
      for( final GM_Triangle lTriAct : lArrTriRes )
      {
        {
          lListAllResults.add( lTriAct );
        }
      }
      return lListAllResults.toArray( new GM_Triangle[lListAllResults.size()] );
    }

    // collect the data
    final TrianglePolyFileData trianglePolyFileData = new TrianglePolyFileData( nodeList, segmentList, null );

    try
    {
      pwSimuLog = new PrintWriter( System.out );

      final File tempDir = FileUtilities.createNewTempDir( "Triangle" ); //$NON-NLS-1$
      final File polyfile = new File( tempDir, "input.poly" ); //$NON-NLS-1$

      BufferedOutputStream strmPolyInput = null;
      strmPolyInput = new BufferedOutputStream( new FileOutputStream( polyfile ) );

      final IStatus writeStatus = writePolyFile( strmPolyInput, trianglePolyFileData );
// IStatus writeStatus = writePolyFileForPolygon( strmPolyInput, exterior );
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

      final List<GM_Surface<GM_SurfacePatch>> elements = parseTriangleElementOutput( eleReader, crs, points );

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
    catch( final Exception e )
    {
      e.printStackTrace();
      return null;
    }
  }

  private static GM_Triangle[] getTrianglesWithTriangulationDT( final GM_Position[] pPositions, final String pStrCrs )
  {
    TriangulationDT lTriangulationDT = new TriangulationDT( pPositions, pStrCrs );
    QuadraticAlgorithm lAlgorithmRunner = new QuadraticAlgorithm();
    lAlgorithmRunner.triangulate( lTriangulationDT );
    List<GM_Triangle> lListActResults = lTriangulationDT.getListGMTriangles();
    return lListActResults.toArray( new GM_Triangle[lListActResults.size()] );

  }

}
