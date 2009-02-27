package org.kalypso.chart.framework.model.mapper;

import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DATATYPE;

/**
 * 
 * Axes use a logical and a numerical data range; the numerical range is needed to express values between discrete
 * logical data values.
 * 
 * Keep in mind that logical and numerical data ranges have to be synchronized when one of them is set.
 * 
 * 
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
  @Deprecated
  public Class< ? > getDataClass( );

  /**
   * @return Axis property - discrete or continous
   */
  public IAxisConstants.PROPERTY getProperty( );

  /**
   * @return axis position - left, right, top, bottom
   */
  public IAxisConstants.POSITION getPosition( );

  /**
   * @return axis direction - positive or negative
   */
  public IAxisConstants.DIRECTION getDirection( );

  /** Same as getDirection() == NEGATIVE */
  public boolean isInverted( );

  @Deprecated
  public int logicalToScreen( T_logical value );

  @Deprecated
  public T_logical screenToLogical( int value );

  public int zeroToScreen( );

  /**
   * returns the internally used number range
   */
  public IDataRange<Number> getNumericRange( );

  /**
   * sets the internally used number range
   */
  public void setNumericRange( IDataRange<Number> range );

  /**
   * transforms a screen position into a numeric value
   */
  public Number screenToNumeric( int value );

  /**
   * transforms a numeric value into a screen position
   */
  public int numericToScreen( Number value );

  /**
   * if an axis contains a preferred axis adjustment, than the auto adjustment will range the axis in a way that there
   * can also be some space before and after the data range;
   * 
   * @return the preferred adjustment or null if there isn't any
   */
  public IAxisAdjustment getPreferredAdjustment( );

  public void setPreferredAdjustment( IAxisAdjustment adj );

  public DATATYPE getDataType( );

}
