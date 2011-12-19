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

import java.net.URL;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.kalypso.chart.ui.editor.mousehandler.ZoomPanMaximizeHandler;
import org.kalypso.chart.ui.editor.mousehandler.ZoomPanMaximizeHandler.DIRECTION;
import org.kalypso.chart.ui.internal.workbench.ChartPartComposite;
import org.kalypso.zml.core.base.IMultipleZmlSourceElement;
import org.kalypso.zml.core.diagram.base.ChartTypeHandler;
import org.kalypso.zml.core.diagram.base.visitors.ResetZmlLayerVisitor;
import org.kalypso.zml.ui.chart.layer.visitor.SingleGridVisibilityVisitor;

import de.openali.odysseus.chart.factory.config.ChartExtensionLoader;
import de.openali.odysseus.chart.factory.config.ChartFactory;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Dirk Kuch
 */
public class ZmlDiagramChartPartComposite extends ChartPartComposite
{
  private final URL m_template;

  public ZmlDiagramChartPartComposite( final IWorkbenchPart part, final URL template )
  {
    super( part );

    m_template = template;
  }

  @Override
  public void loadInput( final IEditorInput input )
  {
    try
    {
      final IChartModel model = getChartModel();
      model.clear();

      final ChartTypeHandler handler = new ChartTypeHandler( m_template ); //$NON-NLS-1$
      ChartFactory.doConfiguration( model, handler.getReferenceResolver(), handler.getChartType(), ChartExtensionLoader.getInstance(), handler.getContext() );
    }
    catch( final Throwable t )
    {
      t.printStackTrace();
    }

    // FIXME load strategy by .kod property (extension point)
    /**
     * update data handler and visibility
     */
    final ILayerManager layerManager = getChartModel().getLayerManager();
    layerManager.accept( new UpdateRequestStrategyVisitor() );

    updateControl();
    setDirty( false );

  }

  @Override
  public Composite createControl( final Composite parent )
  {
    loadInput( null );

    final Composite control = super.createControl( parent );
    control.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );

    final IChartComposite chartComposite = getChartComposite();
    final ZoomPanMaximizeHandler handler = new ZoomPanMaximizeHandler( chartComposite, DIRECTION.eBoth );
    chartComposite.getPlotHandler().activatePlotHandler( handler );

    return control;
  }

  public void setSelection( final IMultipleZmlSourceElement[] selection )
  {
    reset();

    // TODO
// final String title = delegate.getTitle();
// updateDiagramTitle( title );

    final IChartModel model = getChartModel();
    DiagramCompositeSelection.doApply( model, selection );

    final ILayerManager layerManager = model.getLayerManager();
    layerManager.accept( new HideUnuseLayersVisitor() );
    layerManager.accept( new SingleGridVisibilityVisitor() );

    model.autoscale();
  }

  public void reset( )
  {
    final ILayerManager layerManager = getChartModel().getLayerManager();
    layerManager.accept( new ResetZmlLayerVisitor() );
  }

}
