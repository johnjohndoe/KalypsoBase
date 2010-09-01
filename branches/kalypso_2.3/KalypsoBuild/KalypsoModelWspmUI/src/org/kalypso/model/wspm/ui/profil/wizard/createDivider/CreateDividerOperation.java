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
package org.kalypso.model.wspm.ui.profil.wizard.createDivider;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.command.ICommand;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.gml.ProfileFeatureFactory;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilPointMarker;
import org.kalypso.model.wspm.core.profil.IProfilPointPropertyProvider;
import org.kalypso.model.wspm.core.profil.util.ProfilUtil;
import org.kalypso.model.wspm.core.util.WspmProfileHelper;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.command.ChangeFeaturesCommand;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author Gernot Belger
 */
public class CreateDividerOperation implements ICoreRunnableWithProgress
{
  private final List<FeatureChange> m_changes = new ArrayList<FeatureChange>();

  private final Object[] m_profileFeatures;

  private final FeatureList m_lineFeatures;

  private final IPropertyType m_lineGeomProperty;

  private final IComponent m_deviderType;

  private final boolean m_useExisting;

  private final IKalypsoFeatureTheme m_commandTarget;

  public CreateDividerOperation( final Object[] choosen, final FeatureList lineFeatures, final IPropertyType lineGeomProperty, final IComponent deviderType, final boolean useExisting, final IKalypsoFeatureTheme commandTarget )
  {
    m_profileFeatures = choosen;
    m_lineFeatures = lineFeatures;
    m_lineGeomProperty = lineGeomProperty;
    m_deviderType = deviderType;
    m_useExisting = useExisting;
    m_commandTarget = commandTarget;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws InvocationTargetException
  {
    try
    {
      createDevider( monitor );
      final FeatureChange[] changes = m_changes.toArray( new FeatureChange[m_changes.size()] );
      if( changes.length > 0 )
      {
        final GMLWorkspace gmlworkspace = changes[0].getFeature().getWorkspace();
        final ICommand command = new ChangeFeaturesCommand( gmlworkspace, changes );
        m_commandTarget.postCommand( command, null );
      }
    }
    catch( final Exception e )
    {
      throw new InvocationTargetException( e );
    }

    return Status.OK_STATUS;
  }

  protected void createDevider( final IProgressMonitor monitor ) throws CoreException
  {
    monitor.beginTask( Messages.getString( "org.kalypso.model.wspm.ui.wizard.CreateProfileDeviderWizard.6" ), m_profileFeatures.length ); //$NON-NLS-1$

    for( final Object object : m_profileFeatures )
    {
      try
      {
        final IProfileFeature profile = (IProfileFeature) object;

        monitor.subTask( String.format( "%s (km %s)", profile.getName(), profile.getBigStation() ) );

        final IProfil profil = profile.getProfil();

        // create marker for each point
        final Integer[] newMarkerPoints = findNewMarkerPoints( profile );

        if( createNewDevider( profil, newMarkerPoints ) )
          Collections.addAll( m_changes, ProfileFeatureFactory.toFeatureAsChanges( profil, profile ) );
      }
      catch( final GM_Exception e )
      {
        final IStatus status = StatusUtilities.statusFromThrowable( e );
        KalypsoModelWspmUIPlugin.getDefault().getLog().log( status );
      }

      ProgressUtilities.worked( monitor, 1 );
    }
  }

  private Integer[] findNewMarkerPoints( final IProfileFeature profile ) throws GM_Exception
  {
    final Point[] intersectionPoints = findNewMarkerLocations( profile );

    final IProfil profil = profile.getProfil();
    final double[] intersectionWidths = getIntersectionWidths( profil, intersectionPoints );

    final Integer[] nearestPointIndices = ProfilUtil.findNearestPointIndices( profil, intersectionWidths );

    final Integer[] bestMarkers = findBestMarkers( profil, nearestPointIndices );

    final Integer[] cleanMarkers = cleanupMarkers( profil, bestMarkers );

    return cleanMarkers;
  }

  /**
   * Makes sure we always have exactly two markers.<br>
   * If we have too many, we take the outermost markers.<br>
   * If we have too less, we add markers at profile end or beginning.
   */
  private Integer[] cleanupMarkers( final IProfil profil, final Integer[] bestMarkers )
  {
    final IRecord[] points = profil.getPoints();
    if( points.length < 2 )
      return new Integer[0];

    final int first = 0;
    final int last = points.length - 1;

    switch( bestMarkers.length )
    {
      case 0:
        return new Integer[] { first, last };

      case 1:
      {
        final Integer oneIndex = bestMarkers[0];
        final int lowPointIndex = WspmProfileHelper.findLowestPointIndex( profil );
        if( lowPointIndex == -1 )
          return new Integer[] { oneIndex };

        if( oneIndex < lowPointIndex )
          return new Integer[]{oneIndex, last};
        else
          return new Integer[] { first, oneIndex };
      }

      case 2:
        return bestMarkers;

        // > 2
      default:
        return findOutermost( bestMarkers );
    }
  }

  private Integer[] findBestMarkers( final IProfil profil, final Integer[] nearestPointIndices )
  {
    /* Clear points that are contained multiple times */
    final Set<Integer> nearestPointSet = new HashSet<Integer>( Arrays.asList( nearestPointIndices ) );
    nearestPointSet.remove( null );
    final Integer[] uniqueIntersectionPoints = nearestPointSet.toArray( new Integer[nearestPointSet.size()] );

    if( m_useExisting )
      return findBestMarkersAndKeepExisting( profil, uniqueIntersectionPoints );
    else
      return findBestMarkersAndDeleteExisting( uniqueIntersectionPoints );
  }

  private Integer[] findBestMarkersAndDeleteExisting( final Integer[] intersectionPoints )
  {
    switch( intersectionPoints.length )
    {
      case 0:
        return new Integer[0];

      case 1:
      case 2:
        return intersectionPoints;

      default:
        return findOutermost( intersectionPoints );
    }
  }

  private Integer[] findBestMarkersAndKeepExisting( final IProfil profil, final Integer[] intersectionPoints )
  {
    switch( intersectionPoints.length )
    {
      case 0:
        return existingMarkersAsIndices( profil );

      case 1:
        return mixExistingWithIntersectionPoint( profil, intersectionPoints[0] );

      case 2:
        return intersectionPoints;

      default:
        return findOutermost( intersectionPoints );
    }
  }

  private Integer[] findOutermost( final Integer[] intersectionPoints )
  {
    Assert.isTrue( intersectionPoints.length > 1 );

    final Integer[] result = new Integer[2];

    final SortedSet<Integer> markerIndices = new TreeSet<Integer>( Arrays.asList( intersectionPoints ) );

    result[0] = markerIndices.first();
    result[1] = markerIndices.last();

    return result;
  }

  private Integer[] mixExistingWithIntersectionPoint( final IProfil profil, final Integer intersectionIndex )
  {
    final Integer[] markerPoints = existingMarkersAsIndices( profil );
    final SortedSet<Integer> markerIndices = new TreeSet<Integer>( Arrays.asList( markerPoints ) );

    // depends on the side of the profile!
    final int lowPointIndex = WspmProfileHelper.findLowestPointIndex( profil );
    if( lowPointIndex == -1 )
      return new Integer[] { intersectionIndex };

    final Collection<Integer> result = new ArrayList<Integer>( 2 );
    result.add( intersectionIndex );

    if( intersectionIndex > lowPointIndex )
    {
      // use leftmost of all left markers
      final SortedSet<Integer> leftSet = markerIndices.headSet( lowPointIndex );
      if( !leftSet.isEmpty() )
        result.add( leftSet.first() );
    }
    else
    {
      // use leftmost of all left markers
      final SortedSet<Integer> rightSet = markerIndices.tailSet( lowPointIndex );
      if( !rightSet.isEmpty() )
        result.add( rightSet.last() );
    }

    return result.toArray( new Integer[result.size()] );
  }

  private Integer[] existingMarkersAsIndices( final IProfil profil )
  {
    final IProfilPointMarker[] existingMarkers = profil.getPointMarkerFor( m_deviderType );
    final Integer[] asPoints = new Integer[existingMarkers.length];
    for( int i = 0; i < asPoints.length; i++ )
    {
      final IRecord markerPoint = existingMarkers[i].getPoint();
      asPoints[i] = profil.indexOfPoint( markerPoint );
    }

    return asPoints;
  }

  private Point[] findNewMarkerLocations( final IProfileFeature profile ) throws GM_Exception
  {
    final GM_Curve curve = profile.getLine();
    if( curve == null )
      return new Point[0];

    final LineString profileLine = (LineString) JTSAdapter.export( curve );

    // find intersectors with curve
    final GM_Envelope curveEnvelope = curve.getEnvelope();
    @SuppressWarnings("unchecked")
    final List< ? > lineIntersectors = m_lineFeatures.query( curveEnvelope, null );
    final List<Point> pointList = new ArrayList<Point>();

    for( final Object lineF : lineIntersectors )
    {
      final Feature lineFeature = (Feature) lineF;
      final Geometry line = getAsLine( lineFeature );
      if( line == null )
        continue;

      // find intersecting points
      final Geometry intersection = profileLine.intersection( line );
      final Point[] points = getPointFromGeometry( intersection );
      Collections.addAll( pointList, points );
    }

    return pointList.toArray( new Point[pointList.size()] );
  }

  private Geometry getAsLine( final Feature lineFeature ) throws GM_Exception
  {
    final GM_Object lineGeom = (GM_Object) lineFeature.getProperty( m_lineGeomProperty );
    if( lineGeom == null )
      return null;

    final Geometry lineGeometry = JTSAdapter.export( lineGeom );
    if( lineGeometry instanceof Polygon || lineGeometry instanceof MultiPolygon )
      return lineGeometry.getBoundary();

    return lineGeometry;
  }

  private static Point[] getPointFromGeometry( final Geometry points )
  {
    if( points instanceof Point )
      return new Point[] { (Point) points };

    if( points instanceof MultiPoint )
    {
      final MultiPoint mp = (MultiPoint) points;
      final Point[] result = new Point[mp.getNumGeometries()];
      for( int i = 0; i < result.length; i++ )
        result[i] = (Point) mp.getGeometryN( i );

      return result;
    }

    return new Point[] {};
  }

  /**
   * At the moment, only existing points are taken
   */
  private boolean createNewDevider( final IProfil profil, final Integer[] newMarkerPoints )
  {
    /** Clear existing points */
    final IProfilPointMarker[] existingMarkers = profil.getPointMarkerFor( m_deviderType );
    for( final IProfilPointMarker marker : existingMarkers )
      profil.removePointMarker( marker );

    /** Add new Points */
    final IProfilPointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders( profil.getType() );
    final String id = m_deviderType.getId();

    boolean markerHasBeenAdded = false;
    for( final Integer markerIndex : newMarkerPoints )
    {
      if( markerIndex != null )
      {
        final IRecord markerPoint = profil.getPoint( markerIndex );
        final IProfilPointMarker marker = profil.createPointMarker( id, markerPoint );
        final Object defaultValue = provider.getDefaultValue( id );
        marker.setValue( defaultValue );
        markerHasBeenAdded = true;
      }
    }

    return markerHasBeenAdded;
  }

  private double[] getIntersectionWidths( final IProfil profil, final Point[] intersectionPoints )
  {
    final Collection<Double> widthList = new ArrayList<Double>( intersectionPoints.length );
    for( final Point point : intersectionPoints )
    {
      try
      {
        final GM_Point p = (GM_Point) JTSAdapter.wrap( point, KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );
        final Double width = WspmProfileHelper.getWidthPosition( p, profil );
        if( width != null )
          widthList.add( width );
      }
      catch( final Exception e )
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    final Double[] widths = widthList.toArray( new Double[widthList.size()] );
    return ArrayUtils.toPrimitive( widths );
  }
}
