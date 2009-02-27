package de.openali.diagram.framework.model.mapper.registry.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.openali.diagram.framework.logging.Logger;
import de.openali.diagram.framework.model.mapper.IAxis;
import de.openali.diagram.framework.model.mapper.IMapper;
import de.openali.diagram.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.diagram.framework.model.mapper.component.IAxisComponent;
import de.openali.diagram.framework.model.mapper.registry.IMapperRegistry;
import de.openali.diagram.framework.model.mapper.registry.IMapperRegistryEventListener;
import de.openali.diagram.framework.model.mapper.renderer.IAxisRenderer;

/**
 * @author burtscher
 */
public class MapperRegistry implements IMapperRegistry
{
  private final MapperRegistryEventHandler m_handler = new MapperRegistryEventHandler();

  /** axis-identifier --> axis */
  private final Map<String, IMapper> m_mappers = new HashMap<String, IMapper>();

  /** axis-identifier --> renderer */
  private final Map<String, IAxisRenderer> m_id2renderers = new HashMap<String, IAxisRenderer>();

  private final Map<Class< ? >, IAxisRenderer> m_class2renderers = new HashMap<Class< ? >, IAxisRenderer>();

  /** axis --> component */
  private final Map<IAxis, IAxisComponent> m_components = new HashMap<IAxis, IAxisComponent>();

  /* (non-Javadoc)
 * @see de.openali.diagram.framework.model.axis.registry.IMapperRegistry#hasMapper(java.lang.String)
 */
  public boolean hasMapper( String identifier )
  {
    return m_mappers.containsKey( identifier );
  }

  /* (non-Javadoc)
 * @see de.openali.diagram.framework.model.axis.registry.IMapperRegistry#getMapper(java.lang.String)
 */
  public IMapper getMapper( String identifier )
  {
    return m_mappers.get( identifier );
  }

  /* (non-Javadoc)
 * @see de.openali.diagram.framework.model.axis.registry.IMapperRegistry#addMapper(de.openali.diagram.framework.model.axis.IMapper)
 */
  public void addMapper( IMapper mapper )
  {
    if( m_mappers.containsKey( mapper.getIdentifier() ) )
      Logger.trace( "Mapper already present in registry: " + mapper.getIdentifier() + " - ");
    else
    {
      mapper.setRegistry( this );
      m_mappers.put( mapper.getIdentifier(), mapper );

      m_handler.fireMapperAdded( mapper );
    }
  }

  /* (non-Javadoc)
 * @see de.openali.diagram.framework.model.axis.registry.IMapperRegistry#removeMapper(de.openali.diagram.framework.model.axis.IMapper)
 */
  public void removeMapper( IMapper mapper )
  {
    m_mappers.remove( mapper.getIdentifier() );

    if (mapper instanceof de.openali.diagram.framework.model.mapper.IAxis)
    {
    	IAxis axis=(IAxis) mapper;
	    IAxisComponent component = m_components.get( axis );
	    if( component != null )
	    {
	      component.dispose();
	      m_components.remove( axis );
	    }
    }
    m_handler.fireMapperRemoved( mapper );
  }

  /* (non-Javadoc)
 * @see de.openali.diagram.framework.model.axis.registry.IMapperRegistry#clear()
 */
  public void clear( )
  {
    for( final IAxisComponent comp : m_components.values() )
      comp.dispose();
    m_components.clear();

    for( final IMapper mapper : m_mappers.values() )
    	m_handler.fireMapperRemoved( mapper );
    m_mappers.clear();

    m_id2renderers.clear();
    m_class2renderers.clear();
  }

  /* (non-Javadoc)
 * @see de.openali.diagram.framework.model.axis.registry.IMapperRegistry#addMapperRegistryEventListener(de.openali.diagram.framework.model.axis.registry.IMapperRegistryEventListener)
 */
  public void addMapperRegistryEventListener( IMapperRegistryEventListener l )
  {
    m_handler.addMapperRegistryEventListener( l );
  }

  /* (non-Javadoc)
 * @see de.openali.diagram.framework.model.axis.registry.IMapperRegistry#removeMapperRegistryEventListener(de.openali.diagram.framework.model.axis.registry.IMapperRegistryEventListener)
 */
  public void removeMapperRegistryEventListener( IMapperRegistryEventListener l )
  {
    m_handler.removeMapperRegistryEventListener( l );
  }



  /* (non-Javadoc)
 * @see de.openali.diagram.framework.model.axis.registry.IMapperRegistry#getAxesAt(de.openali.diagram.framework.model.axis.IAxisConstants.POSITION)
 */
  public IAxis[] getAxesAt( POSITION pos )
  {
	  List<IAxis> axes=new ArrayList<IAxis>(); 
	  for( final IAxis axis : getAxes() )
	    {
	    	if( axis.getPosition() == pos )
	    	      axes.add( axis );
	    }
	  return (IAxis[]) axes.toArray(new IAxis[0]);
  }

  /* (non-Javadoc)
 * @see de.openali.diagram.framework.model.axis.registry.IMapperRegistry#getRenderer(de.openali.diagram.framework.model.axis.IAxis)
 */
  public IAxisRenderer<?> getRenderer( final IAxis<?> axis )
  {
    IAxisRenderer<?> renderer = m_id2renderers.get( axis.getIdentifier() );
    if( renderer != null )
      return renderer;

    renderer = m_class2renderers.get( axis.getDataClass() );
    if( renderer != null )
      return renderer;

    for( final Entry<Class< ? >, IAxisRenderer> entry : m_class2renderers.entrySet() )
    {
      final Class< ? > dataClass = entry.getKey();
      renderer = entry.getValue();

      if( dataClass.isAssignableFrom( axis.getDataClass() ) )
        return renderer;
    }

    return null;
  }

  /* (non-Javadoc)
 * @see de.openali.diagram.framework.model.axis.registry.IMapperRegistry#getComponent(de.openali.diagram.framework.model.axis.IAxis)
 */
  public IAxisComponent getComponent( final IAxis axis )
  {
    return m_components.get( axis );
  }

  /* (non-Javadoc)
 * @see de.openali.diagram.framework.model.axis.registry.IMapperRegistry#setRenderer(java.lang.String, de.openali.diagram.framework.model.axis.renderer.IAxisRenderer)
 */
  public void setRenderer( String identifier, IAxisRenderer renderer )
  {
    m_id2renderers.put( identifier, renderer );
  }

  /* (non-Javadoc)
 * @see de.openali.diagram.framework.model.axis.registry.IMapperRegistry#unsetRenderer(java.lang.String)
 */
  public void unsetRenderer( final String axisIdentifier )
  {
    m_id2renderers.remove( axisIdentifier );
  }

  /* (non-Javadoc)
 * @see de.openali.diagram.framework.model.axis.registry.IMapperRegistry#setComponent(de.openali.diagram.framework.model.axis.IAxis, de.openali.diagram.framework.model.axis.component.IAxisComponent)
 */
  public void setComponent( final IAxis axis, final IAxisComponent comp )
  {
    m_components.put( axis, comp );
  }

  /* (non-Javadoc)
 * @see de.openali.diagram.framework.model.axis.registry.IMapperRegistry#setRenderer(java.lang.Class, de.openali.diagram.framework.model.axis.renderer.IAxisRenderer)
 */
  public void setRenderer( final Class< ? > dataClass, final IAxisRenderer renderer )
  {
    m_class2renderers.put( dataClass, renderer );
  }

  /* (non-Javadoc)
 * @see de.openali.diagram.framework.model.axis.registry.IMapperRegistry#unsetRenderer(java.lang.Class)
 */
  public void unsetRenderer( final Class< ? > dataClass )
  {
    m_class2renderers.remove( dataClass );
  }

  /* (non-Javadoc)
 * @see de.openali.diagram.framework.model.axis.registry.IMapperRegistry#getMappers()
 */
public IMapper[] getMappers()
  {
	  return (IMapper[]) m_mappers.values().toArray();
  }
  
  
  /* (non-Javadoc)
 * @see de.openali.diagram.framework.model.axis.registry.IMapperRegistry#getAxes()
 */
  public IAxis[] getAxes( )
  {
    final Collection<IMapper> mappers = m_mappers.values();
    final ArrayList<IAxis> axes=new ArrayList<IAxis>();
    for ( IMapper mapper : mappers) {
		if (mapper instanceof de.openali.diagram.framework.model.mapper.IAxis)
			axes.add((IAxis)mapper);
	}
    return axes.toArray( new IAxis[axes.size()] );
  }

  /* (non-Javadoc)
 * @see de.openali.diagram.framework.model.axis.registry.IMapperRegistry#getComponents()
 */
public Map<IAxis, IAxisComponent> getAxesToComponentsMap( )
  {
    return m_components;
  }


public IAxis getAxis(String id) {
	IMapper mapper = m_mappers.get(id);
	if (mapper instanceof IAxis)
		return (IAxis) mapper;
	return null;
}

 
}
