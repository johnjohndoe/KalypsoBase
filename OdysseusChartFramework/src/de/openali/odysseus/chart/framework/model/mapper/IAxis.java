package de.openali.odysseus.chart.framework.model.mapper;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRangeRestriction;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;

/**
 * An axis is a mapper which maps a number to a position. Axes use a logical and a numerical data range; the numerical
 * range is needed to express values between discrete logical data values. Keep in mind that logical and numerical data
 * ranges have to be synchronized when one of them is set.
 * 
 * @author burtscher
 */
public interface IAxis extends IMapper
{
  /**
   * This is used for configuration purposes: the numeric range has to be mapped to a concrete range in the Chartfile
   * (e.g. to data values). This field is set according to the configuration attribute type from the chartfile, so it
   * can be used to get a dataoperator which handles the original configuration type. (getDataOperator(getDataType())
   * 
   * @return class of data type which is intended by this axis
   */
  Class< ? > getDataClass( );

  /**
   * @return axis direction - positive or negative
   */
  DIRECTION getDirection( );

  /**
   * @return axis label
   */
  String getLabel( );

  /**
   * returns the internally used number range
   */
  IDataRange<Number> getNumericRange( );

  /**
   * @return axis position - left, right, top, bottom
   */
  POSITION getPosition( );

  /**
   * if an axis contains a preferred axis adjustment, than the auto adjustment will range the axis in a way that there
   * can also be some space before and after the data range;
   * 
   * @return the preferred adjustment or null if there isn't any
   */
  IAxisAdjustment getPreferredAdjustment( );

  /**
   * returns the internally absolute Min-Max-Value and min-max-intervall
   */
  IDataRange<Number> getRangeRestriction( );

  /**
   * @return The Renderer for this Axis, if any (maybe null)
   */
  IAxisRenderer getRenderer( );

  int getScreenHeight( );

  /** Same as getDirection() == NEGATIVE */
  boolean isInverted( );

  /**
   * @return true if this axis is used by Layers
   */
  boolean isVisible( );

  /**
   * converts a double value into the corresponding screen value
   */
  int normalizedToScreen( double d );

  /**
   * transforms a numeric value into a screen position
   */
  Integer numericToScreen( Number value );

  /**
   * converts a screen value into a normalized value
   */
  double screenToNormalized( int value );

  /**
   * transforms a screen position into a numeric value
   */
  Number screenToNumeric( int value );

  void setDirection( DIRECTION dir );

  void setLabel( String label );

  /**
   * sets the internally used number range
   */
  void setNumericRange( IDataRange<Number> range );

  void setPreferredAdjustment( IAxisAdjustment adj );

  /**
   * sets the internally used absolute Min-Max-Value
   */
  void setRangeRestriction( DataRangeRestriction<Number> range );

  void setRenderer( final IAxisRenderer axisRenderer );

  void setScreenHeight( int height );

  void setVisible( final boolean visible );

}
