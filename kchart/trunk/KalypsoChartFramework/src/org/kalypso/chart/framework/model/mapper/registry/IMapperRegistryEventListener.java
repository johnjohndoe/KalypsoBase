package org.kalypso.chart.framework.model.mapper.registry;

import org.kalypso.chart.framework.model.mapper.IMapper;

/**
 * @author alibu
 */
public interface IMapperRegistryEventListener
{
  public void onMapperAdded( IMapper mapper );

  public void onMapperRemoved( IMapper mapper );
}
