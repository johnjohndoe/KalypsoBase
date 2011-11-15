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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.util.ProfilUtil;
import org.kalypso.observation.result.IComponent;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollectionIterator;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.Assert;

/**
 * Calculates waterlevel segments for a {@link org.kalypso.model.wspm.core.profil.IProfil}.
 * 
 * @author Gernot Belger
 */
public class WaterlevelIntersectionWorker
{
  private final List<LineSegment> m_segments = new LinkedList<LineSegment>();

  private final GeometryFactory m_factory = new GeometryFactory();

  private final IProfil m_profile;

  private final double m_height;

  public WaterlevelIntersectionWorker( final IProfil profile, final double height )
  {
    m_profile = profile;
    m_height = height;
  }

  public void execute( )
  {
    final int iHoehe = m_profile.indexOfProperty( IWspmConstants.POINT_PROPERTY_HOEHE );
// final int iBreite = m_profile.indexOfProperty( cBreite );

// final IRecord[] points = m_profile.getPoints();
// final IRecord firstPoint = points[0];
// final IRecord lastPoint = points[points.length - 1];

// final double firstX = (Double) firstPoint.getValue( iBreite );
// final double firstY = (Double) firstPoint.getValue( iHoehe );
// final double lastX = (Double) lastPoint.getValue( iBreite );
// final double lastY = (Double) lastPoint.getValue( iHoehe );

    final Double maxHeight = ProfilUtil.getMaxValueFor( m_profile, iHoehe );

    final double top = Math.max( maxHeight, m_height ) + 1.0;

    final List<Coordinate> profileCoordinates = buildProfileCoordinates();

    final Polygon profileNegative = buildProfileNegative( profileCoordinates, top );
    final LineString waterlevelLine = buildWaterlevelLine( profileCoordinates );

    /* Decompose segments */
    final Geometry waterlevelSegments = profileNegative.intersection( waterlevelLine );
    addSegments( waterlevelSegments );
  }

  private void addSegments( final Geometry segments )
  {
    for( final GeometryCollectionIterator iterator = new GeometryCollectionIterator( segments ); iterator.hasNext(); )
    {
      final Object segment = iterator.next();
      addSegment( segment );
    }
  }

  private void addSegment( final Object segment )
  {
    if( segment instanceof LineString )
    {
      final LineString line = (LineString) segment;

      Assert.isTrue( line.getNumPoints() == 2 );

      final LineSegment waterLevel = new LineSegment( line.getCoordinateN( 0 ), line.getCoordinateN( 1 ) );

      m_segments.add( waterLevel );
      return;
    }
  }

  private List<Coordinate> buildProfileCoordinates( )
  {
    final IComponent cHeight = m_profile.hasPointProperty( IWspmConstants.POINT_PROPERTY_HOEHE );
    final IComponent cWidth = m_profile.hasPointProperty( IWspmConstants.POINT_PROPERTY_BREITE );

    final Double[] widthValues = ProfilUtil.getDoubleValuesFor( m_profile, cWidth, false );
    final Double[] heightValues = ProfilUtil.getDoubleValuesFor( m_profile, cHeight, false );

    Assert.isTrue( widthValues.length == heightValues.length );

    final List<Coordinate> crds = new ArrayList<Coordinate>();

    for( int i = 0; i < widthValues.length; i++ )
    {
      final Double width = widthValues[i];
      final Double height = heightValues[i];
      if( width != null && height != null )
      {
        crds.add( new Coordinate( width, height ) );
      }
    }

    return crds;
  }

  /**
   * Builds a 'negative' of the profile area.
   */
  private Polygon buildProfileNegative( final List<Coordinate> profileCoordinates, final double top )
  {
    if( profileCoordinates.size() < 3 )
      return null;

    final List<Coordinate> negativeCoordinates = new ArrayList<Coordinate>();

    final Coordinate firstCrd = profileCoordinates.get( 0 );
    final Coordinate lastCrd = profileCoordinates.get( profileCoordinates.size() - 1 );

    negativeCoordinates.addAll( profileCoordinates );
    negativeCoordinates.add( new Coordinate( lastCrd.x, top ) );
    negativeCoordinates.add( new Coordinate( firstCrd.x, top ) );
    negativeCoordinates.add( new Coordinate( firstCrd ) );

    final LinearRing shell = m_factory.createLinearRing( negativeCoordinates.toArray( new Coordinate[negativeCoordinates.size()] ) );
    return m_factory.createPolygon( shell, null );
  }

  private LineString buildWaterlevelLine( final List<Coordinate> profileCoordinates )
  {
    final Coordinate firstCrd = profileCoordinates.get( 0 );
    final Coordinate lastCrd = profileCoordinates.get( profileCoordinates.size() - 1 );

    final Coordinate waterlevelStart = new Coordinate( firstCrd.x, m_height );
    final Coordinate waterlevelEnd = new Coordinate( lastCrd.x, m_height );

    return m_factory.createLineString( new Coordinate[] { waterlevelStart, waterlevelEnd } );
  }

  public LineSegment[] getSegments( )
  {
    return m_segments.toArray( new LineSegment[m_segments.size()] );
  }
}
