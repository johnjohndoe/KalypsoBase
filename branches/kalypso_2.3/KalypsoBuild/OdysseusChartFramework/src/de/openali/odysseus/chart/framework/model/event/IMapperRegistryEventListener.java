package de.openali.odysseus.chart.framework.model.event;

import de.openali.odysseus.chart.framework.model.mapper.IMapper;

/**
 * @author alibu
 */
public interface IMapperRegistryEventListener
{
  public void onMapperAdded( IMapper mapper );

  public void onMapperRemoved( IMapper mapper );

  public void onMapperChanged( IMapper mapper );
}
