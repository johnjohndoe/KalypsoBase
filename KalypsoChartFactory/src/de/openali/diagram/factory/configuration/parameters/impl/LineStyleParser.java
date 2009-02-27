package de.openali.diagram.factory.configuration.parameters.impl;

import org.eclipse.swt.SWT;

import de.openali.diagram.factory.configuration.parameters.IStringParser;

/**
 * @author alibu
 *
 */
public class LineStyleParser implements IStringParser<Integer>
{

  /**
   * @see de.openali.diagram.factory.configuration.parameters.IStringParser#createValueFromString(java.lang.String)
   */
  public Integer createValueFromString( String value ) 
  {
    if( value.compareTo( "DASH" )==0)
      return SWT.LINE_DASH;
    else if( value.compareTo( "DOT" )==0)
      return SWT.LINE_DOT;
    else if( value.compareTo( "DASHDOT" )==0)
      return SWT.LINE_DASHDOT;
    else if( value.compareTo( "DASHDOTDOT" )==0)
      return SWT.LINE_DASHDOTDOT;
    else
      return SWT.LINE_SOLID;
  }

  /**
   * @see de.openali.diagram.factory.configuration.parameters.IStringParser#getFormatHint()
   */
  public String getFormatHint( )
  {
    // TODO Auto-generated method stub
    return null;
  }

}
