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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.Marshaller;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.kalypso.commons.command.ICommand;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.jaxb.TemplateUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.i18n.Messages;
import org.kalypso.metadoc.IExportableObject;
import org.kalypso.metadoc.IExportableObjectFactory;
import org.kalypso.metadoc.configuration.IPublishingConfiguration;
import org.kalypso.ogc.gml.GisTemplateHelper;
import org.kalypso.ogc.gml.IFeaturesProvider;
import org.kalypso.ogc.gml.IFeaturesProviderListener;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.table.ILayerTableInput;
import org.kalypso.ogc.gml.table.LayerTableViewer;
import org.kalypso.ogc.gml.table.celleditors.IFeatureModifierFactory;
import org.kalypso.ogc.gml.table.wizard.ExportTableOptionsPage;
import org.kalypso.ogc.gml.table.wizard.ExportableLayerTable;
import org.kalypso.template.gistableview.Gistableview;
import org.kalypso.template.gistableview.Gistableview.Layer;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.editor.AbstractEditorPart;
import org.kalypso.ui.editor.gistableeditor.actions.ColumnAction;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.event.ModellEvent;
import org.kalypsodeegree.model.feature.event.ModellEventProvider;
import org.kalypsodeegree.model.feature.event.ModellEventProviderAdapter;

/**
 * <p>
 * Eclipse-Editor zum editieren der Gis-Tabellen-Templates.
 * </p>
 * <p>
 * Zeigt das ganze als Tabelendarstellung, die einzelnen Datenquellen k?nnen potentiell editiert werden
 * </p>
 * 
 * @author belger
 */
public class GisTableEditor extends AbstractEditorPart implements IEditorPart, ISelectionProvider, IExportableObjectFactory
{
  private final IFeatureChangeListener m_fcl = new IFeatureChangeListener()
  {
    @Override
    public void featureChanged( final ICommand changeCommand )
    {
    }

    @Override
    public void openFeatureRequested( final Feature feature, final IPropertyType ftp )
    {
      // feature view öffnen
      final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      final IWorkbenchPage page = window.getActivePage();
      try
      {
        page.showView( "org.kalypso.featureview.views.FeatureView", null, IWorkbenchPage.VIEW_VISIBLE ); //$NON-NLS-1$
      }
      catch( final PartInitException e )
      {
        e.printStackTrace();
        final Shell shell = window.getShell();
        ErrorDialog.openError( shell, Messages.getString( "org.kalypso.ui.editor.gistableeditor.GisTableEditor.1" ), Messages.getString( "org.kalypso.ui.editor.gistableeditor.GisTableEditor.2" ), e.getStatus() ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
  };

  private final IFeaturesProviderListener m_featuresProviderListener = new IFeaturesProviderListener()
  {
    @Override
    public void featuresChanged( final IFeaturesProvider source, final ModellEvent modellEvent )
    {
      fireModellChanged( modellEvent );
    }
  };

  private final ModellEventProvider m_eventProvider = new ModellEventProviderAdapter();

  private LayerTableViewer m_layerTable = null;

  private Gistableview m_tableTemplate;

  /**
   * @see org.kalypso.ui.editor.AbstractEditorPart#dispose()
   */
  @Override
  public void dispose( )
  {
    final IWorkbenchPartSite site = getSite();
    if( site != null )
      site.setSelectionProvider( this );

    super.dispose();
  }

  /**
   * @see org.kalypso.ui.editor.AbstractEditorPart#doSaveInternal(org.eclipse.core.runtime.IProgressMonitor,
   *      org.eclipse.core.resources.IFile)
   */
  @Override
  protected void doSaveInternal( final IProgressMonitor monitor, final IFile file )
  {
    if( m_layerTable == null )
      return;

    ByteArrayOutputStream bos = null;
    ByteArrayInputStream bis = null;
    try
    {
      final Gistableview tableTemplate = m_layerTable.createTableTemplate();

      final String charset = file.getCharset();

      // die Vorlagendatei ist klein, deswegen einfach in ein ByteArray serialisieren
      bos = new ByteArrayOutputStream();
      final OutputStreamWriter osw = new OutputStreamWriter( bos, charset );

      final Marshaller marshaller = TemplateUtilities.createGistableviewMarshaller( charset );
      marshaller.marshal( tableTemplate, osw );
      bos.close();

      bis = new ByteArrayInputStream( bos.toByteArray() );

      if( file.exists() )
        file.setContents( bis, false, true, monitor );
      else
        file.create( bis, false, monitor );

      bis.close();
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
    finally
    {
      IOUtils.closeQuietly( bos );
      IOUtils.closeQuietly( bis );
    }
  }

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPartControl( final Composite parent )
  {
    super.createPartControl( parent );

    final KalypsoGisPlugin plugin = KalypsoGisPlugin.getDefault();
    final IFeatureModifierFactory factory = plugin.createFeatureTypeCellEditorFactory();
    m_layerTable = new LayerTableViewer( parent, SWT.BORDER, this, factory, KalypsoCorePlugin.getDefault().getSelectionManager(), m_fcl );

    final MenuManager menuManager = new MenuManager();
    menuManager.setRemoveAllWhenShown( true );
    menuManager.addMenuListener( new IMenuListener()
    {
      @Override
      public void menuAboutToShow( final IMenuManager manager )
      {
        handleContextMenuAboutToShow( manager );
      }
    } );

    getEditorSite().registerContextMenu( menuManager, m_layerTable, false );
    getSite().setSelectionProvider( getLayerTable() );
    m_layerTable.setMenu( menuManager );

    try
    {
      final IFile inputFile = ((IFileEditorInput) getEditorInput()).getFile();
      final URL context = ResourceUtilities.createURL( inputFile );

      if( m_tableTemplate != null )
      {
        final Layer layer = m_tableTemplate.getLayer();
        m_layerTable.setInput( layer, context );
        m_layerTable.applyLayer( layer );
        m_layerTable.getInput().addFeaturesProviderListener( m_featuresProviderListener );
      }
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();
    }
  }

  protected void handleContextMenuAboutToShow( final IMenuManager manager )
  {
    appendNewFeatureActions( manager );
    manager.add( new GroupMarker( IWorkbenchActionConstants.MB_ADDITIONS ) );
    manager.add( new Separator() );
    // mgr.add(selectAllAction);
    appendSpaltenActions( manager );
  }

  private void appendNewFeatureActions( final IMenuManager manager )
  {
    final IMenuManager newFeatureMenu = new MenuManager( Messages.getString( "org.kalypso.ui.editor.actions.FeatureActionUtilities.7" ) );
    manager.add( newFeatureMenu );
    GisTableEditorActionBarContributor.fillNewFeatureMenu( newFeatureMenu, this );

  }

  @Override
  protected final void loadInternal( final IProgressMonitor monitor, final IStorageEditorInput input ) throws Exception
  {
    if( !(input instanceof IFileEditorInput) )
      throw new IllegalArgumentException( Messages.getString( "org.kalypso.ui.editor.gistableeditor.GisTableEditor.3" ) ); //$NON-NLS-1$

    monitor.beginTask( Messages.getString( "org.kalypso.ui.editor.gistableeditor.GisTableEditor.4" ), 1000 ); //$NON-NLS-1$

    final IStorage storage = input.getStorage();
    m_tableTemplate = GisTemplateHelper.loadGisTableview( storage );

    monitor.worked( 1000 );
  }

  public LayerTableViewer getLayerTable( )
  {
    return m_layerTable;
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  @Override
  public void addSelectionChangedListener( final ISelectionChangedListener listener )
  {
    m_layerTable.addSelectionChangedListener( listener );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
   */
  @Override
  public ISelection getSelection( )
  {
    return m_layerTable.getSelection();
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  @Override
  public void removeSelectionChangedListener( final ISelectionChangedListener listener )
  {
    m_layerTable.removeSelectionChangedListener( listener );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
   */
  @Override
  public void setSelection( final ISelection selection )
  {
    m_layerTable.setSelection( selection );
  }

  public void appendSpaltenActions( final IMenuManager manager )
  {
    final IFeaturesProvider features = m_layerTable.getInput();
    if( features == null )
      return;

    final IFeatureType featureType = features.getFeatureType();
    if( featureType == null )
      return;

    final IPropertyType[] ftps = featureType.getProperties();
    for( final IPropertyType element : ftps )
    {
      final String columnName = element.getQName().getLocalPart();
      manager.add( new ColumnAction( this, m_layerTable, columnName, element.getAnnotation() ) );
    }
  }

  /**
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class adapter )
  {
    if( adapter == IExportableObjectFactory.class )
      return this;

    if( adapter == ModellEventProvider.class )
      return m_eventProvider;

    return super.getAdapter( adapter );
  }

  /**
   * @see org.kalypso.metadoc.IExportableObjectFactory#createExportableObjects(org.apache.commons.configuration.Configuration)
   */
  @Override
  public IExportableObject[] createExportableObjects( final Configuration configuration )
  {
    final ExportableLayerTable exp = new ExportableLayerTable( m_layerTable );

    return new IExportableObject[] { exp };
  }

  /**
   * @see org.kalypso.metadoc.IExportableObjectFactory#createWizardPages(org.kalypso.metadoc.configuration.IPublishingConfiguration,
   *      ImageDescriptor)
   */
  @Override
  public IWizardPage[] createWizardPages( final IPublishingConfiguration configuration, final ImageDescriptor defaultImage )
  {
    final IWizardPage page = new ExportTableOptionsPage( "optionPage", Messages.getString( "org.kalypso.ui.editor.gistableeditor.GisTableEditor.6" ), ImageProvider.IMAGE_UTIL_BERICHT_WIZ ); //$NON-NLS-1$ //$NON-NLS-2$

    return new IWizardPage[] { page };
  }

  protected void fireModellChanged( final ModellEvent modellEvent )
  {
    // Is only used to refresh any actions on this editor... should sometimes be refactored...
    if( modellEvent != null )
      m_eventProvider.fireModellEvent( modellEvent );
  }

  public ILayerTableInput getTableInput( )
  {
    if( m_layerTable == null )
      return null;

    return m_layerTable.getInput();
  }

}