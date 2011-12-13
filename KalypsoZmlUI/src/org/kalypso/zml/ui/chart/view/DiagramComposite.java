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
package org.kalypso.zml.ui.chart.view;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.services.IServiceLocator;
import org.kalypso.chart.ui.editor.mousehandler.ZoomPanMaximizeHandler;
import org.kalypso.chart.ui.editor.mousehandler.ZoomPanMaximizeHandler.DIRECTION;
import org.kalypso.contribs.eclipse.jface.action.ContributionUtils;
import org.kalypso.contribs.eclipse.jface.wizard.IUpdateable;
import org.kalypso.contribs.eclipse.swt.layout.Layouts;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.IObservationListener;
import org.kalypso.zml.core.diagram.base.ChartTypeHandler;
import org.kalypso.zml.core.diagram.base.visitors.ResetZmlLayerVisitor;
import org.kalypso.zml.ui.debug.KalypsoZmlUiDebug;

import de.openali.odysseus.chart.factory.config.ChartExtensionLoader;
import de.openali.odysseus.chart.factory.config.ChartFactory;
import de.openali.odysseus.chart.framework.model.impl.ChartModel;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.impl.ChartImageComposite;

/**
 * @author Dirk Kuch
 */
public class DiagramComposite extends Composite implements IUpdateable, IObservationListener
{
//  private static final String TOOLBAR_DATENAUSTAUSCH_DIALOG = "toolbar:org.kalypso.hwv.product.sachsenanhalt.zml.diagram.datenaustauschdialog.menu"; //$NON-NLS-1$

  private static final RGB CHART_BACKGROUND = new RGB( 255, 255, 255 );

  protected ChartModel m_model = new ChartModel();

  protected ChartImageComposite m_chartComposite;

  private IObservation m_selection;

  private final IServiceLocator m_context;

  public DiagramComposite( final Composite parent, final FormToolkit toolkit, final IServiceLocator context )
  {
    super( parent, SWT.BORDER );

    m_context = context;

    final GridLayout layout = Layouts.createGridLayout();
    layout.verticalSpacing = 0;
    setLayout( layout );

    init();

    draw( toolkit );

    m_model.autoscale( new IAxis[] {} );
  }

  public IChartComposite getChartComposite( )
  {
    return m_chartComposite;
  }

  private void init( )
  {
    try
    {
      final ChartTypeHandler handler = new ChartTypeHandler( getClass().getResource( "templates/diagram.kod" ) ); //$NON-NLS-1$
      ChartFactory.doConfiguration( m_model, handler.getReferenceResolver(), handler.getChartType(), ChartExtensionLoader.getInstance(), handler.getContext() );
    }
    catch( final Throwable t )
    {
      t.printStackTrace();
    }

    /**
     * update data handler and visibility
     */
    final ILayerManager layerManager = m_model.getLayerManager();
    // TODO
// layerManager.accept( new SetDataHandlerVisitor( this ) );

// final String parameterType = m_structure.getParameterType();
// m_model.getMapperRegistry().accept( new HideUnusedAxisVisitor( parameterType ) );
// layerManager.accept( new UpdateVisibilityVisitor( parameterType ) );

  }

  private void draw( final FormToolkit toolkit )
  {
    createToolbar( this, toolkit );

    m_chartComposite = new ChartImageComposite( this, SWT.BORDER, m_model, CHART_BACKGROUND );
    m_chartComposite.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );

    final ZoomPanMaximizeHandler handler = new ZoomPanMaximizeHandler( m_chartComposite, DIRECTION.eBoth );
    m_chartComposite.getPlotHandler().activatePlotHandler( handler );

    toolkit.adapt( this );
  }

  private void createToolbar( final Composite body, final FormToolkit toolkit )
  {
    // FIXME
    if( !KalypsoZmlUiDebug.DEBUG_DIAGRAM.isEnabled() )
      return;

    final ToolBarManager manager = new ToolBarManager( SWT.HORIZONTAL | SWT.FLAT );

    final ToolBar control = manager.createControl( body );
    control.setLayoutData( new GridData( SWT.RIGHT, GridData.FILL, true, false ) );

    // TODO toolbar
// for( final String reference : contributions )
// {
// ContributionUtils.populateContributionManager( PlatformUI.getWorkbench(), manager, reference );
// }

    if( KalypsoZmlUiDebug.DEBUG_DIAGRAM.isEnabled() )
    {
      ContributionUtils.populateContributionManager( PlatformUI.getWorkbench(), manager, "toolbar:org.kalypso.zml.ui.chart.view.debug" ); //$NON-NLS-1$
    }

    manager.update( true );

    toolkit.adapt( control );
  }

  public void setSelection( final IZmlDiagramSelectionBuilder delegate )
  {
    reset();

    delegate.doSelectionUpdate( m_model );
  }

  @Override
  public final void dispose( )
  {
    m_chartComposite.dispose();
    m_model.dispose();
  }

  public void reset( )
  {
    final ILayerManager layerManager = m_model.getLayerManager();
    layerManager.accept( new ResetZmlLayerVisitor() );
  }

  @Override
  public void observationChanged( final IObservation obs, final Object source )
  {
    final UIJob job = new UIJob( "Observation changed - invalidating diagram" )
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        m_chartComposite.invalidate();

        return Status.OK_STATUS;
      }
    };

    job.setUser( false );
    job.setSystem( true );

    job.schedule();

  }
}
