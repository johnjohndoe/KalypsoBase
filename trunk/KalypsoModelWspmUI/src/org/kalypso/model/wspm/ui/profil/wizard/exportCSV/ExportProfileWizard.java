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
package org.kalypso.model.wspm.ui.profil.wizard.exportCSV;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.Wizard;
import org.kalypso.contribs.eclipse.core.runtime.PluginUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.contribs.eclipse.jface.wizard.ArrayChooserPage;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.serializer.IProfilSink;
import org.kalypso.model.wspm.core.profil.serializer.ProfilSerializerUtilitites;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.profil.wizard.ProfilesChooserPage;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author kimwerner
 */
public class ExportProfileWizard extends Wizard
{

  final private ArrayChooserPage m_profileChooserPage;

  final protected ExportFileChooserPage m_profileFileChooserPage = new ExportFileChooserPage();

  final private List<Feature> m_profiles;

  final private List<Feature> m_selectedProfiles;

  final protected CommandableWorkspace m_workspace;

  public ExportProfileWizard( final CommandableWorkspace workspace, final List<Feature> profiles, final List<Feature> selection )
  {
    m_workspace = workspace;
    m_profiles = profiles;
    m_selectedProfiles = selection;

    setWindowTitle( "Exportiere Profildateien" );
    setNeedsProgressMonitor( true );
    setDialogSettings( PluginUtilities.getDialogSettings( KalypsoModelWspmUIPlugin.getDefault(), getClass().getName() ) );
    m_profileChooserPage = new ProfilesChooserPage( "Bitte w‰hlen Sie die Profile aus die exportiert werden sollen.", m_profiles, new Object[0], m_selectedProfiles.toArray(), 1 );
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#addPages()
   */
  @Override
  public void addPages( )
  {
    super.addPages();
    addPage( m_profileChooserPage );
    addPage( m_profileFileChooserPage );

  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#canFinish()
   */
  @Override
  public boolean canFinish( )
  {
    return m_profileFileChooserPage.isPageComplete() && m_profileChooserPage.isPageComplete();
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish( )
  {
    final Object[] profilFeatures = m_profileChooserPage.getChoosen();
    final File file = m_profileFileChooserPage.getFile();

    final ArrayList<IProfil> profiles = new ArrayList<IProfil>( profilFeatures.length );

    final ICoreRunnableWithProgress m_exportJob = new ICoreRunnableWithProgress()
    {
      public IStatus execute( final IProgressMonitor monitor )
      {
        try
        {
          monitor.beginTask( "Profile exportieren", profilFeatures.length );
          final IProfilSink sink = m_profileFileChooserPage.getProfilSink();
          for( int i = 0; i < profilFeatures.length; i++ )
          {
            if( profilFeatures[i] instanceof Feature )
            {
              final IProfileFeature wspmProfil = (IProfileFeature) profilFeatures[i];
              if( wspmProfil != null )
                profiles.add( wspmProfil.getProfil() );
            }
            monitor.worked( i );
          }
          ProfilSerializerUtilitites.writeProfile( sink, profiles.toArray( new IProfil[] {} ), file );
          return Status.OK_STATUS;
        }
        catch( final Exception e )
        {
          monitor.done();
          return new Status( IStatus.ERROR, KalypsoModelWspmCorePlugin.getID(), e.getLocalizedMessage(), e );
        }
      }
    };

    final IStatus result = RunnableContextHelper.execute( getContainer(), true, true, m_exportJob );
    ErrorDialog.openError( getShell(), getWindowTitle(), "Failed to export profiles", result );
    return !result.matches( IStatus.ERROR );
  }
}
