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
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.gml.ProfileFeatureBinding;
import org.kalypso.model.wspm.core.gml.WspmProject;
import org.kalypso.model.wspm.core.gml.WspmWaterBody;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.core.profil.wrappers.Profiles;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.action.ProfilesSelection;
import org.kalypso.model.wspm.ui.action.base.ProfileWidgetHelper;
import org.kalypso.model.wspm.ui.dialog.straighten.data.CORRECT_POINTS_AMOUNT;
import org.kalypso.model.wspm.ui.dialog.straighten.data.CORRECT_POINTS_ENABLEMENT;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.event.FeaturesChangedModellEvent;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

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

  @Override
  public IStatus execute( IProgressMonitor monitor )
  {
    /* Monitor. */
    if( monitor == null )
      monitor = new NullProgressMonitor();

    try
    {
      /* Monitor. */
      monitor.beginTask( Messages.getString("StraightenProfileOperation_0"), 1250 ); //$NON-NLS-1$
      monitor.subTask( Messages.getString("StraightenProfileOperation_1") ); //$NON-NLS-1$

      /* Get the profile. */
      final IProfileFeature profile = m_data.getProfile();

      /* Create a temporary profile to work on, in case something wents wrong. */
      final IProfileFeature tmpProfile = createTmpProfile( profile );

      /* Get the first and second point. */
      final Point firstPoint = m_data.getFirstPoint();
      final Point secondPoint = m_data.getSecondPoint();

      /* Add the first and second point to the profile, if equivalents do not exist there. */
      checkAnchorPoint( tmpProfile, firstPoint );
      checkAnchorPoint( tmpProfile, secondPoint );

      /* Monitor. */
      monitor.worked( 250 );
      monitor.subTask( Messages.getString("StraightenProfileOperation_2") ); //$NON-NLS-1$

      /* Find all points in the profile between the first and the second point. */
      final IProfileRecord[] points = findPoints( tmpProfile, firstPoint, secondPoint );
      if( points.length == 0 )
        throw new IllegalStateException( "There are no points between the first and second point..." ); //$NON-NLS-1$

      /* Monitor. */
      monitor.worked( 250 );
      monitor.subTask( Messages.getString("StraightenProfileOperation_4") ); //$NON-NLS-1$

      /* Project them and adjust the x/y coordinates. */
      projectPoints( tmpProfile, firstPoint, secondPoint, points );

      /* Monitor. */
      monitor.worked( 250 );
      monitor.subTask( Messages.getString("StraightenProfileOperation_5") ); //$NON-NLS-1$

      /* Recalculate the width of the points. */
      if( m_data.getCorrectPointsEnablement() == CORRECT_POINTS_ENABLEMENT.ON )
        recalculateWidth( tmpProfile, points );

      /* Monitor. */
      monitor.worked( 250 );
      monitor.subTask( Messages.getString("StraightenProfileOperation_6") ); //$NON-NLS-1$

      /* Copy data of the temporary profile feature to the profile feature. */
      final ProfileFeatureBinding profileBinding = (ProfileFeatureBinding) profile;
      profileBinding.updateWithProfile( tmpProfile.getProfile() );
      final ProfilesSelection profileSelection = m_data.getProfileSelection();
      final Feature item = profileSelection.getItem( profile );
      if( item != null )
      {
        /* HINT: Needed to make the feature layer redraw, if there is a container element in it. */
        final GMLWorkspace workspace = item.getWorkspace();
        workspace.fireModellEvent( new FeaturesChangedModellEvent( workspace, new Feature[] { item } ) );
      }

      /* Monitor. */
      monitor.worked( 250 );

      return new Status( IStatus.OK, KalypsoModelWspmUIPlugin.ID, Messages.getString("StraightenProfileOperation_7") ); //$NON-NLS-1$
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

  private IProfileFeature createTmpProfile( final IProfileFeature profile ) throws Exception
  {
    /* Get the feature type of the root feature of the workspace. */
    final GMLWorkspace workspace = profile.getWorkspace();
    final Feature rootFeature = workspace.getRootFeature();
    final IFeatureType rootFeatureType = rootFeature.getFeatureType();

    /* Create temporary workspace. */
    final GMLWorkspace tmpWorkspace = FeatureFactory.createGMLWorkspace( rootFeatureType, workspace.getContext(), workspace.getFeatureProviderFactory() );
    final Feature tmpRootFeature = tmpWorkspace.getRootFeature();
    final IFeatureType tmpRootFeatureType = tmpRootFeature.getFeatureType();
    final IRelationType tmpWaterBodyRelation = (IRelationType) tmpRootFeatureType.getProperty( WspmProject.QN_MEMBER_WATER_BODY );
    final IFeatureType tmpWaterBodyFeatureType = tmpWaterBodyRelation.getTargetFeatureType();
    final IRelationType tmpProfileRelation = (IRelationType) tmpWaterBodyFeatureType.getProperty( WspmWaterBody.MEMBER_PROFILE );
    final Feature tmpWaterBody = workspace.createFeature( tmpRootFeature, tmpWaterBodyRelation, tmpWaterBodyFeatureType );
    final IProfileFeature tmpProfile = (IProfileFeature) FeatureHelper.cloneFeature( tmpWaterBody, tmpProfileRelation, profile );

    return tmpProfile;
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
    final IProfile profil = profile.getProfile();
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
      throw new IllegalStateException( "No next point found..." ); //$NON-NLS-1$
    }

    /* Get the index of the next point. */
    final int index = profil.indexOf( nextPoint );

    /* Add the point to the profile. */
    profil.addPoint( index, point );
  }

  private IProfileRecord[] findPoints( final IProfileFeature profile, final Point firstPoint, final Point secondPoint ) throws GM_Exception
  {
    /* Check the first and second point. */
    if( firstPoint.equals( secondPoint ) || firstPoint.distance( secondPoint ) < 0.001 )
      throw new IllegalStateException( "The first and second point are the same or too near to each other..." ); //$NON-NLS-1$

    /* Get the start width and end width. */
    final IProfile profil = profile.getProfile();
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

  private void projectPoints( final IProfileFeature profile, final Point firstPoint, final Point secondPoint, final IProfileRecord[] points ) throws Exception
  {
    /* Create the straight line. */
    final GeometryFactory factory = new GeometryFactory( firstPoint.getPrecisionModel(), firstPoint.getSRID() );
    final LineString straightLine = factory.createLineString( new Coordinate[] { firstPoint.getCoordinate(), secondPoint.getCoordinate() } );
    final GM_Object gmStraighteLine = JTSAdapter.wrap( straightLine, KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );

    /* Transform the straight line into the coordinate system of the profile. */
    final IGeoTransformer geoTransformer = GeoTransformerFactory.getGeoTransformer( profile.getSrsName() );
    final GM_Object gmTransformedLine = geoTransformer.transform( gmStraighteLine );
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

  private void recalculateWidth( final IProfileFeature profile, final IProfileRecord[] points ) throws Exception
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
      final IProfile profil = profile.getProfile();
      pointsToCorrect = profil.getPoints();
    }

    /* Get the profile. */
    final IProfile profil = profile.getProfile();

    /* Get the geo transformer. */
    final IGeoTransformer geoTransformer = GeoTransformerFactory.getGeoTransformer( profile.getSrsName() );

    /* Get the line of the profile (must be build from the records). */
    final GM_Curve gmProfileLine = profile.getLine();
    final GM_Object gmTransformedLine = geoTransformer.transform( gmProfileLine );
    final LineString transformedLine = (LineString) JTSAdapter.export( gmTransformedLine );

    /* The position for width zero. */
    final GM_Point zeroWidthPoint = Profiles.getPosition( profil, 0.0 );

    final double fixedWidth;
    if( zeroWidthPoint == null )
    {
      /* SPECIAL CASE: Zero does not lie on line - > use first point as fixed-point. */
      final IProfileRecord firstPoint = profil.getPoint( 0 );
      fixedWidth = firstPoint.getBreite();
    }
    else
      fixedWidth = 0.0;

    /* HINT: Remember, the line is already modified. */
    /* HINT: This position should be calculated by the new geo coordinates. */
    final GM_Point fixedWidthPoint = Profiles.getPosition( profil, fixedWidth );
    final GM_Object gmFixedTransformedPoint = geoTransformer.transform( fixedWidthPoint );
    final Point fixedTransformedPoint = (Point) JTSAdapter.export( gmFixedTransformedPoint );

    /* Get the distance of the zero width point on the line. */
    final LengthIndexedLine lengthIndex = new LengthIndexedLine( transformedLine );
    final double fixedDistance = lengthIndex.indexOf( fixedTransformedPoint.getCoordinate() );

    /* Loop the points. */
    for( final IProfileRecord point : pointsToCorrect )
    {
      /* Get the coordinate. */
      final Coordinate coordinate = point.getCoordinate();

      /* 200 (point distance) - 500 (zero width distance) = -300 (width). */
      /* 500 (point distance) - 500 (zero width distance) = 0 (width). */
      /* 700 (point distance) - 500 (zero width distance) = 200 (width). */
      final double pointDistance = lengthIndex.indexOf( coordinate );
      point.setBreite( fixedWidth + (pointDistance - fixedDistance) );
    }
  }
}