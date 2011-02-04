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

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilChange;
import org.kalypso.model.wspm.core.profil.IProfilListener;
import org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIExtensions;

import de.openali.odysseus.chart.framework.model.impl.ChartModel;
import de.openali.odysseus.chart.framework.model.impl.visitors.AutoScaleVisitor;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;

/**
 * @author kimwerner
 */
public class ProfilChartModel extends ChartModel
{
  private final IProfilListener m_profilListener = new IProfilListener()
  {
    /**
     * @see org.kalypso.model.wspm.core.profil.IProfilListener#onProblemMarkerChanged(org.kalypso.model.wspm.core.profil.IProfil)
     */
    @Override
    public void onProblemMarkerChanged( final IProfil source )
    {
      // TODO: what?
    }

    /**
     * @see org.kalypso.model.wspm.core.profil.IProfilListener#onProfilChanged(org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint,
     *      org.kalypso.model.wspm.core.profil.IProfilChange[])
     */
    @Override
    public void onProfilChanged( final ProfilChangeHint hint, final IProfilChange[] changes )
    {
      if( hint.isObjectChanged() )
        updateLayers();
      else if( hint.isPointPropertiesChanged() )
        handlePropertyOrBuildingChanged( changes );
      else
      {
        for( final IChartLayer layer : getLayerManager().getLayers() )
          ((IProfilChartLayer) layer).onProfilChanged( hint, changes );
      }
    }
  };

  private final IProfil m_profil;

  private IProfilLayerProvider m_layerProvider;

  private final Object m_result;

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

  public ProfilChartModel( final IProfilLayerProvider layerProvider, final IProfil profil, final Object result )
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
  }
  
  /**
   * automatically scales all given axes; scaling means here: show all available values
   */
  @Override
  public void autoscale( final IAxis... axes )
  {
    final AutoScaleVisitor visitor = new AutoScaleVisitor( this, false );

    // TODO ?!? auto scaled axes will be updated when?!? strange behaviour
    final IAxis[] autoscaledAxes = ArrayUtils.isEmpty( axes ) ? getMapperRegistry().getAxes() : axes;
    for( final IAxis axis : autoscaledAxes )
    {
      visitor.visit( axis );
    }
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
      return (IProfilChartLayer) layer;

    return null;
  }

  public IProfil getProfil( )
  {
    return m_profil;
  }

  protected void handlePropertyOrBuildingChanged( @SuppressWarnings("unused") final IProfilChange[] changes )
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
      final ILayerManager layerManager = getLayerManager();
      layerManager.clear();

      final IProfilLayerProvider lp = getProfilLayerProvider();
      if( lp == null )
        return;

      final IProfilChartLayer[] layers = lp.createLayers( m_profil, m_result );
      layerManager.addLayer( layers );

      // FIXME
// getState().restoreState( this );
    }

  }
}
