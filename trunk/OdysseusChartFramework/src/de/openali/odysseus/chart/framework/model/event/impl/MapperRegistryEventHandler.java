package de.openali.odysseus.chart.framework.model.event.impl;

import de.openali.odysseus.chart.framework.model.event.IMapperRegistryEventListener;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;

/**
 * @author alibu
 */
public class MapperRegistryEventHandler extends AbstractEventProvider<IMapperRegistryEventListener>
{
  public void fireMapperAdded( final IMapper mapper )
  {
    for( final IMapperRegistryEventListener l : getListeners() )
      l.onMapperAdded( mapper );
  }

  public void fireMapperRemoved( final IMapper mapper )
  {
    for( final IMapperRegistryEventListener l : getListeners() )
      l.onMapperRemoved( mapper );
  }

  public void fireMapperChanged( final IMapper mapper )
  {
    for( final IMapperRegistryEventListener l : getListeners() )
      l.onMapperChanged( mapper );
  }
}
