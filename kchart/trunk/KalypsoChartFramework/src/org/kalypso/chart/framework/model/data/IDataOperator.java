package org.kalypso.chart.framework.model.data;

import java.util.Comparator;

public interface IDataOperator<T_logical> extends IStringDataConverter<T_logical>
{

  public Number logicalToNumerical( T_logical logVal );

  public T_logical numericalToLogical( Number numVal );

  public IDataRange<T_logical> getContainingInterval( Number numVal, Number numIntervalWidth, Number numFixedPoint );

  public Comparator<T_logical> getComparator( );

  /**
   * returns a default range - this can be used if the range can be built automatically by an outside operation
   */
  public IDataRange<T_logical> getDefaultRange( );

}
