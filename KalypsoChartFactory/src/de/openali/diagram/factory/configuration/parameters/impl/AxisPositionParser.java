package de.openali.diagram.factory.configuration.parameters.impl;


import de.openali.diagram.factory.configuration.parameters.IStringParser;
import de.openali.diagram.framework.model.mapper.IAxisConstants.POSITION;

/**
 * @author alibu
 *
 */
public class AxisPositionParser implements IStringParser<POSITION>
{

  private String m_formatHint="one of 'BOTTOM', 'LEFT', 'RIGHT', 'TOP'";

  /**
   * @see de.openali.diagram.factory.configuration.parameters.IStringParser#createValueFromString(java.lang.String)
   */
  public POSITION createValueFromString( String value )
  {
    POSITION pos = POSITION.BOTTOM;
    if( value.equals( "TOP" )  )
      pos = POSITION.TOP;
    else if( value.equals( "RIGHT" ) )
      pos = POSITION.RIGHT;
    else if( value.equals( "LEFT" ) )
      pos = POSITION.LEFT;
    else if( value.equals( "BOTTOM" )  )
    	pos = POSITION.BOTTOM;
    return pos;
  }

  /**
   * @see de.openali.diagram.factory.configuration.parameters.IStringParser#getFormatHint()
   */
  public String getFormatHint( )
  {
    return m_formatHint;
  }

}
