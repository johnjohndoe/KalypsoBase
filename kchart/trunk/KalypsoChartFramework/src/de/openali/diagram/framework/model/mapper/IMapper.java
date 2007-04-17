package de.openali.diagram.framework.model.mapper;

import de.openali.diagram.framework.model.data.IDataRange;
import de.openali.diagram.framework.model.mapper.registry.IMapperRegistry;

/**
 * @author burtscher
 */
public interface IMapper<T_logical extends Comparable, T_visual>
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
   * @return minimal displayable value
   *
  public T_logical getFrom( );

  /**
   * sets minimal displayable value
   *
  public void setFrom( T_logical min );

  /**
   * @return maximum displayable value
   *
  public T_logical getTo( );

  /**
   * sets maximum displayable value
   *
  public void setTo( T_logical max );
	*/

  public void autorange( IDataRange<T_logical>[] ranges );

  public int logicalToScreen( T_logical value );

  public void setRegistry( IMapperRegistry mapperRegistry );

  public IMapperRegistry getRegistry(  );
  
  

}
