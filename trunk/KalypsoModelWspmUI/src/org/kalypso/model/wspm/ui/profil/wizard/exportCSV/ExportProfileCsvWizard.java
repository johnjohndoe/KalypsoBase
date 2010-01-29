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
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.contribs.eclipse.core.runtime.PluginUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.contribs.eclipse.jface.wizard.ArrayChooserPage;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.serializer.IProfilSink;
import org.kalypso.model.wspm.core.profil.serializer.ProfilSerializerUtilitites;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ui.editor.gmleditor.ui.GMLLabelProvider;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author kimwerner
 */
public class ExportProfileCsvWizard extends Wizard
{

  final private ArrayChooserPage m_profileChooserPage;

  final private CsvFileChooserPage m_csvFileChooserPage = new CsvFileChooserPage();

  final private List<Feature> m_profiles;

  final private List<Feature> m_selectedProfiles;

  final protected CommandableWorkspace m_workspace;

  public ExportProfileCsvWizard( final CommandableWorkspace workspace, final List<Feature> profiles, final List<Feature> selection )
  {
    m_workspace = workspace;
    m_profiles = profiles;
    m_selectedProfiles = selection;
 
    setWindowTitle( org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.profil.wizard.validateProfiles.ValidateProfilesWizard.0" ) ); //$NON-NLS-1$
    setNeedsProgressMonitor( true );
    setDialogSettings( PluginUtilities.getDialogSettings( KalypsoModelWspmUIPlugin.getDefault(), getClass().getName() ) );
    m_profileChooserPage = new ArrayChooserPage( m_profiles, new Object[0], m_selectedProfiles.toArray(), 1, "profilesChooserPage", org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.profil.wizard.validateProfiles.ValidateProfilesWizard.1" ), null ); //$NON-NLS-1$ //$NON-NLS-2$
    m_profileChooserPage.setLabelProvider( new GMLLabelProvider() );
    m_profileChooserPage.setMessage( org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.profil.wizard.validateProfiles.ValidateProfilesWizard.2" ) ); //$NON-NLS-1$
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#addPages()
   */
  @Override
  public void addPages( )
  {
    super.addPages();
    addPage( m_profileChooserPage );
    addPage( m_csvFileChooserPage );

  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#canFinish()
   */
  @Override
  public boolean canFinish( )
  {
    return m_csvFileChooserPage.isPageComplete() && m_profileChooserPage.isPageComplete();
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish( )
  {
    final Object[] profilFeatures = m_profileChooserPage.getChoosen();
    final File file = m_csvFileChooserPage.getFile();
    final ArrayList<IProfil> profiles = new ArrayList<IProfil>( profilFeatures.length );

    final ICoreRunnableWithProgress m_exportJob = new ICoreRunnableWithProgress()
    {
      public IStatus execute( final IProgressMonitor monitor )
      {
        monitor.beginTask( org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.profil.wizard.validateProfiles.ValidateProfilesWizard.7" ), 2 * profilFeatures.length ); //$NON-NLS-1$
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

        try
        {
          final IProfilSink sink = KalypsoModelWspmCoreExtensions.createProfilSink(FileUtilities.getSuffix( file )); 
          ProfilSerializerUtilitites.writeProfile( sink, profiles.toArray(new IProfil[]{}), file );
//          Writer writer = new PrintWriter( file );
//          sink.write( profiles, writer );
//          writer.close();
          return  new Status( IStatus.OK,"","");
        }
        catch( Exception e )
        {
          monitor.done();
          return new Status( IStatus.ERROR, KalypsoModelWspmCorePlugin.getID(), e.getLocalizedMessage() );
        }
      }
    };

    Display.getDefault().asyncExec( new Runnable()
    {
      public void run( )
      {
        RunnableContextHelper.execute( new ProgressMonitorDialog( getShell() ), true, true, m_exportJob );

      }
    } );
    return true;
  }
}
