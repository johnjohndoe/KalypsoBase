package org.kalypso.chart.factory.provider;

import org.kalypso.chart.factory.configuration.exception.MapperProviderException;
import org.ksp.chart.factory.MapperType;
import org.kalypso.chart.framework.model.mapper.IMapper;


/**
 * @author alibu
 *
 */
public interface IMapperProvider
{
  /**
   * @return axis created by the MapperProvider
   */
  public IMapper getMapper( ) throws MapperProviderException;

  public void init( final MapperType mt );

  public Class<?> getDataClass();
}
