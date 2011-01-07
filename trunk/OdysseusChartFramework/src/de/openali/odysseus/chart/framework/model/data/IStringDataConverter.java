package de.openali.odysseus.chart.framework.model.data;

public interface IStringDataConverter<T> extends IStringParser<T>
{

  String logicalToString( T value );

}
