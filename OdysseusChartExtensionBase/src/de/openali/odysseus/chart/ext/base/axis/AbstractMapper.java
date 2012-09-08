package de.openali.odysseus.chart.ext.base.axis;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.event.IMapperEventListener;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;
import de.openali.odysseus.chart.framework.model.mapper.registry.impl.DataOperatorHelper;

/**
 * @author burtscher Abstract implementation of IAxis - implements some methods which are equal for all concrete
 *         IAxis-classes
 */
public abstract class AbstractMapper implements IMapper
{
  private final Set<IMapperEventListener> m_listeners = new LinkedHashSet<>();

  @Override
  public void addListener( final IMapperEventListener listener )
  {
    m_listeners.add( listener );
  }

  @Override
  public void removeListener( final IMapperEventListener listener )
  {
    m_listeners.remove( listener );

  }

  private final String m_identifier;

  /**
   * Hashmap to store arbitrary key value pairs
   */
  private final Map<String, Object> m_data = new HashMap<>();

  private final Map<Class< ? >, IDataOperator< ? >> m_dataOperators = new HashMap<>();

  public AbstractMapper( final String id )
  {
    m_identifier = id;
  }

  protected void fireMapperChanged( final IMapper mapper )
  {
    final IMapperEventListener[] listeners = m_listeners.toArray( new IMapperEventListener[] {} );
    for( final IMapperEventListener listener : listeners )
    {
      listener.onMapperChanged( mapper );
    }
  }

  @Override
  public String getIdentifier( )
  {
    return m_identifier;
  }

  @Override
  public void setData( final String id, final Object data )
  {
    m_data.put( id, data );
  }

  @Override
  public Object getData( final String id )
  {
    return m_data.get( id );
  }

  /**
   * returns a data converter which may be used to convert data to numbers which can directly be used by the mapper (and
   * vice versa)
   */
  @Override
  public <T> IDataOperator<T> getDataOperator( final Class<T> clazz )
  {
    for( final Class< ? > c : m_dataOperators.keySet() )
    {
      if( c.isAssignableFrom( clazz ) )
        return (IDataOperator<T>) m_dataOperators.get( c );
    }

    return new DataOperatorHelper().getDataOperator( clazz );
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