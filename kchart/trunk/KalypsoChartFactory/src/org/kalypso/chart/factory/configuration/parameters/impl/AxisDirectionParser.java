package org.kalypso.chart.factory.configuration.parameters.impl;


import org.kalypso.chart.factory.configuration.parameters.IStringParser;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DIRECTION;


/**
 * @author alibu
 *
 */
public class AxisDirectionParser implements IStringParser<DIRECTION>
{

  String m_formatHint="one of 'POSITIVE' or 'NEGATIVE'";
  /**
   * @see org.kalypso.chart.factory.configuration.parameters.IStringParser#createValueFromString(java.lang.String)
   */
  public DIRECTION createValueFromString( String value )
  {
    DIRECTION dir = DIRECTION.POSITIVE;
    if( value.compareTo( "NEGATIVE" ) == 0 )
      dir = DIRECTION.NEGATIVE;
    return dir;
  }

  /**
   * @see org.kalypso.chart.factory.configuration.parameters.IStringParser#getFormatHint()
   */
  public String getFormatHint( )
  {
    return m_formatHint;
  }

}
