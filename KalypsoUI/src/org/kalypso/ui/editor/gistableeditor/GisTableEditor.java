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

import javax.xml.namespace.QName;

import org.apache.commons.collections.ExtendedProperties;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.kalypso.commons.command.ICommand;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.metadoc.IExportableObject;
import org.kalypso.metadoc.IExportableObjectFactory;
import org.kalypso.metadoc.configuration.PublishingConfiguration;
import org.kalypso.ogc.gml.IFeaturesProvider;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.gui.GuiTypeRegistrySingleton;
import org.kalypso.ogc.gml.gui.IGuiTypeHandler;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ogc.gml.table.ILayerTableInput;
import org.kalypso.ogc.gml.table.LayerTableViewer;
import org.kalypso.ogc.gml.table.wizard.ExportTableOptionsPage;
import org.kalypso.ogc.gml.table.wizard.ExportableLayerTable;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.editor.AbstractWorkbenchPart;
import org.kalypso.ui.editor.gistableeditor.actions.ColumnAction;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.event.ModellEventProvider;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;

/**
 * Eclipse-Editor zum editieren der Gis-Tabellen-Templates.<br/>
 * Zeigt das ganze als Tabelendarstellung, die einzelnen Datenquellen können potentiell editiert werden.<br/>
 * 
 * @author Gernot Belger
 */
public class GisTableEditor extends AbstractWorkbenchPart implements IEditorPart, IExportableObjectFactory
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

  private final GmlTablePartDelegate m_delegate = new GmlTablePartDelegate();

  @Override
  protected void doSaveInternal( final IProgressMonitor monitor, final IFile file ) throws CoreException
  {
    m_delegate.save( file, monitor );
  }

  @Override
  public void createPartControl( final Composite parent )
  {
    super.createPartControl( parent );

    final IWorkbenchPartSite site = getSite();

    m_delegate.createControl( parent, this, m_fcl, site );

    setSourceProvider( new GmlTableSourceProvider( site, getLayerTable() ) );

    final ISelectionProvider selectionProvider = m_delegate.getSelectionProvider();

    final MenuManager menuManager = m_delegate.getMenuManager();
    menuManager.setRemoveAllWhenShown( true );
    menuManager.addMenuListener( new IMenuListener()
    {
      @Override
      public void menuAboutToShow( final IMenuManager manager )
      {
        handleContextMenuAboutToShow( manager );
      }
    } );

    getEditorSite().registerContextMenu( menuManager, selectionProvider, false );
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
    final IMenuManager newFeatureMenu = new MenuManager( Messages.getString( "org.kalypso.ui.editor.actions.FeatureActionUtilities.7" ) ); //$NON-NLS-1$
    manager.add( newFeatureMenu );
    GisTableEditorActionBarContributor.fillNewFeatureMenu( newFeatureMenu, this );
  }

  @Override
  protected final void loadInternal( final IProgressMonitor monitor, final IStorageEditorInput input ) throws CoreException
  {
    m_delegate.load( input, monitor );
  }

  public LayerTableViewer getLayerTable( )
  {
    return m_delegate.getLayerTable();
  }

  private void appendSpaltenActions( final IMenuManager manager )
  {
    final IFeaturesProvider features = getLayerTable().getInput();
    if( features == null )
      return;

    final IFeatureType featureType = features.getFeatureType();
    if( featureType == null )
      return;

    final IPropertyType[] ftps = featureType.getProperties();
    for( final IPropertyType element : ftps )
    {
      if( isColumnShowable( element ) )
      {
        final GMLXPath columnPath = new GMLXPath( element.getQName() );
        manager.add( new ColumnAction( this, getLayerTable(), columnPath, element.getAnnotation() ) );
      }
    }
  }

  private boolean isColumnShowable( final IPropertyType type )
  {
    final QName typeName = type.getQName();

    if( Feature.QN_NAME.equals( typeName ) )
      return true;

    if( Feature.QN_BOUNDED_BY.equals( typeName ) )
      return false;

    if( type instanceof IValuePropertyType )
    {
      final IValuePropertyType vpt = (IValuePropertyType)type;
      if( vpt.isGeometry() )
      {
        /* Do not show geometries without type handler */
        final ITypeRegistry<IGuiTypeHandler> registry = GuiTypeRegistrySingleton.getTypeRegistry();
        final IGuiTypeHandler typeHandler = registry.getTypeHandlerFor( vpt );
        if( typeHandler == null )
          return false;
      }
    }

    return true;
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    if( adapter == IExportableObjectFactory.class )
      return this;

    if( adapter == ModellEventProvider.class )
      return m_delegate.getEventProvider();

    return super.getAdapter( adapter );
  }

  @Override
  public IExportableObject[] createExportableObjects( final ExtendedProperties configuration )
  {
    final ExportableLayerTable exp = new ExportableLayerTable( getLayerTable() );

    return new IExportableObject[] { exp };
  }

  @Override
  public IWizardPage[] createWizardPages( final PublishingConfiguration configuration, final ImageDescriptor defaultImage )
  {
    final IWizardPage page = new ExportTableOptionsPage( "optionPage", Messages.getString( "org.kalypso.ui.editor.gistableeditor.GisTableEditor.6" ), ImageProvider.IMAGE_UTIL_BERICHT_WIZ ); //$NON-NLS-1$ //$NON-NLS-2$

    return new IWizardPage[] { page };
  }

  public ILayerTableInput getTableInput( )
  {
    return m_delegate.getTableInput();
  }

  public void saveData( final IProgressMonitor monitor ) throws CoreException
  {
    getLayerTable().saveData( monitor );
  }

  IFeatureSelectionManager getSelectionManager( )
  {
    return getLayerTable().getSelectionManager();
  }
}