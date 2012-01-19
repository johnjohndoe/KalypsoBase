/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.model.wspm.core.profil.wrappers;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.jts.JTSUtilities;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.visitors.ProfileVisitors;
import org.kalypso.model.wspm.core.util.WspmGeometryUtilities;
import org.kalypso.model.wspm.core.util.WspmProfileHelper;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * @author Dirk Kuch
 */
public final class Profiles
{
  private Profiles( )
  {
  }

  public static IProfileRecord addPoint( final IProfil profile, final Double width )
  {
    final IProfileRecord point = ProfileVisitors.findPoint( profile, width );
    if( Objects.isNotNull( point ) )
      return point;

    final IProfileRecord add = profile.createProfilPoint();
    add.setBreite( width );
    add.setHoehe( getHoehe( profile, width ) );

    return WspmProfileHelper.addRecordByWidth( profile, add );
  }

  public static double getHoehe( final IProfil profile, final Double width )
  {
    final IProfileRecord before = profile.findPreviousPoint( width );
    final IProfileRecord after = profile.findNextPoint( width );
    if( Objects.isNull( before, after ) )
      return 0.0;

    final double deltaH = after.getHoehe() - before.getHoehe();
    final double distanceDeltaH = Math.abs( before.getBreite() - after.getBreite() );

    final double distance = Math.abs( before.getBreite() - width );
    final double hoehe = deltaH / distanceDeltaH * distance;

    return before.getHoehe() + hoehe;
  }

  public static double getWidth( final IProfil profile, final Point point ) throws GM_Exception
  {

    final LineString lineString = getGeometry( profile );
    // TODO: dangerous: width/rechtswert/hochwert are not alwayws related!
    // TODO: maybe delegate to WspProfileHelper#getWidthPosition
    final double jtsDistance = JTSUtilities.pointDistanceOnLine( lineString, point );
    final double width = profile.getFirstPoint().getBreite() + jtsDistance;

    return width;
  }

  public static LineString getGeometry( final IProfil profile ) throws GM_Exception
  {
    final String crs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();

    final GM_Curve profileCurve = WspmGeometryUtilities.createProfileSegment( profile, crs );
    final LineString profileLineString = (LineString) JTSAdapter.export( profileCurve );

    return profileLineString;
  }
}
