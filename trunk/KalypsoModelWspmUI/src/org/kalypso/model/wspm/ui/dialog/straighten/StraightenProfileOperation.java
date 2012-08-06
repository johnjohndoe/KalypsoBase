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
package org.kalypso.model.wspm.ui.dialog.straighten;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.core.profil.wrappers.Profiles;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.action.base.ProfileWidgetHelper;
import org.kalypso.model.wspm.ui.dialog.straighten.data.CORRECT_POINTS_AMOUNT;
import org.kalypso.model.wspm.ui.dialog.straighten.data.CORRECT_POINTS_ENABLEMENT;
import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * This operation straightens a profile between two points.<br />
 * <br />
 * <strong>HINT:</strong><br/>
 * <ul>
 * <li>All geometries given by {@link StraightenProfileData} are assumed to be in the coordinate system of Kalypso.</li>
 * <li>The geometries provided by {@link IProfileFeature} are assumed to be transformed by the profile in the coordinate
 * system of Kalypso.</li>
 * <li>The coordinates in the {@link IProfileRecord} are assumed to be in the coordinate system of the profile.</li>
 * </ul>
 * 
 * @author Holger Albert
 */
public class StraightenProfileOperation implements ICoreRunnableWithProgress
{
  /**
   * The straighten profile data.
   */
  private final StraightenProfileData m_data;

  /**
   * The constructor.
   * 
   * @param data
   *          The straighten profile data.
   */
  public StraightenProfileOperation( final StraightenProfileData data )
  {
    m_data = data;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus execute( IProgressMonitor monitor )
  {
    /* Monitor. */
    if( monitor == null )
      monitor = new NullProgressMonitor();

    try
    {
      /* Monitor. */
      monitor.beginTask( "Straightening profile", 1000 );
      monitor.subTask( "Inspecting profile..." );

      /* Add the first and second point to the profile, if equivalents do not exist there. */
      checkAnchorPoint( m_data.getProfile(), m_data.getFirstPoint() );
      checkAnchorPoint( m_data.getProfile(), m_data.getSecondPoint() );

      /* Monitor. */
      monitor.worked( 250 );
      monitor.subTask( "Collecting all points between the first and the second point..." );

      /* Find all points in the profile between the first and the second point. */
      final IProfileRecord[] points = findPoints( m_data );
      if( points.length == 0 )
        throw new IllegalStateException( "There are no points between the first and second point..." );

      /* Monitor. */
      monitor.worked( 250 );
      monitor.subTask( "Projecting all points to the line..." );

      /* Project them and adjust the x/y coordinates. */
      projectPoints( m_data, points );

      /* Monitor. */
      monitor.worked( 250 );
      monitor.subTask( "Recalculating the width of the points..." );

      /* Recalculate the width of the points. */
      if( m_data.getCorrectPointsEnablement() == CORRECT_POINTS_ENABLEMENT.ON )
        recalculateWidth( m_data, points );

      /* Monitor. */
      monitor.worked( 250 );

      return new Status( IStatus.OK, KalypsoModelWspmUIPlugin.ID, "Straightening successfull." );
    }
    catch( final Exception ex )
    {
      return new Status( IStatus.ERROR, KalypsoModelWspmUIPlugin.ID, ex.getLocalizedMessage(), ex );
    }
    finally
    {
      /* Monitor. */
      monitor.done();
    }
  }

  private void checkAnchorPoint( final IProfileFeature profile, final Point anchorPoint ) throws Exception
  {
    /* This point is a point on the profile. */
    if( ProfileWidgetHelper.isVertexPoint( profile.getJtsLine(), anchorPoint.getCoordinate() ) )
      return;

    /* Transform the anchor point into the coordinate system of the profile. */
    final IGeoTransformer geoTransformer = GeoTransformerFactory.getGeoTransformer( profile.getSrsName() );
    final GM_Object gmAnchorPoint = JTSAdapter.wrap( anchorPoint, KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );
    final GM_Point gmTransformedPoint = (GM_Point) geoTransformer.transform( gmAnchorPoint );

    /* Create the new profile point. */
    final IProfil profil = profile.getProfil();
    final IProfileRecord point = profil.createProfilPoint();

    /* Calculate the values. */
    final double rechtswert = gmTransformedPoint.getX();
    final double hochwert = gmTransformedPoint.getY();
    final double breite = Profiles.getWidth( profil, anchorPoint );
    final double hoehe = Profiles.getHoehe( profil, breite );

    /* Set the values. */
    point.setRechtswert( rechtswert );
    point.setHochwert( hochwert );
    point.setBreite( breite );
    point.setHoehe( hoehe );

    /* Get the next point. */
    final IProfileRecord nextPoint = profil.findNextPoint( breite );
    if( nextPoint == null )
    {
      /* Should not happen, because the new point cannot lie outside of the profile. */
      /* If it lies on a point of the profile (e.g. the last), it should not reach here (see first if). */
      throw new IllegalStateException( "No next point found..." );
    }

    /* Get the index of the next point. */
    final int index = profil.indexOf( nextPoint );

    /* Add the point to the profile. */
    profil.addPoint( index, point );
  }

  private IProfileRecord[] findPoints( final StraightenProfileData data ) throws GM_Exception
  {
    /* Get the profile. */
    final IProfileFeature profile = data.getProfile();

    /* Get the first and the second point. */
    final Point firstPoint = data.getFirstPoint();
    final Point secondPoint = data.getSecondPoint();
    if( firstPoint.equals( secondPoint ) || firstPoint.distance( secondPoint ) < 0.001 )
      throw new IllegalStateException( "The first and second point are the same or too near to each other..." );

    /* Get the start width and end width. */
    final IProfil profil = profile.getProfil();
    final double firstWidth = Profiles.getWidth( profil, firstPoint );
    final double secondWidth = Profiles.getWidth( profil, secondPoint );

    /* Find the start point and end point of the points between the first and second point. */
    /* HINT: [First] [Start] ... [End] [Second] */
    final IProfileRecord startPoint = profil.findNextPoint( firstWidth );
    final IProfileRecord endPoint = profil.findPreviousPoint( secondWidth );

    /* Find the indexes of the start point and the end point. */
    final int startIndex = profil.indexOf( startPoint );
    final int endIndex = profil.indexOf( endPoint );

    /* In this case there are no points (between the first point and the second point). */
    /* HINT: [First/End] [Second/Start] */
    if( startIndex > endIndex )
      return new IProfileRecord[] {};

    /* In this case, there is only one point (between the first point and the second point). */
    /* This means, startPoint and endPoint are equal. */
    /* HINT: [First] [Start/End] [Second] */
    if( startIndex == endIndex )
      return new IProfileRecord[] { startPoint };

    /* In this case, there are two or more points (between the first point and the second point). */
    /* HINT: [First] [Start] ... [End] [Second] */
    return profil.getPoints( startIndex, endIndex );
  }

  private void projectPoints( final StraightenProfileData data, final IProfileRecord[] points ) throws Exception
  {
    /* Get the profile. */
    final IProfileFeature profile = data.getProfile();

    /* Get the first and the second point. */
    final Point firstPoint = data.getFirstPoint();
    final Point secondPoint = data.getSecondPoint();

    /* Create the straight line. */
    final GeometryFactory factory = new GeometryFactory( firstPoint.getPrecisionModel(), firstPoint.getSRID() );
    final LineString straightLine = factory.createLineString( new Coordinate[] { firstPoint.getCoordinate(), secondPoint.getCoordinate() } );
    final GM_Object gmStraighteLine = JTSAdapter.wrap( straightLine, KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );

    /* Transform the straight line into the coordinate system of the profile. */
    final IGeoTransformer geoTransformer = GeoTransformerFactory.getGeoTransformer( profile.getSrsName() );
    final GM_Curve gmTransformedLine = (GM_Curve) geoTransformer.transform( gmStraighteLine );
    final LineString transformedLine = (LineString) JTSAdapter.export( gmTransformedLine );

    /* Create a line segment. */
    final LineSegment lineSegment = new LineSegment( transformedLine.getStartPoint().getCoordinate(), transformedLine.getEndPoint().getCoordinate() );

    /* Adjust all points. */
    for( final IProfileRecord point : points )
    {
      /* Get the coordinate. */
      final Coordinate coordinate = point.getCoordinate();

      /* Project the coordinate on the profile (90 deegrees). */
      final Coordinate projectedCoordinate = lineSegment.closestPoint( coordinate );

      /* Get the x/y coordinates. */
      final double rechtswert = projectedCoordinate.x;
      final double hochwert = projectedCoordinate.y;

      /* Set the x/y coordinates. */
      point.setRechtswert( rechtswert );
      point.setHochwert( hochwert );
    }
  }

  private void recalculateWidth( final StraightenProfileData data, final IProfileRecord[] points ) throws GM_Exception
  {
    /* The points to correct. */
    IProfileRecord[] pointsToCorrect;

    /* What points should be corrected? */
    if( m_data.getCorrectPointsAmount() == CORRECT_POINTS_AMOUNT.BETWEEN )
    {
      /* Corrent only the points found between the first and the second point. */
      pointsToCorrect = points;
    }
    else
    {
      /* Correct all points of the profile. */
      final IProfileFeature profile = data.getProfile();
      final IProfil profil = profile.getProfil();
      pointsToCorrect = profil.getPoints();
    }

    /* Get the profile. */
    final IProfileFeature profile = data.getProfile();

    /* Get the line of the profile (must be build from the records). */
    final LineString profileLine = profile.getJtsLine();

    /* Loop the points. */
    for( final IProfileRecord point : pointsToCorrect )
    {
      // TODO
    }
  }
}