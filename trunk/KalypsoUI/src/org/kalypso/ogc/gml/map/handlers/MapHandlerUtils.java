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
package org.kalypso.ogc.gml.map.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.core.runtime.PluginUtilities;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.IKalypsoThemeProvider;
import org.kalypso.ogc.gml.command.ChangeExtentCommand;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.MapPanelSourceProvider;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.outline.MapOutline;
import org.kalypso.ogc.gml.outline.nodes.IThemeNode;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.editor.mapeditor.AbstractMapPart;
import org.kalypso.ui.editor.mapeditor.GisMapOutlinePage;

/**
 * Helper class for implementors of {@link org.eclipse.core.commands.IHandler} for map commands.
 * 
 * @author Gernot Belger
 */
public class MapHandlerUtils
{
  private static final String SETTINGS_LAST_DIR = "lastDir"; //$NON-NLS-1$

  private MapHandlerUtils( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" ); //$NON-NLS-1$
  }

  /**
   * Post a command to the currently active map.
   */
  public static void postCommandChecked( final IEvaluationContext context, final ICommand command, final Runnable runnnable ) throws ExecutionException
  {
    final ICommandTarget commandTarget = findCommandTarget( context );
    if( commandTarget == null )
      throw new ExecutionException( Messages.getString( "org.kalypso.ogc.gml.map.handlers.MapHandlerUtils.1" ) ); //$NON-NLS-1$

    commandTarget.postCommand( command, runnnable );
  }

  /**
   * Returns either the map outline or the currently active map-part, suitable to accept map-commands.
   */
  private static ICommandTarget findCommandTarget( final IEvaluationContext context )
  {
    final ICommandTarget outline = MapHandlerUtils.getMapOutline( context );
    if( outline != null )
      return outline;

    final Object part = context.getVariable( ISources.ACTIVE_PART_NAME );
    if( part instanceof AbstractMapPart )
      return ((AbstractMapPart) part).getCommandTarget();

    return null;
  }

  public static void postMapCommandChecked( final IMapPanel mapPanel, final ChangeExtentCommand command, final Runnable runnable ) throws ExecutionException
  {
    final ICommandTarget commandTarget = mapPanel.getWidgetManager().getCommandTarget();
    if( commandTarget == null )
      throw new ExecutionException( "No active command target" ); //$NON-NLS-1$

    commandTarget.postCommand( command, runnable );
  }

  /**
   * Gets the currently active mapPanel from the handler event.<br>
   * To be more precise, gets the <code>activeMapPanel</code> source from the events context.
   * 
   * @return <code>null</code>, if no {@link IMapPanel} was found in the context.
   */
  public static IMapPanel getMapPanel( final IEvaluationContext context )
  {
    return (IMapPanel) context.getVariable( MapPanelSourceProvider.ACTIVE_MAPPANEL_NAME );
  }

  /**
   * Gets the currently active mapPanel from the handler event.<br>
   * To be more precise, gets the <code>activeMapPanel</code> source from the events context.
   * 
   * @throws ExecutionException
   *           If the current context contains no mapPanel.
   */
  public static IMapPanel getMapPanelChecked( final IEvaluationContext context ) throws ExecutionException
  {
    final IMapPanel mapPanel = getMapPanel( context );
    if( mapPanel == null )
      throw new ExecutionException( "No mapPanel in context." ); //$NON-NLS-1$

    return mapPanel;
  }

  /**
   * Gets the currently active mapModell from the handler event.<br>
   * To be more precise, gets the <code>activeMapPanel</code> source from the events context, and from it, its map
   * modell.
   * 
   * @return <code>null</code>, if no {@link IMapModell} was found in the context.
   */
  public static IMapModell getMapModell( final IEvaluationContext context )
  {
    final IMapPanel mapPanel = getMapPanel( context );
    if( mapPanel == null )
      return null;

    return mapPanel.getMapModell();
  }

  /**
   * Gets the currently active mapModell from the handler event.<br>
   * To be more precise, gets the <code>activeMapPanel</code> source from the events context, and from it, its map
   * modell.
   * 
   * @throws ExecutionException
   *           If the current context contains no mapPanel.
   */
  public static IMapModell getMapModellChecked( final IEvaluationContext context ) throws ExecutionException
  {
    final IMapModell mapModell = getMapModell( context );
    if( mapModell == null )
      throw new ExecutionException( "No mapModell in context." ); //$NON-NLS-1$

    return mapModell;
  }

  /**
   * Gets the currently active theme from the handler event.<br>
   * To be more precise, gets the <code>activeMapPanel</code> source from the events context, and from it, its active
   * theme.
   * 
   * @throws ExecutionException
   *           If the current context contains no mapPanel.
   */
  public static IKalypsoTheme getActiveThemeChecked( final IEvaluationContext context ) throws ExecutionException
  {
    final IMapModell mapModell = getMapModellChecked( context );
    final IKalypsoTheme activeTheme = mapModell.getActiveTheme();
    if( activeTheme == null )
      throw new ExecutionException( "No active theme in context" ); //$NON-NLS-1$

    return activeTheme;
  }

  /**
   * Extracts all themes from the given selection.
   */
  public static IKalypsoTheme[] getSelectedThemes( final ISelection selection )
  {
    final List<IKalypsoTheme> themes = new ArrayList<IKalypsoTheme>();

    if( selection instanceof IStructuredSelection )
    {
      final IStructuredSelection s = (IStructuredSelection) selection;
      final Object[] elements = s.toArray();
      for( final Object element : elements )
      {
        if( element instanceof IKalypsoTheme )
          themes.add( (IKalypsoTheme) element );
        else if( element instanceof IKalypsoThemeProvider )
          themes.add( ((IKalypsoThemeProvider) element).getTheme() );
      }

    }

    return themes.toArray( new IKalypsoTheme[themes.size()] );
  }

  /**
   * Extracts all styles from the given selection.
   */
  public static IThemeNode[] getSelectedNodes( final ISelection selection )
  {
    final List<IThemeNode> nodes = new ArrayList<IThemeNode>();

    if( selection instanceof IStructuredSelection )
    {
      final IStructuredSelection s = (IStructuredSelection) selection;
      final Object[] elements = s.toArray();
      for( final Object element : elements )
      {
        if( element instanceof IThemeNode )
          nodes.add( (IThemeNode) element );
      }
    }

    return nodes.toArray( new IThemeNode[nodes.size()] );
  }

  public static GisMapOutlinePage getMapOutline( final IEvaluationContext context )
  {
    final IWorkbenchPart part = (IWorkbenchPart) context.getVariable( ISources.ACTIVE_PART_NAME );
    final GisMapOutlinePage tryOne = (GisMapOutlinePage) part.getAdapter( GisMapOutlinePage.class );
    if( tryOne != null )
      return tryOne;

    // HACK: also check for specific views, propably does not always work as expected...
    final IWorkbenchWindow window = (IWorkbenchWindow) context.getVariable( ISources.ACTIVE_WORKBENCH_WINDOW_NAME );
    final IWorkbenchPage activePage = window.getActivePage();
    final ContentOutline outlineView = (ContentOutline) activePage.findView( IPageLayout.ID_OUTLINE );
    if( outlineView != null )
    {
      final IPage currentPage = outlineView.getCurrentPage();
      if( currentPage instanceof GisMapOutlinePage )
        return (GisMapOutlinePage) currentPage;
    }

    final MapOutline mapOutline = (MapOutline) activePage.findView( MapOutline.ID );
    if( mapOutline != null )
    {
      final IPage currentPage = mapOutline.getCurrentPage();
      if( currentPage instanceof GisMapOutlinePage )
        return (GisMapOutlinePage) currentPage;
    }

    // Last try: adapt current view to the outline
    // PROBLEMATIC: this created outline must be disposed (but in the other cases not...).
    // We should reduce the dependency to the outline page and remove this whole mtehod...
    final IContentOutlinePage outline = (IContentOutlinePage) part.getAdapter( IContentOutlinePage.class );
    if( outline instanceof GisMapOutlinePage )
      return (GisMapOutlinePage) outline;

    return null;
  }

  public static IKalypsoTheme[] getSelectedThemesInOrder( final ISelection selection )
  {
    final IKalypsoTheme[] selectedThemes = getSelectedThemes( selection );
    /* We can only sort within one map model */
    final IMapModell[] selectedModels = getSelectedModels( selectedThemes );
    if( selectedModels.length != 1 )
      return new IKalypsoTheme[0];

    final IMapModell mapModell = selectedModels[0];
    final IKalypsoTheme[] allThemes = mapModell.getAllThemes();
    final List<IKalypsoTheme> allThemesList = new ArrayList<IKalypsoTheme>( Arrays.asList( allThemes ) );

    final List<IKalypsoTheme> selectedThemesList = Arrays.asList( selectedThemes );
    allThemesList.retainAll( selectedThemesList );

    return allThemesList.toArray( new IKalypsoTheme[allThemesList.size()] );
  }

  public static <T> T getFirstElement( final ISelection selection, final Class<T> classToFind )
  {
    if( !(selection instanceof IStructuredSelection) || selection.isEmpty() )
      return null;

    final IStructuredSelection structSel = (IStructuredSelection) selection;
    final Iterator< ? > iterator = structSel.iterator();
    for( final Iterator< ? > selIt = iterator; selIt.hasNext(); )
    {
      final Object object = selIt.next();
      if( object instanceof IKalypsoFeatureTheme )
        return classToFind.cast( object );

      if( object instanceof IAdaptable )
      {
        final Object adapter = ((IAdaptable) object).getAdapter( classToFind );
        if( adapter != null )
          return classToFind.cast( adapter );
      }
    }

    return null;
  }

  /** Returns the models (=parents) of the given themes. Filters all duplicates. */
  public static IMapModell[] getSelectedModels( final IKalypsoTheme[] themes )
  {
    final Set<IMapModell> models = new HashSet<IMapModell>( themes.length );

    for( final IKalypsoTheme kalypsoTheme : themes )
      models.add( kalypsoTheme.getMapModell() );

    return models.toArray( new IMapModell[models.size()] );
  }

  /**
   * Shows a save dialog and returns the chosen file.<br/>
   * Should be used by all map tools that save something in order to have similar functionality.
   */
  public static File showSaveFileDialog( final Shell shell, final String title, final String fileName, final String settingsSectionName, final String[] filterExtensions, final String[] filterNames )
  {
    final IDialogSettings dialogSettings = PluginUtilities.getDialogSettings( KalypsoGisPlugin.getDefault(), settingsSectionName );
    final String lastDirPath = dialogSettings.get( SETTINGS_LAST_DIR );
    final FileDialog fileDialog = new FileDialog( shell, SWT.SAVE );

    final String[] filterExtensionsAll = addAllFilterExtension( filterExtensions );
    final String[] filterNamesAll = addAllFilterName( filterNames );

    fileDialog.setOverwrite( true );
    fileDialog.setFilterExtensions( filterExtensionsAll );
    fileDialog.setFilterNames( filterNamesAll );
    fileDialog.setText( title );
    if( lastDirPath != null )
      fileDialog.setFilterPath( lastDirPath );

    fileDialog.setFileName( fileName );

    final String result = fileDialog.open();

    if( result == null )
      return null;

    final File file = new File( result );
    dialogSettings.put( SETTINGS_LAST_DIR, file.getParent() );
    return file;
  }

  private static String[] addAllFilterName( final String[] filterNames )
  {
    final String[] result = new String[filterNames.length + 1];
    System.arraycopy( filterNames, 0, result, 0, filterNames.length );
    result[filterNames.length] = Messages.getString( "org.kalypso.ogc.gml.outline.handler.ExportGml2ShapeThemeHandler.8" ); //$NON-NLS-1$
    return result;
  }

  private static String[] addAllFilterExtension( final String[] filterExtensions )
  {
    final String[] result = new String[filterExtensions.length + 1];
    System.arraycopy( filterExtensions, 0, result, 0, filterExtensions.length );
    result[filterExtensions.length] = "*.*"; //$NON-NLS-1$
    return result;
  }
}
