package de.openali.odysseus.chart.framework.model.layer;

import java.net.URL;

import de.openali.odysseus.chart.framework.model.exception.ConfigurationException;

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
   * @param context
   *          context path - needed to resolve relative paths from xml configuration
   * @return Array of all layers created by the LayerProvider
   */
  IChartLayer getLayer( final URL context ) throws ConfigurationException;

  void init( ILayerProviderSource source );
}
