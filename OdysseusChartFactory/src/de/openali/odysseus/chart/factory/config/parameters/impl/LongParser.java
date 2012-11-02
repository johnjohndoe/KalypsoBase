package de.openali.odysseus.chart.factory.config.parameters.impl;

import de.openali.odysseus.chart.framework.exception.MalformedValueException;
import de.openali.odysseus.chart.framework.model.data.IStringParser;

/**
 * @author alibu
 */
public class LongParser implements IStringParser<Long>
{

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#stringToLogical(java.lang.String)
   */
  @Override
  public Long stringToLogical( final String value ) throws MalformedValueException
  {
    Long n = null;
    try
    {
      n = Long.parseLong( value );
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
    return "Number without decimal separator"; //$NON-NLS-1$
  }

}
