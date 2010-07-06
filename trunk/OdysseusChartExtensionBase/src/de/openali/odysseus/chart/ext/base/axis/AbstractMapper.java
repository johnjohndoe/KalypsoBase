package de.openali.odysseus.chart.ext.base.axis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.event.IMapperEventListener;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;

/**
 * @author burtscher Abstract implementation of IAxis - implements some methods which are equal for all concrete
 *         IAxis-classes
 */
public abstract class AbstractMapper implements IMapper
{
  private List<IMapperEventListener> m_listeners = new ArrayList<IMapperEventListener>();

  /**
   * @see de.openali.odysseus.chart.framework.model.event.IEventProvider#addListener(java.lang.Object)
   */
  @Override
  public void addListener( IMapperEventListener listener )
  {
    if( m_listeners.contains( listener ) )
      return;
    m_listeners.add( listener );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.event.IEventProvider#removeListener(java.lang.Object)
   */
  @Override
  public void removeListener( IMapperEventListener listener )
  {
    m_listeners.remove( listener );

  }

  private final String m_id;

  /**
   * Hashmap to store arbitrary key value pairs
   */
  private final HashMap<String, Object> m_data = new HashMap<String, Object>();

  @SuppressWarnings("rawtypes")
  private final HashMap<Class, IDataOperator> m_dataOperators = new HashMap<Class, IDataOperator>();

  public AbstractMapper( final String id )
  {
    m_id = id;
  }

  protected void fireMapperChanged( final IMapper mapper )
  {
    for( final IMapperEventListener l : m_listeners )
      l.onMapperChanged( mapper );
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#setRegistry(org.kalypso.chart.framework.axis.registry.IMapperRegistry)
   */
// public void setRegistry( final IMapperRegistry mapperRegistry )
// {
// m_registry = mapperRegistry;
// }
//
// public IMapperRegistry getRegistry( )
// {
// return m_registry;
// }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#getIdentifier()
   */
  @Override
  public String getId( )
  {
    return m_id;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#setData()
   */
  @Override
  public void setData( String id, Object data )
  {
    m_data.put( id, data );
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#getData()
   */
  @Override
  public Object getData( String id )
  {
    return m_data.get( id );
  }

//
// public void addListener( IMapperEventListener l )
// {
// m_handler.addListener( l );
// }
//
// public void removeListener( IMapperEventListener l )
// {
// m_handler.removeListener( l );
// }

// // EVIL: do not exhibit internal event manager
// public MapperEventHandler getEventHandler( )
// {
// return m_handler;
// }

  /**
   * returns a data converter which may be used to convert data to numbers which can directly be used by the mapper (and
   * vice versa)
   */
  @Override
  @SuppressWarnings({ "cast", "unchecked" })
  public <T> IDataOperator<T> getDataOperator( Class<T> clazz )
  {
    for( Class c : m_dataOperators.keySet() )
      if( c.isAssignableFrom( clazz ) )
        return (IDataOperator<T>) m_dataOperators.get( c );
    return null;// getRegistry().getDataOperator( clazz );
  }

  /**
   * Setting a dataOperator overrides global data operators; that way, individual operators can be set for each mapper
   * instance
   */
  @Override
  public <T> void addDataOperator( Class<T> clazz, IDataOperator<T> dataOperator )
  {
    m_dataOperators.put( clazz, dataOperator );
  }

}
