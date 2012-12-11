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
package org.kalypso.model.wspm.ui.view.chart;

import java.awt.Insets;

import org.kalypso.model.wspm.core.gml.IProfileSelection;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileListener;
import org.kalypso.model.wspm.core.profil.ProfileListenerAdapter;
import org.kalypso.model.wspm.core.profil.changes.ProfileChangeHint;

import de.openali.odysseus.chart.framework.model.impl.ChartModel;
import de.openali.odysseus.chart.framework.model.impl.settings.IBasicChartSettings;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;

/**
 * @author kimwerner
 */
public class ProfilChartModel extends ChartModel
{
  private final IProfileListener m_profileListener = new ProfileListenerAdapter()
  {
    @Override
    public void onProfilChanged( final ProfileChangeHint hint )
    {
      if( hint.isObjectChanged() )
      {
        updateLayers();
      }
      else if( hint.isPointPropertiesChanged() )
      {
        handlePropertyOrBuildingChanged();
      }
      // REMARK: we directly redraw trhe chart for any selection changes here; before this was managed by the selction handler,
      // but listeners never got unregistered. This is a better place.
      else if( /* hint.isSelectionChanged() || */hint.isSelectionCursorChanged() )
      {
        getLayerManager().getEventHandler().redrawRequested();
      }
      else
      {
        for( final IChartLayer layer : getLayerManager().getLayers() )
        {
          ((IProfilChartLayer)layer).onProfilChanged( hint );
        }
      }
    }
  };

  private final IProfileSelection m_profileSelection;

  private final IProfilLayerProvider m_layerProvider;

  public ProfilChartModel( final IProfilLayerProvider layerProvider, final IProfileSelection profileSelection )
  {
    m_layerProvider = layerProvider;
    m_profileSelection = profileSelection;

    if( m_profileSelection != null && m_layerProvider != null )
    {
      m_profileSelection.addProfilListener( m_profileListener );
      m_layerProvider.registerAxis( getAxisRegistry() );
      updateLayers();
    }

    final IBasicChartSettings settings = getSettings();
    settings.setChartInsets( new Insets( 10, 0, 0, 0 ) );
    settings.setPlotInsets( new Insets( 0, 0, 0, 0 ) );
  }

  @Override
  public void dispose( )
  {
    if( m_profileSelection != null )
      m_profileSelection.removeProfileListener( m_profileListener );
  }

  public final IProfilChartLayer getLayer( final String layerID )
  {
    final IChartLayer layer = getLayerManager().findLayer( layerID );
    if( layer instanceof IProfilChartLayer )
      return (IProfilChartLayer)layer;

    return null;
  }

  public IProfileSelection getProfileSelection( )
  {
    return m_profileSelection;
  }

  void handlePropertyOrBuildingChanged( )
  {
    updateLayers();
  }

  /**
   * Recreate all layers.
   */
  synchronized void updateLayers( )
  {
    final ILayerManager layerManager = getLayerManager();
    layerManager.clear();

    if( m_layerProvider == null )
      return;

    final IProfile profile = m_profileSelection.getProfile();
    if( profile != null )
    {
      final Object result = m_profileSelection.getResult();
      final IProfilChartLayer[] layers = m_layerProvider.createLayers( profile, result );
      layerManager.addLayer( layers );
    }
  }
}