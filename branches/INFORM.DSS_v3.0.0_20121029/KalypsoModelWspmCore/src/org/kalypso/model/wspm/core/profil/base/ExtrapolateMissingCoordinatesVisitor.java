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
package org.kalypso.model.wspm.core.profil.base;

import org.kalypso.commons.java.lang.Doubles;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.jts.JtsVectorUtilities;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecordVisitor;

import com.vividsolutions.jts.geom.Point;

/**
 * @author Dirk Kuch
 * @deprecated Buggy!
 */
@Deprecated
public class ExtrapolateMissingCoordinatesVisitor implements IProfileRecordVisitor
{
  // FIXME: this is just nonsense and does not work

  // TODO:
  // handle start and end separately
  // - find last two points and calculate vector
  // - for each missing point, use vector * (distance width )

  /**
   * <pre>
   *
   *
   *  -------x--              ------x-x--
   *            -           -
   *             -x---?-----
   *
   * ? = missing coordinate
   * x = existing coordinate
   *
   * algorithm looks for nearest vector of existing points and extrapolates the missing
   * position ("shoot in the dark")
   * </pre>
   */
  @Override
  public void visit( final IProfileRecord point, final int searchDirection )
  {
    if( !Doubles.isNaN( point.getRechtswert(), point.getHochwert() ) )
      return;

    final IProfile profile = point.getProfile();
    final double p0 = profile.getFirstPoint().getBreite();
    final double pn = profile.getLastPoint().getBreite();

    // before
    final FindVectorVisitor v0 = new FindVectorVisitor();
    profile.accept( v0, p0, point.getBreite(), true, -1 );

    // after
    final FindVectorVisitor vn = new FindVectorVisitor();
    profile.accept( vn, point.getBreite(), pn, true, 1 );

    final FindVectorVisitor visitor = getVector( point, v0, vn );
    if( Objects.isNull( visitor ) )
      return;

    final double distance = Math.abs( visitor.getP0() - point.getBreite() );

    Point moved = null;
    if( visitor == v0 )
      moved = JtsVectorUtilities.movePoint( visitor.getP0Coordinate(), visitor.getVector(), distance, -1 );
    else
      moved = JtsVectorUtilities.movePoint( visitor.getP0Coordinate(), visitor.getVector(), distance, 1 );

    point.setRechtswert( moved.getX() );
    point.setHochwert( moved.getY() );
  }

  private FindVectorVisitor getVector( final IProfileRecord point, final FindVectorVisitor... visitors )
  {
    FindVectorVisitor ptr = null;
    for( final FindVectorVisitor visitor : visitors )
    {
      if( !visitor.isValid() )
        continue;
      else if( Objects.isNull( ptr ) )
        ptr = visitor;
      else
      {
        final double p0 = ptr.getP0();
        final double pn = visitor.getP0();

        final double d0 = Math.abs( p0 - point.getBreite() );
        final double dn = Math.abs( pn - point.getBreite() );

        if( dn < d0 )
          ptr = visitor;
      }
    }

    return ptr;
  }

  @Override
  public boolean isWriter( )
  {
    return true;
  }

}
