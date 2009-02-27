package de.openali.diagram.factory.configuration.parameters.impl;

import de.openali.diagram.factory.configuration.exception.MalformedValueException;
import de.openali.diagram.factory.configuration.parameters.IStringParser;

/**
 * @author alibu
 *
 */
public class DoubleParser implements IStringParser<Double>
{

  /**
   * @see de.openali.diagram.factory.configuration.parameters.IStringParser#createValueFromString(java.lang.String)
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
   * @see de.openali.diagram.factory.configuration.parameters.IStringParser#getFormatHint()
   */
  public String getFormatHint( )
  {
    return "Number; use '.' as decimal seperator";
  }

}
