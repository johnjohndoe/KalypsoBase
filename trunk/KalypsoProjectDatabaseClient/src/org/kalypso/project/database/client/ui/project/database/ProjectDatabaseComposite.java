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
package org.kalypso.project.database.client.ui.project.database;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.project.database.client.KalypsoProjectDatabaseClient;
import org.kalypso.project.database.client.core.base.handlers.IProjectUiHandler;
import org.kalypso.project.database.client.core.base.handlers.ProjectUIHandlerFabrication;
import org.kalypso.project.database.client.core.model.interfaces.IProjectDatabaseModel;
import org.kalypso.project.database.client.extension.IKalypsoModule;
import org.kalypso.project.database.client.extension.database.IKalypsoModuleDatabaseSettings;
import org.kalypso.project.database.client.extension.database.IProjectDatabaseUiLocker;
import org.kalypso.project.database.client.extension.database.handlers.IProjectHandler;
import org.kalypso.project.database.common.interfaces.IProjectDatabaseListener;

/**
 * Composite for rendering and handling remote and local projects
 * 
 * @author Dirk Kuch
 */
public class ProjectDatabaseComposite extends Composite implements IProjectDatabaseListener, IPreferenceChangeListener, IProjectDatabaseUiLocker
{
  private final FormToolkit m_toolkit;

  private Composite m_body = null;

  protected UIJob m_updateJob = null;

  private boolean m_updateLock = false;

  private final IKalypsoModule m_module;

  private final IProjectDatabaseModel m_model;

  /**
   * @param parent
   *          composite
   * @param localProjectNatures
   *          handle project with these project nature id's
   * @param remoteProjectTypes
   *          handle remote projects with these type id's
   * @param isExpert
   *          show expert debug informations?
   */
  public ProjectDatabaseComposite( final IKalypsoModule module, final Composite parent, final FormToolkit toolkit )
  {
    super( parent, SWT.NONE );
    m_toolkit = toolkit;
    m_module = module;

    m_model = KalypsoProjectDatabaseClient.getModel();
    m_model.addListener( this );

    update();
  }

  /**
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose( )
  {
    super.dispose();

    m_model.removeListener( this );
  }

  /**
   * @see org.eclipse.swt.widgets.Control#update()
   */
  @Override
  public final void update( )
  {
    if( m_updateLock )
      return;

    if( this.isDisposed() )
      return;

    if( m_body != null && !m_body.isDisposed() )
    {
      m_body.dispose();
      m_body = null;
    }

    m_body = m_toolkit.createComposite( this );
    m_body.setLayout( new GridLayout( 6, false ) );
    m_body.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );

    final IKalypsoModuleDatabaseSettings settings = m_module.getDatabaseSettings();

    final IProjectHandler[] projects = m_model.getProjects( settings.getFilter() );
    for( final IProjectHandler project : projects )
      renderProject( m_body, project );

    m_toolkit.adapt( this );
    this.layout();
  }

  private void renderProject( final Composite body, final IProjectHandler project )
  {
    final IProjectUiHandler handler = ProjectUIHandlerFabrication.getHandler( project, m_module, this );

    /* first row - project actions */
    handler.getOpenAction().render( body, m_toolkit );
    handler.getInfoAction().render( body, m_toolkit );
    handler.getEditAction().render( body, m_toolkit );
    handler.getDeleteAction().render( body, m_toolkit );
    handler.getDatabaseAction().render( body, m_toolkit );
    handler.getExportAction().render( body, m_toolkit );

    /* second row - enshorted project description */
    String description = project.getDescription();
    if( description != null && !description.isEmpty() )
      if( !description.trim().equalsIgnoreCase( project.getName().trim() ) )
      {
        if( description.length() > 50 )
        {
          description = description.substring( 0, 50 ) + "...";
        }

        m_toolkit.createLabel( body, String.format( "     %s", description ) ).setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false, 6, 0 ) );
      }

  }

  /**
   * @see org.kalypso.project.database.client.core.interfaces.IProjectDatabaseListener#projectModelChanged()
   */
  @Override
  public void projectModelChanged( )
  {
    updateUI();
  }

  /**
   * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener#preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent)
   */
  @Override
  public void preferenceChange( final PreferenceChangeEvent event )
  {
    updateUI();
  }

  private void updateUI( )
  {
    if( m_updateLock )
      return;

    if( m_updateJob == null )
    {
      m_updateJob = new UIJob( "" ) //$NON-NLS-1$
      {
        @Override
        public IStatus runInUIThread( final IProgressMonitor monitor )
        {
          update();
          m_updateJob = null;

          return Status.OK_STATUS;
        }
      };

      m_updateJob.schedule( 750 );
    }
  }

  /**
   * @see org.kalypso.project.database.client.ui.project.list.IProjectDatabaseUIHandler#acquireUiUpdateLock()
   */
  @Override
  public void acquireUiUpdateLock( )
  {
    m_updateLock = true;
  }

  /**
   * @see org.kalypso.project.database.client.ui.project.list.IProjectDatabaseUIHandler#releaseUiUpdateLock()
   */
  @Override
  public void releaseUiUpdateLock( )
  {
    m_updateLock = false;
    updateUI();
  }
}
