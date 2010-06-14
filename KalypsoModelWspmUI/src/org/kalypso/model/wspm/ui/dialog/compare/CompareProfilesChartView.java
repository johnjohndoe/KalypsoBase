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
package org.kalypso.model.wspm.ui.dialog.compare;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.chart.ui.editor.mousehandler.AxisDragHandlerDelegate;
import org.kalypso.chart.ui.editor.mousehandler.PlotDragHandlerDelegate;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.ui.view.chart.IProfilChartLayer;
import org.kalypso.model.wspm.ui.view.chart.IProfilChartView;
import org.kalypso.model.wspm.ui.view.chart.IProfilLayerProvider;

import de.openali.odysseus.chart.ext.base.axis.GenericLinearAxis;
import de.openali.odysseus.chart.ext.base.axisrenderer.AxisRendererConfig;
import de.openali.odysseus.chart.ext.base.axisrenderer.GenericAxisRenderer;
import de.openali.odysseus.chart.ext.base.axisrenderer.GenericNumberTickCalculator;
import de.openali.odysseus.chart.ext.base.axisrenderer.NumberLabelCreator;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.impl.ChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.impl.AxisAdjustment;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;

/**
 * @author belger
 * @author kimwerner
 * @author Dirk Kuch
 */
public class CompareProfilesChartView implements IChartPart, IProfilChartView
{
  private AxisDragHandlerDelegate m_axisDragHandler;

  private ChartComposite m_chartComposite = null;

  private PlotDragHandlerDelegate m_plotDragHandler;

  private final IProfilLayerProvider m_layerProvider;

  protected IProfil m_profile;

  public CompareProfilesChartView( final IProfil profile, final IProfilLayerProvider provider )
  {
    m_profile = profile;
    m_layerProvider = provider;
  }

  protected final void activeLayerChanged( final IChartLayer layer )
  {
    // if layer is deactivated do nothing
    if( !layer.isActive() )
      return;
    // otherwise deactivate all others
    for( final IChartLayer l : m_chartComposite.getChartModel().getLayerManager().getLayers() )
    {
      if( l != layer )
        l.setActive( false );
    }
  }

  public void updateLayer( )
  {
    if( m_chartComposite == null )
      return;

    final IChartModel chartModel = m_chartComposite.getChartModel();
    if( chartModel == null )
      return;

    final IMapperRegistry mr = chartModel.getMapperRegistry();
    final IAxis[] existingAxis = mr.getAxes();
    if( existingAxis == null || existingAxis.length == 0 )
    {

      final IAxis[] axis = m_layerProvider.registerAxis( mr );
      if( axis.length > 0 )
        m_layerProvider.registerAxisRenderer( mr );
      else
      {
        /* Register default axis and axis renderer. */
        setDefaultAxis( mr );
      }
    }

    if( m_chartComposite != null && m_chartComposite.getChartModel() != null && m_chartComposite.getChartModel().getLayerManager() != null )
    {
      final ILayerManager lm = m_chartComposite.getChartModel().getLayerManager();

      // remove layer
      for( final IChartLayer layer : lm.getLayers() )
        lm.removeLayer( layer );

      // add layer
      final IProfilChartLayer[] profileLayers = m_layerProvider.createLayers( new IProfilChartView()
      {

        @Override
        public IProfil getProfil( )
        {
          return m_profile;
        }

        @Override
        public ChartComposite getChart( )
        {
          return CompareProfilesChartView.this.getChart();
        }

        @Override
        public void setProfil( final IProfil profile )
        {
        }
      } );

      for( final IProfilChartLayer layer : profileLayers )
        lm.addLayer( layer );
    }
  }

  private void setDefaultAxis( final IMapperRegistry mr )
  {
    final IAxis domainAxis = new GenericLinearAxis( "ID_AXIS_DOMAIN", POSITION.BOTTOM, null );//$NON-NLS-1$
    final AxisAdjustment aaDom = new AxisAdjustment( 3, 94, 3 );
    domainAxis.setPreferredAdjustment( aaDom );

    final IAxis targetAxisLeft = new GenericLinearAxis( "ID_AXIS_LEFT", POSITION.LEFT, null );//$NON-NLS-1$
    final AxisAdjustment aaLeft = new AxisAdjustment( 15, 75, 10 );
    targetAxisLeft.setPreferredAdjustment( aaLeft );

    final IAxis targetAxisRight = new GenericLinearAxis( "ID_AXIS_RIGHT", POSITION.RIGHT, null );//$NON-NLS-1$
    final AxisAdjustment aaRight = new AxisAdjustment( 2, 40, 58 );
    targetAxisRight.setPreferredAdjustment( aaRight );

    domainAxis.setLabel( "[m]" ); //$NON-NLS-1$

    targetAxisLeft.setLabel( "[m+NN]" ); //$NON-NLS-1$
    targetAxisRight.setLabel( "[KS]" ); //$NON-NLS-1$

    mr.addMapper( domainAxis );
    mr.addMapper( targetAxisLeft );
    mr.addMapper( targetAxisRight );

    final AxisRendererConfig configDom = new AxisRendererConfig();
    final IAxisRenderer aRendDom = new GenericAxisRenderer( "rendDom", new NumberLabelCreator( "%s" ), new GenericNumberTickCalculator(), configDom ); //$NON-NLS-1$ //$NON-NLS-2$

    final AxisRendererConfig configLR = new AxisRendererConfig();
    configLR.gap = 5;
    final IAxisRenderer aRendLR = new GenericAxisRenderer( "rendLR", new NumberLabelCreator( "%s" ), new GenericNumberTickCalculator(), configLR ); //$NON-NLS-1$ //$NON-NLS-2$

    mr.setRenderer( "ID_AXIS_DOMAIN", aRendDom );//$NON-NLS-1$
    mr.setRenderer( "ID_AXIS_LEFT", aRendLR );//$NON-NLS-1$
    mr.setRenderer( "ID_AXIS_RIGHT", aRendLR );//$NON-NLS-1$
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.IProfilView#createControl(org.eclipse.swt.widgets.Composite,
   *      org.eclipse.ui.forms.widgets.FormToolkit)
   */
  public Control createControl( final Composite parent )
  {
    m_chartComposite = new ChartComposite( parent, parent.getStyle(), new ChartModel(), new RGB( 255, 255, 255 ) );
    final GridData gD = new GridData( SWT.FILL, SWT.FILL, true, true );
    gD.exclude = true;
    m_chartComposite.setLayoutData( gD );
    m_chartComposite.getChartModel().setHideUnusedAxes( true );

    m_chartComposite.getChartModel().getLayerManager().addListener( new AbstractLayerManagerEventListener()
    {
      /**
       * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onActivLayerChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onActivLayerChanged( final IChartLayer layer )
      {
        activeLayerChanged( layer );
      }
    } );

    m_plotDragHandler = new PlotDragHandlerDelegate( m_chartComposite );
    m_axisDragHandler = new AxisDragHandlerDelegate( m_chartComposite );

    // remove all layers
    final ILayerManager lm = m_chartComposite.getChartModel().getLayerManager();
    for( final IChartLayer layer : lm.getLayers() )
      lm.removeLayer( layer );

    ((GridData) (m_chartComposite.getLayoutData())).exclude = false;

    updateLayer();

    return m_chartComposite;
  }

  public void dispose( )
  {
    if( (m_chartComposite != null) && !m_chartComposite.isDisposed() )
      m_chartComposite.dispose();

    if( m_axisDragHandler != null )
      m_axisDragHandler.dispose();

    if( m_plotDragHandler != null )
      m_plotDragHandler.dispose();
  }

  /**
   * @see org.kalypso.chart.ui.IChartPart#getAdapter(java.lang.Class)
   */
  @Override
  public Object getAdapter( final Class< ? > clazz )
  {
    if( IChartPart.class.equals( clazz ) )
    {
      return this;
    }

    return null;
  }

  public IAxis getAxis( final String id )
  {
    return m_chartComposite.getChartModel().getMapperRegistry().getAxis( id );
  }

  /**
   * @see org.kalypso.chart.ui.IChartPart#getAxisDragHandler()
   */

  @Override
  public AxisDragHandlerDelegate getAxisDragHandler( )
  {
    return m_axisDragHandler;
  }

  @Override
  public ChartComposite getChart( )
  {
    return m_chartComposite;
  }

  /**
   * @see org.kalypso.chart.ui.IChartPart#getChartComposite()
   */
  @Override
  public ChartComposite getChartComposite( )
  {
    return m_chartComposite;
  }

  /**
   * @see org.kalypso.chart.ui.IChartPart#getPlotDragHandler()
   */
  @Override
  public PlotDragHandlerDelegate getPlotDragHandler( )
  {
    return m_plotDragHandler;
  }

  protected void redrawChart( )
  {
    final ChartComposite chart = m_chartComposite;
    if( (chart != null) && !chart.isDisposed() )
      chart.getDisplay().syncExec( new Runnable()
      {

        @Override
        public void run( )
        {
          chart.redraw();
        }
      } );
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.chart.IProfilChartView#getProfil()
   */
  @Override
  public IProfil getProfil( )
  {
    return m_profile;
  }

  @Override
  public synchronized void setProfil( final IProfil profile )
  {
    if( m_profile == profile )
      return;

    m_profile = profile;
    if( m_profile == null )
    {
      ((GridData) (m_chartComposite.getLayoutData())).exclude = true;
      final ILayerManager lm = m_chartComposite.getChartModel().getLayerManager();
      lm.dispose();
    }
    else
    {
      if( m_chartComposite != null && !m_chartComposite.isDisposed() )
      {
        ((GridData) (m_chartComposite.getLayoutData())).exclude = false;
        updateLayer();
      }
    }
  }
}