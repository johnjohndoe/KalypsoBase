package de.openali.odysseus.chart.framework.model.mapper.registry.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;
import de.openali.odysseus.chart.framework.model.event.IMapperEventListener;
import de.openali.odysseus.chart.framework.model.event.IMapperRegistryEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.AbstractMapperEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.MapperRegistryEventHandler;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;
import de.openali.odysseus.chart.framework.model.mapper.registry.IAxisVisitor;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperVisitor;

/**
 * @author burtscher
 */
public class MapperRegistry implements IMapperRegistry
{
  protected final MapperRegistryEventHandler m_handler = new MapperRegistryEventHandler();

  /** axis-identifier --> axis */
  private final Map<String, IMapper> m_mappers = new HashMap<String, IMapper>();

  private final DataOperatorHelper m_dataOperatorHelper = new DataOperatorHelper();

  private final IMapperEventListener m_mapperEventListener = new AbstractMapperEventListener()
  {
    /**
     * @see de.openali.odysseus.chart.framework.impl.model.event.AbstractMapperEventListener#onMapperRangeChanged(de.openali.odysseus.chart.framework.model.mapper.IMapper)
     */
    @Override
    public void onMapperChanged( final IMapper eventMapper )
    {
      m_handler.fireMapperChanged( eventMapper );
    }
  };

  /*
   * (non-Javadoc)
   * @see org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#hasMapper(java.lang.String)
   */
  public boolean hasMapper( final String identifier )
  {
    return m_mappers.containsKey( identifier );
  }

  /*
   * (non-Javadoc)
   * @see org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#getMapper(java.lang.String)
   */
  @Override
  public IMapper getMapper( final String identifier )
  {
    return m_mappers.get( identifier );
  }

  /*
   * (non-Javadoc)
   * @see
   * org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#addMapper(org.kalypso.chart.framework.model.axis
   * .IMapper)
   */
  @Override
  public void addMapper( final IMapper mapper )
  {
    if( m_mappers.containsKey( mapper.getId() ) )
      Logger.logInfo( Logger.TOPIC_LOG_AXIS, "Mapper already present in registry: " + mapper.getId() + " - " );
    else
    {
      m_mappers.put( mapper.getId(), mapper );

      mapper.addListener( m_mapperEventListener );

      m_handler.fireMapperAdded( mapper );
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#removeMapper(org.kalypso.chart.framework.model.
   * axis.IMapper)
   */
  public void removeMapper( final IMapper mapper )
  {
    m_mappers.remove( mapper.getId() );
    m_handler.fireMapperRemoved( mapper );
  }

  /*
   * (non-Javadoc)
   * @see
   * org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#addMapperRegistryEventListener(org.kalypso.chart
   * .framework.model.axis.registry.IMapperRegistryEventListener)
   */
  @Override
  public void addListener( final IMapperRegistryEventListener l )
  {
    m_handler.addListener( l );
  }

  /*
   * (non-Javadoc)
   * @see
   * org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#removeMapperRegistryEventListener(org.kalypso.chart
   * .framework.model.axis.registry.IMapperRegistryEventListener)
   */
  @Override
  public void removeListener( final IMapperRegistryEventListener l )
  {
    m_handler.removeListener( l );
  }

  /*
   * (non-Javadoc)
   * @see
   * org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#getAxesAt(org.kalypso.chart.framework.model.axis
   * .IAxisConstants.POSITION)
   */
  @Override
  public IAxis[] getAxesAt( final POSITION pos )
  {
    final List<IAxis> axes = new ArrayList<IAxis>();
    for( final IAxis axis : getAxes() )
      if( axis.getPosition() == pos )
        axes.add( axis );
    return axes.toArray( new IAxis[0] );
  }

  /*
   * (non-Javadoc)
   * @see org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#getMappers()
   */
  @Override
  public IMapper[] getMappers( )
  {
    final Collection<IMapper> allMappers = m_mappers.values();
    final ArrayList<IMapper> mappers = new ArrayList<IMapper>();
    for( final IMapper mapper : allMappers )
      // nur hinzuf�gen, wenn keine Axis
      if( !(mapper instanceof de.openali.odysseus.chart.framework.model.mapper.IAxis) )
        mappers.add( mapper );
    return mappers.toArray( new IMapper[mappers.size()] );
  }

  /*
   * (non-Javadoc)
   * @see org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#getAxes()
   */
  @Override
  public IAxis[] getAxes( )
  {
    final Collection<IMapper> mappers = m_mappers.values();
    final ArrayList<IAxis> axes = new ArrayList<IAxis>();
    for( final IMapper mapper : mappers )
      if( mapper instanceof de.openali.odysseus.chart.framework.model.mapper.IAxis )
        axes.add( (IAxis) mapper );
    return axes.toArray( new IAxis[axes.size()] );
  }

  @Override
  public void accept( final IAxisVisitor visitor )
  {
    final Collection<IMapper> mappers = m_mappers.values();

    for( final IMapper mapper : mappers )
    {
      if( mapper instanceof IAxis )
        visitor.visit( (IAxis) mapper );
    }
  }

  @Override
  public void accept( final IMapperVisitor visitor )
  {
    final Collection<IMapper> mappers = m_mappers.values();

    for( final IMapper mapper : mappers )
    {
      visitor.visit( mapper );
    }
  }

  @Override
  public IAxis getAxis( final String id )
  {
    final IMapper mapper = m_mappers.get( id );
    if( mapper instanceof IAxis )
      return (IAxis) mapper;
    return null;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry#getNumericRangeAxisSnapshot()
   */
  @Override
  public Map<IAxis, IDataRange<Number>> getNumericRangeAxisSnapshot( )
  {
    final IAxis[] axes = getAxes();
    final Map<IAxis, IDataRange<Number>> axisMap = new HashMap<IAxis, IDataRange<Number>>();
    for( final IAxis axis : axes )
    {
      final IDataRange<Number> nr = axis.getNumericRange();
      axisMap.put( axis, new ComparableDataRange<Number>( new Number[] { nr.getMin(), nr.getMax() } ) );
    }
    return axisMap;
  }

  @Override
  public <T> IDataOperator<T> getDataOperator( final Class<T> clazz )
  {
    return m_dataOperatorHelper.getDataOperator( clazz );
  }

}