package de.openali.diagram.factory.configuration.parameters.impl;

import de.openali.diagram.factory.configuration.exception.MalformedValueException;
import de.openali.diagram.factory.configuration.parameters.IStringParser;

/**
 * @author alibu
 *
 */
public class BooleanParser implements IStringParser<Boolean>
{
  final String m_formatHint="one of 'true' or 'false'";

  /**
   * @see de.openali.diagram.factory.configuration.parameters.IStringParser#createValueFromString(java.lang.String)
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
   * @see de.openali.diagram.factory.configuration.parameters.IStringParser#getFormatHint()
   */
  public String getFormatHint( )
  {
    return m_formatHint;
  }

}
