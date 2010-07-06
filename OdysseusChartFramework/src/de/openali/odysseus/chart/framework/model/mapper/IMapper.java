package de.openali.odysseus.chart.framework.model.mapper;

import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.event.IEventProvider;
import de.openali.odysseus.chart.framework.model.event.IMapperEventListener;

/**
 * @author burtscher
 */
public interface IMapper extends IEventProvider<IMapperEventListener>
{
  /**
   * @return the unique identifier
   */
  public String getId( );

  // public void setRegistry( IMapperRegistry mapperRegistry );

  // public IMapperRegistry getRegistry( );

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

  /**
   * Setting a dataOperator overrides global data operators; that way, individual operators can be set for each mapper
   * instance
   */
  public <T> void addDataOperator( Class<T> clazz, IDataOperator<T> dataOperator );

}
