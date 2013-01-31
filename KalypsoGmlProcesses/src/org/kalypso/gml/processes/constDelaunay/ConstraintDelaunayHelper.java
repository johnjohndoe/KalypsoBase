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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.gml.processes.constDelaunay.DelaunayImpl.QuadraticAlgorithm;
import org.kalypso.gml.processes.constDelaunay.DelaunayImpl.TriangulationDT;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree.model.geometry.GM_PolygonPatch;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/**
 * Helper class for tringle.exe<BR>
 * 
 * @author Thomas Jung extension for creating triangulated surfaces without writing temporary files and using
 *         triangle.exe
 * @author ig
 */
public class ConstraintDelaunayHelper
{
  private static GM_Triangle[] getTrivialTriangleOrNull( final GM_Polygon boundary )
  {
    final GM_PolygonPatch surfacePatch = boundary.getSurfacePatch();
    final GM_Position[][] interiorRings = surfacePatch.getInteriorRings();
    if( !ArrayUtils.isEmpty( interiorRings ) )
      return null;

    final GM_Position[] exterior = surfacePatch.getExteriorRing();
    return getTrivialTriangleOrNull( exterior, boundary.getCoordinateSystem() );
  }

  private static GM_Triangle[] getTrivialTriangleOrNull( final GM_Position[] exterior, final String crs )
  {
    if( exterior.length == 4 )
      return new GM_Triangle[] { GeometryFactory.createGM_Triangle( exterior[0], exterior[1], exterior[2], crs ) };
    else
      return null;
  }

  /**
   * Triangulate a boundary using {@link QuadraticAlgorithm}
   */
  public static GM_Triangle[] triangulateSimple( final GM_Polygon boundary )
  {
    final GM_PolygonPatch surfacePatch = boundary.getSurfacePatch();
    Assert.isTrue( ArrayUtils.isEmpty( surfacePatch.getInteriorRings() ), "Polygon must not have any interior rings" ); //$NON-NLS-1$

    final GM_Position[] exteriorRing = surfacePatch.getExteriorRing();
    final String crs = boundary.getCoordinateSystem();
    final GM_Triangle[] trivialTriangleOrNull = getTrivialTriangleOrNull( exteriorRing, crs );
    if( trivialTriangleOrNull != null )
      return trivialTriangleOrNull;

    // triangulate without triangle.exe
    final TriangulationDT lTriangulationDT = new TriangulationDT( exteriorRing, crs );
    final QuadraticAlgorithm lAlgorithmRunner = new QuadraticAlgorithm();
    lAlgorithmRunner.triangulate( lTriangulationDT );
    final List<GM_Triangle> lListActResults = lTriangulationDT.getListGMTriangles();

    // remove concavities if necessary
    try
    {
      final boolean isConvex = boundary.getConvexHull().difference( boundary ) == null;
      if( !isConvex )
      {
        for( final Iterator<GM_Triangle> it = lListActResults.iterator(); it.hasNext(); )
          if( !boundary.contains( it.next().getCentroid().getPosition() ) )
            it.remove();
      }
    }
    catch( final GM_Exception e )
    {
      throw new IllegalStateException( e );
    }

    return lListActResults.toArray( new GM_Triangle[lListActResults.size()] );
  }

  /**
   * Triangulate boundaries with breaklines using Triangle. At least one boundary polygon must be specified.
   * The boundary polygons may contain holes.
   * The given arguments and the PSLG from the geometries are passed to triangle.exe,
   * which must reside inside the installation's "bin" folder. If triangle.exe is not
   * found, an empty list of triangles will be returned. The caller may check for
   * existance of triangle.exe by calling <code>findTriangleExe().isFile()</code>.
   * <p>
   * <b>For more information on Triangle visit http://www.cs.cmu.edu/~quake/triangle.html.</b>
   */
  public static GM_Triangle[] triangulateWithTriangle( final GM_Polygon[] boundaries, final GM_Curve[] breaklines, final String... triangleArgs )
  {
    if( !findTriangleExe().isFile() )
      return new GM_Triangle[0];
    
    if( ArrayUtils.isEmpty( boundaries ) )
      return new GM_Triangle[0];

    if( ArrayUtils.isEmpty( breaklines ) && boundaries.length == 1 )
    {
      final GM_Triangle[] trivialTriangleOrNull = getTrivialTriangleOrNull( boundaries[0] );
      if( trivialTriangleOrNull != null )
        return trivialTriangleOrNull;
    }

    // triangulate with triangle.exe
    final TriangleExe triangleHelper = new TriangleExe( triangleArgs );
    return triangleHelper.triangulate( boundaries, breaklines, true );
  }

  public static File findTriangleExe( )
  {
    // check system path
    final String triangleName = "triangle.exe"; //$NON-NLS-1$
    final File onPath = FileUtilities.findExecutableOnPath( triangleName );
    if( onPath != null )
      return onPath;

    // check Kalypso/bin folder
    final Location installLocation = Platform.getInstallLocation();
    final File installDir = FileUtils.toFile( installLocation.getURL() );
    final File binDir = new File( installDir, "bin" ); //$NON-NLS-1$
    final File inBin = new File( binDir, triangleName );
    if( inBin.isFile() )
      return inBin;

    // not found
    return null;
  }
}