package de.openali.diagram.factory.configuration.parameters.impl;


import de.openali.diagram.factory.configuration.parameters.IStringParser;
import de.openali.diagram.framework.model.mapper.IAxisConstants.DIRECTION;

/**
 * @author alibu
 *
 */
public class AxisDirectionParser implements IStringParser<DIRECTION>
{

  String m_formatHint="one of 'POSITIVE' or 'NEGATIVE'";
  /**
   * @see de.openali.diagram.factory.configuration.parameters.IStringParser#createValueFromString(java.lang.String)
   */
  public DIRECTION createValueFromString( String value )
  {
    DIRECTION dir = DIRECTION.POSITIVE;
    if( value.compareTo( "NEGATIVE" ) == 0 )
      dir = DIRECTION.NEGATIVE;
    return dir;
  }

  /**
   * @see de.openali.diagram.factory.configuration.parameters.IStringParser#getFormatHint()
   */
  public String getFormatHint( )
  {
    return m_formatHint;
  }

}
