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

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.gml.IProfileSelection;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIExtensions;
import org.kalypso.model.wspm.ui.commands.MousePositionChartHandler;
import org.kalypso.model.wspm.ui.view.chart.IProfilChart;
import org.kalypso.model.wspm.ui.view.chart.IProfilLayerProvider;
import org.kalypso.model.wspm.ui.view.chart.ProfilChartModel;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.IChartHandlerManager;
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

  // HACK: use non null model to prevent NPE's everywhere if there is no profile initially
  // TODO: use null instead; all handlers need to deactivate, if no model is present
  private ProfilChartModel m_profilChartModel = new ProfilChartModel( getProfilLayerProvider( null ), null );

  public ProfileChartComposite( final Composite parent, final int style, final IProfilLayerProvider layerProvider, final IProfileSelection profileSelection )
  {
    super( parent, style, null, BACKGROUND_RGB );

    m_profilLayerProvider = layerProvider;

    setChartModel( null, m_profilChartModel );

    invalidate( profileSelection );

    final IChartHandlerManager plotHandler = getPlotHandler();
    plotHandler.addPlotHandler( new MousePositionChartHandler( this ) );
  }

  @Override
  public void dispose( )
  {
    if( Objects.isNotNull( m_profilChartModel ) )
      m_profilChartModel.dispose();

    super.dispose();
  }

  @Override
  public IChartComposite getChartComposite( )
  {
    return this;
  }

  @Override
  public IProfileSelection getProfileSelection( )
  {
    if( Objects.isNull( m_profilChartModel ) )
      return null;

    return m_profilChartModel.getProfileSelection();
  }

  private void invalidate( final IProfileSelection profileSelection )
  {
    if( isDisposed() )
      return;

    // FIXME: bad and ugly! we should keep only one model, m_chartModel; not two references to the same thing
    final IChartModel oldModel = m_profilChartModel;
    final IProfileSelection oldProfileSelection = m_profilChartModel == null ? null : m_profilChartModel.getProfileSelection();

    final IProfile profile = profileSelection != null ? profileSelection.getProfile() : null;
    final IProfile oldProfile = oldProfileSelection != null ? oldProfileSelection.getProfile() : null;
    if( ObjectUtils.equals( profile, oldProfile ) )
      return;

    if( m_profilChartModel != null )
      m_profilChartModel.dispose();

    m_profilChartModel = new ProfilChartModel( getProfilLayerProvider( profile ), profileSelection );

    // TODO: don't autoscale, restore zoom instead
    m_profilChartModel.autoscale();

    setChartModel( oldModel, m_profilChartModel );
  }

  private IProfilLayerProvider getProfilLayerProvider( final IProfile profile )
  {
    if( m_profilLayerProvider != null )
      return m_profilLayerProvider;

    if( profile == null )
      return null;

    return KalypsoModelWspmUIExtensions.createProfilLayerProvider( profile.getType() );
  }

  @Override
  public synchronized void setProfileSelection( final IProfileSelection profileSelection )
  {
    invalidate( profileSelection );
  }
}