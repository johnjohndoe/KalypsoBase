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
package org.kalypso.ui.editor.mapeditor;

import java.awt.Component;
import java.awt.Rectangle;
import java.net.URL;

import javax.swing.SwingUtilities;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.ui.partlistener.PartAdapter2;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.i18n.Messages;
import org.kalypso.metadoc.IExportableObject;
import org.kalypso.metadoc.IExportableObjectFactory;
import org.kalypso.metadoc.configuration.IPublishingConfiguration;
import org.kalypso.metadoc.ui.ImageExportPage;
import org.kalypso.ogc.gml.GisTemplateHelper;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.map.BaseMapSchedulingRule;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.MapPanelSourceProvider;
import org.kalypso.ogc.gml.map.listeners.IMapPanelListener;
import org.kalypso.ogc.gml.map.listeners.MapPanelAdapter;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.IMapPanelProvider;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.template.gismapview.Gismapview;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.editor.AbstractEditorPart;
import org.kalypso.util.command.JobExclusiveCommandTarget;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.event.ModellEventProvider;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * Abstract superclass for map editor and map view. Inherits from AbstractEditorPart for editor behavior (save when
 * dirty, command target). Based on the old {@link GisMapEditor} implementation.
 * 
 * @author Stefan Kurzbach
 */
// TODO: Why is it right here to inherit from AbstractEdtiorPart even when used within a View? Please comment on that.
// (SK) This might have to be looked at. GisMapEditor used to implement AbstractEditorPart for basic gml editor
// functionality (save when dirty, command target).
public abstract class AbstractMapPart extends AbstractEditorPart implements IExportableObjectFactory, IMapPanelProvider
{
  // TODO: we probably should move this elsewhere
  public static final String MAP_COMMAND_CATEGORY = "org.kalypso.ogc.gml.map.category"; //$NON-NLS-1$

  private final IFeatureSelectionManager m_selectionManager = KalypsoCorePlugin.getDefault().getSelectionManager();

  public final StatusLineContributionItem m_statusBar = new StatusLineContributionItem( "MapViewStatusBar", 100 ); //$NON-NLS-1$

  private IMapPanel m_mapPanel;

  private GisTemplateMapModell m_mapModell;

  private Form m_control;

  private boolean m_disposed = false;

  // TODO: this would also probably better made by a general map context: a general status line item that looks
  // for map context changes; it then always gets the current message from the map
  private final IMapPanelListener m_mapPanelListener = new MapPanelAdapter()
  {
    /**
     * @see org.kalypso.ogc.gml.map.MapPanelAdapter#onMessageChanged(org.kalypso.ogc.gml.map.MapPanel, java.lang.String)
     */
    @Override
    public void onMessageChanged( final IMapPanel source, final String message )
    {
      final Display display = getSite().getShell().getDisplay();

      /* Update the text. */
      display.asyncExec( new Runnable()
      {
        public void run( )
        {
          m_statusBar.setText( message );
        }
      } );
    }
  };

  private MapPanelSourceProvider m_mapSourceProvider;

  private final PartAdapter2 m_partListener = new PartAdapter2()
  {
    @Override
    public void partActivated( final IWorkbenchPartReference partRef )
    {
      handlePartActivated( partRef );
    }
  };

  private GM_Envelope m_initialEnv;

  private BaseMapSchedulingRule m_baseMapSchedulingRule;

  protected AbstractMapPart( )
  {
    super();
  }

  /**
   * @see org.kalypso.ui.editor.AbstractEditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
   */
  @Override
  public void init( final IEditorSite site, final IEditorInput input )
  {
    initMapPanel( site );

    super.init( site, input );
  }

  /**
   * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite)
   */
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

  /**
   * @see org.kalypso.ui.editor.AbstractEditorPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public synchronized void createPartControl( final Composite parent )
  {
    final IWorkbenchPartSite site = getSite();

    m_control = MapPartHelper.createMapForm( parent );
    m_mapPanel = MapPartHelper.createMapPanelInForm( m_control, this, m_selectionManager );
    updatePanel( m_mapModell, m_initialEnv );
    m_mapPanel.addMapPanelListener( m_mapPanelListener );
    m_mapSourceProvider = new MapPanelSourceProvider( site, m_mapPanel );

    // HACK: at the moment views never have a menu... maybe we could get the information,
    // if a context menu is desired from the defining extension
    if( this instanceof IEditorPart )
    {
      final MenuManager contextMenu = MapPartHelper.createMapContextMenu( m_control.getBody(), m_mapPanel, site );
      ((IEditorSite) site).registerContextMenu( contextMenu, m_mapPanel, false );
    }

    site.setSelectionProvider( m_mapPanel );
  }

  /**
   * We need to fire a source change event, in order to tell the map context which panel is the currently active one.
   */
  protected void handlePartActivated( final IWorkbenchPartReference partRef )
  {
    if( m_mapSourceProvider == null )
      return;

    final IWorkbenchPart part = partRef.getPart( false );
    if( part == AbstractMapPart.this )
      m_mapSourceProvider.fireSourceChanged();
  }

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
   */
  @Override
  public void setFocus( )
  {
    if( m_control != null && !m_control.isDisposed() )
    {
      m_control.setFocus();
      final IMapPanel mapPanel = m_mapPanel;
      if( mapPanel instanceof Component )
      {
        SwingUtilities.invokeLater( new Runnable()
        {
          public void run( )
          {
            ((Component) mapPanel).requestFocusInWindow();
          }
        } );
      }
      else
        m_control.setFocus();
    }
  }

  /**
   * @see org.eclipse.ui.IViewPart#getViewSite()
   */
  public IViewSite getViewSite( )
  {
    return (IViewSite) getSite();
  }

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#setSite(org.eclipse.ui.IWorkbenchPartSite)
   */
  @Override
  protected void setSite( final IWorkbenchPartSite site )
  {
    final IWorkbenchPartSite currentSite = getSite();
    if( currentSite != null )
      currentSite.getPage().addPartListener( m_partListener );

    super.setSite( site );

    if( site != null )
      site.getPage().addPartListener( m_partListener );
  }

  /**
   * @see org.kalypso.ui.editor.AbstractEditorPart#loadInternal(org.eclipse.core.runtime.IProgressMonitor,
   *      org.eclipse.ui.IStorageEditorInput)
   */
  @Override
  protected synchronized void loadInternal( final IProgressMonitor monitor, final IStorageEditorInput input ) throws Exception, CoreException
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
        m_mapPanel.setStatus( StatusUtilities.createStatus( IStatus.INFO, message, null ) );
      }

      final Gismapview gisview = GisTemplateHelper.loadGisMapView( input.getStorage() );
      monitor.worked( 1 );

      final URL context;
      final IProject project;
      if( input instanceof IFileEditorInput )
      {
        final IFile file = ((IFileEditorInput) input).getFile();
        context = ResourceUtilities.createURL( file );
        project = file.getProject();
      }
      else
      {
        context = null;
        project = null;
      }

      if( !m_disposed )
      {
        final GM_Envelope env = GisTemplateHelper.getBoundingBox( gisview );
        final GisTemplateMapModell mapModell = new GisTemplateMapModell( context, KalypsoDeegreePlugin.getDefault().getCoordinateSystem(), project, m_selectionManager );
        mapModell.createFromTemplate( gisview );
        setMapModell( mapModell, env );
      }
    }
    catch( final Throwable e )
    {
      e.printStackTrace();

      final IStatus status = StatusUtilities.statusFromThrowable( e );

      setMapModell( null, null );

      if( m_mapPanel != null )
        m_mapPanel.setStatus( new MultiStatus( KalypsoGisPlugin.getId(), -1, new IStatus[] { status }, Messages.getString( "org.kalypso.ui.editor.mapeditor.AbstractMapPart.2" ), null ) ); //$NON-NLS-1$

      throw new CoreException( status );
    }
    finally
    {
      monitor.done();
    }
  }

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#showBusy(boolean)
   */
  @Override
  public void showBusy( final boolean busy )
  {
    super.showBusy( busy );

    final Form control = m_control;
    if( control != null && !control.isDisposed() )
    {
      control.getDisplay().asyncExec( new Runnable()
      {
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
      file = ((IFileEditorInput) input).getFile();
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

  /**
   * @see org.kalypso.ui.editor.AbstractEditorPart#doSaveInternal(org.eclipse.core.runtime.IProgressMonitor,
   *      org.eclipse.core.resources.IFile)
   */
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
    setPartName( partName );

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

  protected void updatePanel( final IMapModell mapModell, final GM_Envelope initialEnv )
  {
    if( m_mapPanel != null )
    {
      m_mapPanel.setMapModell( mapModell );
      if( initialEnv != null )
        m_mapPanel.setBoundingBox( initialEnv );
    }
  }

  protected GisTemplateMapModell getMapModell( )
  {
    return m_mapModell;
  }

  /**
   * @see org.kalypso.ogc.gml.mapmodel.IMapPanelProvider#getMapPanel()
   */
  public IMapPanel getMapPanel( )
  {
    return m_mapPanel;
  }

  /**
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  @SuppressWarnings("unchecked")
  @Override
  public Object getAdapter( final Class adapter )
  {
    if( IExportableObjectFactory.class.equals( adapter ) )
      return this;

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
        return ((IFileEditorInput) getEditorInput()).getFile();
    }

    if( adapter == IMapPanel.class )
      return m_mapPanel;

    if( adapter == ModellEventProvider.class )
      return new MapPanelModellEventProvider( m_mapPanel );

    if( adapter == Form.class )
      return m_control;

    return super.getAdapter( adapter );
  }

  /**
   * @see org.kalypso.metadoc.IExportableObjectFactory#createExportableObjects(org.apache.commons.configuration.Configuration)
   */
  public IExportableObject[] createExportableObjects( final Configuration conf )
  {
    return new IExportableObject[] { new ExportableMap( getMapPanel(), conf.getInt( ImageExportPage.CONF_IMAGE_WIDTH, 640 ), conf.getInt( ImageExportPage.CONF_IMAGE_HEIGHT, 480 ), conf.getString( ImageExportPage.CONF_IMAGE_FORMAT, "png" ) ) }; //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.metadoc.IExportableObjectFactory#createWizardPages(org.kalypso.metadoc.configuration.IPublishingConfiguration,
   *      ImageDescriptor)
   */
  public IWizardPage[] createWizardPages( final IPublishingConfiguration configuration, final ImageDescriptor defaultImage )
  {
    final ImageDescriptor imgDesc = AbstractUIPlugin.imageDescriptorFromPlugin( KalypsoGisPlugin.getId(), "icons/util/img_props.gif" ); //$NON-NLS-1$
    final Rectangle bounds = getMapPanel().getScreenBounds();
    final double width = bounds.width;
    final double height = bounds.height;
    final double actualWidthToHeigthRatio = width / height;
    final IWizardPage page = new ImageExportPage( configuration, "mapprops", Messages.getString( "org.kalypso.ui.editor.mapeditor.AbstractMapPart.16" ), imgDesc, actualWidthToHeigthRatio ); //$NON-NLS-1$ //$NON-NLS-2$

    return new IWizardPage[] { page };
  }

  @Override
  public void dispose( )
  {
    getSite().getPage().removePartListener( m_partListener );

    m_mapSourceProvider.dispose();

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