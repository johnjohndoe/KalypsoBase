package org.kalypso.chart.framework.model.mapper.registry.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.data.impl.ComparableDataRange;
import org.kalypso.chart.framework.model.event.IMapperEventListener;
import org.kalypso.chart.framework.model.event.IMapperRegistryEventListener;
import org.kalypso.chart.framework.model.event.impl.AbstractMapperEventListener;
import org.kalypso.chart.framework.model.event.impl.MapperRegistryEventHandler;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IMapper;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.POSITION;
import org.kalypso.chart.framework.model.mapper.component.IAxisComponent;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;
import org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer;

/**
 * @author burtscher
 */
public class MapperRegistry implements IMapperRegistry
{
  protected final MapperRegistryEventHandler m_handler = new MapperRegistryEventHandler();

  /** axis-identifier --> axis */
  private final Map<String, IMapper< ? , ? >> m_mappers = new HashMap<String, IMapper< ? , ? >>();

  /** axis-identifier --> renderer */
  private final Map<String, IAxisRenderer< ? >> m_id2renderers = new HashMap<String, IAxisRenderer< ? >>();

  private final Map<Class< ? >, IAxisRenderer< ? >> m_class2renderers = new HashMap<Class< ? >, IAxisRenderer< ? >>();

  /** axis --> component */
  private final Map<IAxis< ? >, IAxisComponent> m_components = new HashMap<IAxis< ? >, IAxisComponent>();

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#hasMapper(java.lang.String)
   */
  public boolean hasMapper( String identifier )
  {
    return m_mappers.containsKey( identifier );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#getMapper(java.lang.String)
   */
  public IMapper< ? , ? > getMapper( String identifier )
  {
    return m_mappers.get( identifier );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#addMapper(org.kalypso.chart.framework.model.axis.IMapper)
   */
  public void addMapper( IMapper< ? , ? > mapper )
  {
    if( m_mappers.containsKey( mapper.getIdentifier() ) )
      Logger.trace( "Mapper already present in registry: " + mapper.getIdentifier() + " - " );
    else
    {
      mapper.setRegistry( this );
      m_mappers.put( mapper.getIdentifier(), mapper );

      IMapperEventListener mel = new AbstractMapperEventListener()
      {
        /**
         * @see org.kalypso.chart.framework.model.event.impl.AbstractMapperEventListener#onMapperRangeChanged(org.kalypso.chart.framework.model.mapper.IMapper)
         */
        @Override
        public void onMapperRangeChanged( IMapper< ? , ? > eventMapper )
        {
          m_handler.fireMapperRangeChanged( eventMapper );
          if( eventMapper instanceof IAxis< ? > )
          {
            IAxis axis = (IAxis) eventMapper;
            IAxisRenderer< ? > renderer = getRenderer( axis );
            renderer.invalidateTicks( axis );
          }
        }
      };
      mapper.addListener( mel );

      m_handler.fireMapperAdded( mapper );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#removeMapper(org.kalypso.chart.framework.model.axis.IMapper)
   */
  public void removeMapper( IMapper< ? , ? > mapper )
  {
    m_mappers.remove( mapper.getIdentifier() );

    if( mapper instanceof org.kalypso.chart.framework.model.mapper.IAxis )
    {
      final IAxis< ? > axis = (IAxis< ? >) mapper;
      final IAxisComponent component = m_components.get( axis );
      if( component != null )
      {
        component.dispose();
        m_components.remove( axis );
      }
    }
    m_handler.fireMapperRemoved( mapper );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#clear()
   */
  public void clear( )
  {
    for( final IAxisComponent comp : m_components.values() )
      comp.dispose();
    m_components.clear();

    for( final IMapper< ? , ? > mapper : m_mappers.values() )
      m_handler.fireMapperRemoved( mapper );
    m_mappers.clear();

    Collection<IAxisComponent> values = getAxesToComponentsMap().values();
    for( IAxisComponent comp : values )
    {
      comp.dispose();
    }

    for( IAxisRenderer< ? > rend : m_id2renderers.values() )
    {
      rend.dispose();
    }

    m_id2renderers.clear();
    m_class2renderers.clear();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#addMapperRegistryEventListener(org.kalypso.chart.framework.model.axis.registry.IMapperRegistryEventListener)
   */
  public void addListener( IMapperRegistryEventListener l )
  {
    m_handler.addListener( l );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#removeMapperRegistryEventListener(org.kalypso.chart.framework.model.axis.registry.IMapperRegistryEventListener)
   */
  public void removeListener( IMapperRegistryEventListener l )
  {
    m_handler.removeListener( l );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#getAxesAt(org.kalypso.chart.framework.model.axis.IAxisConstants.POSITION)
   */
  public IAxis< ? >[] getAxesAt( POSITION pos )
  {
    final List<IAxis< ? >> axes = new ArrayList<IAxis< ? >>();
    for( final IAxis< ? > axis : getAxes() )
    {
      if( axis.getPosition() == pos )
        axes.add( axis );
    }
    return axes.toArray( new IAxis[0] );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#getRenderer(org.kalypso.chart.framework.model.axis.IAxis)
   */
  public IAxisRenderer< ? > getRenderer( final IAxis< ? > axis )
  {
    IAxisRenderer< ? > renderer = m_id2renderers.get( axis.getIdentifier() );
    if( renderer != null )
      return renderer;

    renderer = m_class2renderers.get( axis.getDataClass() );
    if( renderer != null )
      return renderer;

    for( final Entry<Class< ? >, IAxisRenderer< ? >> entry : m_class2renderers.entrySet() )
    {
      final Class< ? > dataClass = entry.getKey();
      renderer = entry.getValue();

      if( dataClass.isAssignableFrom( axis.getDataClass() ) )
        return renderer;
    }

    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#getComponent(org.kalypso.chart.framework.model.axis.IAxis)
   */
  public IAxisComponent getComponent( final IAxis< ? > axis )
  {
    return m_components.get( axis );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#setRenderer(java.lang.String,
   *      org.kalypso.chart.framework.model.axis.renderer.IAxisRenderer)
   */
  public void setRenderer( String identifier, IAxisRenderer< ? > renderer )
  {
    m_id2renderers.put( identifier, renderer );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#unsetRenderer(java.lang.String)
   */
  public void unsetRenderer( final String axisIdentifier )
  {
    m_id2renderers.remove( axisIdentifier );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#setComponent(org.kalypso.chart.framework.model.axis.IAxis,
   *      org.kalypso.chart.framework.model.axis.component.IAxisComponent)
   */
  public void setComponent( final IAxis< ? > axis, final IAxisComponent comp )
  {
    m_components.put( axis, comp );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#setRenderer(java.lang.Class,
   *      org.kalypso.chart.framework.model.axis.renderer.IAxisRenderer)
   */
  public void setRenderer( final Class< ? > dataClass, final IAxisRenderer< ? > renderer )
  {
    m_class2renderers.put( dataClass, renderer );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#unsetRenderer(java.lang.Class)
   */
  public void unsetRenderer( final Class< ? > dataClass )
  {
    m_class2renderers.remove( dataClass );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#getMappers()
   */
  public IMapper< ? , ? >[] getMappers( )
  {
    final Collection<IMapper< ? , ? >> allMappers = m_mappers.values();
    final ArrayList<IMapper< ? , ? >> mappers = new ArrayList<IMapper< ? , ? >>();
    for( final IMapper< ? , ? > mapper : allMappers )
    {
      // nur hinzufügen, wenn keine Axis
      if( !(mapper instanceof org.kalypso.chart.framework.model.mapper.IAxis) )
        mappers.add( mapper );
    }
    return mappers.toArray( new IMapper[mappers.size()] );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#getAxes()
   */
  public IAxis< ? >[] getAxes( )
  {
    final Collection<IMapper< ? , ? >> mappers = m_mappers.values();
    final ArrayList<IAxis< ? >> axes = new ArrayList<IAxis< ? >>();
    for( final IMapper< ? , ? > mapper : mappers )
    {
      if( mapper instanceof org.kalypso.chart.framework.model.mapper.IAxis )
        axes.add( (IAxis< ? >) mapper );
    }
    return axes.toArray( new IAxis[axes.size()] );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#getComponents()
   */
  public Map<IAxis< ? >, IAxisComponent> getAxesToComponentsMap( )
  {
    return m_components;
  }

  public IAxis< ? > getAxis( String id )
  {
    final IMapper< ? , ? > mapper = m_mappers.get( id );
    if( mapper instanceof IAxis )
      return (IAxis< ? >) mapper;
    return null;
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry#getRenderer(java.lang.String)
   */
  public IAxisRenderer< ? > getRenderer( String id )
  {
    for( IAxisRenderer< ? > rend : m_id2renderers.values() )
    {
      if( rend.getId().equals( id ) )
        return rend;
    }
    return null;
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry#getNumericRangeAxisSnapshot()
   */
  public Map<IAxis< ? >, IDataRange<Number>> getNumericRangeAxisSnapshot( )
  {
    IAxis< ? >[] axes = getAxes();
    Map<IAxis< ? >, IDataRange<Number>> axisMap = new HashMap<IAxis< ? >, IDataRange<Number>>();
    for( IAxis< ? > axis : axes )
    {
      IDataRange<Number> nr = axis.getNumericRange();
      axisMap.put( axis, new ComparableDataRange<Number>( new Number[] { nr.getMin(), nr.getMax() } ) );
    }
    return axisMap;
  }

}
