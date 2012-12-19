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
package org.kalypso.module.welcome;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.kalypso.contribs.eclipse.jface.action.ActionHyperlink;
import org.kalypso.contribs.eclipse.swt.browser.OpenExternalLocationAdapter;
import org.kalypso.contribs.eclipse.ui.controls.ScrolledSection;
import org.kalypso.module.IKalypsoModule;
import org.kalypso.module.internal.i18n.Messages;
import org.kalypso.module.project.local.ProjectHandlesComposite;
import org.kalypso.module.utils.ModuleHandleFilter;

/**
 * @author Gernot Belger
 */
public class ProjectListViewer
{
  private final IKalypsoModule m_module;

  private final FormToolkit m_toolkit;

  public ProjectListViewer( final IKalypsoModule module, final FormToolkit toolkit )
  {
    m_module = module;
    m_toolkit = toolkit;
  }

  public void createProjectList( final Composite parent )
  {
    final ScrolledSection sectionProjects = new ScrolledSection( parent, m_toolkit, ExpandableComposite.TITLE_BAR, true );
    final Composite bodyProjects = sectionProjects.setup( Messages.getString( "org.kalypso.module.welcome.ProjectListViewer.0" ), new GridData( GridData.FILL, GridData.FILL, true, true ), new GridData( GridData.FILL, GridData.FILL, true, true ) ); //$NON-NLS-1$
    GridLayoutFactory.fillDefaults().numColumns( 2 ).equalWidth( true ).applyTo( bodyProjects );
    bodyProjects.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );

    final ProjectHandlesComposite projects = new ProjectHandlesComposite( bodyProjects, m_toolkit );
    projects.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true, 2, 0 ) );
    projects.setFilter( new ModuleHandleFilter( m_module.getId() ) );
    projects.setModel( m_module.getProjectProvider() );

    renderProjectActions( bodyProjects );
  }

  public void createProjectInfo( final Composite parent )
  {
    final Browser browser = new Browser( parent, SWT.NULL );
    browser.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );

    try
    {
      final URL url = m_module.getInfoURL();
      if( url == null )
        return;

      final String projectInfoLocation = url.toExternalForm();
      browser.setUrl( projectInfoLocation );
      browser.addLocationListener( new OpenExternalLocationAdapter( true ) );
    }
    catch( final Exception e )
    {
      final StringWriter stringWriter = new StringWriter();
      stringWriter.append( e.toString() );
      stringWriter.append( '\n' );
      e.printStackTrace( new PrintWriter( stringWriter, true ) );
      browser.setText( stringWriter.toString() );
    }
  }

  private void renderProjectActions( final Composite bodyProjects )
  {
    final IAction[] projectActions = findProjectActions();
    for( final IAction action : projectActions )
    {
      if( action == null )
        new Label( bodyProjects, SWT.NONE );
      else
      {
        final ImageHyperlink actionLink = ActionHyperlink.createHyperlink( m_toolkit, bodyProjects, SWT.NONE, action );
        actionLink.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
      }
    }
  }

  private IAction[] findProjectActions( )
  {
    final IAction[] projectActions = m_module.getProjectActions();

    // FIXME: get from project model?

// if( ProjectDatabaseServerUtils.handleRemoteProject() )
// {
// final ProjectDatabaseServerStatusAction serverStatusAction = new ProjectDatabaseServerStatusAction();
// return (IAction[]) ArrayUtils.add( projectActions, serverStatusAction );
// }

    return projectActions;
  }

}
