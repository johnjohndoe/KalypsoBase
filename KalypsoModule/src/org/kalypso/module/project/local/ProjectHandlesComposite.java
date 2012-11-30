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
package org.kalypso.module.project.local;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.jface.action.ActionHyperlink;
import org.kalypso.module.project.IProjectHandle;
import org.kalypso.module.project.internal.ProjectHandleComparator;

/**
 * Composite for rendering and handling remote and local projects
 * 
 * @author Dirk Kuch
 */
public class ProjectHandlesComposite extends Composite
{
  private final IProjectHandlesChangedListener m_itemListener = new IProjectHandlesChangedListener()
  {
    @Override
    public void itemsChanged( )
    {
      handleItemsChanged();
    }
  };

  private final FormToolkit m_toolkit;

  private Composite m_body = null;

  private final UIJob m_updateJob = new UIJob( "" ) //$NON-NLS-1$
  {
    @Override
    public IStatus runInUIThread( final IProgressMonitor monitor )
    {
      updateControl();

      return Status.OK_STATUS;
    }
  };

  private IProjectHandleProvider m_model;

  private IProjectHandleFilter m_filter;

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
  public ProjectHandlesComposite( final Composite parent, final FormToolkit toolkit )
  {
    super( parent, SWT.NONE );

    m_toolkit = toolkit;

    m_toolkit.adapt( this );
    setLayout( new FillLayout() );

    addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        dispose();
      }
    } );

    updateControl();
  }

  public void setFilter( final IProjectHandleFilter filter )
  {
    m_filter = filter;

    updateControl();
  }

  public void setModel( final IProjectHandleProvider model )
  {
    synchronized( this )
    {
      if( m_model != null )
        m_model.removeProviderChangedListener( m_itemListener );

      m_model = model;

      if( m_model != null )
        m_model.addProviderChangedListener( m_itemListener );
    }

    updateControl();
  }

  protected void handleItemsChanged( )
  {
    m_updateJob.schedule( 250 );
  }

  /**
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose( )
  {
    super.dispose();

    if( m_model != null )
      m_model.removeProviderChangedListener( m_itemListener );
  }

  protected void updateControl( )
  {
    if( isDisposed() )
      return;

    if( m_body != null && !m_body.isDisposed() )
    {
      m_body.dispose();
      m_body = null;
    }

    m_body = m_toolkit.createComposite( this );
    final GridLayout bodyLayout = new GridLayout( 0, false );
    m_body.setLayout( bodyLayout );

    final IProjectHandle[] projects = getProjectItems();

    for( final IProjectHandle project : projects )
    {
      if( select( project ) )
      {
        final IProjectOpenAction mainAction = (IProjectOpenAction) project.getAdapter( IProjectOpenAction.class );
        final IAction[] actions = project.getProjectActions();
        final String description = getDescription( project );

        // FIXME: assumes, that all projects have the same number of actions
        if( bodyLayout.numColumns == 0 )
          bodyLayout.numColumns = actions.length + 1;

        renderProject( m_body, mainAction, actions, project.getName(), description );
      }

    }

    layout();
  }

  private boolean select( final IProjectHandle project )
  {
    if( Objects.isNull( m_filter ) )
      return true;

    return m_filter.select( project );
  }

  private IProjectHandle[] getProjectItems( )
  {
    if( m_model == null )
      return new IProjectHandle[0];

    final IProjectHandle[] projects = m_model.getProjects();
    Arrays.sort( projects, new ProjectHandleComparator() );
    return projects;
  }

  private void renderProject( final Composite body, final IAction mainAction, final IAction[] actions, final String name, final String description )
  {
    final Control mainControl = renderAction( body, mainAction, name );
    mainControl.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    for( final IAction action : actions )
    {
      final Control control = renderAction( body, action, name );
      control.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false ) );
    }

// renderAction( project.getOpenAction() );
// /* first row - project actions */
// handler.getEditAction().render( body, m_toolkit );
// handler.getDeleteAction().render( body, m_toolkit );
// handler.getDatabaseAction().render( body, m_toolkit );
// handler.getExportAction().render( body, m_toolkit );

    /* second row - enshorted project description */
    if( description != null )
    {
      // TODO: ugly: the label should wrap if too big; 50 chars is just arbitrary...
      final String shortDescription = StringUtils.abbreviate( description, 50 );
      final String msg = String.format( "     %s", shortDescription ); //$NON-NLS-1$
      final Label descriptionLabel = m_toolkit.createLabel( body, msg );
      descriptionLabel.setToolTipText( description );

      final int numColumns = ((GridLayout) body.getLayout()).numColumns;
      descriptionLabel.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false, numColumns, 0 ) ); //$NON-NLS-1$
    }
  }

  private Control renderAction( final Composite parent, final IAction action, final String name )
  {
    if( action == null )
    {
      return m_toolkit.createLabel( parent, name );
    }
    else
      return ActionHyperlink.createHyperlink( m_toolkit, parent, SWT.NONE, action );
  }

  private String getDescription( final IProjectHandle project )
  {
    final String description = project.getDescription();

    if( StringUtils.isBlank( description ) )
      return null;

    if( description.trim().equalsIgnoreCase( project.getName().trim() ) )
      return null;

    return description;
  }
}
