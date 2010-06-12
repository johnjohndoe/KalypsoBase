package de.openali.odysseus.chart.ext.base.axis;

import java.util.HashMap;

import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.event.IMapperEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.MapperEventHandler;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;

/**
 * @author burtscher Abstract implementation of IAxis - implements some methods which are equal for all concrete
 *         IAxis-classes
 */
public abstract class AbstractMapper implements IMapper
{
  private IMapperRegistry m_registry = null;

  private final String m_id;

  private final MapperEventHandler m_handler = new MapperEventHandler();

  /**
   * Hashmap to store arbitrary key value pairs
   */
  private final HashMap<String, Object> m_data = new HashMap<String, Object>();

  private final HashMap<Class, IDataOperator> m_dataOperators = new HashMap<Class, IDataOperator>();

  public AbstractMapper( final String id )
  {
    m_id = id;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#setRegistry(org.kalypso.chart.framework.axis.registry.IMapperRegistry)
   */
  @Override
  public void setRegistry( final IMapperRegistry mapperRegistry )
  {
    m_registry = mapperRegistry;
  }

  @Override
  public IMapperRegistry getRegistry( )
  {
    return m_registry;
  }

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
  public void setData( final String id, final Object data )
  {
    m_data.put( id, data );
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#getData()
   */
  @Override
  public Object getData( final String id )
  {
    return m_data.get( id );
  }

  @Override
  public void addListener( final IMapperEventListener l )
  {
    m_handler.addListener( l );
  }

  @Override
  public void removeListener( final IMapperEventListener l )
  {
    m_handler.removeListener( l );
  }

  // EVIL: do not exhibit internal event manager
  public MapperEventHandler getEventHandler( )
  {
    return m_handler;
  }

  /**
   * returns a data converter which may be used to convert data to numbers which can directly be used by the mapper (and
   * vice versa)
   */
  @Override
  @SuppressWarnings({ "cast", "unchecked" })
  public <T> IDataOperator<T> getDataOperator( final Class<T> clazz )
  {
    for( final Class c : m_dataOperators.keySet() )
      if( c.isAssignableFrom( clazz ) )
        return (IDataOperator<T>) m_dataOperators.get( c );
    return getRegistry().getDataOperator( clazz );
  }

  /**
   * Setting a dataOperator overrides global data operators; that way, individual operators can be set for each mapper
   * instance
   */
  @Override
  public <T> void addDataOperator( final Class<T> clazz, final IDataOperator<T> dataOperator )
  {
    m_dataOperators.put( clazz, dataOperator );
  }

}
