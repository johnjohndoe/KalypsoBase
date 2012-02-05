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
package org.kalypsodeegree_impl.io.sax.test;

import org.junit.Assert;
import org.kalypsodeegree.model.geometry.GM_Aggregate;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_CurveSegment;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_MultiCurve;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;

/**
 * 'assert' for {@link org.kalypsodeegree.model.geometry.GM_Object}; i.e. compares to objects and checks if they are
 * equal.
 *
 * @author "Gernot Belger"
 */
public class AssertGeometry extends Assert
{
  public static void assertMultiGeometry( final GM_Aggregate expected, final GM_Aggregate actual ) throws GM_Exception
  {
    assertEquals( expected.getSize(), actual.getSize() );

    final GM_Object[] expectedObjects = actual.getAll();
    final GM_Object[] actualObjects = actual.getAll();

    assertEquals( expectedObjects.length, actualObjects.length );

    for( int i = 0; i < actualObjects.length; i++ )
    {
      final GM_Object expectedObject = expectedObjects[i];
      final GM_Object actualObject = actualObjects[i];

      assertGeometry( expectedObject, actualObject );
    }
  }

  public static void assertGeometry( final GM_Object expected, final GM_Object actual ) throws GM_Exception
  {
    assertEquals( expected.getClass(), actual.getClass() );

    final Class< ? extends GM_Object> type = expected.getClass();
    if( GM_Point.class.isAssignableFrom( type ) )
      assertPoint( (GM_Point) expected, (GM_Point) actual );
    else if( GM_Curve.class.isAssignableFrom( type ) )
      assertCurve( (GM_Curve) expected, (GM_Curve) actual );
// else if( GM_Surface.class.isAssignableFrom( type ) )
// assertSurface( (GM_Surface< ? >) expected, (GM_Surface< ? >) actual );
    else
      throw new UnsupportedOperationException();
  }

  private static double DELTA = 0.000001;

  static void assertPoint( final GM_Point expected, final GM_Point actual )
  {
    assertEquals( expected.getCoordinateSystem(), actual.getCoordinateSystem() );

    final double[] expectedArray = expected.getAsArray();
    final double[] actualArray = expected.getAsArray();

    assertArrayEquals( expectedArray, actualArray, DELTA );
  }

  static void assertMultiCurve( final GM_MultiCurve expected, final GM_MultiCurve actual ) throws GM_Exception
  {
    assertMultiGeometry( expected, actual );

    assertEquals( expected.getSize(), actual.getSize() );

    final GM_Curve[] expectedCurves = actual.getAllCurves();
    final GM_Curve[] actualCurves = actual.getAllCurves();

    assertEquals( expectedCurves.length, actualCurves.length );
  }

  static void assertCurve( final GM_Curve expectedCurve, final GM_Curve actualCurve ) throws GM_Exception
  {
    assertEquals( expectedCurve.getCoordinateSystem(), actualCurve.getCoordinateSystem() );

    assertEquals( expectedCurve.getNumberOfCurveSegments(), actualCurve.getNumberOfCurveSegments() );

    final int length = expectedCurve.getNumberOfCurveSegments();

    for( int i = 0; i < length; i++ )
    {
      final GM_CurveSegment expectedSegment = expectedCurve.getCurveSegmentAt( i );
      final GM_CurveSegment actualSegment = expectedCurve.getCurveSegmentAt( i );

      assertSegment( expectedSegment, actualSegment );
    }
  }

  static void assertSegment( final GM_CurveSegment expectedSegment, final GM_CurveSegment actualSegment )
  {
    assertEquals( expectedSegment.getCoordinateSystem(), actualSegment.getCoordinateSystem() );

    final GM_Position[] expectedPositions = expectedSegment.getPositions();
    final GM_Position[] actualPositions = expectedSegment.getPositions();

    assertArrayEquals( expectedPositions, actualPositions );
  }
}
