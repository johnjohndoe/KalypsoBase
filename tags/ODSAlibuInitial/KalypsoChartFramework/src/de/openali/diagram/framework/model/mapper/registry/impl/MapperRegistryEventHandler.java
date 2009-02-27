package de.openali.diagram.framework.model.mapper.registry.impl;

import java.util.ArrayList;
import java.util.List;

import de.openali.diagram.framework.model.mapper.IMapper;
import de.openali.diagram.framework.model.mapper.registry.IMapperRegistryEventListener;
import de.openali.diagram.framework.model.mapper.registry.IMapperRegistryEventProvider;

/**
 * @author alibu
 */
public class MapperRegistryEventHandler implements IMapperRegistryEventProvider
{
  private final List<IMapperRegistryEventListener> m_listeners = new ArrayList<IMapperRegistryEventListener>();

  /**
   * @see de.openali.diagram.framework.axis.IMapperRegistryEventProvider#addAxisRegistryEventListener(de.openali.diagram.framework.axis.IMapperRegistryEventListener)
   */
  public void addMapperRegistryEventListener( final IMapperRegistryEventListener l )
  {
    m_listeners.add( l );
  }

  /**
   * @see de.openali.diagram.framework.axis.IMapperRegistryEventProvider#removeAxisRegistryEventListener(de.openali.diagram.framework.axis.IMapperRegistryEventListener)
   */
  public void removeMapperRegistryEventListener( final IMapperRegistryEventListener l )
  {
    m_listeners.remove( l );
  }

  protected void fireMapperAdded( final IMapper mapper )
  {
    for( IMapperRegistryEventListener l : m_listeners )
      l.onMapperAdded( mapper );
  }

  protected void fireMapperRemoved( final IMapper mapper )
  {
    for( final IMapperRegistryEventListener l : m_listeners )
      l.onMapperRemoved( mapper );
  }
}
