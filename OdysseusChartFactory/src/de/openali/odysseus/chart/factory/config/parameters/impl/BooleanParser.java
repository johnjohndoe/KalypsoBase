package de.openali.odysseus.chart.factory.config.parameters.impl;

import de.openali.odysseus.chart.framework.exception.MalformedValueException;
import de.openali.odysseus.chart.framework.model.data.IStringParser;

/**
 * @author alibu
 */
public class BooleanParser implements IStringParser<Boolean>
{
  private static final String FORMAT_HINT = "one of 'true' or 'false'"; //$NON-NLS-1$

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#stringToLogical(java.lang.String)
   */
  @Override
  public Boolean stringToLogical( final String value ) throws MalformedValueException
  {
    if( value.compareTo( "true" ) == 0 ) //$NON-NLS-1$
      return Boolean.TRUE;
    else if( value.compareTo( "false" ) == 0 ) //$NON-NLS-1$
      return Boolean.FALSE;
    else
      throw new MalformedValueException();
  }

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#getFormatHint()
   */
  @Override
  public String getFormatHint( )
  {
    return FORMAT_HINT;
  }

}
