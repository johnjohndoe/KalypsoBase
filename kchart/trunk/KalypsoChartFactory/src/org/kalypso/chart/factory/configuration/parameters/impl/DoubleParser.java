package org.kalypso.chart.factory.configuration.parameters.impl;

import org.kalypso.chart.factory.configuration.exception.MalformedValueException;
import org.kalypso.chart.factory.configuration.parameters.IStringParser;

/**
 * @author alibu
 *
 */
public class DoubleParser implements IStringParser<Double>
{

  /**
   * @see org.kalypso.chart.factory.configuration.parameters.IStringParser#createValueFromString(java.lang.String)
   */
  public Double createValueFromString( String value ) throws MalformedValueException
  {
    Double n=null;
    try
    {
      n=Double.parseDouble( value );
    }
    /**
     * TODO: Überprüfen, welche Exceptions noch autreten können
     */
    catch (Exception e)
    {
      throw new MalformedValueException();
    }
    return n;
    
  }

  /**
   * @see org.kalypso.chart.factory.configuration.parameters.IStringParser#getFormatHint()
   */
  public String getFormatHint( )
  {
    return "Number; use '.' as decimal seperator";
  }

}
