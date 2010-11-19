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

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.chart.ui.editor.mousehandler.AxisDragHandlerDelegate;
import org.kalypso.chart.ui.editor.mousehandler.PlotDragHandlerDelegate;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIExtensions;
import org.kalypso.model.wspm.ui.view.chart.IProfilChart;
import org.kalypso.model.wspm.ui.view.chart.IProfilLayerProvider;
import org.kalypso.model.wspm.ui.view.chart.ProfilChartModel;

import de.openali.odysseus.chart.framework.model.IChartModelState;
import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;

/**
 * @author belger
 * @author kimwerner
 * @author Dirk Kuch
 */
public class ProfileChartComposite extends ChartComposite implements IProfilChart
{
  private static final RGB BACKGROUND_RGB = new RGB( 255, 255, 255 );

  private IProfilLayerProvider m_profilLayerProvider = null;

  protected IProfilLayerProvider getProfilLayerProvider( final IProfil profile )
  {
    if( m_profilLayerProvider != null )
      return m_profilLayerProvider;
    if( profile != null )
    {
      m_profilLayerProvider = KalypsoModelWspmUIExtensions.createProfilLayerProvider( profile.getType() );
    }
    else if( getProfil() != null )
    {
      m_profilLayerProvider = KalypsoModelWspmUIExtensions.createProfilLayerProvider( getProfil().getType() );
    }
    return m_profilLayerProvider;
  }

  private ProfilChartModel m_profilChartModel = null;

  public ProfileChartComposite( final Composite parent, final int style, final IProfilLayerProvider layerProvider, final IProfil profile )
  {
    super( parent, style, null, BACKGROUND_RGB );

    m_profilLayerProvider = layerProvider;
    new PlotDragHandlerDelegate( this );
    new AxisDragHandlerDelegate( this );

    invalidate( profile, null );
  }

  /**
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose( )
  {

    if( m_profilChartModel != null )
      m_profilChartModel.dispose();
    super.dispose();
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.chart.IProfilChart#getChart()
   */

  @Override
  public IChartComposite getChart( )
  {
    return this;
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.chart.IProfilChartView#getProfil()
   */
  @Override
  public IProfil getProfil( )
  {
    return m_profilChartModel == null ? null : m_profilChartModel.getProfil();
  }

  public void invalidate( final IProfil profile, final Object result )
  {
    if( isDisposed() )
      return;

    final IProfil oldProfile = m_profilChartModel == null ? null : m_profilChartModel.getProfil();

    if( profile != null && profile == oldProfile )
      return;

    final IChartModelState state = m_profilChartModel == null ? null : m_profilChartModel.getState();
    if( m_profilChartModel != null )
      m_profilChartModel.dispose();

    m_profilChartModel = new ProfilChartModel( getProfilLayerProvider( profile ), profile, result );
    if( state != null )
      state.restoreState( m_profilChartModel );
    // TODO: don't autoscale, restore zoom instead
    m_profilChartModel.autoscale( null );
    setChartModel( m_profilChartModel );
  }

  @Override
  public synchronized void setProfil( final IProfil profile, final Object result )
  {
    invalidate( profile, result );
  }

}