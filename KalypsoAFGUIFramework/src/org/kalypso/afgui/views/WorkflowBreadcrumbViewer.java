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
package org.kalypso.afgui.views;

import nu.bibi.breadcrumb.BreadcrumbViewer;
import nu.bibi.breadcrumb.IMenuSelectionListener;
import nu.bibi.breadcrumb.MenuSelectionEvent;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.afgui.scenarios.ScenarioHelper;
import org.kalypso.core.status.StatusDialog;

import de.renew.workflow.connector.cases.IScenario;

/**
 * @author Gernot Belger
 */
public class WorkflowBreadcrumbViewer
{
  private BreadcrumbViewer m_viewer;

  public WorkflowBreadcrumbViewer( final Composite parent )
  {
    createControl( parent );
  }

  private void createControl( final Composite parent )
  {
    m_viewer = new BreadcrumbViewer( parent, SWT.NONE )
    {
      @Override
      protected void configureDropDownViewer( final TreeViewer viewer, final Object selection )
      {
        viewer.setContentProvider( getContentProvider() );

        // REMARK: cannot reuse label provider of breadcrumb, because it gets disposed if drop-down is closed
        final WorkflowBreadCrumbLabelProvider labelProvider = new WorkflowBreadCrumbLabelProvider();

        viewer.setLabelProvider( labelProvider );
        setToolTipLabelProvider( labelProvider );

        // setComparator(FileViewerComparator.DEFAULT);
        viewer.setFilters( getFilters() );
      }
    };

    final BreadcrumbViewer viewer = m_viewer;

    final WorkflowBreadCrumbLabelProvider labelProvider = new WorkflowBreadCrumbLabelProvider();
    viewer.setLabelProvider( labelProvider );
    viewer.setToolTipLabelProvider( labelProvider );

    viewer.setContentProvider( new WorkflowBreadcrumbContentProvider() );
    viewer.setInput( ResourcesPlugin.getWorkspace().getRoot() );

    viewer.addFilter( new ScenarioViewerFilter() );

    viewer.addMenuSelectionListener( new IMenuSelectionListener()
    {
      @Override
      public void menuSelect( final MenuSelectionEvent event )
      {
        final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        handleMenuSelection( selection );
      }
    } );

    // display drop-down menu
    viewer.addDoubleClickListener( new IDoubleClickListener()
    {
      @Override
      public void doubleClick( final DoubleClickEvent event )
      {
        // get selection
        final Object element = viewer.getSelection().getFirstElement();
        if( element == null )
        {
          return;
        }

        // get parent
        final ITreeContentProvider contentProvider = viewer.getContentProvider();
        final Object parentElement = contentProvider.getParent( element );

        // open
        viewer.openDropDownMenu( parentElement );
      }
    } );
  }

  protected void handleMenuSelection( final IStructuredSelection selection )
  {
    if( selection.isEmpty() )
    {
      m_viewer.setFocus();
      return;
    }

    try
    {
      final Object firstElement = selection.getFirstElement();

      if( firstElement instanceof IScenario )
      {
        // REMARK: without the next line, open the model dialog in stopTask (during activate Scenario)
        // will get a widget-disposed error
        m_viewer.setFocus();
        ScenarioHelper.activateScenario( (IScenario) firstElement );
      }
      else if( firstElement instanceof String )
      {
        // REMARK: without the next line, open the model dialog in stopTask (during activate Scenario)
        // will get a widget-disposed error
        m_viewer.setFocus();
        ScenarioHelper.activateScenario( null );
      }
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      StatusDialog.open( m_viewer.getControl().getShell(), e.getStatus(), "Activate Scenario" );
    }

    return;
  }

  public Control getControl( )
  {
    return m_viewer.getControl();
  }

  public void setScenario( final IScenario scenario )
  {
    if( scenario == null )
      m_viewer.setInput( Messages.getString( "org.kalypso.afgui.views.WorkflowView.0" ) ); //$NON-NLS-1$
    else
      m_viewer.setInput( scenario );

    m_viewer.refresh();
  }
}