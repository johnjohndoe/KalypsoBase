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
import org.kalypso.jts.JTSConverter;
import org.kalypso.jts.JTSUtilities;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.util.ProfileUtil;
import org.kalypso.model.wspm.core.profil.visitors.ProfileVisitors;
import org.kalypso.model.wspm.core.util.WspmGeometryUtilities;
import org.kalypso.model.wspm.core.util.WspmProfileHelper;
import org.kalypso.observation.result.IComponent;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * @author Dirk Kuch
 */
public final class Profiles
{
  // FIXME: bad -> this is lower than the general precision of 'breite' -> leads to bugs!
  public static final double FUZZINESS = 0.005; // Inaccuracies profile of points

  private Profiles( )
  {
  }

  /**
   * Returns the point at the given width.<br/>
   * If no point with the given width exists, create a new point. The height value of the new point (but no other properties) is interpolated from adjacent points.
   */
  public static IProfileRecord addOrFindPoint( final IProfile profile, final Double width )
  {
    final IProfileRecord point = ProfileVisitors.findPoint( profile, width );
    if( Objects.isNotNull( point ) )
      return point;

    final IProfileRecord add = profile.createProfilPoint();
    add.setBreite( width );

    // FIXME: only height is interpolated here, why?!
    add.setHoehe( getHoehe( profile, width ) );

    return addRecordByWidth( profile, add, false );
  }

  private static IProfileRecord addRecordByWidth( final IProfile profile, final IProfileRecord point, final boolean overwritePointMarkers )
  {
    final Double width = ProfileUtil.getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_BREITE, point );

    final IProfileRecord[] records = profile.getPoints();
    final int iBreite = profile.indexOfProperty( IWspmPointProperties.POINT_PROPERTY_BREITE );

    for( int i = 0; i < records.length; i++ )
    {
      final IProfileRecord r = records[i];
      final Double rw = (Double)r.getValue( iBreite );

      if( Math.abs( width - rw ) < FUZZINESS )
      {
        /* record already exists - copy values */
        for( final IComponent component : profile.getPointProperties() )
        {
          // don't overwrite existing point markers!
          if( !overwritePointMarkers && profile.isPointMarker( component.getId() ) )
          {
            continue;
          }
          final int index = profile.indexOfProperty( component );
          r.setValue( index, point.getValue( index ) );
        }
        return r;
      }
      else if( width < rw )
      {
        // add new record
        profile.addPoint( i, point );
        return point;
      }
      else if( width.equals( rw ) )
        throw new IllegalStateException();
    }

    profile.addPoint( point );

    return point;
  }

  public static double getHoehe( final IProfile profile, final Double width )
  {
    return WspmProfileHelper.getHeightByWidth( width, profile );
  }

  public static double getWidth( final IProfile profile, final Point point ) throws GM_Exception
  {
    try
    {
      final GM_Point gmp = JTSConverter.toGMPoint( point, KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );
      return WspmProfileHelper.getWidthPosition( gmp, profile );
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      final LineString lineString = getGeometry( profile );
      final double jtsDistance = JTSUtilities.pointDistanceOnLine( lineString, point );
      return profile.getFirstPoint().getBreite() + jtsDistance;
    }
  }

  public static LineString getGeometry( final IProfile profile ) throws GM_Exception
  {
    final String crs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();

    final GM_Curve profileCurve = WspmGeometryUtilities.createProfileSegment( profile, crs );
    return (LineString)JTSAdapter.export( profileCurve );
  }

  public static GM_Point getPosition( final IProfile profile, final double breite ) throws Exception
  {
    return WspmProfileHelper.getGeoPositionKalypso( breite, profile );
  }

  public static Coordinate getJtsPosition( final IProfile profile, final double breite ) throws Exception
  {
    final GM_Point point = getPosition( profile, breite );
    final Point p = (Point)JTSAdapter.export( point );
    if( p == null )
      return null;

    return p.getCoordinate();
  }
}
