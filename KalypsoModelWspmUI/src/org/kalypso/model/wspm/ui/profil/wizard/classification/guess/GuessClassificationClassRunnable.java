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
package org.kalypso.model.wspm.ui.profil.wizard.classification.guess;

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
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilChange;
import org.kalypso.model.wspm.core.profil.IProfilPointPropertyProvider;
import org.kalypso.model.wspm.core.profil.base.IProfileManipulator;
import org.kalypso.model.wspm.core.profil.changes.PointPropertyAdd;
import org.kalypso.model.wspm.core.profil.operation.ProfilOperation;
import org.kalypso.model.wspm.core.util.roughnesses.GuessRoughessClassesRunnable;
import org.kalypso.model.wspm.core.util.vegetation.GuessVegetationClassesRunnable;
import org.kalypso.observation.result.IComponent;

/**
 * @author Dirk Kuch
 */
public class GuessClassificationClassRunnable implements IProfileManipulator
{
  private final GuessClassificationPage m_page;

  public GuessClassificationClassRunnable( final GuessClassificationPage page )
  {
    m_page = page;
  }

  @Override
  public final Pair<IProfilChange[], IStatus> performProfileManipulation( final IProfil profile, final IProgressMonitor monitor )
  {
    monitor.beginTask( "", 1 ); //$NON-NLS-1$

    final IProfilChange[] changes = findChanges( profile, monitor );
    return Pair.of( changes, Status.OK_STATUS );
  }

  private IProfilChange[] findChanges( final IProfil profile, final IProgressMonitor monitor )
  {
    final String type = m_page.getType();

    switch( type )
    {
      case IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS:
        return doGuessVegetationClasses( profile, monitor );

      case IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS:
        return doGuessRoughnessClasses( profile, monitor );
    }

    monitor.done();

    return new IProfilChange[] {};
  }

  private IProfilChange[] doGuessRoughnessClasses( final IProfil profile, final IProgressMonitor monitor )
  {
    if( !WspmClassifications.hasRoughnessProperties( profile ) )
      return new IProfilChange[] {};

    if( !WspmClassifications.hasRoughnessClass( profile ) )
      addRoughnessClass( profile, monitor );

    final Set<IProfilChange> changes = new LinkedHashSet<>();

    try
    {
      if( Objects.isNotNull( profile.hasPointProperty( IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KS ) ) )
      {
        final GuessRoughessClassesRunnable runnable = new GuessRoughessClassesRunnable( profile, IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KS, m_page.isOverwriteEnabled(), m_page.getDelta() );
        runnable.execute( monitor );

        Collections.addAll( changes, runnable.getChanges() );
      }
      else if( Objects.isNotNull( profile.hasPointProperty( IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KST ) ) )
      {
        final GuessRoughessClassesRunnable runnable = new GuessRoughessClassesRunnable( profile, IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KST, m_page.isOverwriteEnabled(), m_page.getDelta() );
        runnable.execute( monitor );

        Collections.addAll( changes, runnable.getChanges() );
      }
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
    }

    return changes.toArray( new IProfilChange[] {} );
  }

  private IProfilChange[] doGuessVegetationClasses( final IProfil profile, final IProgressMonitor monitor )
  {
    if( !WspmClassifications.hasVegetationProperties( profile ) )
      return new IProfilChange[] {};

    if( !WspmClassifications.hasVegetationClass( profile ) )
      addVegetationClass( profile, monitor );

    try
    {
      final GuessVegetationClassesRunnable runnable = new GuessVegetationClassesRunnable( profile, m_page.isOverwriteEnabled(), m_page.getDelta() );
      runnable.execute( monitor );

      return runnable.getChanges();
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
    }

    return new IProfilChange[] {};
  }

  protected void addVegetationClass( final IProfil profile, final IProgressMonitor monitor )
  {
    final IProfilPointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders( profile.getType() );
    final IComponent component = provider.getPointProperty( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS );

    final ProfilOperation operation = new ProfilOperation( "Adding profile point propertey - vegetation class", profile, true ); //$NON-NLS-1$
    operation.addChange( new PointPropertyAdd( profile, component ) );

    operation.execute( monitor, null );
  }

  protected void addRoughnessClass( final IProfil profile, final IProgressMonitor monitor )
  {
    final IProfilPointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders( profile.getType() );
    final IComponent component = provider.getPointProperty( IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS );

    final ProfilOperation operation = new ProfilOperation( "Adding profile point propertey - roughness class", profile, true ); //$NON-NLS-1$
    operation.addChange( new PointPropertyAdd( profile, component ) );

    operation.execute( monitor, null );
  }
}
