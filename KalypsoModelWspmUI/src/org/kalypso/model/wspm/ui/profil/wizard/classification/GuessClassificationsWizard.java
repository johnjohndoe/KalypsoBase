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
package org.kalypso.model.wspm.ui.profil.wizard.classification;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.gml.classifications.helper.WspmClassifications;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilPointPropertyProvider;
import org.kalypso.model.wspm.core.profil.changes.PointPropertyAdd;
import org.kalypso.model.wspm.core.profil.operation.ProfilOperation;
import org.kalypso.model.wspm.core.util.roughnesses.GuessRoughessClassesRunnable;
import org.kalypso.model.wspm.core.util.vegetation.GuessVegetationClassesRunnable;
import org.kalypso.model.wspm.ui.profil.wizard.ManipulateProfileWizard;
import org.kalypso.model.wspm.ui.profil.wizard.ProfileManipulationOperation.IProfileManipulator;
import org.kalypso.observation.result.IComponent;

/**
 * @author Dirk Kuch
 */
public class GuessClassificationsWizard extends ManipulateProfileWizard
{
  protected GuessClassificationPage m_page;

  public GuessClassificationsWizard( )
  {
    setWindowTitle( "Guess classification classes from profile properties" );
  }

  @Override
  protected String getProfilePageMessage( )
  {
    return "Guess classification classes of profiles";
  }

  @Override
  public void addPages( )
  {
    m_page = new GuessClassificationPage();
    addPage( m_page );
  }

  @Override
  protected IProfileManipulator getProfileManipulator( )
  {
    return new IProfileManipulator()
    {
      @Override
      public void performProfileManipulation( final IProfil profile, final IProgressMonitor monitor )
      {
        monitor.beginTask( "", 1 ); //$NON-NLS-1$

        final String type = m_page.getType();
        if( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS.equals( type ) )
        {
          if( WspmClassifications.hasVegetationProperties( profile ) )
            return;

          if( !WspmClassifications.hasVegetationClass( profile ) )
            addVegetationClass( profile, monitor );

          try
          {
            final GuessVegetationClassesRunnable runnable = new GuessVegetationClassesRunnable( profile, m_page.isOverwriteEnabled(), m_page.getDelta() );
            runnable.execute( monitor );
          }
          catch( final CoreException e )
          {
            e.printStackTrace();
          }

        }
        else if( IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS.equals( type ) )
        {
          if( WspmClassifications.hasRoughnessProperties( profile ) )
            return;

          if( !WspmClassifications.hasRoughnessClass( profile ) )
            addRoughnessClass( profile, monitor );

          try
          {
            if( Objects.isNotNull( profile.hasPointProperty( IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KS ) ) )
            {
              final GuessRoughessClassesRunnable runnable = new GuessRoughessClassesRunnable( profile, IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KS, m_page.isOverwriteEnabled(), m_page.getDelta() );
              runnable.execute( monitor );
            }
            else if( Objects.isNotNull( profile.hasPointProperty( IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KST ) ) )
            {
              final GuessRoughessClassesRunnable runnable = new GuessRoughessClassesRunnable( profile, IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KST, m_page.isOverwriteEnabled(), m_page.getDelta() );
              runnable.execute( monitor );
            }
          }
          catch( final CoreException e )
          {
            e.printStackTrace();
          }

        }

        monitor.done();
      }

    };
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