package de.openali.diagram.framework.model.mapper.registry;

import de.openali.diagram.framework.model.mapper.IMapper;

/**
 * @author alibu
 */
public interface IMapperRegistryEventListener
{
  public void onMapperAdded( IMapper mapper );

  public void onMapperRemoved( IMapper mapper );
}
