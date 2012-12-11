package de.openali.odysseus.chart.framework.model.mapper.registry.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.event.IAxisEventListener;
import de.openali.odysseus.chart.framework.model.event.IAxisRegistryEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.AbstractAxisEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.AxisRegistryEventHandler;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.registry.IAxisRegistry;
import de.openali.odysseus.chart.framework.model.mapper.registry.IAxisVisitor;

/**
 * @author burtscher
 */
public class AxisRegistry implements IAxisRegistry
{
  protected final AxisRegistryEventHandler m_handler = new AxisRegistryEventHandler();

  /** axis-identifier --> axis */
  private final Map<String, IAxis> m_mappers = new HashMap<>();

  private final IAxisEventListener m_mapperEventListener = new AbstractAxisEventListener()
  {
    /**
     * @see de.openali.odysseus.chart.framework.impl.model.event.AbstractMapperEventListener#onMapperRangeChanged(de.openali.odysseus.chart.framework.model.mapper.IMapper)
     */
    @Override
    public void onAxisChanged( final IAxis<?> axis )
    {
      m_handler.fireAxisChanged( axis );
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
//  @Override
//  public IMapper getMapper( final String identifier )
//  {
//    return m_mappers.get( identifier );
//  }

  /*
   * (non-Javadoc)
   * @see
   * org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#addMapper(org.kalypso.chart.framework.model.axis
   * .IMapper)
   */
  @Override
  public void addAxis( final IAxis axis )
  {
    if( m_mappers.containsKey( axis.getIdentifier() ) )
      Logger.logInfo( Logger.TOPIC_LOG_AXIS, "Axis already present in registry: " + axis.getIdentifier() + " - " ); //$NON-NLS-1$ //$NON-NLS-2$
    else
    {
      m_mappers.put( axis.getIdentifier(), axis );

      axis.addListener( m_mapperEventListener );

      m_handler.fireAxisAdded( axis );
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#removeMapper(org.kalypso.chart.framework.model.
   * axis.IMapper)
   */
  public void removeAxis( final IAxis axis )
  {
    m_mappers.remove( axis.getIdentifier() );
    m_handler.fireAxisRemoved( axis );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.registry.IAxisRegistry#clear()
   */
  @Override
  public void clear( )
  {
    for( final IAxis axis : getAxes() )
    {
      m_mappers.remove( axis.getIdentifier() );
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * org.kalypso.chart.framework.model.axis.registry.IMapperRegistry#addMapperRegistryEventListener(org.kalypso.chart
   * .framework.model.axis.registry.IMapperRegistryEventListener)
   */
  @Override
  public void addListener( final IAxisRegistryEventListener l )
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
  public void removeListener( final IAxisRegistryEventListener l )
  {
    m_handler.removeListener( l );
  }

  @Override
  public IAxis[] getAxesAt( final POSITION pos )
  {
    final List<IAxis> axes = new ArrayList<>();
    for( final IAxis axis : getAxes() )
    {
      if( axis.getPosition() == pos )
        axes.add( axis );
    }

    return axes.toArray( new IAxis[0] );
  }

//  @Override
//  public IMapper[] getMappers( )
//  {
//    final Collection<IMapper> allMappers = m_mappers.values();
//    final ArrayList<IMapper> mappers = new ArrayList<>();
//    for( final IMapper mapper : allMappers )
//    {
//      // nur hinzufügen, wenn keine Axis
//      if( !(mapper instanceof de.openali.odysseus.chart.framework.model.mapper.IAxis) )
//        mappers.add( mapper );
//    }
//
//    return mappers.toArray( new IMapper[mappers.size()] );
//  }

  @Override
  public IAxis[] getAxes( )
  {
    final Collection<IAxis> axes = m_mappers.values();
//    final ArrayList<IAxis> axes = new ArrayList<>();
//    for( final IAxis axis : mappers )
//    {
//      if( mapper instanceof de.openali.odysseus.chart.framework.model.mapper.IAxis )
//        axes.add( (IAxis) mapper );
//    }

    return axes.toArray( new IAxis[axes.size()] );
  }

  @Override
  public void accept( final IAxisVisitor visitor )
  {
    // final Collection<IMapper> mappers = m_mappers.values();

    for( final IAxis mapper : m_mappers.values() )
    {
      // if( mapper instanceof IAxis )
      visitor.visit( mapper );
    }
  }

//  @Override
//  public void accept( final IMapperVisitor visitor )
//  {
//    final Collection<IMapper> mappers = m_mappers.values();
//
//    for( final IMapper mapper : mappers )
//    {
//      visitor.visit( mapper );
//    }
//  }

  @Override
  public IAxis getAxis( final String id )
  {
    return m_mappers.get( id );
//    final IMapper mapper = m_mappers.get( id );
//    if( mapper instanceof IAxis )
//      return (IAxis) mapper;
//    return null;
  }

//  @Override
//  public Map<IAxis, IDataRange<Number>> getNumericRangeAxisSnapshot( )
//  {
//    final IAxis[] axes = getAxes();
//    final Map<IAxis, IDataRange<Number>> axisMap = new HashMap<>();
//    for( final IAxis axis : axes )
//    {
//      final IDataRange<Number> nr = axis.getNumericRange();
//      axisMap.put( axis, DataRange.createFromComparable( nr.getMin(), nr.getMax() ) );
//    }
//    return axisMap;
//  }

//  @Override
//  public <T> IDataOperator<T> getDataOperator( final Class<T> clazz )
//  {
//    return m_dataOperatorHelper.getDataOperator( clazz );
//  }
}