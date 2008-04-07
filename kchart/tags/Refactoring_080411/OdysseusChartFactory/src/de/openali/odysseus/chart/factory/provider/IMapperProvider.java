package de.openali.odysseus.chart.factory.provider;

import de.openali.odysseus.chart.factory.config.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;
import de.openali.odysseus.chartconfig.x010.MapperType;

/**
 * @author alibu
 */
public interface IMapperProvider
{
  /**
   * @return axis created by the MapperProvider
   */
  @SuppressWarnings("unchecked")
  public IMapper getMapper( ) throws ConfigurationException;

  public void init( final MapperType mt );

  /**
   * returns XML configuration element for the given chart element
   */
  @SuppressWarnings("unchecked")
  public MapperType getXMLType( IMapper mapper );
}
