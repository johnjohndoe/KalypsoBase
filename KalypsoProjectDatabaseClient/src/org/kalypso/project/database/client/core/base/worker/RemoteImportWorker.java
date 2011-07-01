/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.project.database.client.core.base.worker;

import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.kalypso.contribs.eclipse.core.resources.ProjectTemplate;
import org.kalypso.contribs.eclipse.jface.wizard.IUpdateable;
import org.kalypso.contribs.eclipse.jface.wizard.ProjectTemplatePage;
import org.kalypso.contribs.eclipse.jface.wizard.WizardDialog2;
import org.kalypso.module.IKalypsoModule;
import org.kalypso.module.welcome.utils.DisableCreateProjectWizardPageElements;
import org.kalypso.project.database.client.i18n.Messages;
import org.kalypso.project.database.client.ui.project.wizard.create.WizardCreateProject;
import org.kalypso.project.database.common.nature.IRemoteProjectPreferences;
import org.kalypso.project.database.common.nature.RemoteProjectNature;
import org.kalypso.project.database.sei.beans.KalypsoProjectBean;

/**
 * @author Dirk Kuch
 */
public class RemoteImportWorker
{
  private final ProjectTemplate[] m_templates;

  private final Map<ProjectTemplate, KalypsoProjectBean> m_mapping;

  private final IKalypsoModule m_module;

  public RemoteImportWorker( final ProjectTemplate[] templates, final Map<ProjectTemplate, KalypsoProjectBean> mapping, final IKalypsoModule module )
  {
    m_templates = templates;
    m_mapping = mapping;
    m_module = module;
  }

  // FIXME FIXME FIXME!
  // Really evil and wrong dependencies to planer client here!!!
  public IStatus execute( ) throws CoreException
  {
    final WizardCreateProject wizard;
    if( m_templates.length == 1 )
    {
      final ProjectTemplatePage page = new ProjectTemplatePage( Messages.getString( "org.kalypso.project.database.client.ui.project.database.internal.RemoteProjectHelper.2" ), Messages.getString( "org.kalypso.project.database.client.ui.project.database.internal.RemoteProjectHelper.3" ), new ProjectTemplate[] { m_templates[0] } ); //$NON-NLS-1$ //$NON-NLS-2$
      wizard = new WizardCreateProject( page, new String[] {}, m_module );
    }
    else
    {
      wizard = new WizardCreateProject( m_templates, new String[] {}, m_module );
    }

    wizard.init( PlatformUI.getWorkbench(), null );
    wizard.setActivateScenarioOnPerformFinish( false );

    final WizardDialog2 dialog = new WizardDialog2( null, wizard );
    dialog.setRememberSize( true );

    dialog.addPageChangedListener( new IPageChangedListener()
    {
      @Override
      public void pageChanged( final PageChangedEvent event )
      {
        final Object page = event.getSelectedPage();

        if( page instanceof IUpdateable )
        {
          final IUpdateable update = (IUpdateable) page;
          update.update();
        }
        else if( page instanceof WizardNewProjectCreationPage )
        {
          final WizardNewProjectCreationPage myPage = (WizardNewProjectCreationPage) page;
          DisableCreateProjectWizardPageElements.disableElementsForProjectDownload( myPage );
        }
      }
    } );

    dialog.open();
    if( Window.OK == dialog.getReturnCode() )
    {
      final IProject project = wizard.getNewProject();

      if( !project.hasNature( RemoteProjectNature.NATURE_ID ) )
      {
        final IProjectDescription description = project.getDescription();
        description.setNatureIds( (String[]) ArrayUtils.add( description.getNatureIds(), RemoteProjectNature.NATURE_ID ) );

        project.setDescription( description, new NullProgressMonitor() );
      }

      final IProjectNature nature = project.getNature( RemoteProjectNature.NATURE_ID );
      if( nature instanceof RemoteProjectNature )
      {
        // bad hack
        final KalypsoProjectBean bean = m_mapping.get( wizard.getSelectedTemplate() );

        final RemoteProjectNature remote = (RemoteProjectNature) nature;
        final IRemoteProjectPreferences preferences = remote.getRemotePreferences( project, null );
        preferences.setVersion( bean.getProjectVersion() );
        preferences.setIsOnServer( Boolean.TRUE );
        preferences.setModified( false );
        preferences.setChangesCommited( true );
      }

      project.refreshLocal( IResource.DEPTH_INFINITE, new NullProgressMonitor() );

    }

    return Status.OK_STATUS;
  }

}
