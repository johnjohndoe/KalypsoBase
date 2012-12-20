package de.openali.odysseus.chart.factory.provider;

import java.net.URL;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.layer.IChartComponentProvider;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;

/**
 * @author burtscher A LayerProvider is needed to create layers from configuration data. There's no 1:1 mapping from
 *         data sources to layers, as 1.) several data sources can be merged to generate layer data and 2.) one data
 *         source can be used to create several layers. The LayerProvider is used to fetch, filter and analyze data and
 *         to provide layers according to the datas needs.
 */
public interface IAxisProvider extends IChartComponentProvider
{
  /**
   * @return axis created by the AxisProvider
   */
  IAxis getAxis( ) throws ConfigurationException;

  IAxis getScreenAxis( final String identifier, final POSITION position );

  void init( final IChartModel model, String id, final IParameterContainer parameters, URL context, POSITION pos, String[] valueArray );
}