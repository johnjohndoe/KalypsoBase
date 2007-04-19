package org.kalypso.chart.factory.configuration.parameters;

import org.kalypso.chart.factory.configuration.exception.MalformedValueException;

/**
 * Interface for classes creating objects from strings
 *
 * @author alibu
 *
 */
public interface IStringParser<T>
{
  public T createValueFromString(String value) throws MalformedValueException;

  public String getFormatHint( );
}
