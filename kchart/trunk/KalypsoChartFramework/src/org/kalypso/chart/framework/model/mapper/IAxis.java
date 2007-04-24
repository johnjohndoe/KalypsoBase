package org.kalypso.chart.framework.model.mapper;

import org.kalypso.chart.framework.exception.MalformedValueException;
import org.kalypso.chart.framework.model.data.IDataRange;

/**
 * @author burtscher
 */
public interface IAxis<T_logical> extends IMapper<T_logical, Integer>
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

  public void autorange( IDataRange<T_logical>[] ranges );

  public int logicalToScreen( T_logical value );

  public T_logical screenToLogical( int value );
  
  public int zeroToScreen();
  
  public IDataRange<T_logical> getDataRange();
  
  /**
   * sets visible range of axis
   * @param dataRange
   */
  public void setDataRange(IDataRange<T_logical> dataRange);
  
  /**
   * parses a String and returns a value the axis can understand
   * @throws MalformedValueException if the String cannot be parsed
   */
  public T_logical parseStringValue(String value) throws MalformedValueException;

}
