package de.openali.odysseus.chart.framework.model.mapper;

import de.openali.odysseus.chart.framework.exception.MalformedValueException;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRangeRestriction;
import de.openali.odysseus.chart.framework.model.event.IEventProvider;
import de.openali.odysseus.chart.framework.model.event.IAxisEventListener;
import de.openali.odysseus.chart.framework.model.impl.IAxisVisitorBehavior;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;

/**
 * An axis is a mapper which maps a number to a position. Axes use a logical and a numerical data range; the numerical
 * range is needed to express values between discrete logical data values. Keep in mind that logical and numerical data
 * ranges have to be synchronized when one of them is set.
 * 
 * @author burtscher
 */
public interface IAxis<T> extends IEventProvider<IAxisEventListener>//extends IMapper
{
  void addLabel( final TitleTypeBean title );

  void clearLabels( );

  IAxisVisitorBehavior getAxisVisitorBehavior( );

  String getIdentifier( );

  /**
   * method to store arbitrary data objects;
   */
  @Deprecated
  void setData( String identifier, Object data );

  @Deprecated
  Object getData( String identifier );

  /**
   * This is used for configuration purposes: the numeric range has to be mapped to a concrete range in the Chartfile
   * (e.g. to data values). This field is set according to the configuration attribute type from the chartfile, so it
   * can be used to get a dataoperator which handles the original configuration type. (getDataOperator(getDataType())
   * 
   * @return class of data type which is intended by this axis
   */
  Class<T> getDataClass( );

  /**
   * @return axis direction - positive or negative
   */
  DIRECTION getDirection( );

  @Deprecated
  /**
   * @return axis label
   * @use getLabels() instead
   * @deprecated
   */
  String getLabel( );

  TitleTypeBean[] getLabels( );

  IDataRange<T> getLogicalRange( );

  /**
   * returns the internally used number range
   */
  IDataRange<Double> getNumericRange( );

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

  int getScreenOffset( );

  /**
   * @return true if this axis is used by Layers
   */
  boolean isVisible( );

  Double logicalToNumeric( T value );

  int logicalToScreen( T value );

  String logicalToXMLString( T value );

  Double normalizedToNumeric( Double value );

  int normalizedToScreen( Double value );

  T numericToLogical( Double value );

  Double numericToNormalized( Double value );

  /**
   * transforms a numeric value into a screen position
   */
  int numericToScreen( Double value );

  T screenToLogical( int value );

  Double screenToNormalized( int value );

  /**
   * transforms a screen position into a numeric value
   */
  Double screenToNumeric( int value );

  void setDirection( DIRECTION dir );

  /**
   * @deprecated * @use addLabel(title) instead
   */
  @Deprecated
  void setLabel( String label );

  void setLogicalRange( IDataRange<T> range );

  /**
   * sets the internally used number range
   */
  void setNumericRange( IDataRange<Double> range );

  void setPreferredAdjustment( IAxisAdjustment adj );

  /**
   * sets the internally used absolute Min-Max-Value
   */
  void setRangeRestriction( DataRangeRestriction<Number> range );

  void setRenderer( final IAxisRenderer axisRenderer );

  void setScreenHeight( int height );

  void setScreenOffset( final int offset, int axisWidth );

  void setVisible( final boolean visible );

  T xmlStringToLogical( String value ) throws MalformedValueException;
}
