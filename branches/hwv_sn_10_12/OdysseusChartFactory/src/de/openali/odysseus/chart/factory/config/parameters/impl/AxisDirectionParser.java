package de.openali.odysseus.chart.factory.config.parameters.impl;

import de.openali.odysseus.chart.framework.model.data.IStringParser;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;

/**
 * @author alibu
 */
public class AxisDirectionParser implements IStringParser<DIRECTION>
{

  String m_formatHint = "one of 'POSITIVE' or 'NEGATIVE'";

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#stringToLogical(java.lang.String)
   */
  @Override
  public DIRECTION stringToLogical( String value )
  {
    DIRECTION dir = DIRECTION.POSITIVE;
    if( value.compareTo( "NEGATIVE" ) == 0 )
      dir = DIRECTION.NEGATIVE;
    return dir;
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
