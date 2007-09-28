package org.kalypso.chart.framework.model.data;


public interface IDataOperator<T_logical> extends IStringDataConverter<T_logical>
{

  public Number logicalToNumerical( T_logical logVal );

  public T_logical numericalToLogical( Number numVal );

  public IDataRange<T_logical> getContainingInterval( Number numVal, Number numIntervalWidth, Number numFixedPoint );

}
