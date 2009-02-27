package org.kalypso.chart.factory.configuration.parameters.impl;

import org.kalypso.chart.framework.model.data.IStringParser;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.POSITION;

/**
 * @author alibu
 */
public class AxisPositionParser implements IStringParser<POSITION>
{

  private final String m_formatHint = "one of 'BOTTOM', 'LEFT', 'RIGHT', 'TOP'";

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#stringToLogical(java.lang.String)
   */
  public POSITION stringToLogical( String value )
  {
    POSITION pos = POSITION.BOTTOM;
    if( value.equals( "TOP" ) )
      pos = POSITION.TOP;
    else if( value.equals( "RIGHT" ) )
      pos = POSITION.RIGHT;
    else if( value.equals( "LEFT" ) )
      pos = POSITION.LEFT;
    else if( value.equals( "BOTTOM" ) )
      pos = POSITION.BOTTOM;
    return pos;
  }

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#getFormatHint()
   */
  public String getFormatHint( )
  {
    return m_formatHint;
  }

}
