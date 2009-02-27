package org.kalypso.chart.factory.configuration.parameters.impl;

import org.eclipse.swt.SWT;
import org.kalypso.chart.framework.model.data.IStringParser;

/**
 * @author alibu
 */
public class LineStyleParser implements IStringParser<Integer>
{

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#stringToLogical(java.lang.String)
   */
  public Integer stringToLogical( String value )
  {
    if( value.compareTo( "DASH" ) == 0 )
      return SWT.LINE_DASH;
    else if( value.compareTo( "DOT" ) == 0 )
      return SWT.LINE_DOT;
    else if( value.compareTo( "DASHDOT" ) == 0 )
      return SWT.LINE_DASHDOT;
    else if( value.compareTo( "DASHDOTDOT" ) == 0 )
      return SWT.LINE_DASHDOTDOT;
    else
      return SWT.LINE_SOLID;
  }

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#getFormatHint()
   */
  public String getFormatHint( )
  {
    // TODO Auto-generated method stub
    return null;
  }

}
