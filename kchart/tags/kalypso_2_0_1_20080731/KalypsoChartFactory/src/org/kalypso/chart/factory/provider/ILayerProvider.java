package org.kalypso.chart.factory.provider;

import java.net.URL;

import org.kalypso.chart.factory.configuration.exception.LayerProviderException;
import org.kalypso.chart.framework.model.IChartModel;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.ksp.chart.factory.LayerType;

/**
 * @author burtscher A LayerProvider is needed to create layers from configuration data. Theres no 1:1 mapping from data
 *         soureces to layers, as 1.) several data sources can be merged to generate layer data and 2.) one data source
 *         can be used to create several layers. The LayerProvider is used to fetch, filter and analyze data and to
 *         provide layers according to the datas needs.
 */
public interface ILayerProvider
{
  /**
   * @return Array of all layers created by the LayerProvider
   * @param context
   *            context path - needed to resolve relative paths from xml configuration
   */
  public IChartLayer getLayer( final URL context ) throws LayerProviderException;

  // TODO: move chart parameter to getLayers
  public void init( final IChartModel model, final LayerType lt );

  /**
   * returns XML configuration element for the given chart element
   */
  public LayerType getXMLType( IChartLayer layer );

  /**
   * returns the IChartModel which the layer belongs to
   */
  public IChartModel getChartModel( );
}
