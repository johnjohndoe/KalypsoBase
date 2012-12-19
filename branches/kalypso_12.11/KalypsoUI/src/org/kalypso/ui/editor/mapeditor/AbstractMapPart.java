/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.ui.editor.mapeditor;

import java.awt.event.FocusAdapter;
import java.net.URL;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.metadoc.IExportableObjectFactory;
import org.kalypso.ogc.gml.GisTemplateHelper;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.IKalypsoLayerModell;
import org.kalypso.ogc.gml.map.BaseMapSchedulingRule;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.MapPanel;
import org.kalypso.ogc.gml.map.MapPanelSourceProvider;
import org.kalypso.ogc.gml.map.listeners.IMapPanelListener;
import org.kalypso.ogc.gml.map.listeners.MapPanelAdapter;
import org.kalypso.ogc.gml.mapmodel.IMapPanelProvider;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.template.gismapview.Gismapview;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.editor.AbstractWorkbenchPart;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypso.util.command.JobExclusiveCommandTarget;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * Abstract superclass for map editor and map view. Inherits from AbstractEditorPart for editor behavior (save when
 * dirty, command target). Based on the old {@link GisMapEditor} implementation.
 * 
 * @author Stefan Kurzbach
 */
public abstract class AbstractMapPart extends AbstractWorkbenchPart implements IMapPanelProvider
{
  // TODO: we probably should move this elsewhere
  public static final String MAP_COMMAND_CATEGORY = "org.kalypso.ogc.gml.map.category"; //$NON-NLS-1$

  private final IFeatureSelectionManager m_selectionManager = KalypsoCorePlugin.getDefault().getSelectionManager();

  public final StatusLineContributionItem m_statusBar = new StatusLineContributionItem( "MapViewStatusBar", 100 ); //$NON-NLS-1$

  private IMapPanel m_mapPanel;

  private GisTemplateMapModell m_mapModell;

  private MapForm m_control;

  private boolean m_disposed = false;

  // TODO: this would also probably better made by a general map context: a general status line item that looks
  // for map context changes; it then always gets the current message from the map
  private final IMapPanelListener m_mapPanelListener = new MapPanelAdapter()
  {
    @Override
    public void onMessageChanged( final IMapPanel source, final String message )
    {
      final Display display = getSite().getShell().getDisplay();

      /* Update the text. */
      display.asyncExec( new Runnable()
      {
        @Override
        public void run( )
        {
          m_statusBar.setText( message );
        }
      } );
    }
  };

  private GM_Envelope m_initialEnv;

  private BaseMapSchedulingRule m_baseMapSchedulingRule;

  protected AbstractMapPart( )
  {
    super();
  }

  @Override
  public void init( final IEditorSite site, final IEditorInput input )
  {
    initMapPanel( site );

    super.init( site, input );
  }

  public void init( final IViewSite site )
  {
    setSite( site );

    initMapPanel( site );
  }

  private void initMapPanel( final IWorkbenchPartSite site )
  {
    final JobExclusiveCommandTarget commandTarget = getCommandTarget();

    m_statusBar.setText( "" ); //$NON-NLS-1$

    // both IViewSite und IEditorSite give access to actionBars
    final IActionBars actionBars = getActionBars( site );
    actionBars.getStatusLineManager().add( m_statusBar );
    actionBars.setGlobalActionHandler( ActionFactory.UNDO.getId(), commandTarget.undoAction );
    actionBars.setGlobalActionHandler( ActionFactory.REDO.getId(), commandTarget.redoAction );
    actionBars.updateActionBars();
  }

  @Override
  public synchronized void createPartControl( final Composite parent )
  {
    final IWorkbenchPartSite site = getSite();

    m_control = MapForm.createMapForm( parent );
    m_mapPanel = m_control.createMapPanel( this, m_selectionManager );

    updatePanel( m_mapModell, m_initialEnv );
    m_mapPanel.addMapPanelListener( m_mapPanelListener );

    final MapPanelSourceProvider sourceProvider = new MapPanelSourceProvider( site, m_mapPanel );
    setSourceProvider( sourceProvider );

    // REMARK: important: the map panel (awt) never gets the focus now. This fixes the following strange behavior,
    // - click into map
    // - click into other view -> view gets activated
    // - again click into map and than into other view -> view does not get activated, only the clicked control gets the focus.

    // Actually playing with focus and view activation did not solve this problem.

    // SOLUTION: is now: we keep the focus in our swt control and translate all swt key/mousewheel event manually to awt events
    // This will lead probably to some effects with the key events (so far testing did not show any strange behavior).
    // so TODO: the map-widgets should directly work with swt events instead; next step would be to incorporate the translation code
    // into the widget manager itself for backwards compatibility (i.e. introduce a new widget interface and make the old one deprecated).
    if( m_mapPanel instanceof MapPanel )
    {
      final MapPanel mapPanelComponent = (MapPanel)m_mapPanel;
      mapPanelComponent.addFocusListener( new FocusAdapter()
      {
        @Override
        public void focusGained( final java.awt.event.FocusEvent e )
        {
          handleFocuesGained();
        }
      } );

      m_control.addMouseWheelListener( new MapSwtWheelAdapter( mapPanelComponent ) );
      m_control.addKeyListener( new MapSwtKeyAdapter( mapPanelComponent ) );
    }

    // HACK: at the moment views never have a menu... maybe we could get the information,
    // if a context menu is desired from the defining extension
    if( this instanceof IEditorPart )
    {
      final MenuManager contextMenu = MapPartHelper.createMapContextMenu( m_control.getBody(), m_mapPanel, site );
      ((IEditorSite)site).registerContextMenu( contextMenu, m_mapPanel, false );
    }

    site.setSelectionProvider( m_mapPanel );
  }

  protected void handleFocuesGained( )
  {
    final IWorkbenchPartSite site = getSite();
    final IWorkbenchPage activePage = site.getWorkbenchWindow().getActivePage();

    final MapForm control = m_control;

    final Display display = site.getShell().getDisplay();
    display.asyncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        activePage.activate( AbstractMapPart.this );
        if( !control.isDisposed() )
          control.forceFocus();
      }
    } );
  }

  @Override
  public void setFocus( )
  {
    m_control.setFocus();
  }

  public IViewSite getViewSite( )
  {
    return (IViewSite)getSite();
  }

  @Override
  protected synchronized void loadInternal( final IProgressMonitor monitor, final IStorageEditorInput input ) throws CoreException
  {
    monitor.beginTask( Messages.getString( "org.kalypso.ui.editor.mapeditor.AbstractMapPart.6" ), 2 ); //$NON-NLS-1$

    try
    {
      // prepare for exception
      setMapModell( null, null );

      /* If no storage is passed, clear the map view. */
      if( input == null )
      {
        monitor.done();
        return;
      }

      /* "Loading map..." */
      if( m_mapPanel != null )
      {
        final String message = Messages.getString( "org.kalypso.ui.editor.mapeditor.AbstractMapPart.1", input.getName() ); //$NON-NLS-1$;
        m_control.setStatus( new Status( IStatus.INFO, KalypsoGisPlugin.getId(), message ) );
      }

      final Gismapview gisview = GisTemplateHelper.loadGisMapView( input.getStorage() );
      monitor.worked( 1 );

      final URL context = findContext( input );

      if( !m_disposed )
      {
        final GM_Envelope env = GisTemplateHelper.getBoundingBox( gisview );
        final GisTemplateMapModell mapModell = new GisTemplateMapModell( context, KalypsoDeegreePlugin.getDefault().getCoordinateSystem(), m_selectionManager );
        mapModell.createFromTemplate( gisview );
        setMapModell( mapModell, env );
      }
    }
    catch( final Throwable e )
    {
      e.printStackTrace();

      final IStatus status = StatusUtilities.statusFromThrowable( e );

      setMapModell( null, null );

      final IStatus error = new MultiStatus( KalypsoGisPlugin.getId(), -1, new IStatus[] { status }, Messages.getString( "org.kalypso.ui.editor.mapeditor.AbstractMapPart.2" ), null ); //$NON-NLS-1$
      if( m_control != null )
        // FIXME: the control might not yet have been created, so this status is lost.
        // shouldn't we keep it ourselfs and set it to the control when created?
        m_control.setStatus( error );

      throw new CoreException( status );
    }
    finally
    {
      monitor.done();
    }
  }

  @Override
  public void showBusy( final boolean busy )
  {
    super.showBusy( busy );

    final Form control = m_control;
    if( control != null && !control.isDisposed() )
    {
      control.getDisplay().asyncExec( new Runnable()
      {
        @Override
        public void run( )
        {
          if( !control.isDisposed() )
          {
            control.setBusy( false );
          }
        }
      } );
    }
  }

  public BaseMapSchedulingRule getSchedulingRule( )
  {
    final IFile file;
    final IStorageEditorInput input = getEditorInput();
    if( input instanceof IFileEditorInput )
      file = ((IFileEditorInput)input).getFile();
    else
      file = null;

    return getSchedulingRule( m_mapPanel, file );
  }

  public synchronized BaseMapSchedulingRule getSchedulingRule( final IMapPanel mapPanel, final IFile file )
  {
    if( m_baseMapSchedulingRule != null )
    {
      final IResource oldMapFile = m_baseMapSchedulingRule.getMapFile();
      final IMapPanel oldMapPanel = m_baseMapSchedulingRule.getMapPanel();
      if( ObjectUtils.equals( oldMapFile, file ) && oldMapPanel == mapPanel )
        return m_baseMapSchedulingRule;
    }

    m_baseMapSchedulingRule = new BaseMapSchedulingRule( mapPanel, file );
    return m_baseMapSchedulingRule;
  }

  @Override
  protected synchronized void doSaveInternal( final IProgressMonitor monitor, final IFile file ) throws CoreException
  {
    if( m_mapModell == null )
      return;

    try
    {
      monitor.beginTask( Messages.getString( "org.kalypso.ui.editor.mapeditor.AbstractMapPart.8" ), 2000 ); //$NON-NLS-1$
      final GM_Envelope boundingBox = m_mapPanel.getBoundingBox();
      final String srsName = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
      m_mapModell.saveGismapTemplate( boundingBox, srsName, monitor, file );
    }
    catch( final CoreException e )
    {
      throw e;
    }
    catch( final Throwable e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e, Messages.getString( "org.kalypso.ui.editor.mapeditor.AbstractMapPart.9" ) ) ); //$NON-NLS-1$
    }
  }

  private void setMapModell( final GisTemplateMapModell mapModell, final GM_Envelope env )
  {
    if( m_mapModell != null && m_mapModell != mapModell )
      m_mapModell.dispose();

    m_mapModell = mapModell;
    m_initialEnv = env; // only needed, if mapPanel not yet available

    final String partName = getPartName( mapModell );

    // set as UI-Thread call to prevent the "invalid thread access" exception
    final Display display = getSite().getShell().getDisplay();
    display.asyncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        setPartName( partName );
      }
    } );

    updatePanel( m_mapModell, m_initialEnv );
  }

  private String getPartName( final GisTemplateMapModell mapModell )
  {
    if( mapModell == null )
      return Messages.getString( "org.kalypso.ui.editor.mapeditor.AbstractMapPart.11" ); //$NON-NLS-1$

    final String label = mapModell.getLabel();
    if( label == null )
      return getEditorInput().getName();

    return label;
  }

  protected void updatePanel( final IKalypsoLayerModell mapModell, final GM_Envelope initialEnv )
  {
    if( m_mapPanel != null )
    {
      m_mapPanel.setMapModell( mapModell );
      if( initialEnv != null )
        m_mapPanel.setBoundingBox( initialEnv );
    }
  }

  public GisTemplateMapModell getMapModell( )
  {
    return m_mapModell;
  }

  @Override
  public IMapPanel getMapPanel( )
  {
    return m_mapPanel;
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    if( IExportableObjectFactory.class.equals( adapter ) )
      return new MapExportableObjectFactory( getMapPanel() );

    if( IContentOutlinePage.class.equals( adapter ) )
    {
      final GisMapOutlinePage page = new GisMapOutlinePage( getCommandTarget() );
      page.setMapPanel( getMapPanel() );
      return page;
    }

    if( adapter == IFile.class )
    {
      final IEditorInput input = getEditorInput();
      if( input instanceof IFileEditorInput )
        return ((IFileEditorInput)getEditorInput()).getFile();
    }

    if( adapter == IMapPanel.class )
      return m_mapPanel;

    if( adapter == Form.class )
      return m_control;

    return super.getAdapter( adapter );
  }

  @Override
  public void dispose( )
  {
    m_disposed = true;

    setMapModell( null, null );

    m_mapPanel.dispose();

    super.dispose();
  }

  public void setStatusBarMessage( final String message )
  {
    m_statusBar.setText( message );
  }

  @Override
  public void setPartName( final String partName )
  {
    super.setPartName( partName );
  }
}