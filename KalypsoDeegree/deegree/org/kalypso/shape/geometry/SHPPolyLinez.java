/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 *
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always.
 *
 * If you intend to use this software in other ways than in kalypso
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree,
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypso.shape.geometry;

import org.kalypso.shape.ShapeType;

import com.vividsolutions.jts.util.Assert;

/**
 * @author Thomas Jung
 */
public class SHPPolyLinez extends AbstractSHPPolyLine
{
  public SHPPolyLinez( final ISHPMultiPoint points, final int[] parts )
  {
    super( points, parts, ShapeType.POLYLINEZ );

    Assert.isTrue( points instanceof SHPMultiPointz );
  }

  public SHPPolyLinez( final byte[] recBuf )
  {
    super( recBuf, ShapeType.POLYLINEZ );
  }

  @Override
  protected ISHPMultiPoint readPoints( final byte[] recBuf, final SHPEnvelope envelope, final int numParts, final int numPoints )
  {
    return SHPMultiPointz.read( recBuf, 4 + 32 + 4 + 4 + numParts * 4, envelope, numPoints );
  }

  @Override
  public int length( )
  {
    // TODO: depends on m: m might be optional...

    return 32 + 4 + 4 + getNumParts() * 4 + getNumPoints() * 16 + 16 + getNumPoints() * 8 + 16 + getNumPoints() * 8;
  }
}