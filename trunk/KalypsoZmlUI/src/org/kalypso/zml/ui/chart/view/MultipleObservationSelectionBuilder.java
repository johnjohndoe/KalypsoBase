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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ObservationTokenHelper;
import org.kalypso.ogc.sensor.provider.IObsProvider;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.core.diagram.data.ZmlObsProviderDataHandler;
import org.kalypso.zml.core.diagram.layer.IZmlLayer;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.chart.update.IClonedLayer;
import org.kalypso.zml.ui.chart.update.ParameterTypeLayerVisitor;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.ILayerContainer;
import de.openali.odysseus.chart.framework.model.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.mapper.impl.CoordinateMapper;

/**
 * @author Dirk Kuch
 */
public class MultipleObservationSelectionBuilder implements IZmlDiagramSelectionBuilder
{
  Map<String, List<IObsProvider>> m_observations = new HashMap<>();

  public MultipleObservationSelectionBuilder( final IObsProvider[] providers )
  {
    init( providers );
  }

  private void init( final IObsProvider[] providers )
  {
    for( final IObsProvider provider : providers )
    {
      final IObservation observation = provider.getObservation(); // FIXME
      final IAxis[] valueAxes = AxisUtils.findValueAxes( observation.getAxes(), false );
      for( final IAxis axis : valueAxes )
      {
        final String type = axis.getType();
        List<IObsProvider> obses = m_observations.get( type );
        if( Objects.isNull( obses ) )
        {
          obses = new ArrayList<>();
          m_observations.put( type, obses );
        }

        obses.add( provider );
      }
    }
  }

  @Override
  public void doSelectionUpdate( final IChartModel model )
  {
    final ILayerManager manager = model.getLayerManager();

    final Set<Entry<String, List<IObsProvider>>> entries = m_observations.entrySet();
    for( final Entry<String, List<IObsProvider>> entry : entries )
    {
      final String type = entry.getKey();

      final ParameterTypeLayerVisitor visitor = new ParameterTypeLayerVisitor( type );
      manager.accept( visitor );

      final IZmlLayer[] layers = visitor.getLayers();
      if( ArrayUtils.isEmpty( layers ) )
        continue;

      final List<IObsProvider> providers = entry.getValue();
      for( int index = 0; index < providers.size(); index++ )
      {
        final IObsProvider provider = providers.get( index );
        update( layers, provider, type, index );

        provider.dispose();
      }
    }
  }

  private void update( final IZmlLayer[] layers, final IObsProvider provider, final String type, final int index )
  {
    for( final IZmlLayer baseLayer : layers )
    {
      try
      {
        final IZmlLayer layer = buildLayer( baseLayer, index );

        final IZmlLayerDataHandler handler = layer.getDataHandler();
        if( handler instanceof ZmlObsProviderDataHandler )
          ((ZmlObsProviderDataHandler) handler).setObsProvider( provider );

        final IObservation observation = provider.getObservation();
        if( Objects.isNotNull( observation ) )
        {
          final IAxis axis = AxisUtils.findAxis( observation.getAxes(), type );
          final String name = ObservationTokenHelper.replaceTokens( "%axistype% - %obsname%", observation, axis ); //$NON-NLS-1$
          layer.setTitle( name );
        }

      }
      catch( final ConfigurationException e )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }
  }

  private IZmlLayer buildLayer( final IZmlLayer baseLayer, final int index ) throws ConfigurationException
  {
    if( index == 0 )
      return baseLayer;

    final ILayerProvider provider = baseLayer.getProvider();
    final IZmlLayer clone = (IZmlLayer) provider.getLayer( provider.getContext() );
    clone.setIdentifier( String.format( IClonedLayer.CLONED_LAYER_POSTFIX_FORMAT, baseLayer.getIdentifier(), index ) );
    clone.setDataHandler( new ZmlObsProviderDataHandler( clone, baseLayer.getDataHandler().getTargetAxisId() ) );

    final ICoordinateMapper baseMapper = baseLayer.getCoordinateMapper();
    clone.setCoordinateMapper( new CoordinateMapper( baseMapper.getDomainAxis(), baseMapper.getTargetAxis() ) );

    clone.setVisible( baseLayer.isVisible() );
    clone.setFilter( baseLayer.getFilters() );
    clone.setTitle( baseLayer.getTitle() );
    clone.setLegend( baseLayer.isLegend() );

    final ILayerContainer parent = baseLayer.getParent();
    parent.getLayerManager().addLayer( clone );

    return clone;
  }
}
