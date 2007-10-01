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
import org.kalypso.chart.framework.model.styles.ILayerStyle;
import org.kalypso.chart.framework.model.styles.IStyledElement;
import org.ksp.chart.factory.AxisRendererType;
import org.ksp.chart.factory.AxisType;
import org.ksp.chart.factory.ChartConfigurationDocument;
import org.ksp.chart.factory.LayerType;
import org.ksp.chart.factory.MapperType;
import org.ksp.chart.factory.StyleType;

/**
 * saves a chart to an XML document
 * 
 * @author burtscher1
 */
public class ChartConfigurationSaver
{

  private HashMap<String, LayerType> m_layerTypes;

  private HashMap<String, AxisType> m_axisTypes;

  private HashMap<String, AxisRendererType> m_axisRendererTypes;

  private HashMap<String, MapperType> m_mapperTypes;

  private HashMap<String, StyleType> m_styleTypes;

  private IChartModel m_model = null;

  private ChartConfigurationDocument m_ccd;

  private void init( )
  {
    m_layerTypes = new HashMap<String, LayerType>();
    m_axisTypes = new HashMap<String, AxisType>();
    m_axisRendererTypes = new HashMap<String, AxisRendererType>();
    m_mapperTypes = new HashMap<String, MapperType>();
    m_styleTypes = new HashMap<String, StyleType>();
  }

  public synchronized ChartConfigurationDocument save( IChartModel model )
  {
    m_model = model;

    m_ccd = ChartConfigurationDocument.Factory.newInstance();
    extractLayers( model.getLayerManager() );
    extractMappers( model.getMapperRegistry() );
    extractAxes( model.getMapperRegistry() );

    return null;
  }

  private void extractAxes( IMapperRegistry registry )
  {
    IAxis[] axes = registry.getAxes();
    for( IAxis axis : axes )
    {
      IAxisProvider provider = (IAxisProvider) axis.getData( ChartFactory.AXIS_PROVIDER_KEY );
      m_axisTypes.put( axis.getIdentifier(), provider.getXMLType( axis ) );
      saveAxisRenderer( registry.getRenderer( axis ) );
    }

  }

  private void saveAxisRenderer( IAxisRenderer renderer )
  {
    if( renderer != null )
    {
      IAxisRendererProvider provider = (IAxisRendererProvider) renderer.getData( ChartFactory.AXISRENDERER_PROVIDER_KEY );
      m_axisRendererTypes.put( renderer.getId(), provider.getXMLType( renderer ) );
    }
  }

  private void extractMappers( IMapperRegistry registry )
  {
    IMapper[] mappers = registry.getMappers();
    for( IMapper mapper : mappers )
    {
      IMapperProvider provider = (IMapperProvider) mapper.getData( ChartFactory.MAPPER_PROVIDER_KEY );
      m_mapperTypes.put( mapper.getIdentifier(), provider.getXMLType( mapper ) );
    }
  }

  private void extractLayers( ILayerManager manager )
  {
    for( IChartLayer layer : manager.getLayers() )
    {
      ILayerProvider provider = (ILayerProvider) layer.getData( ChartFactory.LAYER_PROVIDER_KEY );
      m_layerTypes.put( layer.getId(), provider.getXMLType( layer ) );
      extractStyle( layer.getStyle() );
    }
  }

  private void extractStyle( ILayerStyle style )
  {
    IStyledElement[] styledElements = style.getElements();
    for( IStyledElement styledElement : styledElements )
    {
      IStyledElementProvider provider = (IStyledElementProvider) styledElement.getData( ChartFactory.STYLE_PROVIDER_KEY );
      m_styleTypes.put( styledElement.getId(), provider.getXMLType( styledElement ) );
    }
  }
}
