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
package org.kalypso.model.wspm.ui.profil.wizard.classification.apply;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.gml.classifications.helper.WspmClassifications;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileChange;
import org.kalypso.model.wspm.core.profil.IProfilePointPropertyProvider;
import org.kalypso.model.wspm.core.profil.base.IProfileManipulator;
import org.kalypso.model.wspm.core.profil.changes.PointPropertyAdd;
import org.kalypso.model.wspm.core.profil.operation.ProfileOperation;
import org.kalypso.model.wspm.core.util.roughnesses.UpdateSimpleRoughnessProperty;
import org.kalypso.model.wspm.core.util.vegetation.UpdateVegetationProperties;
import org.kalypso.observation.result.IComponent;

/**
 * @author Dirk Kuch
 */
public class ClassificationClassesProfileManipulator implements IProfileManipulator
{
  private final ApplyClassificationsPage m_page;

  public ClassificationClassesProfileManipulator( final ApplyClassificationsPage page )
  {
    m_page = page;
  }

  @Override
  public Pair<IProfileChange[], IStatus> performProfileManipulation( final IProfile profile, final IProgressMonitor monitor )
  {
    monitor.beginTask( "", 1 ); //$NON-NLS-1$

    final IProfileChange[] changes = findChanges( profile, monitor );
    return Pair.of( changes, Status.OK_STATUS );
  }

  private IProfileChange[] findChanges( final IProfile profile, final IProgressMonitor monitor )
  {
    final String type = m_page.getType();

    switch( type )
    {
      case IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS:
        return doBewuchsClassification( profile, monitor );

      case IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS:
        return doRoughnessClassification( profile, monitor );
    }

    monitor.done();

    return new IProfileChange[] {};
  }

  private IProfileChange[] doRoughnessClassification( final IProfile profile, final IProgressMonitor monitor )
  {
    if( !WspmClassifications.hasRoughnessClass( profile ) )
      return new IProfileChange[] {};

    if( !WspmClassifications.hasRoughnessProperties( profile ) )
      addRoughnessProperties( profile, monitor );

    final Set<IProfileChange> changes = new LinkedHashSet<>();

    try
    {
      if( Objects.isNotNull( profile.hasPointProperty( IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KS ) ) )
      {
        final UpdateSimpleRoughnessProperty runnable = new UpdateSimpleRoughnessProperty( profile, IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KS, m_page.isOverwriteEnabled() );
        runnable.execute( monitor );

        Collections.addAll( changes, runnable.getChanges() );
      }

      if( Objects.isNotNull( profile.hasPointProperty( IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KST ) ) )
      {
        final UpdateSimpleRoughnessProperty runnable = new UpdateSimpleRoughnessProperty( profile, IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KST, m_page.isOverwriteEnabled() );
        runnable.execute( monitor );

        Collections.addAll( changes, runnable.getChanges() );
      }
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
    }

    return changes.toArray( new IProfileChange[] {} );
  }

  private IProfileChange[] doBewuchsClassification( final IProfile profile, final IProgressMonitor monitor )
  {
    if( !WspmClassifications.hasVegetationClass( profile ) )
      return new IProfileChange[] {};

    if( !WspmClassifications.hasVegetationProperties( profile ) )
      addVegetationProperties( profile, monitor );

    try
    {
      final UpdateVegetationProperties runnable = new UpdateVegetationProperties( profile, m_page.isOverwriteEnabled() );
      runnable.execute( monitor );

      return runnable.getChanges();
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
    }

    return new IProfileChange[] {};
  }

  private void addRoughnessProperties( final IProfile profile, final IProgressMonitor monitor )
  {
    final IProfilePointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders( profile.getType() );
    final IComponent propertyKs = provider.getPointProperty( IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KS );
    final IComponent propertyKst = provider.getPointProperty( IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KST );

    final ProfileOperation operation = new ProfileOperation( "Adding profile point propertey - vegetation class", profile, true ); //$NON-NLS-1$
    operation.addChange( new PointPropertyAdd( profile, propertyKs ) );
    operation.addChange( new PointPropertyAdd( profile, propertyKst ) );

    operation.execute( monitor, null );
  }

  private void addVegetationProperties( final IProfile profile, final IProgressMonitor monitor )
  {
    final IProfilePointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders( profile.getType() );
    final IComponent propertyAx = provider.getPointProperty( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_AX );
    final IComponent propertyAy = provider.getPointProperty( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_AY );
    final IComponent propertyDp = provider.getPointProperty( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_DP );

    final ProfileOperation operation = new ProfileOperation( "Adding profile point propertey - vegetation class", profile, true ); //$NON-NLS-1$
    operation.addChange( new PointPropertyAdd( profile, propertyAx ) );
    operation.addChange( new PointPropertyAdd( profile, propertyAy ) );
    operation.addChange( new PointPropertyAdd( profile, propertyDp ) );

    operation.execute( monitor, null );
  }

}
