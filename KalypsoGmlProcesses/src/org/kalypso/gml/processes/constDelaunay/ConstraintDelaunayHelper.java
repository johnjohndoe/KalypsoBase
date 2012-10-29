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

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree.model.geometry.GM_AbstractSurfacePatch;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/**
 * Helper class for tringle.exe<BR>
 * for more information goto: http://www.cs.cmu.edu/~quake/triangle.html
 *
 * @author Thomas Jung extension for creating triangulated surfaces without writing temporary files and using
 *         triangle.exe
 * @author ig
 */
public class ConstraintDelaunayHelper
{
  public static final long PROCESS_TIMEOUT = 50000;

  /**
   * writes out a triangle-polyfile with linestrings for the console program Triangle.exe
   */
  public static String writePolyFileForLinestrings( final OutputStream polyStream, final List< ? > list, final PrintStream simLog )
  {
    final List<GM_LineString> breaklines = new ArrayList<>( list.size() );
    int totalPointCount = 0;
    int totalSegmentCount = 0;

    String crs = null;

    for( final Object geoObject : list )
    {
      if( geoObject instanceof GM_Curve )
      {
        try
        {
          final GM_Curve curve = (GM_Curve)geoObject;

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
          final Feature feature = (Feature)geoObject;
          final GM_Curve curve = (GM_Curve)feature.getDefaultGeometryPropertyValue();

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

  public static GM_Triangle[] convertToTriangles( final GM_Position[] positions, final String crs )
  {
    return convertToTriangles( positions, crs, true );
  }

  public static GM_Triangle[] convertToTriangles( final GM_Position[] positions, final String crs, final boolean useTriangleExe )
  {
    final List<GM_Triangle> triangleList = new LinkedList<>();

    final GM_Triangle[] triangles = createGM_Triangles( positions, null, crs, useTriangleExe );

    triangleList.addAll( Arrays.asList( triangles ) );

    return triangleList.toArray( new GM_Triangle[triangleList.size()] );
  }

  @SuppressWarnings( "unchecked" )
  public static GM_Triangle[] convertToTriangles( final GM_MultiSurface polygonSurface, final String crs ) throws GM_Exception
  {
    final List<GM_Triangle> triangleList = new LinkedList<>();

    final GM_Object[] objects = polygonSurface.getAll();
    for( final GM_Object object : objects )
    {
      if( object instanceof GM_Polygon )
      {
        final GM_Polygon surface = (GM_Polygon)object;
        final GM_Triangle[] triangles = convertToTriangles( surface, crs );
        for( final GM_Triangle triangle : triangles )
        {
          triangleList.add( triangle );
        }
      }
    }
    return triangleList.toArray( new GM_Triangle[triangleList.size()] );
  }

  public static GM_Triangle[] convertToTriangles( final GM_Polygon surface, final String crs ) throws GM_Exception
  {
    return convertToTriangles( surface, crs, new String[0] );
  }

  public static GM_Triangle[] convertToTriangles( final GM_Polygon pSurface, final String crs, final String... triangleArgs ) throws GM_Exception
  {
    return convertToTriangles( pSurface, crs, true, triangleArgs );
  }

  public static GM_Triangle[] convertToTriangles( final GM_Polygon pSurface, final String crs, final boolean useTriangleExe, final String... triangleArgs ) throws GM_Exception
  {
    final List<GM_Triangle> triangleList = new LinkedList<>();
    final boolean lBoolConvex = pSurface.getConvexHull().difference( pSurface ) == null;
    for( final GM_AbstractSurfacePatch surfacePatch : pSurface )
    {
      final GM_Position[] exterior = surfacePatch.getExteriorRing();

      final GM_Triangle[] lArrTriangles;
      lArrTriangles = createGM_Triangles( exterior, null, crs, useTriangleExe, triangleArgs );

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

          triangleList.add( lTriangle );
        }
      }
    }
    // can be used for reducing of all angles in triangles
    return triangleList.toArray( new GM_Triangle[triangleList.size()] );
  }

  public static GM_Triangle[] createGM_Triangles( final GM_Position[] exterior, final GM_Curve[] breaklines, final String crs, final String... triangleArgs )
  {
    return createGM_Triangles( exterior, breaklines, crs, true, triangleArgs );
  }

  /**
   * converts an array of {@link GM_Position} into a list of {@link GM_Triangle}. If there are more than 3 positions in
   * the array the positions gets triangulated by Triangle.exe The positions must build a closed polygon.
   */
  public static GM_Triangle[] createGM_Triangles( final GM_Position[] exterior, final GM_Curve[] breaklines, final String crs, final boolean useTriangleExe, final String... triangleArgs )
  {
    // check if pos arrays are closed polygons

    // first, check the exterior ring. If it is not closed or has less than 4 positions cancel operation.
    if( checkForPolygon( exterior ) == false )
      throw new UnsupportedOperationException( Messages.getString( "org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayHelper.26" ) ); //$NON-NLS-1$

    // if there are more than 3 corner positions triangulate the pos array.
    if( exterior.length > 4 )
    {
      return triangulatePolygon( exterior, breaklines, crs, useTriangleExe, triangleArgs );
    }
    else
    {
      return new GM_Triangle[] { GeometryFactory.createGM_Triangle( exterior[0], exterior[1], exterior[2], crs ) };
    }
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

  public static GM_Triangle[] triangulatePolygon( final GM_Position[] exterior, final GM_Curve[] breaklines, final String crs, final String... triangleArgs )
  {
    return triangulatePolygon( exterior, breaklines, crs, true, triangleArgs );
  }

  public static GM_Triangle[] triangulatePolygon( final GM_Position[] exterior, final GM_Curve[] breaklines, final String crs, final boolean useTriangleExe, final String... triangleArgs )
  {
    final File triangleExe = TriangleExe.findTriangleExe();

    // FIXME mega ugly: this is not transparent to the user, wether triangle exe was used or not. Makes much more
    // explicit.
    // TODO: instead of the flag, we should split the whole helper class into two trinagulation classes, using the same interface
    if( !useTriangleExe || !triangleExe.exists() )
    {
      // triangulate without triangle.exe
      final TriangulationDT lTriangulationDT = new TriangulationDT( exterior, crs );
      final QuadraticAlgorithm lAlgorithmRunner = new QuadraticAlgorithm();
      lAlgorithmRunner.triangulate( lTriangulationDT );

      final List<GM_Triangle> lListActResults = lTriangulationDT.getListGMTriangles();
      return lListActResults.toArray( new GM_Triangle[lListActResults.size()] );
    }

    final TriangleExe triangleHelper = new TriangleExe( crs, triangleArgs );
    return triangleHelper.triangulate( exterior, breaklines );
  }
}