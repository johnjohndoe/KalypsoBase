package com.bce.gis.operation.hmo2fli;

import org.kalypso.gml.processes.i18n.Messages;

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
    assertNotNull( Messages.getString("com.bce.gis.operation.hmo2fli.GetRingsOperationTest.0"), rings1 ); //$NON-NLS-1$
    assertEquals( Messages.getString("com.bce.gis.operation.hmo2fli.GetRingsOperationTest.1"), 1, rings1.length ); //$NON-NLS-1$
    assertSame( Messages.getString("com.bce.gis.operation.hmo2fli.GetRingsOperationTest.2"), TestData.triangle1, rings1[0] ); //$NON-NLS-1$

    final LinearRing[] rings2 = op.getAllContaining( TestData.cin2 );
    assertNotNull( Messages.getString("com.bce.gis.operation.hmo2fli.GetRingsOperationTest.3"), rings2 ); //$NON-NLS-1$
    assertEquals( Messages.getString("com.bce.gis.operation.hmo2fli.GetRingsOperationTest.4"), 1, rings2.length ); //$NON-NLS-1$
    assertSame( Messages.getString("com.bce.gis.operation.hmo2fli.GetRingsOperationTest.5"), TestData.triangle2, rings2[0] ); //$NON-NLS-1$

    final LinearRing[] rings4 = op.getAllContaining( TestData.cout );
    assertNotNull( Messages.getString("com.bce.gis.operation.hmo2fli.GetRingsOperationTest.6"), rings4 ); //$NON-NLS-1$
    assertEquals( Messages.getString("com.bce.gis.operation.hmo2fli.GetRingsOperationTest.7"), 0, rings4.length ); //$NON-NLS-1$
  }

  private Geometry checkSingleGeometry( final LinearRing ring, final Coordinate c )
  {
    final GetRingsOperation op = new GetRingsOperation( new LinearRing[] { ring }, null );

    final LinearRing[] rings = op.getAllContaining( c );

    assertNotNull( Messages.getString("com.bce.gis.operation.hmo2fli.GetRingsOperationTest.8"), rings ); //$NON-NLS-1$

    final PointInRing pir = new SimplePointInRing( ring );

    if( pir.isInside( c ) )
    {
      assertEquals( Messages.getString("com.bce.gis.operation.hmo2fli.GetRingsOperationTest.9"), 1, rings.length ); //$NON-NLS-1$

      final Geometry result = rings[0];
      assertSame( Messages.getString("com.bce.gis.operation.hmo2fli.GetRingsOperationTest.10"), ring, result ); //$NON-NLS-1$
      return result;
    }

    assertEquals( Messages.getString("com.bce.gis.operation.hmo2fli.GetRingsOperationTest.11"), 0, rings.length ); //$NON-NLS-1$
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
