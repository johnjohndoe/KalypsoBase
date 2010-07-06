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

import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilChange;
import org.kalypso.model.wspm.core.profil.IProfilListener;
import org.kalypso.model.wspm.core.profil.changes.PointPropertyAdd;
import org.kalypso.model.wspm.core.profil.changes.PointPropertyRemove;
import org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIExtensions;

import de.openali.odysseus.chart.framework.model.impl.ChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;

/**
 * @author kimwerner
 */
public class ProfilChartModel extends ChartModel implements IProfilListener
{
  private final IProfil m_profil;

  public IProfil getProfil( )
  {
    return m_profil;
  }

  private IProfilLayerProvider m_layerProvider = null;

  public ProfilChartModel( final IProfilLayerProvider layerProvider, final IProfil profil, final Object result )
  {
    super();

    m_layerProvider = layerProvider;
    m_profil = profil;

    if( m_profil == null )
      return;

    final IProfilChartLayer[] profileLayers = m_layerProvider.createLayers( profil, result );
    for( final IProfilChartLayer layer : profileLayers )
      getLayerManager().addLayer( layer );
    m_layerProvider.registerAxis( getMapperRegistry() );
  }

  public ProfilChartModel( final IProfil profil, final Object result )
  {
    this( KalypsoModelWspmUIExtensions.createProfilLayerProvider( profil.getType() ), profil, result );
  }

  public final void unregisterListener( )
  {
    if( m_profil != null )
      m_profil.removeProfilListener( this );
  }

  public final void registerListener( )
  {
    if( m_profil != null )
      m_profil.addProfilListener( this );
  }

  public final IProfilChartLayer getLayer( final String layerID )
  {
    final IChartLayer layer = getLayerManager().getLayerById( layerID );
    if( layer != null && layer instanceof IProfilChartLayer )
      return (IProfilChartLayer) layer;

    else
      return null;
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfilListener#onProblemMarkerChanged(org.kalypso.model.wspm.core.profil.IProfil)
   */
  @Override
  public void onProblemMarkerChanged( final IProfil source )
  {
    if( source == getProfil() )
    {
// Todo: what?
    }

  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfilListener#onProfilChanged(org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint,
   *      org.kalypso.model.wspm.core.profil.IProfilChange[])
   */
  @Override
  public void onProfilChanged( final ProfilChangeHint hint, final IProfilChange[] changes )
  {
    if( hint.isPointPropertiesChanged() )
    {
      for( final IProfilChange change : changes )
      {
        final LayerDescriptor layerDescriptor = m_layerProvider.getLayer( change == null ? null : change.getInfo() );

        if( change instanceof PointPropertyRemove )
        {
          final IChartLayer layer = getLayerManager().getLayerById( layerDescriptor.getId() );
          if( layer != null )
            getLayerManager().removeLayer( layer );
        }
        else if( change instanceof PointPropertyAdd )
        {
          if( getLayerManager().getLayerById( layerDescriptor.getId() ) == null )
          {
            getLayerManager().addLayer( m_layerProvider.createLayer( getProfil(), layerDescriptor.getId() ) );
          }
        }
      }
    }

    else
    {
      for( final IChartLayer layer : getLayerManager().getLayers() )
      {
        ((IProfilChartLayer) layer).onProfilChanged( hint, changes );
      }
    }
  }
}
