package de.openali.diagram.factory.provider;

import java.net.URL;

import de.openali.diagram.factory.configuration.exception.LayerProviderException;
import de.openali.diagram.factory.configuration.xsd.LayerType;
import de.openali.diagram.framework.model.IDiagramModel;
import de.openali.diagram.framework.model.layer.IChartLayer;

/**
 * @author burtscher
 *
 * A LayerProvider is needed to create layers from configuration data.
 * Theres no 1:1 mapping from data soureces to layers, as
 * 1.) several data sources can be merged to generate layer data and
 * 2.) one data source can be used to create several layers.
 * The LayerProvider is used to fetch, filter and analyze data and to provide
 * layers according to the datas needs.
 *
 */
public interface ILayerProvider
{
  /**
   * @return Array of all layers created by the LayerProvider
   * @param context context path - needed to resolve relative paths from xml configuration
   */
  public IChartLayer getLayer( final URL context ) throws LayerProviderException;

  // TODO: move chart parameter to getLayers
  public void init( final IDiagramModel model, final LayerType lt);
}
