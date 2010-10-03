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

import java.awt.Point;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.kalypso.afgui.wizards.INewProjectWizard;
import org.kalypso.contribs.eclipse.jface.action.ActionHyperlink;
import org.kalypso.contribs.eclipse.swt.canvas.DefaultContentArea;
import org.kalypso.contribs.eclipse.swt.canvas.ImageCanvas2;
import org.kalypso.contribs.eclipse.ui.controls.ScrolledSection;
import org.kalypso.project.database.client.core.utils.ProjectDatabaseServerUtils;
import org.kalypso.project.database.client.extension.IKalypsoModule;
import org.kalypso.project.database.client.extension.database.IKalypsoModuleDatabaseSettings;
import org.kalypso.project.database.client.extension.pages.module.IKalypsoModulePage;
import org.kalypso.project.database.client.i18n.Messages;
import org.kalypso.project.database.client.ui.MyColors;
import org.kalypso.project.database.client.ui.MyFonts;
import org.kalypso.project.database.client.ui.project.database.ProjectDatabaseComposite;
import org.kalypso.project.database.client.ui.project.status.ProjectDatabaseServerStatusAction;

/**
 * FIXME: this does not belong into KalypsoBase.<br/>
 * We need an (extension?) mechanism to inject the additional functionality of the project database into the welcome
 * page: two ideas/strategies to do this: 1) use actions instead of fixed wizard 2) create an extension point mechanism
 * that provides the IProjectHandles (so only the Project database may provide remote projects)
 * 
 * @author Dirk Kuch
 */
public class ModulePageComposite extends Composite
{
  // FIXME: never disposed!
  private static final Color COLOR_BOX = new Color( null, 0x7f, 0xb2, 0x99 );

  private final FormToolkit m_toolkit;

  private final IKalypsoModule m_module;

  public ModulePageComposite( final IKalypsoModule module, final FormToolkit toolkit, final Composite parent, final int style )
  {
    super( parent, style );
    m_module = module;
    m_toolkit = toolkit;

    final GridLayout layout = new GridLayout( 2, false );
    layout.horizontalSpacing = 100;
    layout.verticalSpacing = 25;
    layout.marginWidth = 75;

    this.setLayout( layout );

    update();
  }

  @Override
  public void update( )
  {
    final IKalypsoModulePage modulePage = m_module.getModulePage();

    /* header */
    // icon / button
    final ImageCanvas2 headerCanvas = new ImageCanvas2( this, SWT.NO_REDRAW_RESIZE );
    final GridData headerIconData = new GridData( GridData.FILL, GridData.FILL, true, false, 2, 0 );
    headerIconData.heightHint = headerIconData.minimumHeight = 110;
    headerCanvas.setLayoutData( headerIconData );

    final DefaultContentArea headerContent = new DefaultContentArea()
    {
      @Override
      public Point getContentAreaAnchorPoint( )
      {
        return new Point( 5, 40 );
      }
    };

    headerContent.setText( modulePage.getHeader(), MyFonts.WELCOME_PAGE_HEADING, MyColors.COLOR_WELCOME_PAGE_HEADING, SWT.RIGHT );
    headerCanvas.addContentArea( headerContent );

    /* left pane */
    final Composite leftPane = m_toolkit.createComposite( this, SWT.NONE );
    leftPane.setLayout( new GridLayout() );
    final GridData leftGridData = new GridData( GridData.FILL, GridData.FILL, false, true );
    leftGridData.widthHint = leftGridData.minimumWidth = 400;
    leftPane.setLayoutData( leftGridData );
    leftPane.setBackground( COLOR_BOX );

    /* right pane */
    final Composite rightPane = m_toolkit.createComposite( this, SWT.NONE );
    rightPane.setLayout( new GridLayout() );
    rightPane.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
    rightPane.setBackground( COLOR_BOX );

    renderListOfProjects( leftPane );
    renderProjectInfo( rightPane );
  }

  private void renderProjectInfo( final Composite body )
  {
    final IKalypsoModulePage modulePage = m_module.getModulePage();

    final Browser browser = new Browser( body, SWT.NULL );
    browser.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );

    try
    {
      final URL url = modulePage.getInfoURL();
      if( url == null )
        return;

      final String projectInfoLocation = url.toExternalForm();
      browser.setUrl( projectInfoLocation );
      browser.addLocationListener( new OpenExternalLocationAdapter( true ) );
    }
    catch( final Exception e )
    {
      // FIXME: error handling! show message
      e.printStackTrace();
    }
  }

  private void renderListOfProjects( final Composite body )
  {
    final IKalypsoModulePage modulePage = m_module.getModulePage();

    // list of projects
    // FIXME: this scrolling does nothing, as the whole page lives in a scrolled composite
    final ScrolledSection sectionProjects = new ScrolledSection( body, m_toolkit, ExpandableComposite.TITLE_BAR, true );
    final Composite bodyProjects = sectionProjects.setup( Messages.getString("org.kalypso.project.database.client.ui.composites.ModulePageComposite.0"), new GridData( GridData.FILL, GridData.FILL, true, true ), new GridData( GridData.FILL, GridData.FILL, true, true ) ); //$NON-NLS-1$
    final GridLayout layout = new GridLayout( 2, true );
    layout.verticalSpacing = layout.marginWidth = 0;
    bodyProjects.setLayout( layout );
    bodyProjects.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );

    final ProjectDatabaseComposite projects = new ProjectDatabaseComposite( m_module, bodyProjects, m_toolkit );
    projects.setLayout( new GridLayout() );
    projects.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true, 2, 0 ) );

    final IKalypsoModuleDatabaseSettings settings = m_module.getDatabaseSettings();

    final String commitType = settings.getModuleCommitType();

    // FIXME: not flexible enough, we should directly fetch the actions from the module, so we may have
    // different behavior depending on the module

    final String createProjectLabel = Messages.getString( "org.kalypso.project.database.client.ui.composites.ModulePageComposite.1" ); //$NON-NLS-1$
    final INewProjectWizard createProjectWizard = modulePage.getProjectWizard();
    final CreateProjectAction createProjectAction = new CreateProjectAction( createProjectLabel, commitType, createProjectWizard );
    final ImageHyperlink createProjectLink = ActionHyperlink.createHyperlink( m_toolkit, bodyProjects, SWT.NONE, createProjectAction );
    createProjectLink.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );

    final ImportProjectAction importProjectAction = new ImportProjectAction();
    final ImageHyperlink importProjectLink = ActionHyperlink.createHyperlink( m_toolkit, bodyProjects, SWT.NONE, importProjectAction );
    importProjectLink.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );

    // TODO: we do not need the 'hasXXX' methods if we fetch actions from the module
    if( modulePage.hasDemoProjectWizard() )
    {
      final String demoProjectLabel = Messages.getString( "org.kalypso.project.database.client.ui.composites.ModulePageComposite.2" ); //$NON-NLS-1$

      final INewProjectWizard demoProjectWizard = modulePage.getDemoProjectWizard();
      final CreateProjectAction demoProjectAction = new CreateProjectAction( demoProjectLabel, commitType, demoProjectWizard );
      demoProjectAction.setImageDescriptor( CreateProjectAction.IMG_EXTRACT_DEMO );

      final ImageHyperlink demoHyperlink = ActionHyperlink.createHyperlink( m_toolkit, bodyProjects, SWT.NONE, demoProjectAction );
      demoHyperlink.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
    }
    else
    {
      m_toolkit.createLabel( bodyProjects, "" ); // spacer //$NON-NLS-1$
    }

    if( modulePage.hasImportWizard() )
    {
      final SpecialImportProjectAction specialImportProjectAction = new SpecialImportProjectAction( modulePage );
      final ImageHyperlink specialImportLink = ActionHyperlink.createHyperlink( m_toolkit, bodyProjects, SWT.NULL, specialImportProjectAction );
      specialImportLink.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
    }

    if( ProjectDatabaseServerUtils.handleRemoteProject() )
    {
      final ProjectDatabaseServerStatusAction serverStatusAction = new ProjectDatabaseServerStatusAction();
      final ImageHyperlink serverStatusLink = ActionHyperlink.createHyperlink( m_toolkit, bodyProjects, SWT.RIGHT | SWT.END, serverStatusAction );
      serverStatusLink.setUnderlined( false );
      serverStatusLink.setLayoutData( new GridData( GridData.FILL, GridData.FILL, false, false ) );
    }
  }
}
