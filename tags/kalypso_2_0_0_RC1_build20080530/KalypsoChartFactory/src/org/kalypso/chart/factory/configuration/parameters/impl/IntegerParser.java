package org.kalypso.chart.factory.configuration.parameters.impl;

import org.kalypso.chart.framework.exception.MalformedValueException;
import org.kalypso.chart.framework.model.data.IStringParser;

/**
 * @author alibu
 */
public class IntegerParser implements IStringParser<Integer>
{

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#stringToLogical(java.lang.String)
   */
  public Integer stringToLogical( String value ) throws MalformedValueException
  {
    Integer n = null;
    try
    {
      n = Integer.parseInt( value );
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
    return "Integer value";
  }

}
