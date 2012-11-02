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
package org.kalypso.ui.editor.gistableeditor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.httpclient.URIException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.jaxb.TemplateUtilities;
import org.kalypso.ogc.gml.GisTemplateHelper;
import org.kalypso.ogc.gml.IFeaturesProvider;
import org.kalypso.ogc.gml.IFeaturesProviderListener;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.table.ILayerTableInput;
import org.kalypso.ogc.gml.table.LayerTableViewer;
import org.kalypso.ogc.gml.table.celleditors.IFeatureModifierFactory;
import org.kalypso.template.gistableview.Gistableview;
import org.kalypso.template.gistableview.Gistableview.Layer;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.editor.AbstractWorkbenchPart;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.model.feature.event.ModellEvent;
import org.kalypsodeegree.model.feature.event.ModellEventProvider;
import org.kalypsodeegree.model.feature.event.ModellEventProviderAdapter;

/**
 * Implements some common code used by all {@link org.eclipse.ui.IWorkbenchPart} implementations that show a .gft file.
 * 
 * @author Gernot Belger
 */
public class GmlTablePartDelegate
{
  private final IFeaturesProviderListener m_featuresProviderListener = new IFeaturesProviderListener()
  {
    @Override
    public void featuresChanged( final IFeaturesProvider source, final ModellEvent modellEvent )
    {
      fireModellChanged( modellEvent );
    }
  };

  private final ModellEventProvider m_eventProvider = new ModellEventProviderAdapter();

  private LayerTableViewer m_layerTable;

  private Gistableview m_tableTemplate;

  private URL m_tableContext;

  private MenuManager m_menuManager;

  public void createControl( final Composite parent, final ICommandTarget commandTarget, final IFeatureChangeListener fcl, final IWorkbenchPartSite site )
  {
    final KalypsoGisPlugin plugin = KalypsoGisPlugin.getDefault();

    final IFeatureModifierFactory factory = plugin.getFeatureTypeCellEditorFactory();
    m_layerTable = new LayerTableViewer( parent, SWT.BORDER, commandTarget, factory, KalypsoCorePlugin.getDefault().getSelectionManager(), fcl );

    site.setSelectionProvider( m_layerTable );

    m_menuManager = new MenuManager();
    m_layerTable.setMenu( m_menuManager );

    templateChanged();
  }

  private void templateChanged( )
  {
    final LayerTableViewer layerTable = m_layerTable;
    if( layerTable == null )
      return;

    final ILayerTableInput oldInput = layerTable.getInput();
    final IFeaturesProviderListener featuresProviderListener = m_featuresProviderListener;

    if( oldInput != null )
      oldInput.removeFeaturesProviderListener( featuresProviderListener );

    final Layer layer = m_tableTemplate == null ? null : m_tableTemplate.getLayer();
    final URL tableContext = m_tableContext;

    final Job job = new UIJob( "Template changed" ) //$NON-NLS-1$
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        if( layerTable.getControl().isDisposed() )
          return Status.OK_STATUS;

        layerTable.setInput( layer, tableContext );
        layerTable.applyLayer( layer, tableContext );

        final ILayerTableInput newInput = layerTable.getInput();
        if( newInput != null )
          newInput.addFeaturesProviderListener( featuresProviderListener );
        return Status.OK_STATUS;
      }
    };
    job.setSystem( true );
    job.schedule();
  }

  public void load( final IStorageEditorInput input, final IProgressMonitor monitor ) throws CoreException
  {
    monitor.beginTask( Messages.getString( "org.kalypso.ui.editor.gistableeditor.GisTableEditor.4" ), 1000 ); //$NON-NLS-1$

    try
    {
      final IStorage storage = input.getStorage();

      m_tableTemplate = GisTemplateHelper.loadGisTableview( storage );
      m_tableContext = AbstractWorkbenchPart.findContext( input );

      templateChanged();
    }
    catch( final JAXBException | MalformedURLException | URIException e )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), "Failed to load table template", e ); //$NON-NLS-1$
      throw new CoreException( status );
    }
    finally
    {
      monitor.worked( 1000 );
    }
  }

  public void save( final IFile file, final IProgressMonitor monitor ) throws CoreException
  {
    if( m_layerTable == null )
      return;

    final String charset = file.getCharset();

    final byte[] bytes = saveAsBytes( charset );
    writeBytes( file, bytes, monitor );
  }

  private byte[] saveAsBytes( final String charset ) throws CoreException
  {
    final Gistableview tableTemplate = m_layerTable.createTableTemplate();

    try( ByteArrayOutputStream bos = new ByteArrayOutputStream(); )
    {
      final OutputStreamWriter osw = new OutputStreamWriter( bos, charset );

      final Marshaller marshaller = TemplateUtilities.createGistableviewMarshaller( charset );
      marshaller.marshal( tableTemplate, osw );
      bos.close();

      return bos.toByteArray();
    }
    catch( final IOException e )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), "Failed to serialize table", e ); //$NON-NLS-1$
      throw new CoreException( status );
    }
    catch( final JAXBException e )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), "Table binding failed", e ); //$NON-NLS-1$
      throw new CoreException( status );
    }
  }

  // TODO: could be moved to general IFile helper
  private static void writeBytes( final IFile file, final byte[] bytes, final IProgressMonitor monitor ) throws CoreException
  {
    // die Vorlagendatei ist klein, deswegen einfach in ein ByteArray serialisieren
    try( ByteArrayInputStream bis = new ByteArrayInputStream( bytes ); )
    {
      if( file.exists() )
        file.setContents( bis, false, true, monitor );
      else
        file.create( bis, false, monitor );

      bis.close();
    }
    catch( final IOException e )
    {
      // should never happen for ByteArrayInputStream
      final IStatus status = new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), "Failed to write bytes", e ); //$NON-NLS-1$
      throw new CoreException( status );
    }
  }

  public ISelectionProvider getSelectionProvider( )
  {
    return m_layerTable;
  }

  public MenuManager getMenuManager( )
  {
    return m_menuManager;
  }

  public LayerTableViewer getLayerTable( )
  {
    return m_layerTable;
  }

  public ILayerTableInput getTableInput( )
  {
    if( m_layerTable == null )
      return null;

    return m_layerTable.getInput();
  }

  protected void fireModellChanged( final ModellEvent modellEvent )
  {
    // Is only used to refresh any actions on this editor... should sometimes be refactored...
    if( modellEvent != null )
      m_eventProvider.fireModellEvent( modellEvent );
  }

  public ModellEventProvider getEventProvider( )
  {
    return m_eventProvider;
  }
}