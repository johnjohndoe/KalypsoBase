package de.openali.odysseus.chart.framework.model.data;

public interface IStringDataConverter<T> extends IStringParser<T>
{

  public String logicalToString( T value );

}
