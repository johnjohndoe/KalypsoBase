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

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.commons.xml.XmlTypes;
import org.kalypso.contribs.eclipse.ui.plugin.AbstractUIPluginExt;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.gml.ProfileFeatureFactory;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilPointPropertyProvider;
import org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.core.util.WspmGeometryUtilities;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.observation.result.TupleResultUtilities;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Dirk Kuch
 */
public class ApplyLanduseWorker implements IRunnableWithProgress
{
  private final IApplyLanduseData m_delegate;

  final Set<FeatureChange> m_changes = new LinkedHashSet<FeatureChange>();

  public ApplyLanduseWorker( final IApplyLanduseData delegate )
  {
    m_delegate = delegate;
  }

  @Override
  public void run( final IProgressMonitor monitor ) throws InvocationTargetException
  {
    final Set<IStatus> stati = new LinkedHashSet<IStatus>();

    final IProfileFeature[] profiles = m_delegate.getProfiles();
    for( final IProfileFeature profileFeature : profiles )
    {
      final String crs = profileFeature.getSrsName();
      final IProfil profile = profileFeature.getProfil();

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
            final GM_Point geoPoint = WspmGeometryUtilities.pointFromRwHw( rechtswert, hochwert, Double.NaN, crs, WspmGeometryUtilities.GEO_TRANSFORMER );
            if( geoPoint != null )
            {
              final Geometry jtsPoint = JTSAdapter.export( geoPoint );
              assignValueToPoint( profile, point, geoPoint, jtsPoint );
            }
          }
        }
        catch( final Throwable t )
        {
          t.printStackTrace();

          stati.add( new Status( IStatus.ERROR, AbstractUIPluginExt.ID, "Applying landuse failed", t ) );
        }

        count++;
      }

      final FeatureChange[] fcs = ProfileFeatureFactory.toFeatureAsChanges( profile, profileFeature );
      Collections.addAll( m_changes, fcs );

      try
      {
        ProgressUtilities.worked( monitor, 1 );
      }
      catch( final CoreException e )
      {
        throw new InvocationTargetException( e );
      }
    }

  }

  public FeatureChange[] getChanges( )
  {
    return m_changes.toArray( new FeatureChange[] {} );
  }

  private void assignValueToPoint( final IProfil profil, final IRecord point, final GM_Point geoPoint, final Geometry jtsPoint ) throws GM_Exception
  {
    final TupleResult owner = point.getOwner();

    /* find polygon for location */
    @SuppressWarnings("unchecked")
    final List<Object> foundPolygones = m_delegate.getPolyonFeatureList().query( geoPoint.getPosition(), null );

    for( final Object polyObject : foundPolygones )
    {
      final Feature polygoneFeature = (Feature) polyObject;

      // BUGFIX: use any gm_object here, because we do not know what it is (surface, multi surface, ...)
      final GM_Object gmObject = (GM_Object) polygoneFeature.getProperty( m_delegate.getGeometryPropertyType() );
      if( Objects.isNull( gmObject ) )
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

          final IProfilPointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders( profil.getType() );
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

  private Object getDefaultValue( final IComponent component )
  {
    final Object defaultValue = component.getDefaultValue();
    if( Objects.isNull( defaultValue ) && XmlTypes.XS_DOUBLE.equals( component.getValueTypeName() ) )
      return 0.0;

    return defaultValue;
  }

}
