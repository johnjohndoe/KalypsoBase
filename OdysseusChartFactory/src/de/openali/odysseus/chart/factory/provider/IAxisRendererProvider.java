package de.openali.odysseus.chart.factory.provider;

import de.openali.odysseus.chart.factory.config.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chartconfig.x010.AxisRendererType;

/**
 * @author burtscher A LayerProvider is needed to create layers from configuration data. Theres no 1:1 mapping from data
 *         soureces to layers, as 1.) several data sources can be merged to generate layer data and 2.) one data source
 *         can be used to create several layers. The LayerProvider is used to fetch, filter and analyze data and to
 *         provide layers according to the datas needs.
 */
public interface IAxisRendererProvider
{
  /**
   * @return axis created by the AxisProvider
   */
  public IAxisRenderer getAxisRenderer( ) throws ConfigurationException;

  public void init( final AxisRendererType at );

  /**
   * returns XML configuration element for the given chart element
   */
  public AxisRendererType getXMLType( IAxisRenderer axisRenderer );

}
