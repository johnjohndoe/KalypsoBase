package de.openali.odysseus.chart.framework.model.data;

import java.text.Format;
import java.util.Comparator;

public interface IDataOperator<T_logical> extends IStringDataConverter<T_logical>
{

  public Number logicalToNumeric( T_logical logVal );

  public T_logical numericToLogical( Number numVal );

  public IDataRange<T_logical> getContainingInterval( T_logical logVal, Number numIntervalWidth, T_logical logFixedPoint );

  public Comparator<T_logical> getComparator( );

  /**
   * returns a format to format data values as strings; the format shall try to adapt itself to the given range
   */
  public Format getFormat( IDataRange<Number> range );

}
