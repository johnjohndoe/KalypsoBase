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
package org.kalypso.afgui.wizards;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.dialogs.WizardNewProjectReferencePage;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.afgui.scenarios.ScenarioHelper;
import org.kalypso.contribs.eclipse.core.resources.ProjectTemplate;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.contribs.eclipse.jface.wizard.ProjectTemplatePage;
import org.kalypso.core.status.StatusDialog;
import org.kalypso.module.INewProjectHandler;
import org.kalypso.module.welcome.INewProjectWizard;

import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.cases.ScenarioHandlingProjectNature;

/**
 * Basic wizard implementation for the various workflow/scenario based projects.<br>
 * Normally, only the location of the project-template (-zip) should be enough.<br>
 * 
 * @author Gernot Belger
 */
public class NewProjectWizard extends BasicNewProjectResourceWizard implements INewProjectWizard, INewProjectHandler
{
  protected static final String PDE_NATURE_ID = "org.eclipse.pde.PluginNature"; //$NON-NLS-1$

  private final IPageChangingListener m_pageChangeingListener = new IPageChangingListener()
  {
    @Override
    public void handlePageChanging( final PageChangingEvent event )
    {
      doHandlePageChangeing( event );
    }
  };

  private final ProjectTemplatePage m_templateProjectPage;

  protected boolean m_activateScenario = true;

  private final String m_moduleID;

  /**
   * @param categoryId
   *          If non-<code>null</code>, only project templates with this categoryId are shown.
   */
  public NewProjectWizard( final String categoryId, final boolean showTemplatePage, final String moduleID )
  {
    this( new ProjectTemplatePage( categoryId ), showTemplatePage, moduleID );
  }

  public NewProjectWizard( final ProjectTemplatePage page, final boolean showTemplates, final String moduleID )
  {
    m_templateProjectPage = page;
    m_moduleID = moduleID;
    if( showTemplates )
      addPage( m_templateProjectPage );
  }

  @Override
  public void setActivateScenarioOnPerformFinish( final boolean activate )
  {
    m_activateScenario = activate;
  }

  @Override
  public void setContainer( final IWizardContainer wizardContainer )
  {
    final IWizardContainer currentContainer = getContainer();
    if( currentContainer instanceof WizardDialog )
      ((WizardDialog) currentContainer).removePageChangingListener( m_pageChangeingListener );

    super.setContainer( wizardContainer );

    if( wizardContainer instanceof WizardDialog )
      ((WizardDialog) wizardContainer).addPageChangingListener( m_pageChangeingListener );
  }

  @Override
  public void dispose( )
  {
    final IWizardContainer wizardContainer = getContainer();
    if( wizardContainer instanceof WizardDialog )
      ((WizardDialog) wizardContainer).removePageChangingListener( m_pageChangeingListener );

    super.dispose();
  }

  @Override
  public void createPageControls( final Composite pageContainer )
  {
    // Overwritten in order to NOT create the pages during initialization, so we can set
    // the project name later before the next page is created, this works however only once :-(

    // HACK: special case: the resource page: will lead to NPE if not created
    final IWizardPage page = getPage( "basicReferenceProjectPage" ); //$NON-NLS-1$
    if( page != null )
      page.createControl( pageContainer );
  }

  protected void doHandlePageChangeing( final PageChangingEvent event )
  {
    if( event.getCurrentPage() == m_templateProjectPage )
    {
      final ProjectTemplate demoProject = m_templateProjectPage.getSelectedProject();
      // TODO: Does work only the first time :-( ,see above createPageControls

      final WizardNewProjectCreationPage createProjectPage = (WizardNewProjectCreationPage) getPage( "basicNewProjectPage" ); //$NON-NLS-1$
      createProjectPage.setInitialProjectName( demoProject.getProjectName() );
    }
  }

  @Override
  /*
   * This method was overridden in order to get rid of the 'select dependent projects' page from the
   * BasicNewProjectResourceWizard.
   */
  public IWizardPage getNextPage( final IWizardPage page )
  {
    // HACK: to do so, we just skip this particular page
    // Unfortunately we cannot just override 'addPages' and do not add the second page,
    // because the BasicNewProjectResourceWizard relies on the second page to exist.
    final IWizardPage nextPage = super.getNextPage( page );
    if( nextPage instanceof WizardNewProjectReferencePage )
      return super.getNextPage( nextPage );

    return nextPage;
  }

  @Override
  public IWizardPage getPreviousPage( final IWizardPage page )
  {
    // HACK: see get next page
    final IWizardPage previousPage = super.getPreviousPage( page );
    if( previousPage instanceof WizardNewProjectReferencePage )
      return super.getPreviousPage( previousPage );

    return previousPage;
  }

  @Override
  public boolean performFinish( )
  {
    final boolean result = super.performFinish();

    if( !result )
      return false;

    final IProject project = getNewProject();
    final ProjectTemplate selectedProject = m_templateProjectPage.getSelectedProject();
    if( selectedProject == null )
    {
      try
      {
        MessageDialog.openError( getShell(), getWindowTitle(), "No project template available" ); //$NON-NLS-1$
        project.delete( true, new NullProgressMonitor() );
        return false;
      }
      catch( final CoreException e )
      {
        final IStatus status = e.getStatus();
        if( !status.isOK() )
          new StatusDialog( getShell(), status, getWindowTitle() ).open();
        return false;
      }
    }

    final NewProjectData data = new NewProjectData( this, selectedProject, project, m_moduleID );
    final WorkspaceModifyOperation operation = new UnpackProjectTemplateOperation( data );

    final IStatus resultStatus = RunnableContextHelper.execute( getContainer(), true, true, operation );
    KalypsoAFGUIFrameworkPlugin.getDefault().getLog().log( resultStatus );
    // REMARK: we explicitely use != to compare with OK_STATUS: if the status isOK, but
    // not THE OK_STATUS we whish to show an status dialog (with the ok message).
    if( resultStatus != Status.OK_STATUS )
    {
      final StatusDialog statusDialog = new StatusDialog( getShell(), resultStatus, getWindowTitle() );
      // statusDialog.setShowAsTree( true );
      statusDialog.open();
    }

    // REMARK: we always return here, because the BasicNewProjectWizard does not allow to create a project twice
    // So the wizard must be closed now
    return true;
  }

  /**
   * Will be called after the project has been created. Overwrite to additionally change the project.<br>
   * Does nothing by default.
   */
  @SuppressWarnings("unused")
  @Override
  public IStatus postCreateProject( final IProject project, final ProjectTemplate template, final IProgressMonitor monitor ) throws CoreException
  {
    monitor.done();
    return Status.OK_STATUS;
  }

  /**
   * Opens the project after it has been created. By default, if the project is scenario based, the Base-Scenario is
   * opened.
   */
  @Override
  public void openProject( final IProject project ) throws CoreException
  {
    /* Also activate new project */
    final ScenarioHandlingProjectNature nature = ScenarioHandlingProjectNature.toThisNature( project );
    if( nature == null )
      return;

    final List<IScenario> cases = nature.getCaseManager().getCases();
    if( cases.size() == 0 )
      return;

    final IScenario caze = cases.get( 0 );

    if( m_activateScenario )
    {
      new UIJob( StringUtils.EMPTY )
      {
        @Override
        public IStatus runInUIThread( final IProgressMonitor monitor )
        {
          ScenarioHelper.activateScenario2( getShell(), caze );
          return Status.OK_STATUS;
        }
      }.schedule();
    }
  }

  @Override
  public boolean disableProjectCreationUI( )
  {
    return false;
  }

  protected ProjectTemplatePage getTemplatePage( )
  {
    return m_templateProjectPage;
  }

  protected String getModuleID( )
  {
    return m_moduleID;
  }
}