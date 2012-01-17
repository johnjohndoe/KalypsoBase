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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.kalypso.commons.exception.CancelVisitorException;
import org.kalypso.jts.JTSUtilities;
import org.kalypso.model.wspm.core.i18n.Messages;
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

  public void accept( final IProfilePointWrapperVisitor visitor, final int direction )
  {
    doAccept( visitor, getPoints(), direction );
  }

  public void accept( final IProfilePointWrapperVisitor visitor, final Double p1, final Double p2, final boolean includeVertexPoints, final int direction )
  {
    doAccept( visitor, findPointsBetween( p1, p2, includeVertexPoints ), direction );
  }

  private void doAccept( final IProfilePointWrapperVisitor visitor, final IProfileRecord[] points, final int direction )
  {
    try
    {
      if( direction >= 0 )
      {
        for( final IProfileRecord point : points )
        {
          visitor.visit( this, point );
        }
      }
      else
      {
        for( int index = ArrayUtils.getLength( points ) - 1; index > 0; index-- )
        {
          final IProfileRecord point = points[index];
          visitor.visit( this, point );
        }
      }
    }
    catch( final CancelVisitorException ex )
    {
      return;
    }
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

  /**
   * Returns the point with exactly the same width value as the given one.
   */
  public IProfileRecord findPoint( final double width )
  {
    final IRecord[] points = m_profile.getPoints();
    for( final IRecord point : points )
    {
      final ProfileRecord wrapper = new ProfileRecord( point );
      final double breite = wrapper.getBreite();
      if( breite == width )
        return wrapper;
    }

    return null;
  }

  public IProfileRecord addPoint( final Double width )
  {
    if( hasPoint( width ) )
      return findPoint( width );

    final IProfileRecord add = m_profile.createProfilPoint();
    add.setBreite( width );
    add.setHoehe( getHoehe( width ) );

    return WspmProfileHelper.addRecordByWidth( m_profile, add );
  }

  public double getHoehe( final Double width )
  {
    final IProfileRecord before = findPreviousPoint( width );
    final IProfileRecord after = findNextPoint( width );

    if( before == null || after == null )
      return 0.0;

    final double deltaH = after.getHoehe() - before.getHoehe();
    final double distanceDeltaH = Math.abs( before.getBreite() - after.getBreite() );

    final double distance = Math.abs( before.getBreite() - width );
    final double hoehe = deltaH / distanceDeltaH * distance;

    return before.getHoehe() + hoehe;
  }

  public IProfileRecord[] findPointsBetween( final Double p1, final Double p2, final boolean includeVertexPoints )
  {
    final Range<Double> range = Range.between( p1, p2 );

    return findPointsBetween( range, includeVertexPoints );
  }

  public IProfileRecord[] findPointsBetween( final Range<Double> range, final boolean includeVertexPoints )
  {
    final Set<IProfileRecord> found = new TreeSet<IProfileRecord>( ProfileRecord.COMPARATOR );

    final IProfileRecord[] points = getPoints();
    for( final IProfileRecord point : points )
    {
      final double breite = point.getBreite();

      if( range.contains( breite ) )
      {
        if( !includeVertexPoints && range.getMinimum() == breite || range.getMaximum() == breite )
          continue;

        found.add( point );
      }
    }

    return found.toArray( new ProfileRecord[] {} );
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

  public IProfileRecord[] getPoints( )
  {
    final List<IProfileRecord> wrappers = new ArrayList<IProfileRecord>();

    final TupleResult result = m_profile.getResult();
    for( final IRecord record : result )
    {
      wrappers.add( new ProfileRecord( record ) );
    }

    return wrappers.toArray( new IProfileRecord[] {} );
  }

  @Override
  public String toString( )
  {
    return String.format( Messages.getString( "ProfileWrapper_0" ), m_profile.getStation() ); //$NON-NLS-1$
  }

  public IProfileRecord getFirstPoint( )
  {
    final TupleResult result = m_profile.getResult();
    if( result.isEmpty() )
      return null;

    return new ProfileRecord( result.get( 0 ) );

  }

  public IProfileRecord getLastPoint( )
  {
    final TupleResult result = m_profile.getResult();
    if( result.isEmpty() )
      return null;

    return new ProfileRecord( result.get( result.size() - 1 ) );

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
      final ProfileWrapper other = (ProfileWrapper) obj;

      final String station = String.format( "%.3f", getStation() ); //$NON-NLS-1$
      final String otherStation = String.format( "%.3f", other.getStation() ); //$NON-NLS-1$

      final EqualsBuilder builder = new EqualsBuilder();
      builder.append( station, otherStation );

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
    final String station = String.format( "%.3f", getStation() ); //$NON-NLS-1$

    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append( station );

    return builder.toHashCode();
  }

  public IProfileRecord findNextPoint( final double breite )
  {
    final IProfileRecord lastPoint = getLastPoint();
    if( lastPoint == null )
      return null;

    if( breite == lastPoint.getBreite() )
      return lastPoint;

    final IProfileRecord[] points = getPoints();
    for( final IProfileRecord point : points )
    {
      if( point.getBreite() > breite )
        return point;
    }

    return null;
  }

  public IProfileRecord findPreviousPoint( final double breite )
  {
    final IProfileRecord firstPoint = getFirstPoint();
    if( firstPoint == null )
      return null;

    if( breite == firstPoint.getBreite() )
      return firstPoint;

    IProfileRecord last = null;

    final IProfileRecord[] points = getPoints();
    for( final IProfileRecord point : points )
    {
      if( point.getBreite() < breite )
      {
        last = point;
      }
      else if( point.getBreite() >= breite )
      {
        break;
      }
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

  public void remove( final IProfileRecord... remove )
  {
    for( final IProfileRecord record : remove )
    {
      m_profile.removePoint( record );
    }
  }

  public IProfileRecord findLowestPoint( )
  {
    Double height = Double.MAX_VALUE;
    IProfileRecord ptr = null;

    final IProfileRecord[] points = getPoints();
    for( final IProfileRecord point : points )
    {
      if( point.getHoehe() < height )
      {
        height = point.getHoehe();
        ptr = point;
      }
    }

    return ptr;
  }

  public double findLowestHeight( )
  {
    final IProfileRecord point = findLowestPoint();
    return point.getHoehe();

  }

  public double getWidth( final Point point ) throws GM_Exception
  {
    // TODO: dangerous: widht/rw/hw are not alwayws related!
    // TODO: maybe delegate to WspProfileHelper#getWidthPosition
    final double jtsDistance = JTSUtilities.pointDistanceOnLine( getGeometry(), point );
    final double width = getFirstPoint().getBreite() + jtsDistance;

    return width;
  }

  public ProfilePointMarkerWrapper[] getPointMarkers( final IProfileRecord point )
  {
    final IProfilPointMarker[] markers = m_profile.getPointMarkerFor( point );
    if( ArrayUtils.isEmpty( markers ) )
      return new ProfilePointMarkerWrapper[] {};

    final List<ProfilePointMarkerWrapper> myMarkers = new ArrayList<ProfilePointMarkerWrapper>();

    for( final IProfilPointMarker marker : markers )
    {
      myMarkers.add( new ProfilePointMarkerWrapper( marker ) );
    }

    return myMarkers.toArray( new ProfilePointMarkerWrapper[] {} );
  }

  public IProfileRecord hasPoint( final double width, final double fuzziness )
  {
    final double min = width - fuzziness;
    final double max = width + fuzziness;

    final IProfileRecord[] points = getPoints();
    for( final IProfileRecord point : points )
    {
      final double p = point.getBreite();
      if( p >= min && p <= max )
        return point;
    }

    return null;
  }

}
