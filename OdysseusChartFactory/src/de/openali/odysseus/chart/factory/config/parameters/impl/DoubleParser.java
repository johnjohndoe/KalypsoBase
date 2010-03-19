package de.openali.odysseus.chart.factory.config.parameters.impl;

import de.openali.odysseus.chart.framework.exception.MalformedValueException;
import de.openali.odysseus.chart.framework.model.data.IStringParser;

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
     * TODO: �berpr�fen, welche Exceptions noch autreten k�nnen
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
