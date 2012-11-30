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
package org.kalypso.model.wspm.core.profil.visitors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Range;
import org.kalypso.commons.exception.CancelVisitorException;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecordVisitor;

/**
 * @author Dirk Kuch
 */
public final class ProfileVisitors
{
  private ProfileVisitors( )
  {

  }

  public static IProfileRecord[] findPointsBetween( final IProfile profile, final double p0, final double pn, final boolean includeVertexPoints )
  {
    return findPointsBetween( profile, Range.between( p0, pn ), includeVertexPoints );
  }

  public static IProfileRecord[] findPointsBetween( final IProfile profile, final Range<Double> range, final boolean includeVertexPoints )
  {
    final FindPointsBetweenVisitor visitor = new FindPointsBetweenVisitor( range, includeVertexPoints );
    profile.accept( visitor, 1 );

    return visitor.getPoints();
  }

  public static IProfileRecord findNextPoint( final IProfile profile, final double breite )
  {
    final FindNextPointVisitor visitor = new FindNextPointVisitor( breite );
    profile.accept( visitor, -1 );

    return visitor.getPoint();
  }

  public static IProfileRecord findPreviousPoint( final IProfile profile, final double breite )
  {
    final FindPreviousPointVisitor visitor = new FindPreviousPointVisitor( breite );
    profile.accept( visitor, 1 );

    return visitor.getPoint();
  }

  public static IProfileRecord[] findPointsBetween( final IProfile profile, final int startIndex, final int endIndex )
  {
    return ArrayUtils.subarray( profile.getPoints(), startIndex, endIndex + 1 );
  }

  public static IProfileRecord findPoint( final IProfile profile, final double width, final double fuzziness )
  {
    final FindPointVisior visitor = new FindPointVisior( width, fuzziness );
    profile.accept( visitor, 1 );

    return visitor.getPoint();
  }

  /**
   * Returns the point with exactly the same width value as the given one.
   */
  public static IProfileRecord findPoint( final IProfile profile, final double width )
  {
    final FindPointVisior visitor = new FindPointVisior( width, 0.0 );
    profile.accept( visitor, 1 );

    return visitor.getPoint();
  }

  public static IProfileRecord findLowestPoint( final IProfile profile )
  {
    final FindMinMaxVisitor visitor = new FindMinMaxVisitor( IWspmConstants.POINT_PROPERTY_HOEHE );
    profile.accept( visitor, 1 );

    return visitor.getMinimum();
  }

  public static void visit( final IProfileRecordVisitor visitor, final IProfileRecord... points )
  {
    for( final IProfileRecord point : points )
    {
      try
      {
        visitor.visit( point, 1 );
      }
      catch( final CancelVisitorException e )
      {
        return;
      }
    }
  }

  public static IProfileRecord findMinimum( final IProfile profile, final String property )
  {
    final FindMinMaxVisitor visitor = new FindMinMaxVisitor( property );
    profile.accept( visitor, 1 );

    return visitor.getMinimum();
  }

  public static IProfileRecord findMaximum( final IProfile profile, final String property )
  {
    final FindMinMaxVisitor visitor = new FindMinMaxVisitor( property );
    profile.accept( visitor, 1 );

    return visitor.getMaximum();
  }

  public static IProfileRecord findNearestPoint( final IProfile profile, final double value )
  {
    final FindClosestPointVisitor visitor = new FindClosestPointVisitor( value );
    profile.accept( visitor, 1 );

    return visitor.getPoint();
  }

}
