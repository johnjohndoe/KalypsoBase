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
package org.kalypso.chart.factory.configuration;

import java.util.HashMap;
import java.util.Map;

import org.kalypso.chart.factory.provider.IAxisProvider;
import org.kalypso.chart.factory.provider.IAxisRendererProvider;
import org.kalypso.chart.factory.provider.ILayerProvider;
import org.kalypso.chart.factory.provider.IMapperProvider;
import org.kalypso.chart.factory.provider.IStyledElementProvider;
import org.kalypso.chart.framework.model.IChartModel;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.layer.ILayerManager;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IMapper;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;
import org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer;
import org.kalypso.chart.framework.model.styles.IStyledElement;
import org.ksp.chart.factory.AxisRendererType;
import org.ksp.chart.factory.AxisType;
import org.ksp.chart.factory.ChartConfigurationDocument;
import org.ksp.chart.factory.ChartConfigurationType;
import org.ksp.chart.factory.ChartType;
import org.ksp.chart.factory.LayerType;
import org.ksp.chart.factory.MapperType;
import org.ksp.chart.factory.RefType;
import org.ksp.chart.factory.StyleType;
import org.ksp.chart.factory.ChartType.Layers;

/**
 * saves a chart to an XML document
 * 
 * @author burtscher1
 */
public class ChartConfigurationSaver
{
  public static ChartConfigurationDocument createChartConfiguration( final IChartModel model )
  {
    final ChartConfigurationDocument ccd = ChartConfigurationDocument.Factory.newInstance();
    final ChartConfigurationType chartconf = ccd.addNewChartConfiguration();
    final ILayerManager lm = model.getLayerManager();

    // Chart
    final ChartType chartType = chartconf.addNewChart();
    chartType.setId( model.getId() );
    chartType.setDescription( model.getDescription() );
    chartType.setTitle( model.getTitle() );

    // LayerReferenzen
    final Layers layerTypes = chartType.addNewLayers();
    final IChartLayer[] layers = lm.getLayers();
    for( final IChartLayer chartLayer : layers )
    {
      final RefType layerRef = layerTypes.addNewLayerRef();
      layerRef.setRef( chartLayer.getId() );
    }

    chartconf.setLayerArray( extractLayers( model.getLayerManager() ).values().toArray( new LayerType[] {} ) );
    chartconf.setAxisArray( extractAxes( model.getMapperRegistry() ).values().toArray( new AxisType[] {} ) );
    chartconf.setMapperArray( extractMappers( model.getMapperRegistry() ).values().toArray( new MapperType[] {} ) );
    chartconf.setAxisRendererArray( extractAxisRenderers( model.getMapperRegistry() ).values().toArray( new AxisRendererType[] {} ) );
    chartconf.setStyleArray( extractStyledElements( model.getLayerManager() ).values().toArray( new StyleType[] {} ) );

    return ccd;
  }

  private static Map<String, AxisType> extractAxes( final IMapperRegistry registry )
  {
    final Map<String, AxisType> axisTypes = new HashMap<String, AxisType>();
    final IAxis[] axes = registry.getAxes();
    for( final IAxis axis : axes )
    {
      final IAxisProvider provider = (IAxisProvider) axis.getData( ChartFactory.AXIS_PROVIDER_KEY );
      axisTypes.put( axis.getIdentifier(), provider.getXMLType( axis ) );
    }
    return axisTypes;
  }

  private static Map<String, AxisRendererType> extractAxisRenderers( final IMapperRegistry registry )
  {
    final Map<String, AxisRendererType> axisRendererTypes = new HashMap<String, AxisRendererType>();
    final IAxis[] axes = registry.getAxes();
    for( final IAxis axis : axes )
    {
      final IAxisProvider axisProvider = (IAxisProvider) axis.getData( ChartFactory.AXIS_PROVIDER_KEY );
      final IAxisRenderer renderer = registry.getRenderer( axis );
      if( renderer != null )
      {
        final IAxisRendererProvider axisRendererProvider = (IAxisRendererProvider) renderer.getData( ChartFactory.AXISRENDERER_PROVIDER_KEY );
        axisRendererTypes.put( renderer.getId(), axisRendererProvider.getXMLType( renderer ) );
      }
    }
    return axisRendererTypes;
  }

  private static Map<String, MapperType> extractMappers( final IMapperRegistry registry )
  {
    final Map<String, MapperType> mapperTypes = new HashMap<String, MapperType>();
    final IMapper[] mappers = registry.getMappers();
    for( final IMapper mapper : mappers )
    {
      final IMapperProvider provider = (IMapperProvider) mapper.getData( ChartFactory.MAPPER_PROVIDER_KEY );
      mapperTypes.put( mapper.getIdentifier(), provider.getXMLType( mapper ) );
    }
    return mapperTypes;
  }

  private static Map<String, LayerType> extractLayers( final ILayerManager manager )
  {
    final Map<String, LayerType> layerTypes = new HashMap<String, LayerType>();
    for( final IChartLayer layer : manager.getLayers() )
    {
      final ILayerProvider provider = (ILayerProvider) layer.getData( ChartFactory.LAYER_PROVIDER_KEY );
      layerTypes.put( layer.getId(), provider.getXMLType( layer ) );
    }
    return layerTypes;
  }

  private static Map<String, StyleType> extractStyledElements( final ILayerManager manager )
  {
    final Map<String, StyleType> styleTypes = new HashMap<String, StyleType>();
    for( final IChartLayer layer : manager.getLayers() )
    {
      final ILayerProvider lProvider = (ILayerProvider) layer.getData( ChartFactory.LAYER_PROVIDER_KEY );
      final IStyledElement[] styledElements = layer.getStyle().getElements();
      for( final IStyledElement styledElement : styledElements )
      {
        final IStyledElementProvider sProvider = (IStyledElementProvider) styledElement.getData( ChartFactory.STYLE_PROVIDER_KEY );
        styleTypes.put( styledElement.getId(), sProvider.getXMLType( styledElement ) );
      }
    }
    return styleTypes;
  }

}
