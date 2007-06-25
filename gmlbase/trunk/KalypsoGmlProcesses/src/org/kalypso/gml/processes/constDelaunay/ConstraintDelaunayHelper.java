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
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_LineString;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.opengis.cs.CS_CoordinateSystem;

/**
 * @author Thomas Jung
 */
public class ConstraintDelaunayHelper
{
  /**
   * @return the first coordinate system it encounters
   */
  public static CS_CoordinateSystem writePolyFile( final OutputStream polyStream, final List list, final PrintWriter simLog )
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

}
