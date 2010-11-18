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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;
import de.openali.odysseus.chartconfig.x020.AxisDateRangeType;
import de.openali.odysseus.chartconfig.x020.AxisNumberRangeType;
import de.openali.odysseus.chartconfig.x020.AxisRendererType;
import de.openali.odysseus.chartconfig.x020.AxisStringRangeType;
import de.openali.odysseus.chartconfig.x020.AxisType;
import de.openali.odysseus.chartconfig.x020.ChartConfigurationDocument;
import de.openali.odysseus.chartconfig.x020.ChartConfigurationType;
import de.openali.odysseus.chartconfig.x020.ChartType;
import de.openali.odysseus.chartconfig.x020.ChartType.Layers;
import de.openali.odysseus.chartconfig.x020.ChartType.Mappers;
import de.openali.odysseus.chartconfig.x020.ChartType.Renderers;
import de.openali.odysseus.chartconfig.x020.LayerType;
import de.openali.odysseus.chartconfig.x020.MapperType;
import de.openali.odysseus.chartconfig.x020.TitleType;

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

    // Chart
    final ChartType chartType = chartconf.addNewChart();
    chartType.setId( model.getId() );
    chartType.setDescription( model.getDescription() );
    final TitleTypeBean[] titleTypes = model.getTitles();
    final TitleType[] titleArray = new TitleType[titleTypes.length];
    for( int i = 0; i < titleTypes.length; i++ )
    {
      final TitleType type = TitleType.Factory.newInstance();
      type.setStringValue( titleTypes[i].getText() );
      titleArray[i] = type;

    }
    chartType.setTitleArray( titleArray );
    final Layers layers = chartType.addNewLayers();
    layers.setLayerArray( extractLayers( model.getLayerManager() ).values().toArray( new LayerType[] {} ) );

    final Mappers mappers = chartType.addNewMappers();
    mappers.setAxisArray( extractAxes( model.getMapperRegistry() ).values().toArray( new AxisType[] {} ) );
    mappers.setMapperArray( extractMappers( model.getMapperRegistry() ).values().toArray( new MapperType[] {} ) );

    final Renderers renderers = chartType.addNewRenderers();
    renderers.setAxisRendererArray( extractAxisRenderers( model.getMapperRegistry() ).values().toArray( new AxisRendererType[] {} ) );
    return ccd;
  }

  /**
   * saves axes with current range
   */
  private static Map<String, AxisType> extractAxes( final IMapperRegistry registry )
  {

    final Map<String, AxisType> axisTypes = new HashMap<String, AxisType>();
    final IAxis[] axes = registry.getAxes();
    for( final IAxis axis : axes )
    {
      final AxisType at = (AxisType) axis.getData( ChartFactory.CONFIGURATION_TYPE_KEY );
      if( at != null )
      {

        // only set new range
        // TODO: Die Achsen schreiben ihre range nur in die .kod wenn das Element vorher schon angelegt war
        if( at.isSetDateRange() )
        {
          final IDataOperator<Calendar> dop = axis.getDataOperator( Calendar.class );
          final AxisDateRangeType configRange = at.getDateRange();
          final IDataRange<Number> numericRange = axis.getNumericRange();
          configRange.setMinValue( dop.numericToLogical( numericRange.getMin() ) );
          configRange.setMaxValue( dop.numericToLogical( numericRange.getMax() ) );
        }
        else if( at.isSetDurationRange() )
        {
          // TODO: what to do now? either change to date range or leave range as it was
        }
        else if( at.isSetNumberRange() )
        {
          final AxisNumberRangeType configRange = at.getNumberRange();
          final IDataRange<Number> numericRange = axis.getNumericRange();
          configRange.setMinValue( numericRange.getMin().doubleValue() );
          configRange.setMaxValue( numericRange.getMax().doubleValue() );
        }
        else if( at.isSetStringRange() )
        {
          final AxisStringRangeType configRange = at.getStringRange();
          final IDataRange<Number> numericRange = axis.getNumericRange();
          configRange.setMinValue( numericRange.getMin().doubleValue() );
          configRange.setMaxValue( numericRange.getMax().doubleValue() );
        }

        axisTypes.put( axis.getId(), at );
      }
    }
    return axisTypes;
  }

  /**
   * saves renderers without applying any changes
   */
  private static Map<String, AxisRendererType> extractAxisRenderers( final IMapperRegistry registry )
  {
    final Map<String, AxisRendererType> axisRendererTypes = new HashMap<String, AxisRendererType>();
    final IAxis[] axes = registry.getAxes();
    for( final IAxis axis : axes )
    {
      final IAxisRenderer renderer = axis.getRenderer();
      if( renderer != null )
      {
        final AxisRendererType art = (AxisRendererType) renderer.getData( ChartFactory.CONFIGURATION_TYPE_KEY );
        if( art != null )
        {
          // everything stays as it was
          axisRendererTypes.put( renderer.getId(), art );
        }
      }
    }
    return axisRendererTypes;
  }

  /**
   * saves mappers without applying any changes
   */
  private static Map<String, MapperType> extractMappers( final IMapperRegistry registry )
  {
    final Map<String, MapperType> mapperTypes = new HashMap<String, MapperType>();
    final IMapper[] mappers = registry.getMappers();
    for( final IMapper mapper : mappers )
    {
      final MapperType mt = (MapperType) mapper.getData( ChartFactory.CONFIGURATION_TYPE_KEY );
      if( mt != null )
      {
        // everything stays as it was
        mapperTypes.put( mapper.getId(), mt );
      }
    }
    return mapperTypes;
  }

  /**
   * saves layers according to current order and sets current visibilty
   */
  private static Map<String, LayerType> extractLayers( final ILayerManager manager )
  {
    final Map<String, LayerType> layerTypes = new HashMap<String, LayerType>();
    for( final IChartLayer layer : manager.getLayers() )
    {
      final LayerType lt = (LayerType) layer.getData( ChartFactory.CONFIGURATION_TYPE_KEY );
      if( lt != null )
      {
        // set layer visibility
        lt.setVisible( layer.isVisible() );
        // set layer label
        final String title = layer.getTitle();
        final String description = layer.getDescription();
        if( description != null )
          lt.setDescription( description );
        if( title != null )
          lt.setTitle( title );
        // everything else stays as it was
        layerTypes.put( layer.getId(), lt );
      }

    }
    return layerTypes;
  }
}
