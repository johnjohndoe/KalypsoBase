package de.openali.odysseus.chart.framework.model.mapper;

import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.event.IEventProvider;
import de.openali.odysseus.chart.framework.model.event.IMapperEventListener;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;

/**
 * @author burtscher
 */
public interface IMapper<T_logical, T_visual> extends IEventProvider<IMapperEventListener>
{
  /**
   * @return the unique identifier
   */
  public String getIdentifier( );

  /**
   * @return DataClass which is understood by this axis
   */
  public Class< ? > getDataClass( );

  /**
   * 
   */
  public T_visual numericToScreen( T_logical value );

  public void setRegistry( IMapperRegistry mapperRegistry );

  public IMapperRegistry getRegistry( );

  /**
   * method to store arbitrary data objects;
   */
  public void setData( String identifier, Object data );

  public Object getData( String identifier );

  /**
   * returns a data converter which may be used to convert data to numbers which can directly be used by the mapper (and
   * vice versa)
   */
  public <T> IDataOperator<T> getDataOperator( Class<T> clazz );

}
