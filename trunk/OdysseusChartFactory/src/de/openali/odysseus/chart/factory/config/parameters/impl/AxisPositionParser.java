package de.openali.odysseus.chart.factory.config.parameters.impl;

import de.openali.odysseus.chart.framework.model.data.IStringParser;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;

/**
 * @author alibu
 */
public class AxisPositionParser implements IStringParser<POSITION>
{

  private final String m_formatHint = "one of 'BOTTOM', 'LEFT', 'RIGHT', 'TOP'";

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#stringToLogical(java.lang.String)
   */
  @Override
  public POSITION stringToLogical( final String value )
  {
    POSITION pos = POSITION.BOTTOM;
    if( value.equals( "TOP" ) ) //$NON-NLS-1$
      pos = POSITION.TOP;
    else if( value.equals( "RIGHT" ) ) //$NON-NLS-1$
      pos = POSITION.RIGHT;
    else if( value.equals( "LEFT" ) ) //$NON-NLS-1$
      pos = POSITION.LEFT;
    else if( value.equals( "BOTTOM" ) ) //$NON-NLS-1$
      pos = POSITION.BOTTOM;
    return pos;
  }

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#getFormatHint()
   */
  @Override
  public String getFormatHint( )
  {
    return m_formatHint;
  }

}
