package de.openali.diagram.framework.model.mapper;

import de.openali.diagram.framework.model.data.IDataRange;

/**
 * @author burtscher
 */
public interface IAxis<T_logical extends Comparable> extends IMapper<T_logical, Integer>
{
  /**
   * @return the axis' unique identifier
   */
  public String getIdentifier( );

  /**
   * @return axis label
   */
  public String getLabel( );

  /**
   * @return DataClass which is understood by this axis
   */
  public Class< ? > getDataClass( );

  /**
   * @return Axis property - discrete or continous
   */
  public IAxisConstants.PROPERTY getProperty( );

  /**
   *  @return axis position - left, right, top, bottom
   */
  public IAxisConstants.POSITION getPosition( );

  /**
   * @return axis direction - positive or negative
   */
  public IAxisConstants.DIRECTION getDirection( );

  /** Same as getDirection() == NEGATIVE */
  public boolean isInverted( );

  
  
  
  
  /**
   * @return minimal displayable value
  
  public T_logical getFrom( );

  /**
   * sets minimal displayable value
  
  public void setFrom( T_logical min );

  /**
   * @return maximum displayable value
  
  public T_logical getTo( );

  /**
   * sets maximum displayable value
  
  public void setTo( T_logical max );
*/
  public void autorange( IDataRange<T_logical>[] ranges );

  public int logicalToScreen( T_logical value );

  public T_logical screenToLogical( int value );
  
  public int zeroToScreen();
  
  public IDataRange<T_logical> getDataRange();
  
  public void setDataRange(IDataRange<T_logical> dataRange);

}
