package org.kalypso.chart.factory.configuration.parameters.impl;

import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.framework.model.data.IStringParser;

/**
 * @author alibu
 */
public class RGBParser implements IStringParser<RGB>
{

  final String m_formatHint = "'#abcdef' or '255,255,255'";

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#stringToLogical(java.lang.String)
   */
  public RGB stringToLogical( String value )
  {
    RGB rgb = null;
    // Hexadezimalwert
    if( value != null && !value.trim().equals( "" ) )
    {
      if( value.substring( 0, 1 ).compareTo( "#" ) == 0 )
      {
        final String hexval = value.substring( 1, 7 );
        rgb = createRgbFromHex( hexval );
      }
      else if( value.split( "," ).length == 3 )
      {
        final String[] strings = value.split( "," );
        final int r = Integer.parseInt( strings[0].trim() );
        final int g = Integer.parseInt( strings[1].trim() );
        final int b = Integer.parseInt( strings[2].trim() );
        rgb = new RGB( r, g, b );
      }
    }
    return rgb;
  }

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#getFormatHint()
   */
  public String getFormatHint( )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * creates a rgb object from 3byte as returned by the unmarshalling process of heybinary values
   */
  public static RGB createRgbFromHex( String hexstr )
  {
    final int r = hexToInt( hexstr.substring( 0, 2 ) );
    final int g = hexToInt( hexstr.substring( 2, 4 ) );
    final int b = hexToInt( hexstr.substring( 4, 6 ) );
    return new RGB( r, g, b );
  }

  /**
   * transforms a hex string into an integer
   * 
   * @param strHex
   *            hexdec representation of the number as String
   * @return integer representation
   */
  public static int hexToInt( String strHex )
  {
    String str;
    final String hexVals = "0123456789ABCDEF";
    int i, val, n;

    val = 0;
    n = 1;
    str = strHex.toUpperCase();

    for( i = str.length() - 1; i >= 0; i--, n *= 16 )
    {
      val += n * hexVals.indexOf( str.charAt( i ) );
    }
    return val;
  }

}
