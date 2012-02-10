package de.openali.odysseus.chart.factory.provider;

import java.net.URL;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.layer.IChartComponentProvider;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.mapper.IRetinalMapper;

/**
 * @author alibu
 */
public interface IMapperProvider extends IChartComponentProvider
{
  /**
   * @return axis created by the MapperProvider
   */
  IRetinalMapper getMapper( ) throws ConfigurationException;

  void init( final IChartModel model, final String id, final IParameterContainer parameters, final URL context );

// /**
// * returns XML configuration element for the given chart element
// */
// @SuppressWarnings("unchecked")
// public MapperType getXMLType( IMapper mapper );
}
