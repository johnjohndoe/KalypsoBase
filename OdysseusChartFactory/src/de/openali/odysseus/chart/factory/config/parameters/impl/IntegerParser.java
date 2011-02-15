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
  @Override
  public Integer stringToLogical( final String value ) throws MalformedValueException
  {
    Integer n = null;
    try
    {
      n = Integer.parseInt( value );
    }
    catch( final Exception e )
    {
      /**
       * TODO: Überprüfen, welche Exceptions noch autreten können
       */
      throw new MalformedValueException();
    }
    return n;

  }

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#getFormatHint()
   */
  @Override
  public String getFormatHint( )
  {
    return "Integer value";
  }

}
