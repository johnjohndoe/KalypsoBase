package org.kalypso.chart.framework.model.event.impl;

import org.kalypso.chart.framework.model.event.IMapperRegistryEventListener;
import org.kalypso.chart.framework.model.mapper.IMapper;

/**
 * @author alibu
 */
public class MapperRegistryEventHandler extends AbstractEventProvider<IMapperRegistryEventListener>
{

  public void fireMapperAdded( final IMapper< ? , ? > mapper )
  {
    for( final IMapperRegistryEventListener l : getListeners() )
      l.onMapperAdded( mapper );
  }

  public void fireMapperRemoved( final IMapper< ? , ? > mapper )
  {
    for( final IMapperRegistryEventListener l : getListeners() )
      l.onMapperRemoved( mapper );
  }

  public void fireMapperRangeChanged( final IMapper< ? , ? > mapper )
  {
    for( final IMapperRegistryEventListener l : getListeners() )
      l.onMapperRangeChanged( mapper );
  }
}
