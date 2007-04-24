package org.kalypso.chart.framework.model.data;

import org.kalypso.chart.framework.exception.MalformedValueException;

/**
 * Interface for classes creating objects from strings
 *
 * @author alibu
 *
 */
public interface IStringParser<T>
{
  public T dataFromString(String value) throws MalformedValueException;

  /** Human readable format hint */
  public String getFormatHint( );
}
