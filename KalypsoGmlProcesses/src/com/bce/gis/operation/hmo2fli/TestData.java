package com.bce.gis.operation.hmo2fli;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * TestData for Triangles
 * 
 * @author belger
 */
public class TestData
{
  public static GeometryFactory gf = new GeometryFactory();

  public final static Coordinate c1 = new Coordinate( 0.0, 0.0, 1.0 );

  public final static Coordinate c2 = new Coordinate( 1.0, 0.0, 1.0 );

  public final static Coordinate c3 = new Coordinate( 0.0, 1.0, 1.0 );

  public final static Coordinate c4 = new Coordinate( 1.0, 1.0, 2.0 );

  public final static Coordinate cin2 = new Coordinate( 0.9, 0.9 );

  public final static Coordinate cin1 = new Coordinate( 0.1, 0.1 );

  public final static Coordinate cout = new Coordinate( 2.0, 2.0 );

  public final static LinearRing triangle1 = gf.createLinearRing( new Coordinate[] { c1, c2, c3, c1 } );

  public final static LinearRing triangle2 = gf.createLinearRing( new Coordinate[] { c2, c3, c4, c2 } );

  public final static LinearRing[] triangles = new LinearRing[] { triangle1, triangle2 };
}
