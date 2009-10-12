package com.bce.gis.operation.hmo2fli;

import junit.framework.TestCase;

import com.vividsolutions.jts.algorithm.PointInRing;
import com.vividsolutions.jts.algorithm.SimplePointInRing;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * @author belger
 */
public class GetRingsOperationTest extends TestCase
{
  public void testTriangle( )
  {
    assertNull( checkSingleGeometry( TestData.triangle1, TestData.cin2 ) );
    assertSame( TestData.triangle1, checkSingleGeometry( TestData.triangle1, TestData.cin1 ) );

    assertNull( checkSingleGeometry( TestData.triangle2, TestData.cin1 ) );
    assertSame( TestData.triangle2, checkSingleGeometry( TestData.triangle2, TestData.cin2 ) );
  }

  public void testTwoTriangles( )
  {
    final GetRingsOperation op = new GetRingsOperation( TestData.triangles, null );

    final LinearRing[] rings1 = op.getAllContaining( TestData.cin1 );
    assertNotNull( "Ergebnis darf nie null sein", rings1 ); //$NON-NLS-1$
    assertEquals( "Genau eines überdeckt diesen Punkt", 1, rings1.length ); //$NON-NLS-1$
    assertSame( "Es ist Dreieck1", TestData.triangle1, rings1[0] ); //$NON-NLS-1$

    final LinearRing[] rings2 = op.getAllContaining( TestData.cin2 );
    assertNotNull( "Ergebnis darf nie null sein", rings2 ); //$NON-NLS-1$
    assertEquals( "Genau eines überdeckt diesen Punkt", 1, rings2.length ); //$NON-NLS-1$
    assertSame( "Es ist Dreieck2", TestData.triangle2, rings2[0] ); //$NON-NLS-1$

    final LinearRing[] rings4 = op.getAllContaining( TestData.cout );
    assertNotNull( "Ergebnis darf nie null sein", rings4 ); //$NON-NLS-1$
    assertEquals( "Kein Dreieck überdeckt diesen Punkt", 0, rings4.length ); //$NON-NLS-1$
  }

  private Geometry checkSingleGeometry( final LinearRing ring, final Coordinate c )
  {
    final GetRingsOperation op = new GetRingsOperation( new LinearRing[] { ring }, null );

    final LinearRing[] rings = op.getAllContaining( c );

    assertNotNull( "Ergebnis darf nie null sein", rings ); //$NON-NLS-1$

    final PointInRing pir = new SimplePointInRing( ring );

    if( pir.isInside( c ) )
    {
      assertEquals( "Bei Schnitt muss ein Ergebnis da sein", 1, rings.length ); //$NON-NLS-1$

      final Geometry result = rings[0];
      assertSame( "Das Ergebnis muss genau die übergebene Geometry sein", ring, result ); //$NON-NLS-1$
      return result;
    }

    assertEquals( "Schneiden sich die beiden nicht, darf es kein Ergebnis geben", 0, rings.length ); //$NON-NLS-1$
    return null;
  }

  public void testGetHeightAt( )
  {
    final GetRingsOperation op = new GetRingsOperation( TestData.triangles, null );
    final LinearRing[] rings1 = op.getAllContaining( TestData.cin1 );
    final double value1 = rings1.length == 0 ? Double.NaN : new HeightFromTriangle( rings1[0] ).getHeight( TestData.cin1 );
    assertEquals( value1, op.getValue( TestData.cin1 ), 0.0 );

    final LinearRing[] rings2 = op.getAllContaining( TestData.cin2 );
    final double value2 = rings2.length == 0 ? Double.NaN : new HeightFromTriangle( rings2[0] ).getHeight( TestData.cin2 );
    assertEquals( value2, op.getValue( TestData.cin2 ), 0.0 );
  }

}
