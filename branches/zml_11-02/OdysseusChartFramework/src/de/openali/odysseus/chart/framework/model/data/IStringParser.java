package de.openali.odysseus.chart.framework.model.data;

import de.openali.odysseus.chart.framework.exception.MalformedValueException;

/**
 * Interface for classes creating objects from strings
 * 
 * @author alibu
 */
public interface IStringParser<T_logical>
{
  T_logical stringToLogical( String value ) throws MalformedValueException;

  /** Human readable format hint */
  String getFormatHint( );
}
