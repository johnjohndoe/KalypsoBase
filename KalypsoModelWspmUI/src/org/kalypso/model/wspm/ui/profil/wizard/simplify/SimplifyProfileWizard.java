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
package org.kalypso.model.wspm.ui.profil.wizard.simplify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.Wizard;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.util.DouglasPeuckerHelper;
import org.kalypso.model.wspm.ui.action.ProfileSelection;
import org.kalypso.model.wspm.ui.profil.wizard.ProfilesChooserPage;
import org.kalypso.observation.result.IRecord;
import org.kalypso.ogc.gml.command.ChangeFeaturesCommand;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;

/**
 * @author Gernot Belger
 */
public class SimplifyProfileWizard extends Wizard
{
  final private ProfilesChooserPage m_profileChooserPage;

  final private CommandableWorkspace m_workspace;

  private final SimplifyProfilePage m_simplifyPage;

  public SimplifyProfileWizard( final ProfileSelection profileSelection )
  {
    m_workspace = profileSelection.getWorkspace();
    setNeedsProgressMonitor( true );

    final String message = "Please select the profiles that will simplified.";
    m_profileChooserPage = new ProfilesChooserPage( message, profileSelection, false );
    m_simplifyPage = new SimplifyProfilePage( "simplifyPage" );

    addPage( m_profileChooserPage );
    addPage( m_simplifyPage );
  }

  private IProfil[] toProfiles( final Object[] features )
  {
    final IProfil[] choosenProfiles = new IProfil[features.length];
    for( int i = 0; i < features.length; i++ )
    {
      final IProfileFeature wspmProfile = (IProfileFeature) features[i];
      choosenProfiles[i] = wspmProfile.getProfil();
    }

    return choosenProfiles;
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish( )
  {
    final double allowedDistance = m_simplifyPage.getDistance();

    final Object[] profilFeatures = m_profileChooserPage.getChoosen();
    final IProfil[] choosenProfiles = toProfiles( profilFeatures );
    final List<FeatureChange> featureChanges = new ArrayList<FeatureChange>();
    for( final IProfil profile : choosenProfiles )
    {
      final IRecord[] points = profile.getPoints();

      final IRecord[] pointsToKeep = profile.getMarkedPoints();
      final IRecord[] pointsToRemove = DouglasPeuckerHelper.reducePoints( points, pointsToKeep, allowedDistance );
      profile.getResult().removeAll( Arrays.asList( pointsToRemove ) );
    }

    final ChangeFeaturesCommand command = new ChangeFeaturesCommand( m_workspace, featureChanges.toArray( new FeatureChange[0] ) );
    try
    {
      m_workspace.postCommand( command );
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      final IStatus status = StatusUtilities.statusFromThrowable( e );
      ErrorDialog.openError( getShell(), getWindowTitle(), "Failed to simplify profiles", status );
    }

    return true;
  }
}
