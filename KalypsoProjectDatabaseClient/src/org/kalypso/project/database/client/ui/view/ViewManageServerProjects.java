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
package org.kalypso.project.database.client.ui.view;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.contribs.eclipse.ui.forms.ToolkitUtils;
import org.kalypso.project.database.client.KalypsoProjectDatabaseClient;
import org.kalypso.project.database.client.i18n.Messages;
import org.kalypso.project.database.client.ui.management.ManageRemoteProjects;
import org.kalypso.project.database.sei.IProjectDatabase;

/**
 * @author Dirk Kuch
 */
public class ViewManageServerProjects extends ViewPart
{
  protected String m_selectedType = null;

  public static final String ID = "org.kalypso.project.database.client.ui.view.ViewManageServerProjects"; //$NON-NLS-1$

  private Composite m_parent;

  private Composite m_body;

  private ManageRemoteProjects m_manager;

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPartControl( final Composite parent )
  {
    m_parent = parent;
    update();
  }

  protected void update( )
  {
    if( m_parent.isDisposed() )
      return;

    if( m_body != null )
    {
      if( !m_body.isDisposed() )
      {
        m_body.dispose();
      }
    }

    final FormToolkit toolkit = ToolkitUtils.createToolkit( m_body );

    m_body = toolkit.createComposite( m_parent );
    m_body.setLayout( new GridLayout() );
    m_body.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );

    final Group grModelType = new Group( m_body, SWT.NONE );
    grModelType.setText( Messages.getString("org.kalypso.project.database.client.ui.view.ViewManageServerProjects.1") ); //$NON-NLS-1$
    grModelType.setLayout( new GridLayout() );
    grModelType.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
    toolkit.adapt( grModelType );

    final ComboViewer viewerType = new ComboViewer( grModelType, SWT.BORDER | SWT.READ_ONLY );
    viewerType.getCombo().setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
    viewerType.setContentProvider( new ArrayContentProvider() );
    viewerType.setLabelProvider( new LabelProvider() );

    final IProjectDatabase service = KalypsoProjectDatabaseClient.getService();
    if( service == null )
    {
      toolkit.createLabel( m_body, Messages.getString("org.kalypso.project.database.client.ui.view.ViewManageServerProjects.2") ); //$NON-NLS-1$
      return;
    }

    final String[] types = service.getProjectTypes();

    viewerType.setInput( types );

    if( m_selectedType != null )
    {
      viewerType.setSelection( new StructuredSelection( m_selectedType ) );

      final Group grDetails = new Group( m_body, SWT.NONE );
      grDetails.setText( Messages.getString("org.kalypso.project.database.client.ui.view.ViewManageServerProjects.3") ); //$NON-NLS-1$
      grDetails.setLayout( new GridLayout() );
      grDetails.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
      toolkit.adapt( grDetails );

      if( m_manager != null )
      {
        m_manager.dispose();
      }

      m_manager = new ManageRemoteProjects( toolkit, grDetails, m_selectedType );
      m_manager.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
    }

    viewerType.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final IStructuredSelection selection = (IStructuredSelection) viewerType.getSelection();
        final Object element = selection.getFirstElement();
        if( element instanceof String )
        {
          m_selectedType = (String) element;

          new UIJob( "" ) //$NON-NLS-1$
          {
            @Override
            public IStatus runInUIThread( final IProgressMonitor monitor )
            {
              update();
              return Status.OK_STATUS;
            }
          }.schedule();
        }
      }
    } );

    m_body.layout();
    m_parent.layout();
  }

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
   */
  @Override
  public void setFocus( )
  {

  }

}
