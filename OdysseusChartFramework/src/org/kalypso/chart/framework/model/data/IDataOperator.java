package org.kalypso.chart.framework.model.data;

import java.text.Format;
import java.util.Comparator;

public interface IDataOperator<T_logical> extends IStringDataConverter<T_logical>
{

  public Number logicalToNumerical( T_logical logVal );

  public T_logical numericalToLogical( Number numVal );

  public IDataRange<T_logical> getContainingInterval( T_logical logVal, Number numIntervalWidth, T_logical logFixedPoint );

  public Comparator<T_logical> getComparator( );

  /**
   * returns a default range - this can be used if the range can not be built automatically by an outside operation
   */
  public IDataRange<T_logical> getDefaultRange( );

  /**
   * returns a format to format data values as strings; the format shall try to adapt itself to the given range
   */
  public Format getFormat( IDataRange<T_logical> range );

}
