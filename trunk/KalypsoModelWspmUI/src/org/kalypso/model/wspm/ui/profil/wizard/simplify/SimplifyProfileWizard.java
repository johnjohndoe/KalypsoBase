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
package org.kalypso.model.wspm.ui.profil.wizard.simplify;

import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.util.DouglasPeuckerHelper;
import org.kalypso.model.wspm.ui.action.ProfileSelection;
import org.kalypso.model.wspm.ui.profil.wizard.ManipulateProfileWizard;
import org.kalypso.model.wspm.ui.profil.wizard.ProfileManipulationOperation.IProfileManipulator;
import org.kalypso.observation.result.IRecord;

/**
 * @author Gernot Belger
 */
public class SimplifyProfileWizard extends ManipulateProfileWizard
{
  private final SimplifyProfilePage m_simplifyPage;

  public SimplifyProfileWizard( final ProfileSelection profileSelection )
  {
    super( profileSelection, "Please select the profiles that will simplified." );

    m_simplifyPage = new SimplifyProfilePage( "simplifyPage" ); //$NON-NLS-1$

    addPage( m_simplifyPage );
  }

  /**
   * @see org.kalypso.model.wspm.ui.profil.wizard.ManipulateProfileWizard#getProfileManipulator()
   */
  @Override
  protected IProfileManipulator getProfileManipulator( )
  {
    final SimplifyProfilePage simplifyPage = m_simplifyPage;
    final double allowedDistance = simplifyPage.getDistance();

    return new IProfileManipulator()
    {
      @Override
      public void performProfileManipulation( final IProfil profile, final IProgressMonitor monitor )
      {
        monitor.beginTask( "", 1 );

        final IRecord[] points = simplifyPage.getSelectedPoints( profile );
        final IRecord[] pointsToKeep = profile.getMarkedPoints();
        final IRecord[] pointsToRemove = DouglasPeuckerHelper.reducePoints( points, pointsToKeep, allowedDistance );
        profile.getResult().removeAll( Arrays.asList( pointsToRemove ) );

        monitor.done();
      }
    };
  }
}
