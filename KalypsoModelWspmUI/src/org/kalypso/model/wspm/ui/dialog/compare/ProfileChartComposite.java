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
package org.kalypso.model.wspm.ui.dialog.compare;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.chart.ui.editor.mousehandler.PlotDragHandlerDelegate;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.IWspmLayers;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.util.ProfilUtil;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIExtensions;
import org.kalypso.model.wspm.ui.commands.MousePositionChartHandler;
import org.kalypso.model.wspm.ui.view.chart.IProfilChart;
import org.kalypso.model.wspm.ui.view.chart.IProfilLayerProvider;
import org.kalypso.model.wspm.ui.view.chart.ProfilChartModel;
import org.kalypso.observation.result.IRecord;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.IChartModelState;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.impl.ChartModelState;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.impl.ChartImageComposite;

/**
 * @author belger
 * @author kimwerner
 * @author Dirk Kuch
 */
public class ProfileChartComposite extends ChartImageComposite implements IProfilChart
{
  private static final RGB BACKGROUND_RGB = new RGB( 255, 255, 255 );

  private IProfilLayerProvider m_profilLayerProvider = null;

  private ProfilChartModel m_profilChartModel = null;

  public ProfileChartComposite( final Composite parent, final int style, final IProfilLayerProvider layerProvider, final IProfil profile )
  {
    super( parent, style, null, BACKGROUND_RGB );

    m_profilLayerProvider = layerProvider;
    new PlotDragHandlerDelegate( this );

    invalidate( profile, null );

    getPlotHandler().addPlotHandler( new MousePositionChartHandler( this ) );
  }

  /**
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_profilChartModel != null )
    {
      m_profilChartModel.dispose();
    }
    super.dispose();
  }

  /**
   * @see de.openali.odysseus.chart.framework.view.impl.ChartImageComposite#doInvalidateChart()
   */
  @Override
  protected IStatus doInvalidateChart( )
  {
    final IChartLayer layer = getChartModel().getLayerManager().findLayer( IWspmLayers.LAYER_GELAENDE );

    final IRecord point = getSelectedPoint( layer );
    if( point != null )
    {
      getProfil().getSelection().setActivePoint( point );
    }

    return super.doInvalidateChart();
  }

  @Override
  public IChartComposite getChartComposite( )
  {
    return this;
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.chart.IProfilChartView#getProfil()
   */
  @Override
  public IProfil getProfil( )
  {
    if( Objects.isNull( m_profilChartModel ) )
      return null;

    return m_profilChartModel.getProfil();
  }

  protected IProfilLayerProvider getProfilLayerProvider( final IProfil profile )
  {
    if( Objects.isNotNull( m_profilLayerProvider ) )
      return m_profilLayerProvider;
    else if( Objects.isNotNull( profile ) )
    {
      m_profilLayerProvider = KalypsoModelWspmUIExtensions.createProfilLayerProvider( profile.getType() );
    }
    else if( Objects.isNotNull( getProfil() ) )
    {
      m_profilLayerProvider = KalypsoModelWspmUIExtensions.createProfilLayerProvider( getProfil().getType() );
    }

    return m_profilLayerProvider;
  }

  final IRecord getSelectedPoint( final IChartLayer layer )
  {
    final ICoordinateMapper cm = layer == null ? null : layer.getCoordinateMapper();
    final IAxis domAxis = cm == null ? null : cm.getDomainAxis();
    final IAxis valAxis = cm == null ? null : cm.getTargetAxis();
    final DataRange<Number> activeDom = domAxis == null ? null : domAxis.getSelection();
    final DataRange<Number> activeVal = valAxis == null ? null : valAxis.getSelection();

    if( activeDom == null || activeVal == null || getProfil() == null )
      return null;

    for( final IRecord point : getProfil().getPoints() )
    {
      final Double hoehe = ProfilUtil.getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_HOEHE, point );
      final Double breite = ProfilUtil.getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_BREITE, point );
      if( hoehe.isNaN() || breite.isNaN() )
      {
        continue;
      }
      final Double deltaX = Math.abs( activeDom.getMin().doubleValue() - activeDom.getMax().doubleValue() );
      final IRecord record = ProfilUtil.findPoint( getProfil(), activeDom.getMin().doubleValue() + deltaX / 2, deltaX );
      if( record != null && record != getProfil().getSelection().getActivePoint() )
      {
        if( hoehe > activeVal.getMin().doubleValue() && hoehe < activeVal.getMax().doubleValue() && breite > activeDom.getMin().doubleValue() && breite < activeDom.getMax().doubleValue() )
          return record;
      }
    }
    return null;
  }

  public void invalidate( final IProfil profile, final Object result )
  {
    if( isDisposed() )
      return;

    // FIXME: bad and ugly! we should keep only one model, m_chartModel; not two references to the same thing

    final IChartModel oldModel = m_profilChartModel;
    final IProfil oldProfile = m_profilChartModel == null ? null : m_profilChartModel.getProfil();

    if( profile != null && profile == oldProfile )
      return;

    final IChartModelState state = new ChartModelState();
    final IChartModel chartModel = getChartModel();
    state.storeState( chartModel );
    if( m_profilChartModel != null )
    {
      m_profilChartModel.dispose();
    }

    m_profilChartModel = new ProfilChartModel( getProfilLayerProvider( profile ), profile, result );
    if( state != null )
    {
      state.restoreState( m_profilChartModel );
    }

    // TODO: don't autoscale, restore zoom instead
    m_profilChartModel.autoscale();
    setChartModel( oldModel, m_profilChartModel );
  }

  @Override
  public void setProfil( final IProfil profile, final Object result )
  {
    synchronized( this )
    {
      invalidate( profile, result );
    }
  }

}