package org.kalypso.chart.framework.model.mapper;

import org.kalypso.chart.framework.model.data.IDataOperator;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.event.IEventProvider;
import org.kalypso.chart.framework.model.event.IMapperEventListener;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;

/**
 * @author burtscher
 */
public interface IMapper<T_logical, T_visual> extends IEventProvider<IMapperEventListener>
{
  /**
   * @return the axis' unique identifier
   */
  public String getIdentifier( );

  /**
   * @return DataClass which is understood by this axis
   */
  public Class< ? > getDataClass( );

  /**
   * @deprecated use numericToScreen instead
   */
  @Deprecated
  public int logicalToScreen( T_logical value );

  public void setRegistry( IMapperRegistry mapperRegistry );

  public IMapperRegistry getRegistry( );

  /**
   * method to store arbitrary data objects;
   */
  public void setData( String identifier, Object data );

  public Object getData( String identifier );

  /**
   * returns a data converter which may be used to convert strings to data which can directly be used by the mapper (and
   * vice versa)
   * 
   */
  @Deprecated
  public IDataOperator<T_logical> getDataOperator( );

  /**
   * returns a data converter which may be used to convert data to numbers which can directly be used by the mapper (and
   * vice versa)
   */
  public <T> IDataOperator<T> getDataOperator( Class<T> clazz );

  public IDataRange<T_logical> getLogicalRange( );

  /**
   * sets visible range of mapped data
   * 
   * @param dataRange
   */
  public void setLogicalRange( IDataRange<T_logical> dataRange );
}
