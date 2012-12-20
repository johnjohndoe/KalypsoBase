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
package org.kalypso.model.wspm.core.util;

import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.util.ProfileUtil;
import org.kalypso.observation.result.IComponent;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.util.Assert;

/**
 * Calculates waterlevel segments for a {@link org.kalypso.model.wspm.core.profil.IProfil}.
 *
 * @author Gernot Belger
 */
public class WaterlevelIntersectionWorker
{
  private final IProfile m_profile;

  private final double m_waterlevelHeight;

  private LineSegment[] m_segments;

  private final GeometryFactory m_factory;

  public WaterlevelIntersectionWorker( final IProfile profile, final double waterlevelHeight )
  {
    m_profile = profile;
    m_waterlevelHeight = waterlevelHeight;

    final String profileSRS = m_profile.getSrsName();
    final int profileSRID = JTSAdapter.toSrid( profileSRS );
    m_factory = new GeometryFactory( new PrecisionModel(), profileSRID );
  }

  public void execute( )
  {
    final Coordinate[] profileCoordinates = buildProfileCoordinates();

    final LineString waterlevelLine = buildWaterlevelLine( profileCoordinates );

    final JTSWaterlevelIntersector intersector = new JTSWaterlevelIntersector( profileCoordinates );
    final LineString[] lines = intersector.createWaterlevels( waterlevelLine );

    m_segments = extractSegments( lines );
  }

  private Coordinate[] buildProfileCoordinates( )
  {
    final IComponent cHeight = m_profile.hasPointProperty( IWspmConstants.POINT_PROPERTY_HOEHE );
    final IComponent cWidth = m_profile.hasPointProperty( IWspmConstants.POINT_PROPERTY_BREITE );

    final Double[] widthValues = ProfileUtil.getDoubleValuesFor( m_profile, cWidth, false );
    final Double[] heightValues = ProfileUtil.getDoubleValuesFor( m_profile, cHeight, false );

    Assert.isTrue( widthValues.length == heightValues.length );

    final CoordinateList coordinates = new CoordinateList();

    for( int i = 0; i < widthValues.length; i++ )
    {
      final Double width = widthValues[i];
      final Double height = heightValues[i];
      if( width != null && height != null )
      {
        final Coordinate crd = new Coordinate( width, height );
        coordinates.add( crd, false );
      }
    }

    return coordinates.toCoordinateArray();
  }

  private LineString buildWaterlevelLine( final Coordinate[] profileCoordinates )
  {
    if( profileCoordinates.length < 3 )
      return null;

    final Coordinate firstCrd = profileCoordinates[0];
    final Coordinate lastCrd = profileCoordinates[profileCoordinates.length - 1];

    final Coordinate waterlevelStart = new Coordinate( firstCrd.x, m_waterlevelHeight );
    final Coordinate waterlevelEnd = new Coordinate( lastCrd.x, m_waterlevelHeight );

    return m_factory.createLineString( new Coordinate[] { waterlevelStart, waterlevelEnd } );
  }

  public LineSegment[] getSegments( )
  {
    return m_segments;
  }

  private LineSegment[] extractSegments( final LineString[] lines )
  {
    final LineSegment[] segments = new LineSegment[lines.length];

    for( int i = 0; i < segments.length; i++ )
    {
      final LineString line = lines[i];

      // The algorithm should guarantuee, that a horizontal waterlevel only delivers segments
      Assert.isTrue( line.getNumPoints() == 2 );

      segments[i] = new LineSegment( line.getCoordinateN( 0 ), line.getCoordinateN( 1 ) );
    }

    return segments;
  }
}