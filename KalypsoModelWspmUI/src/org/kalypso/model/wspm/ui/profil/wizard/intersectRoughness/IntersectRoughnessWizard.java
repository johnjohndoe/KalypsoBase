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
package org.kalypso.model.wspm.ui.profil.wizard.intersectRoughness;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.kalypso.commons.command.ICommand;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.contribs.eclipse.jface.wizard.ArrayChooserPage;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.action.ProfilesSelection;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.profil.wizard.ProfileHandlerUtils;
import org.kalypso.model.wspm.ui.profil.wizard.ProfilesChooserPage;
import org.kalypso.model.wspm.ui.profil.wizard.classification.landuse.worker.ApplyLanduseWorker;
import org.kalypso.model.wspm.ui.profil.wizard.utils.FeatureThemeWizardUtilitites;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.command.ChangeFeaturesCommand;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypso.ui.editor.gmleditor.part.GMLLabelProvider;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * @author Gernot Belger
 */
public class IntersectRoughnessWizard extends Wizard implements IWorkbenchWizard
{
  private final GMLLabelProvider m_chooserPageLabelProvider = new GMLLabelProvider();

  private ArrayChooserPage m_profileChooserPage;

  protected IntersectRoughnessPage m_roughnessIntersectPage;

  private ProfilesSelection m_profileSelection;

  private IKalypsoFeatureTheme m_theme;

  public IntersectRoughnessWizard( )
  {
    setWindowTitle( org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.wizard.IntersectRoughnessWizard.0" ) ); //$NON-NLS-1$
    setNeedsProgressMonitor( true );
    setDialogSettings( DialogSettingsUtils.getDialogSettings( KalypsoModelWspmUIPlugin.getDefault(), getClass().getName() ) );
  }

  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
    /* retrieve selected profiles, abort if none */
    final IKalypsoFeatureTheme theme = FeatureThemeWizardUtilitites.findTheme( selection );
    final ProfilesSelection profileSelection = ProfileHandlerUtils.getSelectionChecked( selection );

    m_theme = theme;
    m_profileSelection = profileSelection;
  }

  @Override
  public void addPages( )
  {
    final String msg = Messages.getString( "org.kalypso.model.wspm.ui.wizard.IntersectRoughnessWizard.2" ); //$NON-NLS-1$
    final String title = Messages.getString( "org.kalypso.model.wspm.ui.wizard.IntersectRoughnessWizard.1" ); //$NON-NLS-1$

    m_profileChooserPage = new ProfilesChooserPage( msg, m_profileSelection, false );
    m_profileChooserPage.setTitle( title );

    m_roughnessIntersectPage = new IntersectRoughnessPage( m_theme.getMapModell() );

    addPage( m_profileChooserPage );
    addPage( m_roughnessIntersectPage );

    super.addPages();
  }

  @Override
  public void dispose( )
  {
    m_chooserPageLabelProvider.dispose();

    super.dispose();
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish( )
  {
    final Object[] choosen = m_profileChooserPage.getChoosen();
    if( ArrayUtils.isEmpty( choosen ) )
      return true;

    final IKalypsoFeatureTheme theme = m_theme;
    final ICoreRunnableWithProgress runnable = new ICoreRunnableWithProgress()
    {
      @Override
      public IStatus execute( final IProgressMonitor monitor ) throws InvocationTargetException
      {
        monitor.beginTask( Messages.getString( "org.kalypso.model.wspm.ui.wizard.IntersectRoughnessWizard.3" ), 1 + choosen.length ); //$NON-NLS-1$

        try
        {
          monitor.worked( 1 );

          final IntersectRoughnessesLanduseDelegate delegate = new IntersectRoughnessesLanduseDelegate( m_roughnessIntersectPage, (IProfileFeature[]) choosen );

          final ApplyLanduseWorker worker = new ApplyLanduseWorker( delegate );
          RunnableContextHelper.execute( getContainer(), true, false, worker );

          final FeatureChange[] changes = worker.getChanges();
          if( !ArrayUtils.isEmpty( changes ) )
          {
            final GMLWorkspace gmlworkspace = changes[0].getFeature().getWorkspace();
            final ICommand command = new ChangeFeaturesCommand( gmlworkspace, changes );
            theme.postCommand( command, null );
          }
        }
        catch( final Exception e )
        {
          throw new InvocationTargetException( e );
        }
        finally
        {
          monitor.done();
        }

        return Status.OK_STATUS;
      }
    };

    final IStatus status = RunnableContextHelper.execute( getContainer(), true, true, runnable );
    ErrorDialog.openError( getShell(), getWindowTitle(), Messages.getString( "org.kalypso.model.wspm.ui.wizard.IntersectRoughnessWizard.5" ), status ); //$NON-NLS-1$

    return status.isOK();
  }

}
