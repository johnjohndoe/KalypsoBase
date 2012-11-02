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

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.services.IServiceScopes;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.jface.action.ContributionUtils;
import org.kalypso.ogc.gml.IKalypsoLayerModell;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.listeners.IMapPanelListener;
import org.kalypso.ogc.gml.map.listeners.MapPanelAdapter;
import org.kalypso.ogc.gml.outline.GisMapOutlineViewer;
import org.kalypso.ogc.gml.outline.handler.ToggleCompactOutlineHandler;
import org.kalypso.ogc.gml.outline.nodes.IThemeNode;
import org.kalypso.ui.editor.mapeditor.views.StyleEditorViewPart;
import org.kalypso.util.command.JobExclusiveCommandTarget;

/**
 * OutlinePage für das MapView-Template
 * 
 * @author Gernot Belger
 */
public class GisMapOutlinePage extends Page implements IContentOutlinePage, ICommandTarget
{
  private final JobExclusiveCommandTarget m_commandTarget;

  private GisMapOutlineViewer m_outlineViewer;

  private final IMapPanelListener m_mapPanelListener = new MapPanelAdapter()
  {
    @Override
    public void onMapModelChanged( final IMapPanel source, final IKalypsoLayerModell oldModel, final IKalypsoLayerModell newModel )
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
  private final Set<String> m_actionURIs = new HashSet<>();

  private final MenuManager m_pagePopup = new MenuManager( "#MapOutlineContextMenu" ); //$NON-NLS-1$

  private final ToolBarManager m_pageToolbar = new ToolBarManager();

  private final MenuManager m_pageMenu = new MenuManager();

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

  @Override
  public void createControl( final Composite parent )
  {
    if( parent.isDisposed() )
      System.out.println( "parent is disposed" ); //$NON-NLS-1$

    m_outlineViewer.createControl( parent );

    final IPageSite site = getSite();
    final IActionBars actionBars = site.getActionBars();

    // TODO: probably does not work any more...
    actionBars.setGlobalActionHandler( ActionFactory.UNDO.getId(), m_commandTarget.undoAction );
    actionBars.setGlobalActionHandler( ActionFactory.REDO.getId(), m_commandTarget.redoAction );

    final Control outlineControl = m_outlineViewer.getControl();
    final Menu menu = m_pagePopup.createContextMenu( outlineControl );
    outlineControl.setMenu( menu );

    populateActionBars( site );
    setCompact( true );
  }

  /**
   * Populates this pages action bars with items from the given menu-contributions.
   */
  protected void populateActionBars( final IPageSite site )
  {
    releaseActionBars();

    // TRICKY/BUGFIX: we cannot directly fill the contributions into the site-action bars
    // as they do not implement ContributionManager
    // We hence fill everything into separate manager and copy everything to the right place.
    for( final String uri : m_actionURIs )
    {
      if( uri.startsWith( "toolbar" ) ) //$NON-NLS-1$
        ContributionUtils.populateContributionManager( site, m_pageToolbar, uri );
      else if( uri.startsWith( "menu" ) ) //$NON-NLS-1$
        ContributionUtils.populateContributionManager( site, m_pageMenu, uri );
      else if( uri.startsWith( "popup" ) ) //$NON-NLS-1$
        ContributionUtils.populateContributionManager( site, m_pagePopup, uri );
      else
        System.out.println( String.format( "Unable to add uri '%s' to outline action bars. Unknown prefix.", uri ) ); //$NON-NLS-1$
    }

    /* Now copy everything to the real action bars */
    final IActionBars actionBars = site.getActionBars();
    copyItems( m_pageToolbar, actionBars.getToolBarManager() );
    copyItems( m_pageMenu, actionBars.getMenuManager() );

    actionBars.updateActionBars();
  }

  // TODO: check, if this always gives the correct result
  private void copyItems( final IContributionManager from, final IContributionManager to )
  {
    final IContributionItem[] items = from.getItems();
    for( final IContributionItem item : items )
      to.add( item );
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
    final IMenuManager menuManager = actionBars.getMenuManager();
    ContributionUtils.releaseContributions( site, m_pageToolbar );
    ContributionUtils.releaseContributions( site, menuManager );
    if( m_pagePopup != null )
      ContributionUtils.releaseContributions( site, m_pagePopup );
  }

  @Override
  public void dispose( )
  {
    releaseActionBars();

    m_pageToolbar.dispose();

    if( m_outlineViewer != null )
    {
      m_outlineViewer.dispose();
      m_outlineViewer = null;
    }

    setMapPanel( null );

    setStyleSelection( null );

    super.dispose();
  }

  @Override
  public Control getControl( )
  {
    if( m_outlineViewer == null )
      return null;

    return m_outlineViewer.getControl();
  }

  @Override
  public void setFocus( )
  {
    final Control control = getControl();
    if( control != null && !control.isDisposed() )
      control.setFocus();

    setStyleSelection( m_outlineViewer );
  }

  protected void setStyleSelection( final ISelectionProvider selectionProvider )
  {
    // bei jedem Focus, überprüfen, ob outline beim StyleEditor registriert ist.
    // TODO: remove, style editor must pull information instead
    final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    final IWorkbenchPage activePage = window.getActivePage();
    if( activePage == null )
      return;

    final StyleEditorViewPart part = (StyleEditorViewPart)activePage.findView( "org.kalypso.ui.editor.mapeditor.views.styleeditor" ); //$NON-NLS-1$
    if( part != null )
      part.setSelectionChangedProvider( selectionProvider );
  }

  public IMapPanel getMapPanel( )
  {
    return m_panel;
  }

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

  @Override
  public void postCommand( final ICommand command, final Runnable runnable )
  {
    m_commandTarget.postCommand( command, runnable );
  }

  protected void handleMapModelChanged( final IKalypsoLayerModell newModel )
  {
    if( m_outlineViewer != null )
      m_outlineViewer.setMapModel( newModel );
  }

  @Override
  public void init( final IPageSite site )
  {
    super.init( site );
  }

  @Override
  public void addSelectionChangedListener( final ISelectionChangedListener listener )
  {
    // FIXME: The implementation of the ISelectionProvider interface seems wrong.
    // FIXME: Normally the outline page is set as selection provider. Probably by the outline view.
    // FIXME: This however would lead here to a infinite recursion.
    // FIXME: Commenting the required functions out, seems not to change the eclipse outline behavior.
    // FIXME: The infinite recursion did not occure here, because our GisMapOutlineView
    // FIXME: sets a different selection provider. And this page obviously too.
    // FIXME: Perhaps we should change the implementation.
    // getSite().getSelectionProvider().addSelectionChangedListener( listener );
    m_outlineViewer.addSelectionChangedListener( listener );
  }

  @Override
  public ISelection getSelection( )
  {
    return m_outlineViewer.getSelection();
  }

  @Override
  public void removeSelectionChangedListener( final ISelectionChangedListener listener )
  {
    m_outlineViewer.removeSelectionChangedListener( listener );
  }

  @Override
  public void setSelection( final ISelection selection )
  {
    m_outlineViewer.setSelection( selection );
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
    if( m_outlineViewer != null )
      m_outlineViewer.setCompact( compact );

    final IPageSite site = getSite();
    if( site == null )
      return;

    final ICommandService commandService = (ICommandService)site.getService( ICommandService.class );
    final Map<String, Object> filter = new HashMap<>();
    filter.put( IServiceScopes.WINDOW_SCOPE, getSite().getPage().getWorkbenchWindow() );
    commandService.refreshElements( ToggleCompactOutlineHandler.CMD_ID, filter );
  }

  /**
   * This function searches the content of the viewer for a node, which contains the given theme.
   * 
   * @param theme
   *          The theme.
   * @return The node or null.
   */
  public IThemeNode findNode( final IKalypsoTheme theme )
  {
    if( m_outlineViewer == null || m_outlineViewer.getControl().isDisposed() )
      return null;

    return m_outlineViewer.findNode( theme );
  }
}