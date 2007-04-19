package org.kalypso.chart.factory.configuration.parameters.impl;

import org.kalypso.chart.factory.configuration.exception.MalformedValueException;
import org.kalypso.chart.factory.configuration.parameters.IStringParser;

/**
 * @author alibu
 *
 */
public class BooleanParser implements IStringParser<Boolean>
{
  final String m_formatHint="one of 'true' or 'false'";

  /**
   * @see org.kalypso.chart.factory.configuration.parameters.IStringParser#createValueFromString(java.lang.String)
   */
  public Boolean createValueFromString( String value ) throws MalformedValueException
  {
    if (value.compareTo( "true" )==0)
        return Boolean.TRUE;
    else if (value.compareTo( "false" )==0)
        return Boolean.FALSE;
    else
        throw new MalformedValueException();
  }

  /**
   * @see org.kalypso.chart.factory.configuration.parameters.IStringParser#getFormatHint()
   */
  public String getFormatHint( )
  {
    return m_formatHint;
  }

}
