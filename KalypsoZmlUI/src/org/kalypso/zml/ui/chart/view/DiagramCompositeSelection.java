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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ObservationTokenHelper;
import org.kalypso.ogc.sensor.provider.IObsProvider;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.core.base.IMultipleZmlSourceElement;
import org.kalypso.zml.core.base.IZmlSourceElement;
import org.kalypso.zml.core.diagram.base.IZmlLayer;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.core.diagram.data.ZmlObsProviderDataHandler;
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
public final class DiagramCompositeSelection
{
  private DiagramCompositeSelection( )
  {
  }

  public static void doApply( final IChartModel model, final IMultipleZmlSourceElement... selection )
  {
    final ILayerManager manager = model.getLayerManager();

    for( final IMultipleZmlSourceElement element : selection )
    {
      final String type = element.getType();

      final ParameterTypeLayerVisitor visitor = new ParameterTypeLayerVisitor( type );
      manager.accept( visitor );

      final IZmlLayer[] layers = visitor.getLayers();
      if( ArrayUtils.isEmpty( layers ) )
      {
        continue;
      }

      final IZmlSourceElement[] sources = element.getSources();
      for( int index = 0; index < ArrayUtils.getLength( sources ); index++ )
      {
        final IZmlSourceElement source = sources[index];
        update( layers, source, type, index );

        final IObsProvider provider = source.getObsProvider();
        if( provider != null )
          provider.dispose();
      }
    }
  }

  private static void update( final IZmlLayer[] layers, final IZmlSourceElement source, final String type, final int index )
  {
    for( final IZmlLayer baseLayer : layers )
    {
      try
      {
        final IZmlLayer layer = buildLayer( baseLayer, index );
        final IObsProvider provider = source.getObsProvider();

        final IZmlLayerDataHandler handler = layer.getDataHandler();
        if( handler instanceof ZmlObsProviderDataHandler )
        {
          ((ZmlObsProviderDataHandler) handler).setObsProvider( provider );
        }

        final String label = findLabel( source, provider, type );

        layer.setTitle( label );

      }
      catch( final ConfigurationException e )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }
  }

  private static String findLabel( final IZmlSourceElement source, final IObsProvider provider, final String type )
  {
    final String label = source.getLabel();
    if( StringUtils.isNotEmpty( label ) )
      return label;

    final IObservation observation = provider.getObservation();
    if( Objects.isNotNull( observation ) )
    {
      final IAxis axis = AxisUtils.findAxis( observation.getAxes(), type );
      final String name = ObservationTokenHelper.replaceTokens( "%axistype% - %obsname%", observation, axis ); //$NON-NLS-1$

      return name;
    }

    return type;
  }

  private static IZmlLayer buildLayer( final IZmlLayer baseLayer, final int index ) throws ConfigurationException
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
