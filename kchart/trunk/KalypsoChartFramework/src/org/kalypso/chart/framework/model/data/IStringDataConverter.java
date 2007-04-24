package org.kalypso.chart.framework.model.data;

public interface IStringDataConverter<T> extends IStringParser<T> {

	public String stringFromData(T value);
	
}
