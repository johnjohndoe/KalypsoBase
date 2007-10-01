package org.kalypso.chart.framework.model.mapper;

import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;

/**
 * @author burtscher
 */
public interface IMapper<T_logical, T_visual>
{
  /**
   * @return the axis' unique identifier
   */
  public String getIdentifier( );

  /**
   * @return DataClass which is understood by this axis
   */
  public Class< ? > getDataClass( );

  public int logicalToScreen( T_logical value );

  public void setRegistry( IMapperRegistry mapperRegistry );

  public IMapperRegistry getRegistry( );

  /**
   * method to store arbitrary data objects;
   */
  public void setData( String identifier, Object data );

  public Object getData( String identifier );

}
