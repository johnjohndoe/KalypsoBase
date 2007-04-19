package org.kalypso.chart.factory.configuration.parameters.impl;

import org.eclipse.swt.SWT;
import org.kalypso.chart.factory.configuration.parameters.IStringParser;


/**
 * @author alibu
 *
 */
public class FontStyleParser implements IStringParser<Integer>
{
  
  final String m_formatHint="one of 'BOLD', 'ITALIC' or 'NORMAL'"; 

  /**
   * @see org.kalypso.chart.factory.configuration.parameters.IStringParser#createValueFromString(java.lang.String)
   */
  public Integer createValueFromString( String value )
  {
    if (value.compareTo("BOLD")==0)
    {
        return SWT.BOLD;
    }
    if (value.compareTo("ITALIC")==0)
    {
      return SWT.ITALIC;
    }
    else
    {
        return SWT.NORMAL;
    }

  }

  /**
   * @see org.kalypso.chart.factory.configuration.parameters.IStringParser#getFormatHint()
   */
  public String getFormatHint( )
  {
    // TODO Auto-generated method stub
    return m_formatHint;
  }

}
