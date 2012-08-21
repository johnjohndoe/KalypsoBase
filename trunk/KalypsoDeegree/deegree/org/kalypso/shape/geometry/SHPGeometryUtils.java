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
package org.kalypso.shape.geometry;

import org.eclipse.core.runtime.Assert;

/**
 * @author Gernot Belger
 */
public final class SHPGeometryUtils
{
  private SHPGeometryUtils( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" );
  }

  public static SHPEnvelope createEnvelope( final ISHPPoint[][] parts )
  {
    SHPEnvelope mbr = null;

    for( final ISHPPoint[] point : parts )
    {
      final SHPEnvelope pointsEnvelope = createEnvelope( point );
      if( mbr == null )
        mbr = pointsEnvelope;
      else
        mbr.expand( pointsEnvelope );
    }

    return mbr;
  }

  public static SHPEnvelope createEnvelope( final ISHPPoint[] points )
  {
    SHPEnvelope mbr = null;

    for( final ISHPPoint point : points )
    {
      final SHPEnvelope pointMbr = new SHPEnvelope( point, point );
      if( mbr == null )
        mbr = pointMbr;
      else
        mbr.expand( pointMbr );
    }

    return mbr;
  }

  public static int countPoints( final ISHPPoint[][] parts )
  {
    int count = 0;

    for( final ISHPPoint[] points : parts )
      count += points.length;

    return count;
  }

  public static SHPRange createZRange( final ISHPPoint[] points )
  {
    double zmin = Double.MAX_VALUE;
    double zmax = -Double.MAX_VALUE;

    for( final ISHPPoint point : points )
    {
      final double z = point.getZ();

      zmin = Math.min( zmin, z );
      zmax = Math.min( zmax, z );
    }

    return new SHPRange( zmin, zmax );
  }

  public static SHPRange createMRange( final ISHPPoint[] points )
  {
    double mmin = Double.MAX_VALUE;
    double mmax = -Double.MAX_VALUE;

    for( final ISHPPoint point : points )
    {
      final double m = point.getM();

      mmin = Math.min( mmin, m );
      mmax = Math.min( mmax, m );
    }

    return new SHPRange( mmin, mmax );
  }

  /**
   * Due to the shape specification, any line/polygon must contain at least of one part.<br>
   * Each part consists at least of two points.
   */
  public static void checkParts( final ISHPMultiPoint multiPoint, final int[] partIndices )
  {
    Assert.isTrue( partIndices.length > 0, "At least one part must be present." );
    Assert.isTrue( partIndices[0] == 0, "First part must start at 0" );

    for( int i = 0; i < partIndices.length - 1; i++ )
    {
      final int currentPart = partIndices[i];
      final int nextPart = partIndices[i + 1];

      Assert.isTrue( nextPart - currentPart > 1, "Each part must contain at least two points." );
    }

    /* Also the last part must have length > 1 */
    Assert.isTrue( partIndices[partIndices.length - 1] < multiPoint.length() - 1, "Each part must contain at least two points." );
  }
}