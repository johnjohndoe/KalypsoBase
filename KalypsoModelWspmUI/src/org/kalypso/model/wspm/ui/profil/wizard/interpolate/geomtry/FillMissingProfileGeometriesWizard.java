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
package org.kalypso.model.wspm.ui.profil.wizard.interpolate.geomtry;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.base.ExtrapolateMissingCoordinatesVisitor;
import org.kalypso.model.wspm.core.profil.base.InterpolateMissingCoordinatesVisitor;
import org.kalypso.model.wspm.core.profil.wrappers.ProfileRecord;
import org.kalypso.model.wspm.core.profil.wrappers.ProfileWrapper;
import org.kalypso.model.wspm.ui.profil.dialogs.reducepoints.IPointsProvider;
import org.kalypso.observation.result.IRecord;

/**
 * @author Dirk Kuch
 */
public class FillMissingProfileGeometriesWizard extends Wizard implements IWorkbenchWizard
{
  FillMissingProfileGeometriesPage m_page;

  private final IPointsProvider[] m_providers;

  private final IProfil m_profile;

  public FillMissingProfileGeometriesWizard( final IProfil profile, final IPointsProvider[] providers )
  {
    m_profile = profile;
    m_providers = providers;
    setWindowTitle( "Fill missing profile geometries" ); //$NON-NLS-1$

    setNeedsProgressMonitor( true );
  }

  @Override
  public void addPages( )
  {
    m_page = new FillMissingProfileGeometriesPage( m_providers );
    addPage( m_page );
  }

  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
  }

  @Override
  public boolean performFinish( )
  {
    final ProfileWrapper profile = new ProfileWrapper( m_profile );

    final IPointsProvider provider = m_page.getProvider();
    final IRecord[] records = provider.getPoints();

    final InterpolateMissingCoordinatesVisitor interpolation = new InterpolateMissingCoordinatesVisitor();

    for( final IRecord record : records )
    {
      final ProfileRecord point = new ProfileRecord( record );
      interpolation.visit( profile, point );
    }

    if( m_page.doExtrapolation() )
    {
      final ExtrapolateMissingCoordinatesVisitor extrapolation = new ExtrapolateMissingCoordinatesVisitor();

      for( final IRecord record : records )
      {
        final ProfileRecord point = new ProfileRecord( record );
        extrapolation.visit( profile, point );
      }
    }

    return true;
  }
}