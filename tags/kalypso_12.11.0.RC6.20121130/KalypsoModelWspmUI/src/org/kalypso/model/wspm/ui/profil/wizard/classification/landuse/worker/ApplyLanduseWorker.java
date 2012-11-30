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
package org.kalypso.model.wspm.ui.profil.wizard.classification.landuse.worker;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.commons.xml.XmlTypes;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.contribs.eclipse.core.runtime.StatusCollector;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.ui.plugin.AbstractUIPluginExt;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfilePointPropertyProvider;
import org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.core.util.WspmGeometryUtilities;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.observation.result.TupleResultUtilities;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Dirk Kuch
 */
public class ApplyLanduseWorker implements ICoreRunnableWithProgress
{
  private final IApplyLanduseData m_delegate;

  final Set<FeatureChange> m_changes = new LinkedHashSet<>();

  public ApplyLanduseWorker( final IApplyLanduseData delegate )
  {
    m_delegate = delegate;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    final IStatusCollector log = new StatusCollector( KalypsoModelWspmUIPlugin.ID );

    /* we need to transform the coordinates of the profile points to the crs of the shape */
    final String shapeSRS = initShapeSRS();
    final IGeoTransformer transformer = GeoTransformerFactory.getGeoTransformer( shapeSRS );

    final IProfileFeature[] profiles = m_delegate.getProfiles();
    monitor.beginTask( Messages.getString("ApplyLanduseWorker.0"), profiles.length ); //$NON-NLS-1$

    for( final IProfileFeature profileFeature : profiles )
    {
      final String profilSRS = profileFeature.getSrsName();
      final IProfile profile = profileFeature.getProfile();

      // TODO: check if the profile has all components already.
      // but how to do, we don't know here what components are necessary for the current profile...
      final IProfileRecord[] points = profile.getPoints();

      final int indexWidth = profile.indexOfProperty( IWspmPointProperties.POINT_PROPERTY_BREITE );
      final int indexRechtswert = profile.indexOfProperty( IWspmPointProperties.POINT_PROPERTY_RECHTSWERT );
      final int indexHochwert = profile.indexOfProperty( IWspmPointProperties.POINT_PROPERTY_HOCHWERT );

      final double[] arrayRechtswert = TupleResultUtilities.getInterpolatedValues( profile, indexRechtswert, indexWidth );
      final double[] arrayHochwert = TupleResultUtilities.getInterpolatedValues( profile, indexHochwert, indexWidth );

      int count = 1;

      for( int index = 0; index < points.length; index++ )
      {
        try
        {
          final IProfileRecord point = points[index];

          if( count % 10 == 0 )
          {
            final String label = FeatureHelper.getAnnotationValue( profileFeature, IAnnotation.ANNO_LABEL );

            final String subTaskMsg = String.format( "%s (%d/%d)", label, count, points.length ); //$NON-NLS-1$
            monitor.subTask( subTaskMsg );
          }

          final IProfilePointFilter filter = m_delegate.getFilter();
          if( !filter.accept( profile, point ) )
            continue;

          final double rechtswert = arrayRechtswert[index];
          final double hochwert = arrayHochwert[index];
          if( !Double.isNaN( rechtswert ) && !Double.isNaN( hochwert ) )
          {
            final GM_Point geoPoint = WspmGeometryUtilities.pointFromRwHw( rechtswert, hochwert, Double.NaN, profilSRS, transformer );
            assignValueToPoint( profile, point, geoPoint );
          }
        }
        catch( final Throwable t )
        {
          t.printStackTrace();

          log.add( new Status( IStatus.ERROR, AbstractUIPluginExt.ID, Messages.getString( "ApplyLanduseWorker_0" ), t ) ); //$NON-NLS-1$
        }

        count++;
      }

      ProgressUtilities.worked( monitor, 1 );
    }

    return log.asMultiStatusOrOK( Messages.getString("ApplyLanduseWorker.1") ); //$NON-NLS-1$
  }

  private String initShapeSRS( )
  {
    // FALLBACK to kalypso srs if we find no shape srs, does not really matter
    final String kalypsoSRS = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();

    // Use first found srs of polygons
    final FeatureList polygonList = m_delegate.getPolyonFeatureList();
    if( polygonList.isEmpty() )
      return kalypsoSRS;

    for( final Object poly : polygonList )
    {
      final GM_Object gmObject = getPolygonGeometry( poly );
      if( gmObject != null )
        return gmObject.getCoordinateSystem();
    }

    return kalypsoSRS;
  }

  public FeatureChange[] getChanges( )
  {
    return m_changes.toArray( new FeatureChange[] {} );
  }

  private void assignValueToPoint( final IProfile profil, final IRecord point, final GM_Point geoPoint ) throws GM_Exception
  {
    final TupleResult owner = point.getOwner();

    /* find polygon for location */
    final FeatureList polygonList = m_delegate.getPolyonFeatureList();
    final List<Object> foundPolygones = polygonList.query( geoPoint.getPosition(), null );

    final Geometry jtsPoint = JTSAdapter.export( geoPoint );

    for( final Object polyObject : foundPolygones )
    {
      final Feature polygoneFeature = (Feature)polyObject;
      final GM_Object gmObject = getPolygonGeometry( polygoneFeature );
      if( gmObject == null )
        continue;

      final Geometry jtsGeom = JTSAdapter.export( gmObject );

      if( jtsGeom.contains( jtsPoint ) )
      {
        final Object polygoneValue = polygoneFeature.getProperty( m_delegate.getValuePropertyType() );
        if( Objects.isNull( polygoneValue ) )
          continue;

        // find assignment for polygon
        final Map<String, Object> assignments = m_delegate.getAssignmentsFor( polygoneValue.toString() );
        // apply assignment to point properties
        for( final Map.Entry<String, Object> entry : assignments.entrySet() )
        {
          final String componentId = entry.getKey();
          final Object newValue = entry.getValue();

          if( Objects.isNull( componentId, newValue ) )
            continue;

          final IProfilePointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders( profil.getType() );
          final IComponent component = provider.getPointProperty( componentId );

          final Object defaultValue = getDefaultValue( component );

          profil.addPointProperty( component, defaultValue );
          point.setValue( owner.indexOfComponent( component ), newValue );
        }

        // DONT break, because we may have several polygons covering the point, but only one has an assigned value
        // break;
      }
    }
  }

  private GM_Object getPolygonGeometry( final Object polyObject )
  {
    final Feature polygoneFeature = (Feature)polyObject;

    // BUGFIX: use any gm_object here, because we do not know what it is (surface, multi surface, ...)
    return (GM_Object)polygoneFeature.getProperty( m_delegate.getGeometryPropertyType() );
  }

  private Object getDefaultValue( final IComponent component )
  {
    final Object defaultValue = component.getDefaultValue();
    if( Objects.isNull( defaultValue ) && XmlTypes.XS_DOUBLE.equals( component.getValueTypeName() ) )
      return 0.0;

    return defaultValue;
  }
}