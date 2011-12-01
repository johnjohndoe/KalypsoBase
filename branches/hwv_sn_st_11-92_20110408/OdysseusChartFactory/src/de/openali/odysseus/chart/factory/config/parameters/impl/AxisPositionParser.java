package de.openali.odysseus.chart.factory.config.parameters.impl;

import de.openali.odysseus.chart.framework.model.data.IStringParser;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;

/**
 * @author alibu
 */
public class AxisPositionParser implements IStringParser<POSITION>
{

  private static final String FORMAT_HINT = "one of 'BOTTOM', 'LEFT', 'RIGHT', 'TOP'";

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#stringToLogical(java.lang.String)
   */
  @Override
  public POSITION stringToLogical( final String value )
  {
    POSITION pos = POSITION.BOTTOM;
    if( "TOP".equals( value ) ) //$NON-NLS-1$
    {
      pos = POSITION.TOP;
    }
    else if( "RIGHT".equals( value ) ) //$NON-NLS-1$
    {
      pos = POSITION.RIGHT;
    }
    else if( "LEFT".equals( value ) ) //$NON-NLS-1$
    {
      pos = POSITION.LEFT;
    }
    else if( "BOTTOM".equals( value ) ) //$NON-NLS-1$
    {
      pos = POSITION.BOTTOM;
    }
    return pos;
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
