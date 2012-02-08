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
package org.kalypso.model.wspm.ui.action.base;

import java.awt.Graphics;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilPointMarker;
import org.kalypso.model.wspm.core.profil.IProfilPointMarkerProvider;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.core.profil.wrappers.Profiles;
import org.kalypso.ogc.gml.map.widgets.advanced.utils.SLDPainter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

/**
 * @author Dirk Kuch
 */
public final class ProfilePainter
{
  private ProfilePainter( )
  {
  }

  public static void paintProfilePoints( final Graphics g, final SLDPainter painter, final IProfileFeature profile )
  {
    if( Objects.isNull( profile ) )
      return;

    try
    {
      final LineString lineString = profile.getJtsLine();
      if( Objects.isNull( lineString ) )
        return;

      final Coordinate[] coordinates = lineString.getCoordinates();
      painter.paint( g, ProfilePainter.class.getResource( "symbolization/profile.points.sld" ), coordinates ); //$NON-NLS-1$
    }
    catch( final Throwable t )
    {
      t.printStackTrace();
    }
  }

  public static void paintProfilePointMarkers( final Graphics g, final SLDPainter painter, final IProfileFeature profile )
  {
    if( Objects.isNull( profile ) )
      return;

    final IProfil iProfile = profile.getProfil();
    final IProfilPointMarkerProvider provider = KalypsoModelWspmCoreExtensions.getMarkerProviders( iProfile.getType() );

    final IProfilPointMarker[] markers = iProfile.getPointMarkers();
    for( final IProfilPointMarker marker : markers )
    {
      try
      {
        final URL sld = provider.getSld( marker.getComponent().getId() );
        painter.paint( g, sld, marker.getPoint().getCoordinate() );
      }
      catch( final CoreException e )
      {
        e.printStackTrace();
      }
    }

  }

  public static void doPaintProfileCursor( final Graphics g, final SLDPainter painter, final IProfileFeature profileFeature, final URL sldLinePoint, final URL sldVertexPoint )
  {
    try
    {
      if( Objects.isNull( profileFeature ) )
        return;

      final IProfil profile = profileFeature.getProfil();
      final IRangeSelection selection = profile.getSelection();
      final Double cursor = selection.getCursor();
      if( Objects.isNull( cursor ) )
        return;

      final Coordinate position = Profiles.getJtsPosition( profile, cursor );

      if( isVertexPoint( profileFeature.getJtsLine(), position ) )
        painter.paint( g, sldVertexPoint, position ); //$NON-NLS-1$
      else
        painter.paint( g, sldLinePoint, position ); //$NON-NLS-1$
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
  }

  private static boolean isVertexPoint( final Geometry geometry, final Coordinate point )
  {
    if( point == null )
      return false;

    final Coordinate[] coordinates = geometry.getCoordinates();
    for( final Coordinate c : coordinates )
    {
      if( c.distance( point ) < 0.001 )
        return true;
    }

    return false;
  }
}
