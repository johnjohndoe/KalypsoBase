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
package org.kalypso.chart.ui.workbench;

import java.net.URL;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.chart.ui.editor.ChartEditorTreeOutlinePage;
import org.kalypso.chart.ui.editor.ChartPartListener;
import org.kalypso.chart.ui.i18n.Messages;
import org.kalypso.chart.ui.internal.OdysseusChartUiPlugin;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.swt.widgets.ControlUtils;
import org.kalypso.contribs.eclipse.ui.IPropertyPart;

import de.openali.odysseus.chart.factory.config.ChartConfigurationLoader;
import de.openali.odysseus.chart.factory.config.ChartExtensionLoader;
import de.openali.odysseus.chart.factory.config.ChartFactory;
import de.openali.odysseus.chart.factory.config.IExtensionLoader;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.AbstractMapperRegistryEventListener;
import de.openali.odysseus.chart.framework.model.impl.ChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;
import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.impl.ChartImageComposite;
import de.openali.odysseus.chartconfig.x020.ChartType;

/**
 * @author Gernot Belger
 */
public class ChartPartComposite implements IChartPart
{
  private final ChartModel m_chartModel = new ChartModel();

  private ChartPartListener m_chartPartListener;

  protected final IWorkbenchPart m_part;

  private Composite m_composite = null;

  private ChartEditorTreeOutlinePage m_outlinePage = null;

  private ChartImageComposite m_chartComposite = null;

  private boolean m_dirty = false;

  public ChartPartComposite( final IWorkbenchPart part )
  {
    m_part = part;

    final AbstractMapperRegistryEventListener mapperRegistryListener = new AbstractMapperRegistryEventListener()
    {
      @Override
      public void onAxisChanged( final IAxis< ? > axis )
      {
        setDirty( true );
      }
    };
    m_chartModel.getAxisRegistry().addListener( mapperRegistryListener );

    final AbstractLayerManagerEventListener layerManagerListener = new AbstractLayerManagerEventListener()
    {
      @Override
      public void onLayerVisibilityChanged( final IChartLayer layer )
      {
        setDirty( true );
      }

      @Override
      public void onLayerMoved( final IChartLayer layer )
      {
        setDirty( true );
      }
    };

    m_chartModel.getLayerManager().addListener( layerManagerListener );
  }

  public void dispose( )
  {
    if( m_chartPartListener != null )
    {
      m_chartPartListener.dispose();
      m_part.getSite().getPage().removePartListener( m_chartPartListener );
      m_chartPartListener = null;
    }

    if( m_outlinePage != null )
    {
      m_outlinePage.dispose();
      m_outlinePage = null;
    }

    m_chartModel.dispose();
  }

  public IChartModel getChartModel( )
  {
    return m_chartModel;
  }

  public void init( final IWorkbenchPartSite site )
  {
    m_chartPartListener = new ChartPartListener( m_part, site );
    site.getPage().addPartListener( m_chartPartListener );
  }

  @Override
  public IChartComposite getChartComposite( )
  {
    return m_chartComposite;
  }

  public void loadInput( final IEditorInput input )
  {
    // prepare for exception
    m_chartModel.clear();

    try
    {
      if( input instanceof IFileEditorInput )
      {
        final IStorage storage = ((IStorageEditorInput) input).getStorage();
        final ChartConfigurationLoader loader = new ChartConfigurationLoader( storage );
        final IExtensionLoader cel = ChartExtensionLoader.getInstance();
        final ChartType chart = loader.getCharts()[0];

        final IFile file = ((IFileEditorInput) input).getFile();
        final URL context = ResourceUtilities.createURL( file );

        ChartFactory.doConfiguration( m_chartModel, loader, chart, cel, context );
      }
      else if( input instanceof IDatabaseStorageEditorInput )
      {
        final IStorage storage = ((IStorageEditorInput) input).getStorage();
        final ChartConfigurationLoader loader = new ChartConfigurationLoader( storage );
        final IExtensionLoader cel = ChartExtensionLoader.getInstance();
        final ChartType chart = loader.getCharts()[0];
        ChartFactory.doConfiguration( m_chartModel, loader, chart, cel, null );
      }
    }
    catch( final Exception e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e );
      OdysseusChartUiPlugin.getDefault().getLog().log( status );
      final Shell shell = m_part.getSite().getShell();
      ErrorDialog.openError( shell, Messages.getString( "org.kalypso.chart.ui.editor.ChartEditor.0" ), Messages.getString( "org.kalypso.chart.ui.editor.ChartEditor.1" ), status ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    m_chartModel.getBehaviour().setAutoscale( true );
    updateControl();

    setDirty( false );
  }

  public void setFocus( )
  {
    if( m_composite != null )
      m_composite.setFocus();
  }

  public Composite createControl( final Composite parent )
  {
    m_composite = new Composite( parent, SWT.NONE );
    m_composite.setLayout( new FillLayout() );

    updateControl();

    return m_composite;
  }

  /**
   * Totally refreshes the control based on the contents of {@link m_config.}
   */
  protected void updateControl( )
  {
    if( m_composite == null || m_composite.isDisposed() )
      return;

    /* Reset controls */
    ControlUtils.disposeChildren( m_composite );
    if( m_chartPartListener != null )
      m_chartPartListener.setChart( null );

    /* Dispose old model */
    if( m_chartComposite != null )
    {
      final IChartModel chartModel = m_chartComposite.getChartModel();
      if( chartModel != null )
        chartModel.dispose();
    }

    final boolean hasChart = hasChart();

    if( hasChart )
    {
      createChart();

    }
    else
    {
      final Label label = new Label( m_composite, SWT.NONE );
      label.setText( Messages.getString( "org.kalypso.chart.ui.editor.ChartEditor.6" ) ); //$NON-NLS-1$
    }

    if( m_chartPartListener != null )
      m_chartPartListener.setChart( m_chartComposite );

    m_composite.layout( true, true );
    if( m_outlinePage != null )
      m_outlinePage.updateControl();
  }

  private boolean hasChart( )
  {
    final IChartLayer[] layers = m_chartModel.getLayerManager().getLayers();
    return layers.length > 0;
  }

  private void createChart( )
  {
    m_chartComposite = new ChartImageComposite( m_composite, SWT.BORDER, m_chartModel, new RGB( 255, 255, 255 ) );
    // drag delegates
    m_composite.layout();
  }

  public IContentOutlinePage getOutlinePage( )
  {
    if( m_outlinePage == null && getChartComposite() != null )
    {
      m_outlinePage = new ChartEditorTreeOutlinePage();
      m_outlinePage.setModel( m_chartModel );
    }

    return m_outlinePage;
  }

  public boolean isDirty( )
  {
    return m_dirty;
  }

  public void setDirty( final boolean dirty )
  {
    if( m_dirty == dirty )
      return;

    m_dirty = dirty;

    if( m_part instanceof IPropertyPart )
    {
      /* force into ui threat */
      final UIJob job = new UIJob( "" ) //$NON-NLS-1$
      {
        @Override
        public IStatus runInUIThread( final IProgressMonitor monitor )
        {
          final IPropertyPart part = (IPropertyPart) m_part;
          part.firePropertyChange( IEditorPart.PROP_DIRTY );

          return Status.OK_STATUS;
        }
      };
      job.setUser( false );
      job.setSystem( true );

      job.schedule();
    }

  }

  public String getPartName( )
  {
    final TitleTypeBean[] title = m_chartModel.getSettings().getTitles();
    if( ArrayUtils.isEmpty( title ) )
      // TODO: check: should return the default title defined in the extension ('Chart View')
      return m_part.getTitle();

    return title[0].getText();
  }

  public Object adapt( final Class< ? > adapter )
  {
    if( IContentOutlinePage.class.equals( adapter ) )
      return getOutlinePage();

    if( IChartComposite.class.equals( adapter ) )
      return getChartComposite();

    if( IChartPart.class.equals( adapter ) )
      return this;

    return null;
  }
}