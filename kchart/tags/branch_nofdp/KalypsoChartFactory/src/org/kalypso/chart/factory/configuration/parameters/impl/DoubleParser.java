package org.kalypso.chart.factory.configuration.parameters.impl;

import org.kalypso.chart.framework.exception.MalformedValueException;
import org.kalypso.chart.framework.model.data.IStringParser;

/**
 * @author alibu
 */
public class DoubleParser implements IStringParser<Double>
{

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#stringToLogical(java.lang.String)
   */
  public Double stringToLogical( String value ) throws MalformedValueException
  {
    Double n = null;
    try
    {
      n = Double.parseDouble( value );
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
    return "Number; use '.' as decimal seperator";
  }

}
