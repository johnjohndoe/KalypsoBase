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

import org.apache.commons.lang3.Range;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.jts.JTSConverter;
import org.kalypso.jts.JTSUtilities;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfilePointMarker;
import org.kalypso.model.wspm.core.profil.IProfilePointMarkerProvider;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.core.profil.wrappers.Profiles;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.map.widgets.advanced.utils.SLDPainter;
import org.kalypso.transformation.transformer.JTSTransformer;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;
import org.opengis.referencing.FactoryException;

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

    final IProfile iProfile = profile.getProfile();
    final IProfilePointMarkerProvider provider = KalypsoModelWspmCoreExtensions.getMarkerProviders( iProfile.getType() );

    final String kalypsoSRS = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
    final String profileSRS = profile.getSrsName();

    final int profileSRID = JTSAdapter.toSrid( profileSRS );
    final int kalypsoSRID = JTSAdapter.toSrid( kalypsoSRS );

    try
    {
      final JTSTransformer transformer = new JTSTransformer( profileSRID, kalypsoSRID );

      final IProfilePointMarker[] markers = iProfile.getPointMarkers();
      for( final IProfilePointMarker marker : markers )
      {
        try
        {
          final URL sld = provider.getSld( marker.getComponent().getId() );
          final Coordinate coordinate = marker.getPoint().getCoordinate();

          if( coordinate != null )
          {
            final Coordinate transformedCrd = transformer.transform( coordinate );
            painter.paint( g, sld, transformedCrd );
          }
          // TODO: interpolate coordinate if coordinate of marker point is not there
        }
        catch( final Exception e )
        {
          e.printStackTrace();
        }
      }
    }
    catch( final FactoryException e1 )
    {
      e1.printStackTrace();
    }
  }

  public static void paintProfileCursor( final Graphics g, final SLDPainter painter, final IProfileFeature profileFeature, final URL sldLinePoint, final URL sldVertexPoint )
  {
    try
    {
      if( Objects.isNull( profileFeature ) )
        return;

      final IProfile profile = profileFeature.getProfile();
      final IRangeSelection selection = profile.getSelection();
      final Double cursor = selection.getCursor();
      if( Objects.isNull( cursor ) )
        return;

      final Coordinate position = Profiles.getJtsPosition( profile, cursor );
      if( position == null )
        return;

      if( ProfileWidgetHelper.isVertexPoint( profileFeature.getJtsLine(), position ) )
        painter.paint( g, sldVertexPoint, position ); //$NON-NLS-1$
      else
        painter.paint( g, sldLinePoint, position ); //$NON-NLS-1$
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
  }

  public static void paintSelection( final IMapPanel mapPanel, final IProfileFeature profile, final Graphics g, final SLDPainter painter )
  {
    try
    {
      if( Objects.isNull( profile ) )
        return;

      final IProfile iProfil = profile.getProfile();
      final IRangeSelection selection = iProfil.getSelection();
      if( selection.isEmpty() )
        return;

      final Geometry geometry = toGeometry( profile, selection );
      if( geometry instanceof com.vividsolutions.jts.geom.Point )
      {
        final com.vividsolutions.jts.geom.Point point = (com.vividsolutions.jts.geom.Point) geometry;
        painter.paint( g, ProfilePainter.class.getResource( "symbolization/selection.points.sld" ), point ); //$NON-NLS-1$
      }
      else if( geometry instanceof LineString )
      {
        final LineString lineString = (LineString) geometry;
        final Geometry selectionGeometry = lineString.buffer( MapUtilities.calculateWorldDistance( mapPanel, 8 ) );

        painter.paint( g, ProfilePainter.class.getResource( "symbolization/selection.line.sld" ), selectionGeometry ); //$NON-NLS-1$
      }
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
  }

  private static Geometry toGeometry( final IProfileFeature profile, final IRangeSelection selection ) throws Exception
  {
    final Range<Double> range = selection.getRange();
    final Double minimum = range.getMinimum();
    final Double maximum = range.getMaximum();

    if( Objects.equal( minimum, maximum ) )
    {
      final Coordinate coorinate = Profiles.getJtsPosition( profile.getProfile(), minimum );
      return JTSConverter.toPoint( coorinate );
    }
    else
    {
      final Coordinate c1 = Profiles.getJtsPosition( profile.getProfile(), minimum );
      final Coordinate c2 = Profiles.getJtsPosition( profile.getProfile(), maximum );

      return JTSUtilities.createLineString( profile.getJtsLine(), JTSConverter.toPoint( c1 ), JTSConverter.toPoint( c2 ) );
    }
  }
}