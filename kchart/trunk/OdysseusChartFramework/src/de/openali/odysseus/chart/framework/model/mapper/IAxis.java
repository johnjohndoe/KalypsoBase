package de.openali.odysseus.chart.framework.model.mapper;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;

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
public interface IAxis extends IMapper<Number, Integer>
{

  /**
   * @return axis label
   */
  public String getLabel( );

  public void setLabel( String label );

  /**
   * @return axis position - left, right, top, bottom
   */
  public POSITION getPosition( );

  /**
   * @return axis direction - positive or negative
   */
  public DIRECTION getDirection( );

  public void setDirection( DIRECTION dir );

  /** Same as getDirection() == NEGATIVE */
  public boolean isInverted( );

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
  public Integer numericToScreen( Number value );

  /**
   * if an axis contains a preferred axis adjustment, than the auto adjustment will range the axis in a way that there
   * can also be some space before and after the data range;
   * 
   * @return the preferred adjustment or null if there isn't any
   */
  public IAxisAdjustment getPreferredAdjustment( );

  public void setPreferredAdjustment( IAxisAdjustment adj );

  /**
   * converts a double value into the corresponding screen value
   */
  public int normalizedToScreen( double d );

  /**
   * converts a screen value into a normalized value
   */
  public double screenToNormalized( int value );

  public void setScreenHeight( int height );

  public int getScreenHeight( );

}
