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
package org.kalypso.model.wspm.ui.profil.wizard.classification;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.gml.classifications.helper.WspmClassifications;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilPointPropertyProvider;
import org.kalypso.model.wspm.core.profil.changes.PointPropertyAdd;
import org.kalypso.model.wspm.core.profil.operation.ProfilOperation;
import org.kalypso.model.wspm.core.util.roughnesses.UpdateSimpleRoughnessProperty;
import org.kalypso.model.wspm.core.util.vegetation.UpdateVegetationProperties;
import org.kalypso.model.wspm.ui.profil.wizard.ManipulateProfileWizard;
import org.kalypso.model.wspm.ui.profil.wizard.ProfileManipulationOperation.IProfileManipulator;
import org.kalypso.observation.result.IComponent;

/**
 * @author Dirk Kuch
 */
public class ApplyClassificationsWizard extends ManipulateProfileWizard
{
  protected ApplyClassificationsPage m_page;

  public ApplyClassificationsWizard( )
  {
    setWindowTitle( "Apply classification class values on profiles" );
  }

  @Override
  protected String getProfilePageMessage( )
  {
    return "Apply classification class values on profiles";
  }

  @Override
  public void addPages( )
  {
    m_page = new ApplyClassificationsPage();
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

          if( !WspmClassifications.hasVegetationClass( profile ) )
            return;

          if( !WspmClassifications.hasVegetationProperties( profile ) )
            addVegetationProperties( profile, monitor );

          try
          {
            final UpdateVegetationProperties runnable = new UpdateVegetationProperties( profile, m_page.isOverwriteEnabled() );
            runnable.execute( monitor );
          }
          catch( final CoreException e )
          {
            e.printStackTrace();
          }

        }
        else if( IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS.equals( type ) )
        {
          if( !WspmClassifications.hasRoughnessClass( profile ) )
            return;

          if( !WspmClassifications.hasRoughnessProperties( profile ) )
            addRoughnessProperties( profile, monitor );

          try
          {
            final UpdateSimpleRoughnessProperty updateKsValues = new UpdateSimpleRoughnessProperty( profile, IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KS, m_page.isOverwriteEnabled() );
            updateKsValues.execute( monitor );

            final UpdateSimpleRoughnessProperty updateKstValues = new UpdateSimpleRoughnessProperty( profile, IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KST, m_page.isOverwriteEnabled() );
            updateKstValues.execute( monitor );
          }
          catch( final CoreException e )
          {
            e.printStackTrace();
          }

        }

        monitor.done();
      }

      private void addRoughnessProperties( final IProfil profile, final IProgressMonitor monitor )
      {
        final IProfilPointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders( profile.getType() );
        final IComponent propertyKs = provider.getPointProperty( IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KS );
        final IComponent propertyKst = provider.getPointProperty( IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KST );

        final ProfilOperation operation = new ProfilOperation( "Adding profile point propertey - vegetation class", profile, true ); //$NON-NLS-1$
        operation.addChange( new PointPropertyAdd( profile, propertyKs ) );
        operation.addChange( new PointPropertyAdd( profile, propertyKst ) );

        operation.execute( monitor, null );
      }

      private void addVegetationProperties( final IProfil profile, final IProgressMonitor monitor )
      {
        final IProfilPointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders( profile.getType() );
        final IComponent propertyAx = provider.getPointProperty( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_AX );
        final IComponent propertyAy = provider.getPointProperty( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_AY );
        final IComponent propertyDp = provider.getPointProperty( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_DP );

        final ProfilOperation operation = new ProfilOperation( "Adding profile point propertey - vegetation class", profile, true ); //$NON-NLS-1$
        operation.addChange( new PointPropertyAdd( profile, propertyAx ) );
        operation.addChange( new PointPropertyAdd( profile, propertyAy ) );
        operation.addChange( new PointPropertyAdd( profile, propertyDp ) );

        operation.execute( monitor, null );
      }
    };
  }
}