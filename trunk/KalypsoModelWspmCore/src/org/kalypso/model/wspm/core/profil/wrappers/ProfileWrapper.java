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
package org.kalypso.model.wspm.core.profil.wrappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kalypso.jts.JTSUtilities;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilPointMarker;
import org.kalypso.model.wspm.core.util.WspmGeometryUtilities;
import org.kalypso.model.wspm.core.util.WspmProfileHelper;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
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
public class ProfileWrapper
{
  private final IProfil m_profile;

  public ProfileWrapper( final IProfil profile )
  {
    m_profile = profile;
  }

  public double getStation( )
  {
    return m_profile.getStation();
  }

  public boolean hasPoint( final double width )
  {
    if( findPoint( width ) == null )
      return false;

    return true;
  }

  public ProfilePointWrapper findPoint( final double width )
  {
    final IRecord[] points = m_profile.getPoints();
    for( final IRecord point : points )
    {
      final ProfilePointWrapper wrapper = new ProfilePointWrapper( point );
      final double breite = wrapper.getBreite();
      if( breite == width )
        return wrapper;
    }

    return null;
  }

  public ProfilePointWrapper addPoint( final Double width )
  {
    if( hasPoint( width ) )
      return findPoint( width );

    final IRecord add = m_profile.createProfilPoint();
    final ProfilePointWrapper addWrapper = new ProfilePointWrapper( add );
    addWrapper.setBreite( width );
    addWrapper.setHoehe( getHoehe( width ) );

    final IRecord added = WspmProfileHelper.addRecordByWidth( m_profile, add );
    return new ProfilePointWrapper( added );
  }

  private double getHoehe( final Double width )
  {
    final ProfilePointWrapper before = findPointBefore( width );
    final ProfilePointWrapper after = findPointAfter( width );

    if( before == null || after == null )
      return 0.0;

    final double deltaH = after.getHoehe() - before.getHoehe();
    final double distanceDeltaH = Math.abs( before.getBreite() - after.getBreite() );

    final double distance = Math.abs( before.getBreite() - width );
    final double hoehe = deltaH / distanceDeltaH * distance;

    return before.getHoehe() + hoehe;
  }

  /**
   * TODO obselete? use getNextPoint()
   */
  private ProfilePointWrapper findPointAfter( final Double width )
  {
    ProfilePointWrapper before = null;
    final ProfilePointWrapper[] points = getPoints();
    for( final ProfilePointWrapper point : points )
    {
      if( point.getBreite() < width )
        before = point;
      else
        return before;
    }

    return null;
  }

  /**
   * TODO obselete? use getPreviousPoint()
   */
  private ProfilePointWrapper findPointBefore( final Double width )
  {
    final ProfilePointWrapper[] points = getPoints();
    for( final ProfilePointWrapper point : points )
    {
      if( point.getBreite() > width )
        return point;
    }

    return null;
  }

  public ProfilePointWrapper[] findPointsBetween( final Double p1, final Double p2, final boolean includeVertexPoints )
  {
    final Double min = Math.min( p1, p2 );
    final Double max = Math.max( p1, p2 );

    final Set<ProfilePointWrapper> between = new TreeSet<ProfilePointWrapper>( ProfilePointWrapper.COMPARATOR );

    final IRecord[] points = m_profile.getPoints();
    for( final IRecord point : points )
    {
      final ProfilePointWrapper wrapper = new ProfilePointWrapper( point );
      final double breite = wrapper.getBreite();

      if( breite > min && breite < max )
      {
        between.add( wrapper );
      }
      else if( includeVertexPoints && (breite == min || breite == max) )
      {
        between.add( wrapper );
      }
    }

    return between.toArray( new ProfilePointWrapper[] {} );
  }

  public ProfilePointMarkerWrapper[] getProfilePointMarkerWrapper( final String marker )
  {
    final List<ProfilePointMarkerWrapper> wrappers = new ArrayList<ProfilePointMarkerWrapper>();

    final IProfilPointMarker[] markers = m_profile.getPointMarkerFor( marker );
    for( final IProfilPointMarker m : markers )
    {
      wrappers.add( new ProfilePointMarkerWrapper( m ) );
    }

    return wrappers.toArray( new ProfilePointMarkerWrapper[] {} );
  }

  public ProfilePointWrapper[] getPoints( )
  {
    final List<ProfilePointWrapper> wrappers = new ArrayList<ProfilePointWrapper>();

    final TupleResult result = m_profile.getResult();
    for( final IRecord record : result )
    {
      wrappers.add( new ProfilePointWrapper( record ) );
    }

    return wrappers.toArray( new ProfilePointWrapper[] {} );
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return String.format( "Profile %.3f km", m_profile.getStation() );
  }

  public ProfilePointWrapper getFirstPoint( )
  {
    final TupleResult result = m_profile.getResult();
    if( result.isEmpty() )
      return null;

    return new ProfilePointWrapper( result.get( 0 ) );

  }

  public ProfilePointWrapper getLastPoint( )
  {
    final TupleResult result = m_profile.getResult();
    if( result.isEmpty() )
      return null;

    return new ProfilePointWrapper( result.get( result.size() - 1 ) );

  }

  public LineString getGeometry( ) throws GM_Exception
  {
    final String crs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();

    final GM_Curve profileCurve = WspmGeometryUtilities.createProfileSegment( m_profile, crs );
    final LineString profileLineString = (LineString) JTSAdapter.export( profileCurve );

    return profileLineString;
  }

  public IProfil getProfile( )
  {
    return m_profile;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof ProfileWrapper )
    {
      final EqualsBuilder builder = new EqualsBuilder();
      builder.append( getStation(), ((ProfileWrapper) obj).getStation() );

      return builder.isEquals();
    }

    return super.equals( obj );
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode( )
  {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append( getClass().getName() );
    builder.append( getStation() );

    return builder.toHashCode();
  }

  public ProfilePointWrapper getNextPoint( final double breite )
  {
    final ProfilePointWrapper lastPoint = getLastPoint();
    if( lastPoint == null )
      return null;

    if( breite == lastPoint.getBreite() )
      return lastPoint;

    final ProfilePointWrapper[] points = getPoints();
    for( final ProfilePointWrapper point : points )
    {
      if( point.getBreite() > breite )
        return point;
    }

    return null;
  }

  public ProfilePointWrapper getPreviousPoint( final double breite )
  {
    final ProfilePointWrapper firstPoint = getFirstPoint();
    if( firstPoint == null )
      return null;

    if( breite == firstPoint.getBreite() )
      return firstPoint;

    ProfilePointWrapper last = null;

    final ProfilePointWrapper[] points = getPoints();
    for( final ProfilePointWrapper point : points )
    {
      if( point.getBreite() < breite )
        last = point;
      else if( point.getBreite() >= breite )
        break;
    }

    return last;
  }

  public GM_Point getPosition( final double breite ) throws Exception
  {
    return WspmProfileHelper.getGeoPosition( breite, m_profile );
  }

  public Coordinate getJtsPosition( final double breite ) throws Exception
  {
    final GM_Point point = getPosition( breite );
    final Point p = (Point) JTSAdapter.export( point );

    return p.getCoordinate();
  }

  public void remove( final ProfilePointWrapper... remove )
  {
    for( final ProfilePointWrapper wrapper : remove )
    {
      m_profile.removePoint( wrapper.getRecord() );
    }
  }

  public double getLowestHeight( )
  {
    Double height = Double.MAX_VALUE;

    final ProfilePointWrapper[] points = getPoints();
    for( final ProfilePointWrapper point : points )
    {
      if( point.getHoehe() < height )
        height = point.getHoehe();
    }

    return height;
  }

  public double getWidth( final Point point ) throws GM_Exception
  {
    final double jtsDistance = JTSUtilities.pointDistanceOnLine( getGeometry(), point );
    final double width = getFirstPoint().getBreite() + jtsDistance;

    return width;
  }

  public ProfilePointMarkerWrapper[] getPointMarkers( final ProfilePointWrapper point )
  {
    final IProfilPointMarker[] markers = m_profile.getPointMarkerFor( point.getRecord() );
    if( ArrayUtils.isEmpty( markers ) )
      return new ProfilePointMarkerWrapper[] {};

    final List<ProfilePointMarkerWrapper> myMarkers = new ArrayList<ProfilePointMarkerWrapper>();

    for( final IProfilPointMarker marker : markers )
    {
      myMarkers.add( new ProfilePointMarkerWrapper( marker ) );
    }

    return myMarkers.toArray( new ProfilePointMarkerWrapper[] {} );
  }
}
