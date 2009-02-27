package org.kalypso.chart.ext.base.axis;

import java.util.Comparator;
import java.util.HashMap;

import org.apache.commons.collections.comparators.ComparableComparator;
import org.kalypso.chart.framework.model.event.IMapperEventListener;
import org.kalypso.chart.framework.model.event.impl.MapperEventHandler;
import org.kalypso.chart.framework.model.mapper.IMapper;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;

/**
 * @author burtscher Abstract implementation of IAxis - implements some methods which are equal for all concrete
 *         IAxis-classes
 */
public abstract class AbstractMapper<T_logical, T_visual> implements IMapper<T_logical, T_visual>
{
  private IMapperRegistry m_registry = null;

  private final String m_id;

  private final Comparator<T_logical> m_dataComparator;

  private final Class< ? > m_dataClass;

  private final MapperEventHandler m_handler = new MapperEventHandler();

  /**
   * Hashmap to store arbitrary key value pairs
   */
  private final HashMap<String, Object> m_data = new HashMap<String, Object>();

  /**
   * Uses a ComparableComparator as dataComparator
   */
  @SuppressWarnings("unchecked")
  public AbstractMapper( final String id, final Class< ? > dataClass )
  {
    this( id, new ComparableComparator(), dataClass );
  }

  public AbstractMapper( final String id, final Comparator<T_logical> dataComparator, final Class< ? > dataClass )
  {
    m_id = id;
    m_dataComparator = dataComparator;
    m_dataClass = dataClass;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#getDataClass()
   */
  public Class< ? > getDataClass( )
  {
    return m_dataClass;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#setRegistry(org.kalypso.chart.framework.axis.registry.IMapperRegistry)
   */
  public void setRegistry( final IMapperRegistry mapperRegistry )
  {
    m_registry = mapperRegistry;
  }

  public IMapperRegistry getRegistry( )
  {
    return m_registry;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#getIdentifier()
   */
  public String getIdentifier( )
  {
    return m_id;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#setData()
   */
  public void setData( String id, Object data )
  {
    m_data.put( id, data );
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#getData()
   */
  public Object getData( String id )
  {
    return m_data.get( id );
  }

  protected Comparator<T_logical> getComparator( )
  {
    return m_dataComparator;
  }

  public void addListener( IMapperEventListener l )
  {
    m_handler.addListener( l );
  }

  public void removeListener( IMapperEventListener l )
  {
    m_handler.removeListener( l );
  }

  public MapperEventHandler getEventHandler( )
  {
    return m_handler;
  }
}
