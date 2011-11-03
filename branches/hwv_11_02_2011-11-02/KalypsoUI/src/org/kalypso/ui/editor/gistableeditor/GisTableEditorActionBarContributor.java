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
package org.kalypso.ui.editor.gistableeditor;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionFactory;
import org.kalypso.contribs.eclipse.ui.actions.RetargetActionManager;
import org.kalypso.contribs.eclipse.ui.actions.RetargetActionManager.RetargetInfo;
import org.kalypso.i18n.Messages;
import org.kalypso.metadoc.IExportTargetModes;
import org.kalypso.metadoc.ui.ExportAction;
import org.kalypso.metadoc.ui.ExportActionContributor;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ogc.gml.table.ILayerTableInput;
import org.kalypso.ogc.gml.table.LayerTableViewer;
import org.kalypso.ui.editor.AbstractEditorActionBarContributor;
import org.kalypso.ui.editor.actions.FeatureSelectionActionGroup;
import org.kalypso.ui.editor.actions.NewFeatureScope;
import org.kalypso.ui.editor.actions.SelectionManagedMenu;
import org.kalypso.ui.editor.gistableeditor.actions.CopyEditorPartAction;
import org.kalypso.ui.editor.gistableeditor.actions.PasteEditorPartAction;
import org.kalypsodeegree.model.feature.FeatureList;

/**
 * @author Gernot Belger
 */
public class GisTableEditorActionBarContributor extends AbstractEditorActionBarContributor
{
  private static final String GIS_TABLE_MENU_ID = "org.kalypso.ui.editors.tableeditor.menu";

  private static final String GROUP_SELECTION = "selection";//$NON-NLS-1$

  private static final String NEW_FEATURE_MENU = "newFeatureMenu";//$NON-NLS-1$

  private static final String M_SPALTEN = "spaltenSubMenu"; //$NON-NLS-1$

  private final FeatureSelectionActionGroup m_featureSelectionActionGroup = new FeatureSelectionActionGroup();

  private ExportAction[] m_exportActions = null;

  private final RetargetActionManager m_retargetManager = new RetargetActionManager();

  private final SelectionManagedMenu m_selectionManagedMenu = new SelectionManagedMenu( GIS_TABLE_MENU_ID ); //$NON-NLS-1$

  public GisTableEditorActionBarContributor( )
  {
    final RetargetInfo copyInfo = new RetargetInfo( ActionFactory.COPY.getId(), null, SWT.NONE );
    copyInfo.setActionHandler( new CopyEditorPartAction() );
    m_retargetManager.addRetargetInfo( copyInfo );

    final RetargetInfo pasteInfo = new RetargetInfo( ActionFactory.PASTE.getId(), null, SWT.NONE );
    pasteInfo.setActionHandler( new PasteEditorPartAction() );
    m_retargetManager.addRetargetInfo( pasteInfo );
  }

  /**
   * @see org.eclipse.ui.part.EditorActionBarContributor#init(org.eclipse.ui.IActionBars)
   */
  @Override
  public void init( final IActionBars bars )
  {
    m_selectionManagedMenu.setGroupName( GROUP_SELECTION );

    m_featureSelectionActionGroup.addManagedMenu( m_selectionManagedMenu );
    m_featureSelectionActionGroup.setContext( new ActionContext( StructuredSelection.EMPTY ) );
    m_featureSelectionActionGroup.fillActionBars( bars );

    final IWorkbenchPage page = getPage();

    m_retargetManager.registerGlobalActionHandlers( bars );

    final IMenuManager gisTableMenuManager = bars.getMenuManager().findMenuUsingPath( GIS_TABLE_MENU_ID );
    createNewMenu( gisTableMenuManager );

    bars.updateActionBars();

    m_retargetManager.addPartListeners( page );

    final IWorkbenchPart activePart = page.getActivePart();
    if( activePart != null )
      m_retargetManager.partActivated( activePart );

    super.init( bars );
  }

  private void createNewMenu( final IMenuManager menuManager )
  {
    if( menuManager == null )
      return;

    final IContributionItem existingMenu = menuManager.find( NEW_FEATURE_MENU );
    if( existingMenu != null )
      return;

    final MenuManager newFeatureMenu = new MenuManager( Messages.getString( "org.kalypso.ui.editor.actions.FeatureActionUtilities.7" ), NEW_FEATURE_MENU );
    menuManager.appendToGroup( GROUP_SELECTION, newFeatureMenu );
    newFeatureMenu.setRemoveAllWhenShown( true );
    newFeatureMenu.addMenuListener( new IMenuListener()
    {
      @Override
      public void menuAboutToShow( final IMenuManager manager )
      {
        fillNewFeatureMenu( manager, getActiveEditor() );
      }
    } );
  }

  static void fillNewFeatureMenu( final IMenuManager newFeatureMenu, final IEditorPart activeEditor )
  {
    // TODO: quite hacky how we access all these stuff.....
    if( !(activeEditor instanceof GisTableEditor) )
      return;

    final GisTableEditor tableEditor = (GisTableEditor) activeEditor;
    final LayerTableViewer layerTable = tableEditor.getLayerTable();
    if( layerTable == null )
      return;

    final ILayerTableInput tableInput = layerTable.getInput();
    if( tableInput == null )
      return;

    final FeatureList featureList = tableInput.getFeatureList();
    final CommandableWorkspace workspace = tableInput.getWorkspace();

    if( featureList == null )
      return;

    final IFeatureSelectionManager selectionManager = layerTable.getSelectionManager();

    // FIXME: hard to solve: we should consider if there is a feature-type filter on the list of the table -> only
    // features that may go into this list should be created

    final NewFeatureScope scope = new NewFeatureScope( workspace, featureList, selectionManager );
    scope.addMenuItems( newFeatureMenu );
  }

  /**
   * @see org.eclipse.ui.part.EditorActionBarContributor#dispose()
   */
  @Override
  public void dispose( )
  {
    final IWorkbenchPage page = getPage();
    final IActionBars bars = getActionBars();
    if( page != null )
      m_retargetManager.disposeActions( bars, page );

    m_featureSelectionActionGroup.dispose();
    m_selectionManagedMenu.disposeMenu();

    bars.updateActionBars();

    super.dispose();
  }

  /**
   * @see org.eclipse.ui.IEditorActionBarContributor#setActiveEditor(org.eclipse.ui.IEditorPart)
   */
  @Override
  public void setActiveEditor( final IEditorPart targetEditor )
  {
    super.setActiveEditor( targetEditor );

    m_retargetManager.setActiveEditor( targetEditor );

    if( m_exportActions == null )
      m_exportActions = ExportActionContributor.contributeActions( targetEditor, "org.kalypso.ui.editors.tableeditor.menu/tabelle", "edit", IExportTargetModes.MODE_EXPERT ); //$NON-NLS-1$ //$NON-NLS-2$

    for( final ExportAction m_exportAction : m_exportActions )
      m_exportAction.setActivePart( targetEditor );

    m_featureSelectionActionGroup.setPart( targetEditor );

    if( targetEditor != null )
    {
      final IMenuManager menuManager = getActionBars().getMenuManager();
      final IMenuManager tableMenu = menuManager.findMenuUsingPath( GIS_TABLE_MENU_ID ); //$NON-NLS-1$
      if( tableMenu != null )
      {
        createNewMenu( tableMenu );

        // TODO: strange: what does that do?
        final IContributionItem oldItem = tableMenu.remove( M_SPALTEN );
        if( oldItem != null )
          oldItem.dispose();
      }
    }

    m_featureSelectionActionGroup.updateActionBars();

  }

  @Override
  protected void handleEditorSelectionChanged( final ISelectionProvider provider )
  {
    m_featureSelectionActionGroup.getContext().setSelection( provider.getSelection() );
    m_featureSelectionActionGroup.updateActionBars();
  }

}