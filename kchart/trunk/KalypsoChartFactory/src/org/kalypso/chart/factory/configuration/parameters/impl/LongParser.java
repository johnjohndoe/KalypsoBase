package org.kalypso.chart.factory.configuration.parameters.impl;

import org.kalypso.chart.framework.exception.MalformedValueException;
import org.kalypso.chart.framework.model.data.IStringParser;

/**
 * @author alibu
 */
public class LongParser implements IStringParser<Long>
{

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#stringToLogical(java.lang.String)
   */
  public Long stringToLogical( String value ) throws MalformedValueException
  {
    Long n = null;
    try
    {
      n = Long.parseLong( value );
    }
    /**
     * TODO: Überprüfen, welche Exceptions noch autreten können
     */
    catch( Exception e )
    {
      throw new MalformedValueException();
    }
    return n;

  }

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#getFormatHint()
   */
  public String getFormatHint( )
  {
    return "Number without decimal separator";
  }

}
