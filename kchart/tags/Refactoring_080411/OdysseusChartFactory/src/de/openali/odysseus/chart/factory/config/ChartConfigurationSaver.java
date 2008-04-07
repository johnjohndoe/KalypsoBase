/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package de.openali.odysseus.chart.factory.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.openali.odysseus.chart.factory.provider.IAxisProvider;
import de.openali.odysseus.chart.factory.provider.IAxisRendererProvider;
import de.openali.odysseus.chart.factory.provider.ILayerProvider;
import de.openali.odysseus.chart.factory.provider.IMapperProvider;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.model.style.IStylable;
import de.openali.odysseus.chart.framework.model.style.IStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chartconfig.x010.AreaStyleType;
import de.openali.odysseus.chartconfig.x010.AxisRendererType;
import de.openali.odysseus.chartconfig.x010.AxisType;
import de.openali.odysseus.chartconfig.x010.ChartConfigurationDocument;
import de.openali.odysseus.chartconfig.x010.ChartConfigurationType;
import de.openali.odysseus.chartconfig.x010.ChartType;
import de.openali.odysseus.chartconfig.x010.LayerType;
import de.openali.odysseus.chartconfig.x010.LineStyleType;
import de.openali.odysseus.chartconfig.x010.MapperType;
import de.openali.odysseus.chartconfig.x010.PointStyleType;
import de.openali.odysseus.chartconfig.x010.ReferencingType;
import de.openali.odysseus.chartconfig.x010.TextStyleType;
import de.openali.odysseus.chartconfig.x010.ChartType.Layers;

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
      final ReferencingType layerRef = layerTypes.addNewLayerRef();
      layerRef.setRef( chartLayer.getId() );
    }

    chartconf.setLayerArray( extractLayers( model.getLayerManager() ).values().toArray( new LayerType[] {} ) );
    chartconf.setAxisArray( extractAxes( model.getMapperRegistry() ).values().toArray( new AxisType[] {} ) );
    chartconf.setMapperArray( extractMappers( model.getMapperRegistry() ).values().toArray( new MapperType[] {} ) );
    chartconf.setAxisRendererArray( extractAxisRenderers( model.getMapperRegistry() ).values().toArray( new AxisRendererType[] {} ) );

    // Styles der Layer und AxisRenderer speichern

    Set<IStylable> stylableList = new HashSet<IStylable>();
    for( IAxis axis : model.getMapperRegistry().getAxes() )
    {
      IAxisRenderer renderer = model.getMapperRegistry().getRenderer( axis );
      stylableList.add( renderer );
    }
    for( IChartLayer layer : model.getLayerManager().getLayers() )
    {
      stylableList.add( layer );
    }

    addStyles( stylableList.toArray( new IStylable[] {} ), chartconf );

    return ccd;
  }

  /**
   * Adds style configuration to configufation document Warning: This method does not support styles which where changed
   * from the chartfile; If a style is used by multiple layers, only the configuration of the styles' appearance in the
   * last layer is saved.
   */
  private static void addStyles( IStylable[] stylables, ChartConfigurationType chartconf )
  {
    Map<String, LineStyleType> lineStyleMap = new HashMap<String, LineStyleType>();
    Map<String, PointStyleType> pointStyleMap = new HashMap<String, PointStyleType>();
    Map<String, AreaStyleType> areaStyleMap = new HashMap<String, AreaStyleType>();
    Map<String, TextStyleType> textStyleMap = new HashMap<String, TextStyleType>();

    for( IStylable stylable : stylables )
    {
      IStyleSet styleSet = stylable.getStyles();
      if( styleSet != null )
      {
        Map<String, IStyle> styleMap = styleSet.getStyles();
        for( Entry<String, IStyle> mapEntry : styleMap.entrySet() )
        {
          Object styleType = mapEntry.getValue().getData( ChartFactory.STYLE_KEY );
          if( styleType != null )
          {
            if( styleType instanceof LineStyleType )
            {
              LineStyleType lst = (LineStyleType) styleType;
              lineStyleMap.put( lst.getId(), lst );
            }
            else if( styleType instanceof AreaStyleType )
            {
              AreaStyleType ast = (AreaStyleType) styleType;
              areaStyleMap.put( ast.getId(), ast );
            }
            else if( styleType instanceof PointStyleType )
            {
              PointStyleType pst = (PointStyleType) styleType;
              pointStyleMap.put( pst.getId(), pst );
            }
            else if( styleType instanceof TextStyleType )
            {
              TextStyleType tst = (TextStyleType) styleType;
              textStyleMap.put( tst.getId(), tst );
            }
          }
          else
          {
            Logger.logInfo( Logger.TOPIC_LOG_CONFIG, "Style with role'" + mapEntry.getKey() + "' for chart component '" + stylable.getId()
                + "' was not initially set in the chartfile, so it won't be saved" );
          }
        }
      }
    }
    chartconf.setLineStyleArray( lineStyleMap.values().toArray( new LineStyleType[] {} ) );
    chartconf.setTextStyleArray( textStyleMap.values().toArray( new TextStyleType[] {} ) );
    chartconf.setPointStyleArray( pointStyleMap.values().toArray( new PointStyleType[] {} ) );
    chartconf.setAreaStyleArray( areaStyleMap.values().toArray( new AreaStyleType[] {} ) );
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
      final IAxisRenderer renderer = registry.getRenderer( axis );
      if( renderer != null )
      {
        final IAxisRendererProvider axisRendererProvider = (IAxisRendererProvider) renderer.getData( ChartFactory.AXISRENDERER_PROVIDER_KEY );
        axisRendererTypes.put( renderer.getId(), axisRendererProvider.getXMLType( renderer ) );
      }
    }
    return axisRendererTypes;
  }

  @SuppressWarnings("unchecked")
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

}
