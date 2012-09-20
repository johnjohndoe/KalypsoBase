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
package org.kalypso.model.wspm.core.profil.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.observation.result.IRecord;

/**
 * Helper for thinning a profile with the Douglas Peucker algorithm.
 *
 * @author Holger Albert
 * @author Thomas Jung
 */
public final class DouglasPeuckerHelper
{
  private static final class ProfileSegmentData
  {
    final IRecord[] m_segmPoints;

    final int m_startInd;

    final int m_endInd;

    final double m_distance;

    int m_distInd;

    public ProfileSegmentData( final IRecord[] points, final int start, final int end )
    {
      m_segmPoints = points;
      m_startInd = start;
      m_endInd = end;
      m_distance = maxSegmentDistance();
    }

    private double maxSegmentDistance( )
    {
      double maxDistance = Double.NEGATIVE_INFINITY;
      final int deltaIndex = m_endInd - m_startInd;
      if( deltaIndex > 2 )
      {
        for( int i = 1; i < m_endInd - m_startInd - 1; i++ )
        {
          final double currentDistance = calcDistance( m_segmPoints[m_startInd], m_segmPoints[m_endInd], m_segmPoints[m_startInd + i] );
          if( currentDistance > maxDistance )
          {
            maxDistance = currentDistance;
            m_distInd = m_startInd + i;
          }
        }
      }
      else if( deltaIndex == 2 )
      {
        maxDistance = calcDistance( m_segmPoints[m_startInd], m_segmPoints[m_endInd], m_segmPoints[m_startInd + 1] );
        m_distInd = m_startInd + 1;
      }
      else if( deltaIndex == 1 )
      {
        maxDistance = 0;
        m_distInd = m_startInd;
      }

      return maxDistance;
    }
  }

  /**
   * The constructor.
   */
  private DouglasPeuckerHelper( )
  {
  }

  /**
   * This function starts the creation of the operation, which removes points from the profile. It uses the
   * Douglas.Peucker algorithm, for finding the point to remove.
   *
   * @param allowedDistance
   *          The maximal Douglas-Peucker distance [m].
   * @return The point that should be removed from the profile.
   */
  public static IProfileRecord[] reduce( final double allowedDistance, final IProfileRecord[] points, final IProfile profile )
  {
    /* Reduce points. */
    final IProfileRecord[] pointsToKeep = profile.getMarkedPoints();
    return reducePoints( points, pointsToKeep, allowedDistance );
  }

  /**
   * This function finds all points, which must be removed.
   *
   * @param points
   *          All profile points.
   * @param pointsToKeep
   *          All points, which are important and should be kept.
   * @param allowedDistance
   *          The allowed distance.
   * @return The points to remove.
   */
  public static IProfileRecord[] reducePoints( final IProfileRecord[] points, final IProfileRecord[] pointsToKeep, final double allowedDistance )
  {
    /* Cannot reduce 2 or less points */
    if( points.length <= 2 )
      return new IProfileRecord[0];

    /* Reduce segment wise. */
    final Set<IProfileRecord> pointsToKeepList = new HashSet<>( Arrays.asList( pointsToKeep ) );
    final List<IProfileRecord> pointsToRemove = new ArrayList<>( points.length - 2 );

    int segmentBegin = 0;
    for( int i = 0; i < points.length; i++ )
    {
      if( i == segmentBegin )
      {
        continue;
      }

      final IRecord point = points[i];
      if( pointsToKeepList.contains( point ) || i == points.length - 1 )
      {
        final IProfileRecord[] toRemove = reduceIt( points, segmentBegin, i, allowedDistance );
        pointsToRemove.addAll( Arrays.asList( toRemove ) );
        segmentBegin = i;
      }
    }

    return pointsToRemove.toArray( new IProfileRecord[pointsToRemove.size()] );
  }

  /** @return the points with are redundant */
  private static IProfileRecord[] reduceIt( final IProfileRecord[] points, final int begin, final int end, final double allowedDistance )
  {
    if( end - begin < 2 )
      return new IProfileRecord[0];

    // für alle punkte abstand zu segment[begin-end] ausrechnen
    final double[] distances = new double[end - (begin + 1)];
    double maxdistance = 0.0;
    int maxdistIndex = -1;
    for( int i = 0; i < distances.length; i++ )
    {
      final double distance = calcDistance( points[begin], points[end], points[i + begin + 1] );
      distances[i] = distance;

      if( distance > maxdistance )
      {
        maxdistance = distance;
        maxdistIndex = i + begin + 1;
      }
    }

    // falls ein punkt dabei, dessen diff > maxdiff, splitten
    if( maxdistance > allowedDistance && maxdistIndex != -1 )
    {
      final IProfileRecord[] beginReduced = reduceIt( points, begin, maxdistIndex, allowedDistance );
      final IProfileRecord[] endReduced = reduceIt( points, maxdistIndex, end, allowedDistance );

      final List<IRecord> reduced = new ArrayList<>( beginReduced.length + endReduced.length );

      reduced.addAll( Arrays.asList( beginReduced ) );
      reduced.addAll( Arrays.asList( endReduced ) );
      return reduced.toArray( new IProfileRecord[reduced.size()] );
    }

    // kein Punkt mehr wichtig: alle zwischenpunkte zurückgeben
    final IProfileRecord[] reduced = new IProfileRecord[end - (begin + 1)];
    for( int i = 0; i < reduced.length; i++ )
    {
      reduced[i] = points[i + begin + 1];
    }

    return reduced;
  }

  protected static double calcDistance( final IRecord beginPoint, final IRecord endPoint, final IRecord middlePoint )
  {
    final Double bx = ProfileUtil.getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_BREITE, beginPoint );// (Double)
    final Double by = ProfileUtil.getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_HOEHE, beginPoint );// (Double)
    final Double ex = ProfileUtil.getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_BREITE, endPoint );// (Double)
    final Double ey = ProfileUtil.getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_HOEHE, endPoint );// (Double)
    final Double mx = ProfileUtil.getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_BREITE, middlePoint );// (Double)
    final Double my = ProfileUtil.getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_HOEHE, middlePoint );// (Double)

    final double f = (ey - by) / (ex - bx);

    final double distance = (f * mx - 1 * my - f * bx + by) / Math.sqrt( 1 + f * f );
    return Math.abs( distance );
  }

  /**
   * gets the most important profile points by sequentially adding the points with the maximum distance to the segment
   * initially defined by the start and end point of the profile.
   *
   * @param points
   *          all profile points
   * @param allowedNumPoints
   *          max number of points.
   * @return points to keep
   */
  public static IProfileRecord[] findIProfileVIPPoints( final IProfileRecord[] points, final int allowedNumPoints )
  {
    final List<IProfileRecord> pointsToKeep = new ArrayList<>( allowedNumPoints - 1 );

    // store the first point of the input profile in the profile point list.
    pointsToKeep.add( points[0] );

    final LinkedList<ProfileSegmentData> profSegmentList = new LinkedList<>();

    /* begin with the start and end point of the profile */
    final ProfileSegmentData startSegment = new ProfileSegmentData( points, 0, points.length - 1 );
    profSegmentList.add( startSegment );

    for( int i = 1; i < allowedNumPoints - 1; i++ )
    {
      double maxDist = Double.NEGATIVE_INFINITY;
      int indexMax = 0;

      for( int j = 0; j < profSegmentList.size(); j++ )
      {
        // find the maxDistanceSegment

        final ProfileSegmentData currentProfSegment = profSegmentList.get( j );
        final double currentDist = currentProfSegment.m_distance;
        if( currentDist > maxDist )
        {
          maxDist = currentDist;
          indexMax = j;
        }
      }

      // store the found maximum in the profile point list
      pointsToKeep.add( points[profSegmentList.get( indexMax ).m_distInd] );

      // split the maxDistanceSegment
      final ProfileSegmentData firstSplittedSegment = new ProfileSegmentData( points, profSegmentList.get( indexMax ).m_startInd, profSegmentList.get( indexMax ).m_distInd );
      final ProfileSegmentData secondSplittedSegment = new ProfileSegmentData( points, profSegmentList.get( indexMax ).m_distInd, profSegmentList.get( indexMax ).m_endInd );

      // store the new segments in the list
      profSegmentList.set( indexMax, firstSplittedSegment );
      profSegmentList.add( indexMax + 1, secondSplittedSegment );
    }

    pointsToKeep.add( points[points.length - 1] );
    return pointsToKeep.toArray( new IProfileRecord[pointsToKeep.size()] );
  }
}