package de.openali.odysseus.chart.framework.model.event;

import de.openali.odysseus.chart.framework.model.mapper.IMapper;

/**
 * @author alibu
 */
public interface IMapperRegistryEventListener
{
  void onMapperAdded( IMapper mapper );

  void onMapperRemoved( IMapper mapper );

  void onMapperChanged( IMapper mapper );
}
