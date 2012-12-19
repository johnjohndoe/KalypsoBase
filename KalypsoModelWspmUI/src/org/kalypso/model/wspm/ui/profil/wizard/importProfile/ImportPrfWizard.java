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
package org.kalypso.model.wspm.ui.profil.wizard.importProfile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.kalypso.contribs.eclipse.core.runtime.PluginUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.gml.WspmWaterBody;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.serializer.IProfileSource;
import org.kalypso.model.wspm.core.profil.serializer.ProfileSerializerUtilitites;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.action.ImportProfilesCommand;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypsodeegree.KalypsoDeegreePlugin;

/**
 * Wizard for importing .prf files into wspm.
 *
 * @author Gernot Belger
 */
public class ImportPrfWizard extends Wizard implements IWorkbenchWizard
{
  private static final String SETTINGS_FILTER_PATH = "fileDialogPath"; //$NON-NLS-1$

  private WspmWaterBody m_water;

  private CommandableWorkspace m_workspace;

  public ImportPrfWizard( )
  {
    setWindowTitle( Messages.getString( "ImportPrfWizard.0" ) ); //$NON-NLS-1$

    final IDialogSettings dialogSettings = DialogSettingsUtils.getDialogSettings( KalypsoModelWspmUIPlugin.getDefault(), getClass().getName() );
    setDialogSettings( dialogSettings );
  }

  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
    if( !(selection instanceof IFeatureSelection) )
      throw new IllegalStateException();

    final IFeatureSelection featureSelection = (IFeatureSelection) selection;
    m_water = ImportProfileWizard.findWater( featureSelection );
    m_workspace = featureSelection.getWorkspace( m_water );

    addPage( new ImportPrfWizardPage( "chooseFiles" ) ); //$NON-NLS-1$
  }

  @Override
  public boolean performFinish( )
  {
    final Shell shell = getShell();

    /* open file dialog and choose profile files */
    final File[] files = askForFiles( shell );
    if( files == null || files.length == 0 )
      return true;

    /* read profiles, show warnings */
    final List<IProfile> profiles = new ArrayList<>( files.length );
    final MultiStatus prfReadStatus = readProfiles( shell, files, profiles );

    if( profiles.size() == 0 )
    {
      MessageDialog.openInformation( shell, getWindowTitle(), org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.action.ImportProfilePrfAction.0" ) ); //$NON-NLS-1$
      return true;
    }

    if( !prfReadStatus.isOK() )
    {
      if( !MessageDialog.openConfirm( shell, getWindowTitle(), org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.action.ImportProfilePrfAction.1" ) ) ) //$NON-NLS-1$
        return true;
    }

    /* convert them into the profile-list */
    try
    {
      final IProfile[] profs = profiles.toArray( new IProfile[profiles.size()] );
      final ImportProfilesCommand command = new ImportProfilesCommand( m_water, profs );
      m_workspace.postCommand( command );
    }
    catch( final Exception e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e );
      ErrorDialog.openError( shell, getWindowTitle(), org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.action.ImportProfilePrfAction.2" ), status ); //$NON-NLS-1$
    }

    return true;
  }

  private File[] askForFiles( final Shell shell )
  {
    final IDialogSettings dialogSettings = getDialogSettings();
    final String initialFilterPath = dialogSettings.get( SETTINGS_FILTER_PATH );

    final FileDialog dialog = new FileDialog( shell, SWT.OPEN | SWT.MULTI );
    dialog.setText( ".prf Import" ); //$NON-NLS-1$
    dialog.setFilterExtensions( new String[] { "*.prf", "*.*" } ); //$NON-NLS-1$ //$NON-NLS-2$
    dialog.setFilterPath( initialFilterPath );

    final String result = dialog.open();
    if( result == null )
      return null;

    final String filterPath = dialog.getFilterPath();
    dialogSettings.put( SETTINGS_FILTER_PATH, filterPath );

    final String[] fileNames = dialog.getFileNames();
    final File[] results = new File[fileNames.length];
    for( int i = 0; i < fileNames.length; i++ )
    {
      final String name = fileNames[i];
      results[i] = new File( filterPath, name );
    }

    return results;
  }

  private MultiStatus readProfiles( final Shell shell, final File[] files, final List<IProfile> profiles )
  {
    final MultiStatus prfReadStatus = new MultiStatus( PluginUtilities.id( KalypsoModelWspmUIPlugin.getDefault() ), -1, org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.action.ImportProfilePrfAction.3" ), null ); //$NON-NLS-1$
    // final Date today = new Date();
    // final String todayString = DF.format( today );
    for( final File file : files )
    {
      try
      {
        final IProfileSource prfSource = KalypsoModelWspmCoreExtensions.createProfilSource( "prf" ); //$NON-NLS-1$
        final IProfile[] profs = ProfileSerializerUtilitites.readProfile( prfSource, file, "org.kalypso.model.wspm.tuhh.profiletype" ); //$NON-NLS-1$
        if( profs == null || profs.length < 0 )
        {
          continue;
        }
        final IProfile profile = profs[0];
        profile.setName( org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.action.ImportProfilePrfAction.4" ) ); //$NON-NLS-1$

        // FIXME: ask user for crs!
        final String crs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
        profile.setSrsName( crs );

        // do not overwrite original comment from wspwin profile
        // TODO: put this information into metadata strings
        // final String description = String.format( "Importiert am %s aus %s", todayString, file.getAbsolutePath() );
        // profil.setComment( description );

        profiles.add( profile );
      }
      catch( final IOException e )
      {
        final IStatus status = StatusUtilities.statusFromThrowable( e, file.getName() + ": " ); //$NON-NLS-1$
        prfReadStatus.add( status );
      }
      catch( final CoreException e )
      {
        final IStatus status = StatusUtilities.statusFromThrowable( e, file.getName() + ": " ); //$NON-NLS-1$
        prfReadStatus.add( status );
      }
    }

    if( prfReadStatus.getChildren().length > 0 )
    {
      ErrorDialog.openError( shell, getWindowTitle(), org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.action.ImportProfilePrfAction.5" ), prfReadStatus ); //$NON-NLS-1$
    }
    return prfReadStatus;
  }
}