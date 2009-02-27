package de.openali.diagram.factory.provider;

import de.openali.diagram.factory.configuration.exception.AxisProviderException;
import de.openali.diagram.factory.configuration.xsd.AxisType;
import de.openali.diagram.framework.model.mapper.IAxis;

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
public interface IAxisProvider
{
  /**
   * @return axis created by the AxisProvider
   */
  public IAxis getAxis( ) throws AxisProviderException;

  public void init( final AxisType at );

  public Class<?> getDataClass();
  
}
