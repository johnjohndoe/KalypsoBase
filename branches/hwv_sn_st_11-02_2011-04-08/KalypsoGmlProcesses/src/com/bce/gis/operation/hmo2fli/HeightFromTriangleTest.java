package com.bce.gis.operation.hmo2fli;

import junit.framework.TestCase;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author belger
 */
public class HeightFromTriangleTest extends TestCase
{
  public void testGetHeightFromTriangle( )
  {
    assertEquals( 1.0, new HeightFromTriangle( TestData.triangle1 ).getHeight( TestData.cin1 ), 0.0001 );
    assertEquals( 1.4, new HeightFromTriangle( TestData.triangle2 ).getHeight( new Coordinate( 0.5, 0.9 ) ), 0.0001 );
    assertTrue( Double.isNaN( new HeightFromTriangle( TestData.triangle1 ).getHeight( TestData.cin2 ) ) );
  }

}
