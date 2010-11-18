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
package org.kalypso.project.database.client.ui.composites;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.kalypso.afgui.wizards.INewProjectWizard;
import org.kalypso.afgui.wizards.INewProjectWizardProvider;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.wizard.IUpdateable;
import org.kalypso.contribs.eclipse.jface.wizard.WizardDialog2;
import org.kalypso.project.database.client.KalypsoProjectDatabaseClient;
import org.kalypso.project.database.client.ui.project.wizard.create.DisableCreateProjectWizardPageElements;
import org.kalypso.project.database.common.nature.IRemoteProjectPreferences;
import org.kalypso.project.database.common.nature.RemoteProjectNature;

/**
 * Composite for calling the new project wizard
 * 
 * @author Dirk Kuch
 */
public class CreateProjectAction extends Action
{
  // FIXME: icons should not come from the sources in eclipse!
  public static final ImageDescriptor IMG_ADD_PROJECT = ImageDescriptor.createFromURL( CreateProjectAction.class.getResource( "icons/add_project.gif" ) ); //$NON-NLS-1$

  public static final ImageDescriptor IMG_EXTRACT_DEMO = ImageDescriptor.createFromURL( CreateProjectAction.class.getResource( "icons/extract_demo.gif" ) ); //$NON-NLS-1$

  private final String m_commitType;

  private boolean resetProjectName = true;

  private final INewProjectWizardProvider m_wizardProvider;

  public CreateProjectAction( final String label, final String commitType, final INewProjectWizardProvider wizardProvider )
  {
    m_commitType = commitType;
    m_wizardProvider = wizardProvider;
    setText( label );

    setImageDescriptor( IMG_ADD_PROJECT );
  }

  /**
   * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
   */
  @Override
  public void runWithEvent( final Event event )
  {
    final INewProjectWizard wizard = m_wizardProvider.createWizard();

    wizard.init( PlatformUI.getWorkbench(), null );
    wizard.setActivateScenarioOnPerformFinish( false );

    final WizardDialog2 dialog = new WizardDialog2( PlatformUI.getWorkbench().getDisplay().getActiveShell(), wizard );
    dialog.setRememberSize( true );

    dialog.addPageChangedListener( new IPageChangedListener()
    {
      @Override
      public void pageChanged( final PageChangedEvent pageEvent )
      {
        handlePageChanged( wizard, pageEvent );
      }
    } );

    dialog.open();
    if( dialog.getReturnCode() == Window.OK )
    {
      try
      {
        final IProject project = wizard.getNewProject();
        final IProjectNature nature = project.getNature( RemoteProjectNature.NATURE_ID );
        if( nature instanceof RemoteProjectNature )
        {
          final RemoteProjectNature remote = (RemoteProjectNature) nature;
          final IRemoteProjectPreferences preferences = remote.getRemotePreferences( project, null );
          preferences.setVersion( -1 );
          preferences.setIsOnServer( Boolean.FALSE );
          preferences.setProjectType( m_commitType );
        }
      }
      catch( final CoreException e1 )
      {
        KalypsoProjectDatabaseClient.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e1 ) );
      }
    }
  }

  protected void handlePageChanged( final INewProjectWizard wizard, final PageChangedEvent pageEvent )
  {
    final Object page = pageEvent.getSelectedPage();
    if( page instanceof IUpdateable )
    {
      final IUpdateable update = (IUpdateable) page;
      update.update();
    }
    else if( wizard.disableProjectCreationUI() && page instanceof WizardNewProjectCreationPage )
    {
      if( resetProjectName )
      {
        final WizardNewProjectCreationPage myPage = (WizardNewProjectCreationPage) page;
        DisableCreateProjectWizardPageElements.disableElementsForProjectCreation( myPage );

        resetProjectName = false;
      }
    }
  }
}
