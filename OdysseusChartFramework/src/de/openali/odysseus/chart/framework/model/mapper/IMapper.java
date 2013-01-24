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
  String getIdentifier( );

  /**
   * method to store arbitrary data objects;
   */
  void setData( String identifier, Object data );

  Object getData( String identifier );

  /**
   * returns a data converter which may be used to convert data to numbers which can directly be used by the mapper (and
   * vice versa)
   */
  <T> IDataOperator<T> getDataOperator( Class<T> clazz );

  /**
   * Setting a dataOperator overrides global data operators; that way, individual operators can be set for each mapper
   * instance
   */
  <T> void addDataOperator( Class<T> clazz, IDataOperator<T> dataOperator );

}
