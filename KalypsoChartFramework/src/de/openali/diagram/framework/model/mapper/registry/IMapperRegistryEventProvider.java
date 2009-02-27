package de.openali.diagram.framework.model.mapper.registry;

/**
 * @author alibu
 */
public interface IMapperRegistryEventProvider
{
  public void addMapperRegistryEventListener( IMapperRegistryEventListener l );

  public void removeMapperRegistryEventListener( IMapperRegistryEventListener l );
}
