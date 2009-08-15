/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ui.editor.mapeditor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.services.IServiceScopes;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.jface.action.ContributionUtils;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.listeners.IMapPanelListener;
import org.kalypso.ogc.gml.map.listeners.MapPanelAdapter;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.outline.GisMapOutlineViewer;
import org.kalypso.ogc.gml.outline.handler.ToggleCompactOutlineHandler;
import org.kalypso.ui.editor.mapeditor.views.StyleEditorViewPart;
import org.kalypso.util.command.JobExclusiveCommandTarget;

/**
 * OutlinePage für das MapView-Template
 *
 * @author Gernot Belger
 */
public class GisMapOutlinePage extends Page implements IContentOutlinePage, IPageBookViewPage, ICommandTarget
{
  private final JobExclusiveCommandTarget m_commandTarget;

  private final GisMapOutlineViewer m_outlineViewer;

  private final IMapPanelListener m_mapPanelListener = new MapPanelAdapter()
  {
    /**
     * @see org.kalypso.ogc.gml.map.MapPanelAdapter#onMapModelChanged(org.kalypso.ogc.gml.map.MapPanel,
     *      org.kalypso.ogc.gml.mapmodel.IMapModell, org.kalypso.ogc.gml.mapmodel.IMapModell)
     */
    @Override
    public void onMapModelChanged( final IMapPanel source, final IMapModell oldModel, final IMapModell newModel )
    {
      handleMapModelChanged( newModel );
    }
  };

  private IMapPanel m_panel = null;

  /**
   * Set of URIs to populate this pages action bars with. The following convention applies:
   * <ul>
   * <li>URIs starting with 'toolbar' are applied to the toolbar manager</li>
   * <li>URIs starting with 'menu' are applied to the menu manager</li>
   * <li>URIs starting with 'popup' are applied to the context menu manager</li>
   * </ul>
   * All other entries are ignored.
   */
  private final Set<String> m_actionURIs = new HashSet<String>();

  /** Menu-Manager for context menu */
  private MenuManager m_popupMgr;

  public GisMapOutlinePage( final JobExclusiveCommandTarget commandTarget )
  {
    m_commandTarget = commandTarget;
    m_outlineViewer = new GisMapOutlineViewer( m_commandTarget, null );
  }

  /**
   * Add a new entry to this pages action bar URIs. See {@link #m_actionURIs}.<br/>
   * Entries that already have been added are ignored.
   */
  public void addActionURI( final String uri )
  {
    m_actionURIs.add( uri );
  }

  /**
   * Removes an entry to this pages action bar URIs. See {@link #m_actionURIs}.<br/>
   * Entries that where not previously added are ignored.
   */
  public void removeActionURI( final String uri )
  {
    m_actionURIs.remove( uri );
  }

  public GisMapOutlineViewer getOutlineViewer( )
  {
    return m_outlineViewer;
  }

  /**
   * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl( final Composite parent )
  {
    if( parent.isDisposed() )
      System.out.println( Messages.get( "org.kalypso.ui.editor.mapeditor.GisMapOutlinePage.0" ) ); //$NON-NLS-1$

    m_outlineViewer.createControl( parent );

    final IPageSite site = getSite();
    final IActionBars actionBars = site.getActionBars();

    // TODO: probably does not work any more...
    actionBars.setGlobalActionHandler( ActionFactory.UNDO.getId(), m_commandTarget.undoAction );
    actionBars.setGlobalActionHandler( ActionFactory.REDO.getId(), m_commandTarget.redoAction );

    m_popupMgr = new MenuManager( "#MapOutlineContextMenu" );

    final Menu menu = m_popupMgr.createContextMenu( m_outlineViewer.getControl() );
    m_outlineViewer.getControl().setMenu( menu );


    // Refresh updateable element later, else they won't find this page
    final UIJob job = new UIJob( "Update outline action bars" )
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        populateActionBars();
        setCompact( true );
        return Status.OK_STATUS;
      }
    };

    job.setSystem( true );
    job.schedule();
  }

  /**
   * Populates this pages action bars with items from the given menu-contributions.
   */
  protected void populateActionBars( )
  {
    releaseActionBars();

    final IPageSite site = getSite();
    if( site == null )
      return;

    final IActionBars actionBars = site.getActionBars();
    final IToolBarManager toolBarManager = actionBars.getToolBarManager();
    final IMenuManager menuManager = actionBars.getMenuManager();

    for( final String uri : m_actionURIs )
    {
      if( uri.startsWith( "toolbar" ) )
        ContributionUtils.populateContributionManager( site, toolBarManager, uri );
      else if( uri.startsWith( "menu" ) )
        ContributionUtils.populateContributionManager( site, menuManager, uri );
      else if( uri.startsWith( "popup" ) )
      {
        if( m_popupMgr != null )
          ContributionUtils.populateContributionManager( site, m_popupMgr, uri );
      }
      else
        System.out.println( String.format( "Unable to add uri '%s' to outline action bars. Unknown prefix.", uri ) );
    }

    actionBars.updateActionBars();
  }

  /**
   * Releases any previously populated action bars of this page.
   */
  private void releaseActionBars( )
  {
    final IPageSite site = getSite();
    if( site == null )
      return;

    final IActionBars actionBars = site.getActionBars();
    final IToolBarManager toolBarManager = actionBars.getToolBarManager();
    final IMenuManager menuManager = actionBars.getMenuManager();
    ContributionUtils.releaseContributions( site, toolBarManager );
    ContributionUtils.releaseContributions( site, menuManager );
    if( m_popupMgr != null )
      ContributionUtils.releaseContributions( site, m_popupMgr );
  }

  /**
   * @see org.eclipse.ui.part.IPage#dispose()
   */
  @Override
  public void dispose( )
  {
    super.dispose();

    releaseActionBars();

    if( m_outlineViewer != null )
      m_outlineViewer.dispose();
  }

  /**
   * @see org.eclipse.ui.part.IPage#getControl()
   */
  @Override
  public Control getControl( )
  {
    return m_outlineViewer.getControl();
  }

  /**
   * @see org.eclipse.ui.part.IPage#setFocus()
   */
  @Override
  public void setFocus( )
  {
    // bei jedem Focus, überprüfe ob outline beim StyleEditor registriert ist.
    // TODO: remove, style editor must pull information instead
    final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    final StyleEditorViewPart part = (StyleEditorViewPart) window.getActivePage().findView( "org.kalypso.ui.editor.mapeditor.views.styleeditor" ); //$NON-NLS-1$

    if( part != null )
      part.setSelectionChangedProvider( m_outlineViewer );
  }

  /**
   * @see org.kalypso.ogc.gml.mapmodel.IMapModellView#getMapModell()
   */
  public IMapPanel getMapPanel( )
  {
    return m_panel;
  }

  /**
   * @see org.kalypso.ogc.gml.mapmodel.IMapModellView#setMapModell(org.kalypso.ogc.gml.mapmodel.IMapModell)
   */
  public void setMapPanel( final IMapPanel panel )
  {
    if( m_panel != null )
      m_panel.removeMapPanelListener( m_mapPanelListener );

    m_panel = panel;

    if( m_panel != null )
    {
      m_panel.addMapPanelListener( m_mapPanelListener );
      m_outlineViewer.setMapModel( m_panel.getMapModell() );
    }
  }

  /**
   * @param command
   * @param runnable
   * @see org.kalypso.util.command.JobExclusiveCommandTarget#postCommand(org.kalypso.commons.command.ICommand,
   *      java.lang.Runnable)
   */
  public void postCommand( final ICommand command, final Runnable runnable )
  {
    m_commandTarget.postCommand( command, runnable );
  }

  protected void handleMapModelChanged( final IMapModell newModel )
  {
    if( m_outlineViewer != null )
      m_outlineViewer.setMapModel( newModel );
  }

  /**
   * @see org.eclipse.ui.part.IPageBookViewPage#init(org.eclipse.ui.part.IPageSite)
   */
  @Override
  public void init( final IPageSite site )
  {
    super.init( site );

    site.setSelectionProvider( m_outlineViewer );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  @Override
  public void addSelectionChangedListener( final ISelectionChangedListener listener )
  {
    getSite().getSelectionProvider().addSelectionChangedListener( listener );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
   */
  @Override
  public ISelection getSelection( )
  {
    return getSite().getSelectionProvider().getSelection();
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  @Override
  public void removeSelectionChangedListener( final ISelectionChangedListener listener )
  {
    getSite().getSelectionProvider().removeSelectionChangedListener( listener );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
   */
  @Override
  public void setSelection( final ISelection selection )
  {
    getSite().getSelectionProvider().getSelection();
  }

  /**
   * @return <code>true</code> if the viewer is compact view state.
   * @see #setCompact(boolean)
   */
  public boolean isCompact( )
  {
    return m_outlineViewer.isCompact();
  }

  public void setCompact( final boolean compact )
  {
    m_outlineViewer.setCompact( compact );

    final IPageSite site = getSite();
    if( site == null )
      return;

    final ICommandService commandService = (ICommandService) site.getService( ICommandService.class );
    final Map<String, Object> filter = new HashMap<String, Object>();
    filter.put( IServiceScopes.WINDOW_SCOPE, getSite().getPage().getWorkbenchWindow() );
    commandService.refreshElements( ToggleCompactOutlineHandler.CMD_ID, filter );
  }

}