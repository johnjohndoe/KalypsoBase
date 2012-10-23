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

import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileListener;
import org.kalypso.model.wspm.core.profil.ProfileListenerAdapter;
import org.kalypso.model.wspm.core.profil.changes.ProfileChangeHint;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIExtensions;

import de.openali.odysseus.chart.framework.model.impl.ChartModel;
import de.openali.odysseus.chart.framework.model.impl.ChartModelState;
import de.openali.odysseus.chart.framework.model.impl.settings.IBasicChartSettings;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;

/**
 * @author kimwerner
 */
public class ProfilChartModel extends ChartModel
{
  private final IProfileListener m_profilListener = new ProfileListenerAdapter()
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

      else if( /*hint.isSelectionChanged() || */hint.isSelectionCursorChanged() )
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

  private final IProfile m_profil;

  private IProfilLayerProvider m_layerProvider;

  private final Object m_result;

  public ProfilChartModel( final IProfilLayerProvider layerProvider, final IProfile profil, final Object result )
  {
    m_layerProvider = layerProvider;
    m_profil = profil;
    m_result = result;

    if( m_profil != null && m_layerProvider != null )
    {
      m_profil.addProfilListener( m_profilListener );
      m_layerProvider.registerAxis( getMapperRegistry() );
      updateLayers();
    }

    final IBasicChartSettings settings = getSettings();

    settings.setChartInsets( new Insets( 10, 0, 10, 0 ) );
    settings.setPlotInsets( new Insets( 0, 0, 0, 0 ) );
  }

  @Override
  public void dispose( )
  {
    if( m_profil != null )
      m_profil.removeProfilListener( m_profilListener );
  }

  public final IProfilChartLayer getLayer( final String layerID )
  {
    final IChartLayer layer = getLayerManager().findLayer( layerID );
    if( layer instanceof IProfilChartLayer )
      return (IProfilChartLayer)layer;

    return null;
  }

  public IProfile getProfil( )
  {
    return m_profil;
  }

  public Object getResult( )
  {
    return m_result;
  }

  protected void handlePropertyOrBuildingChanged( )
  {
    updateLayers();
  }

  /**
   * Recreate all layers.
   */
  protected void updateLayers( )
  {
    synchronized( this )
    {
      final ChartModelState modelState = new ChartModelState();
      modelState.storeState( this );
      final ILayerManager layerManager = getLayerManager();
      layerManager.clear();

      final IProfilLayerProvider lp = getProfilLayerProvider();
      if( lp == null )
        return;

      final IProfilChartLayer[] layers = lp.createLayers( m_profil, m_result );
      layerManager.addLayer( layers );

      modelState.restoreState( this );
    }
  }

  protected IProfilLayerProvider getProfilLayerProvider( )
  {
    if( m_layerProvider != null )
      return m_layerProvider;

    if( getProfil() != null )
    {
      m_layerProvider = KalypsoModelWspmUIExtensions.createProfilLayerProvider( getProfil().getType() );
    }

    return m_layerProvider;
  }
}