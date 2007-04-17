package de.openali.diagram.factory.provider;

import de.openali.diagram.factory.configuration.exception.MapperProviderException;
import de.openali.diagram.factory.configuration.xsd.MapperType;
import de.openali.diagram.framework.model.mapper.IMapper;

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

  public void init( final MapperType at );

  public Class<?> getDataClass();
}
