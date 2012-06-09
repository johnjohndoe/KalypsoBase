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
package org.kalypso.afgui.internal.ui.workflow;


import nu.bibi.breadcrumb.BreadcrumbViewer;
import nu.bibi.breadcrumb.IMenuSelectionListener;
import nu.bibi.breadcrumb.MenuSelectionEvent;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.services.IEvaluationService;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.afgui.scenarios.ScenarioHelper;
import org.kalypso.afgui.views.ScenarioViewerFilter;

import de.renew.workflow.connector.cases.IScenario;

/**
 * @author Gernot Belger
 */
public class WorkflowBreadcrumbViewer extends BreadcrumbViewer
{
  private final MenuManager m_itemMenuManager;

  private final WorkflowBreadcrumbItemSourceProvider m_sourceProvider = new WorkflowBreadcrumbItemSourceProvider();

  public WorkflowBreadcrumbViewer( final Composite parent )
  {
    super( parent, SWT.NONE );

    setLabelProvider( new WorkflowBreadCrumbLabelProvider() );
    setToolTipLabelProvider( new WorkflowBreadCrumbTooltipProvider() );

    setContentProvider( new WorkflowBreadcrumbContentProvider() );
    setInput( ResourcesPlugin.getWorkspace().getRoot() );

    addFilter( new ScenarioViewerFilter() );

    m_itemMenuManager = new MenuManager();
    m_itemMenuManager.add( new Separator( "additions" ) );

    final IWorkbench workbench = PlatformUI.getWorkbench();

    final IMenuService menuService = (IMenuService) workbench.getService( IMenuService.class );
    menuService.populateContributionManager( m_itemMenuManager, "popup:org.kalypso.afgui.breadcrumbs" );

    menuService.addSourceProvider( m_sourceProvider );
    ((IEvaluationService) workbench.getService( IEvaluationService.class )).addSourceProvider( m_sourceProvider );
    // ((IContextService) workbench.getService( IContextService.class )).addSourceProvider( m_sourceProvider );
    ((IHandlerService) workbench.getService( IHandlerService.class )).addSourceProvider( m_sourceProvider );

    hookListeners();
  }

  void dispose( )
  {
    final IWorkbench workbench = PlatformUI.getWorkbench();

    final IMenuService menuService = (IMenuService) workbench.getService( IMenuService.class );
    menuService.releaseContributions( m_itemMenuManager );

    menuService.removeSourceProvider( m_sourceProvider );
    ((IEvaluationService) workbench.getService( IEvaluationService.class )).removeSourceProvider( m_sourceProvider );
    // ((IContextService) workbench.getService( IContextService.class )).removeSourceProvider( m_sourceProvider );
    ((IHandlerService) workbench.getService( IHandlerService.class )).removeSourceProvider( m_sourceProvider );

    m_sourceProvider.dispose();
    m_itemMenuManager.dispose();
  }

  @Override
  protected void configureDropDownViewer( final TreeViewer viewer, final Object selection )
  {
    viewer.setContentProvider( getContentProvider() );

    // REMARK: cannot reuse label provider of breadcrumb, because it gets disposed if drop-down is closed
    viewer.setLabelProvider( new WorkflowBreadCrumbLabelProvider() );
    setToolTipLabelProvider( new WorkflowBreadCrumbTooltipProvider() );

    viewer.setFilters( getFilters() );

    viewer.getTree().addMenuDetectListener( new MenuDetectListener()
    {
      @Override
      public void menuDetected( final MenuDetectEvent e )
      {
        final Object menuSelection = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
        if( menuSelection instanceof IProject )
        {
          // REMARK / BUGFIX: see comment in DeleteBreadcrumbsHandler
          handleItemMenuDetected( viewer.getControl(), e, null );
        }
        else
          handleItemMenuDetected( viewer.getControl(), e, menuSelection );
      }
    } );
  }

  private void hookListeners( )
  {
    addMenuSelectionListener( new IMenuSelectionListener()
    {
      @Override
      public void menuSelect( final MenuSelectionEvent event )
      {
        final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        handleMenuSelection( selection );
      }
    } );

    addMenuDetectListener( new MenuDetectListener()
    {
      @Override
      public void menuDetected( final MenuDetectEvent e )
      {
        final Object selectedElement = getSelection().getFirstElement();
        handleItemMenuDetected( getControl(), e, selectedElement );
      }
    } );

    // display drop-down menu
    addDoubleClickListener( new IDoubleClickListener()
    {
      @Override
      public void doubleClick( final DoubleClickEvent event )
      {
        // get selection
        final Object element = getSelection().getFirstElement();
        if( element == null )
        {
          return;
        }

        // get parent
        final ITreeContentProvider contentProvider = getContentProvider();
        final Object parentElement = contentProvider.getParent( element );

        // open
        openDropDownMenu( parentElement );
      }
    } );

    getControl().addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        dispose();
      }
    } );
  }

  protected void handleItemMenuDetected( final Control control, final MenuDetectEvent e, final Object selectedElement )
  {
    final Menu contextMenu = m_itemMenuManager.createContextMenu( control );

    m_sourceProvider.setSelectedItem( selectedElement );

    contextMenu.setLocation( e.x, e.y );
    contextMenu.setVisible( true );
  }

  protected void handleMenuSelection( final IStructuredSelection selection )
  {
    // REMARK: without the next line, open the model dialog in stopTask (during activate Scenario)
    // will get a widget-disposed error
    setFocus();

    if( selection.isEmpty() )
      return;

    final Shell shell = getControl().getShell();

    final Object firstElement = selection.getFirstElement();

    if( firstElement instanceof IScenario )
    {
      ScenarioHelper.activateScenario2( shell, (IScenario) firstElement );
    }
    else if( firstElement instanceof String )
    {
      ScenarioHelper.activateScenario2( shell, null );
    }

    return;
  }

  public void setScenario( final IScenario scenario )
  {
    if( scenario == null )
      setInput( Messages.getString( "org.kalypso.afgui.views.WorkflowView.0" ) ); //$NON-NLS-1$
    else
      setInput( scenario );

    refresh();
  }
}
