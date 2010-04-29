package com.bce.gis.operation.hmo2fli;

import org.kalypso.gml.processes.i18n.Messages;

import com.vividsolutions.jts.algorithm.PointInRing;
import com.vividsolutions.jts.algorithm.SimplePointInRing;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * Utility class to get height from a point inside a triangle.
 * 
 * @author belger
 */
public class HeightFromTriangle
{
  private final LinearRing m_triangle;

  private final PointInRing m_pir;

  public HeightFromTriangle( final LinearRing triangle )
  {
    if( triangle.getNumPoints() != 4 )
      throw new IllegalArgumentException( Messages.getString("com.bce.gis.operation.hmo2fli.HeightFromTriangle.0") ); //$NON-NLS-1$

    m_triangle = triangle;
    m_pir = new SimplePointInRing( triangle );
  }

  public double getHeight( final Coordinate c )
  {
    if( m_triangle.getNumPoints() != 4 )
      throw new IllegalArgumentException( Messages.getString("com.bce.gis.operation.hmo2fli.HeightFromTriangle.1") ); //$NON-NLS-1$

    // Check if point is inside Triangle, if not return immediuately
    // TODO: not always necessary?
    if( !m_pir.isInside( c ) )
      return Double.NaN;

    final Coordinate c1 = m_triangle.getPointN( 0 ).getCoordinate();
    final Coordinate c2 = m_triangle.getPointN( 1 ).getCoordinate();
    final Coordinate c3 = m_triangle.getPointN( 2 ).getCoordinate();

    final double x1 = c1.x;
    final double y1 = c1.y;
    final double z1 = c1.z;

    final double x2 = c2.x;
    final double y2 = c2.y;
    final double z2 = c2.z;

    final double x3 = c3.x;
    final double y3 = c3.y;
    final double z3 = c3.z;

    final double nx = (y2 - y1) * (z3 - z2) - (y3 - y2) * (z2 - z1);
    final double ny = (x3 - x2) * (z2 - z1) - (x2 - x1) * (z3 - z2);
    final double nz = (x2 - x1) * (y3 - y2) - (x3 - x2) * (y2 - y1);

    /*
     * if( nx * nx + ny * ny + nz * nz <= eps * eps ) { log( 1, "Normalenvektor ist zu klein (Dreieck %d)\n", t->n ); };
     */
    /*
     * if( nz == 0.0 && nx * nx + ny * ny > eps*eps ) { log( 1, "Dreieck steht Kopf: nx=%f ny=%f nz=%f\n", nx, ny, nz ); };
     */

    final double k = nx * x1 + ny * y1 + nz * z1;

    // TODO: until now, the concrete coordinate 'c' was not used, move code above into constructor
    // See also
    // /KalypsoModelSimulationBase/src/org/kalypso/kalypsosimulationmodel/core/terrainmodel/TriangleData.java#calculateTrianglePlaneEquation
    return (k - nx * c.x - ny * c.y) / nz;
  }

}
