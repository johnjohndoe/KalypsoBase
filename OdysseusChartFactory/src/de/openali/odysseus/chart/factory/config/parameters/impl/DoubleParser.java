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
  @Override
  public Double stringToLogical( final String value ) throws MalformedValueException
  {
    Double n = null;
    try
    {
      n = Double.parseDouble( value );
    }
    catch( final Exception e )
    {
      /**
       * TODO: �berpr�fen, welche Exceptions noch autreten k�nnen
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
    return "Number; use '.' as decimal seperator"; //$NON-NLS-1$
  }

}
