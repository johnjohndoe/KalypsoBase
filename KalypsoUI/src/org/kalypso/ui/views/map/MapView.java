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
package org.kalypso.ui.views.map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.kalypso.contribs.eclipse.ui.IContentOutlineProvider;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.editor.mapeditor.AbstractMapPart;
import org.kalypso.ui.editor.mapeditor.GisMapOutlinePage;

/**
 * <p>
 * View on a {@link org.kalypso.ogc.gml.mapmodel.IMapModell}.
 * </p>
 * <p>
 * Shows a map of all themes. The sources of the themes can be edited.
 * </p>
 * 
 * @author Stefan Kurzbach
 * @author Gernot Belger
 */
public class MapView extends AbstractMapPart implements IViewPart, IContentOutlineProvider
{
  public static final String ID = "org.kalypso.ui.views.mapView"; //$NON-NLS-1$

  private static final String SAVE_MAP_ON_CLOSE = "saveMapOnClose"; //$NON-NLS-1$

  private static final String RELOAD_MAP_ON_OPEN = "reloadMapOnOpen"; //$NON-NLS-1$

  private static final String MEMENTO_FILE = "mapFile"; //$NON-NLS-1$

  /**
   * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
   */
  @Override
  public void init( final IViewSite site, final IMemento memento )
  {
    init( site );
    if( memento != null )
    {
      final String fullPath = memento.getString( MapView.MEMENTO_FILE );
      if( fullPath != null )
      {
        final IPath path = Path.fromPortableString( fullPath );
        final IFile mementoFile = ResourcesPlugin.getWorkspace().getRoot().getFile( path );
        final String reloadOnOpen = getConfigurationElement().getAttribute( MapView.RELOAD_MAP_ON_OPEN );
        if( (mementoFile != null) && "true".equalsIgnoreCase( reloadOnOpen ) ) //$NON-NLS-1$
          setInput( new FileEditorInput( mementoFile ) );
      }
    }
  }

  /**
   * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
   */
  @Override
  public void saveState( final IMemento memento )
  {
    final IStorageEditorInput input = getEditorInput();
    if( !(input instanceof IFileEditorInput) )
      return;

    final IFile file = ((IFileEditorInput) input).getFile();
    if( file != null )
    {
      final IPath fullPath = file.getFullPath();
      if( fullPath != null )
        memento.putString( MapView.MEMENTO_FILE, fullPath.toPortableString() );
    }
  }

  /**
   * @see org.kalypso.ui.editor.mapeditor.AbstractMapPart#dispose()
   */
  @Override
  public void dispose( )
  {
    getMapPanel().getWidgetManager().setActualWidget( null );

    // FIXME: saving the map here causes dead-locks!
    final String saveOnCloseString = getConfigurationElement().getAttribute( MapView.SAVE_MAP_ON_CLOSE );
    if( "true".equalsIgnoreCase( saveOnCloseString ) ) //$NON-NLS-1$
    {
      try
      {
        final IStorageEditorInput editorInput = getEditorInput();
        if( editorInput instanceof IFileEditorInput )
          doSaveInternal( new NullProgressMonitor(), ((IFileEditorInput) editorInput).getFile() );
      }
      catch( final CoreException e )
      {
        KalypsoGisPlugin.getDefault().getLog().log( e.getStatus() );
      }
    }

    super.dispose();
  }

  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class adapter )
  {
    if( IContentOutlinePage.class.equals( adapter ) )
    {
      final GisMapOutlinePage page = new GisMapOutlinePage( getCommandTarget() );

      // Add id's for which menu/toolbar/popup-items are registered under
      // REMARK: We are using the id under which this view was registered under in the plugin.xml
      // The same view may have been registered under different view-id's. In order to have different
      // actions, re-register this view under another id and add items view the org.eclipse.ui.menus extension.point.
      final String baseUri = getSite().getId() + ".outline"; //$NON-NLS-1$
      page.addActionURI( "toolbar:" + baseUri ); //$NON-NLS-1$
      page.addActionURI( "menu:" + baseUri ); //$NON-NLS-1$
      page.addActionURI( "popup:" + baseUri ); //$NON-NLS-1$
      page.setMapPanel( getMapPanel() );

      return page;
    }

    return super.getAdapter( adapter );
  }

}