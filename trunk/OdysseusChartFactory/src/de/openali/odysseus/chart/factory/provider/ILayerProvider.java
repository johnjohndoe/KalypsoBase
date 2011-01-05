package de.openali.odysseus.chart.factory.provider;

import java.net.URL;
import java.util.Map;

import de.openali.odysseus.chart.factory.config.exception.ConfigurationException;
import de.openali.odysseus.chart.factory.config.parameters.IParameterContainer;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;

/**
 * A LayerProvider is needed to create layers from configuration data. There's no 1:1 mapping from data sources to
 * layers, as 1.) several data sources can be merged to generate layer data and 2.) one data source can be used to
 * create several layers. The LayerProvider is used to fetch, filter and analyze data and to provide layers according to
 * the datas needs.
 * 
 * @author burtscher
 */
public interface ILayerProvider extends IChartComponentProvider
{
  /**
   * @return Array of all layers created by the LayerProvider
   * @param context
   *          context path - needed to resolve relative paths from xml configuration
   */
  IChartLayer getLayer( final URL context ) throws ConfigurationException;

  void init( final IChartModel model, String id, IParameterContainer parameters, final URL context, String domainAxisId, String targetAxisId, Map<String, String> mapperMap, IStyleSet styleSet );
}
