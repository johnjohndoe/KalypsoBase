package de.openali.odysseus.chart.factory.provider;

import java.net.URL;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.layer.IChartComponentProvider;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;

/**
 * @author burtscher A LayerProvider is needed to create layers from configuration data. Theres no 1:1 mapping from data
 *         soureces to layers, as 1.) several data sources can be merged to generate layer data and 2.) one data source
 *         can be used to create several layers. The LayerProvider is used to fetch, filter and analyze data and to
 *         provide layers according to the datas needs.
 */
public interface IAxisRendererProvider extends IChartComponentProvider
{
  /**
   * @return axis created by the AxisProvider
   */
  IAxisRenderer getAxisRenderer( ) throws ConfigurationException;

  void init( final IChartModel model, final String id, final IParameterContainer parameters, final URL context, final IStyleSet styleSet );

// /**
// * returns XML configuration element for the given chart element
// */
// public AxisRendererType getXMLType( IAxisRenderer axisRenderer );

}
