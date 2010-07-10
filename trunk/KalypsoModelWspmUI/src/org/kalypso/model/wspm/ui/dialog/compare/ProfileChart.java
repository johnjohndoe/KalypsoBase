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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.chart.ui.editor.mousehandler.AxisDragHandlerDelegate;
import org.kalypso.chart.ui.editor.mousehandler.PlotDragHandlerDelegate;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIExtensions;
import org.kalypso.model.wspm.ui.view.chart.IProfilChart;
import org.kalypso.model.wspm.ui.view.chart.IProfilLayerProvider;
import org.kalypso.model.wspm.ui.view.chart.ProfilChartModel;

import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.util.ChartUtilities;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;

/**
 * @author belger
 * @author kimwerner
 * @author Dirk Kuch
 */
public class ProfileChart extends Composite implements IProfilChart
{
  private static final RGB BACKGROUND_RGB = new RGB( 255, 255, 255 );

  private AxisDragHandlerDelegate m_axisDragHandler;

  private ChartComposite m_chartComposite = null;

  private PlotDragHandlerDelegate m_plotDragHandler;

  protected IProfil m_profile;

  private final IProfilLayerProvider m_layerProvider;

  private ProfilChartModel m_chartmodel;

  public ProfileChart( final Composite parent, final IProfil profile )
  {
    this( parent, KalypsoModelWspmUIExtensions.createProfilLayerProvider( profile.getType() ), profile );
  }

  public ProfileChart( final Composite parent, final IProfilLayerProvider layerProvider, final IProfil profile )
  {
    super( parent, SWT.NULL );

    this.setLayout( new GridLayout() );

    m_layerProvider = layerProvider;
    m_profile = profile;

    bootstrap();
  }

  private void bootstrap( )
  {
    m_chartComposite = new ChartComposite( this, this.getStyle(), null, BACKGROUND_RGB );
    m_chartComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    m_plotDragHandler = new PlotDragHandlerDelegate( m_chartComposite );
    m_axisDragHandler = new AxisDragHandlerDelegate( m_chartComposite );

    update();
  }

  @Override
  public void update( )
  {
    if( m_chartComposite == null || m_chartComposite.isDisposed() )
      return;
    final IProfil oldProfile = m_chartmodel == null ? null : m_chartmodel.getProfil();

    if( m_profile == oldProfile )
      return;
    if( oldProfile != null )
      oldProfile.removeProfilListener( m_chartmodel );
    m_chartmodel = new ProfilChartModel( m_layerProvider, m_profile, null );
    if( m_profile != null )
      m_profile.addProfilListener( m_chartmodel );

    m_chartmodel.autoscale( null );
    m_chartComposite.setChartModel( m_chartmodel );

  }

  /**
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose( )
  {
    if( (m_chartComposite != null) && !m_chartComposite.isDisposed() )
      m_chartComposite.dispose();

    if( m_profile != null )
      m_profile.removeProfilListener( m_chartmodel );
  }

  @Override
  public ChartComposite getChart( )
  {
    return m_chartComposite;
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

    update();

  }

}