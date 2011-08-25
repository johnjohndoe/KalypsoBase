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
package org.kalypso.model.wspm.core.util.roughnesses;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;
import org.kalypso.model.wspm.core.gml.WspmProject;
import org.kalypso.model.wspm.core.gml.classifications.IRoughnessClass;
import org.kalypso.model.wspm.core.gml.classifications.IWspmClassification;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * updates a "simple" ks / kst value from roughness class
 * 
 * @author Dirk Kuch
 */
public class UpdateSimpleRoughnessProperty implements ICoreRunnableWithProgress
{
  private final IProfil m_profile;

  private final String m_property;

  private final boolean m_overwrite;

  public UpdateSimpleRoughnessProperty( final IProfil profile, final String property, final boolean overwrite )
  {
    m_profile = profile;
    m_property = property;
    m_overwrite = overwrite;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    final IComponent property = m_profile.hasPointProperty( m_property );
    if( Objects.isNull( property ) )
      return new Status( IStatus.CANCEL, KalypsoModelWspmCorePlugin.getID(), String.format( "Can't update profile %.3f km. Missing point property: %s", m_profile.getStation(), m_property ) );

    final IComponent clazz = m_profile.hasPointProperty( IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS );
    if( Objects.isNull( clazz ) )
      return new Status( IStatus.CANCEL, KalypsoModelWspmCorePlugin.getID(), String.format( "Can't update profile %.3f km. Missing point property: %s", m_profile.getStation(), IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS ) );

    final IWspmClassification clazzes = getClassification();
    if( Objects.isNull( clazzes ) )
      return new Status( IStatus.CANCEL, KalypsoModelWspmCorePlugin.getID(), String.format( "Missing profile feature for profile %.3f km.", m_profile.getStation() ) );

    final List<IStatus> statis = new ArrayList<IStatus>();

    final IRecord[] points = m_profile.getPoints();
    for( final IRecord point : points )
    {
      final String lnkClazz = (String) point.getValue( clazz );
      final IRoughnessClass roughness = clazzes.findRoughnessClass( lnkClazz );

      if( Objects.isNull( roughness ) )
      {
        final Double width = (Double) point.getValue( m_profile.hasPointProperty( IWspmPointProperties.POINT_PROPERTY_BREITE ) );
        final IStatus status = new Status( IStatus.WARNING, KalypsoModelWspmCorePlugin.getID(), String.format( "Missing roughness class - point: %.3f", width ) );
        statis.add( status );

        continue;
      }

      final Double value = roughness.getValue( m_property );
      if( m_overwrite )
        point.setValue( property, value );
      else
      {
        if( Objects.isNull( point.getValue( property ) ) )
          point.setValue( property, value );
      }
    }

    return StatusUtilities.createStatus( statis, String.format( "Updating of roughness from roughness classes for profile %.3f", m_profile.getStation() ) );
  }

  private IWspmClassification getClassification( )
  {
    final Object source = m_profile.getSource();
    if( !(source instanceof Feature) )
      return null;

    final Feature feature = (Feature) source;
    final GMLWorkspace workspace = feature.getWorkspace();
    final Feature root = workspace.getRootFeature();
    if( !(root instanceof WspmProject) )
      return null;

    final WspmProject project = (WspmProject) root;

    return project.getClassificationMember();
  }
}
