package de.openali.odysseus.chart.factory.config.parameters.impl;

import de.openali.odysseus.chart.framework.exception.MalformedValueException;
import de.openali.odysseus.chart.framework.model.data.IStringParser;

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
