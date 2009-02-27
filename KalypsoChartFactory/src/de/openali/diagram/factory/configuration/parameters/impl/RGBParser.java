package de.openali.diagram.factory.configuration.parameters.impl;

import org.eclipse.swt.graphics.RGB;

import de.openali.diagram.factory.configuration.parameters.IStringParser;

/**
 * @author alibu
 *
 */
public class RGBParser implements IStringParser<RGB>
{
  
  final String m_formatHint="'#abcdef' or '255,255,255'"; 

  /**
   * @see de.openali.diagram.factory.configuration.parameters.IStringParser#createValueFromString(java.lang.String)
   */
  public RGB createValueFromString( String value ) 
  {
    RGB rgb=null;
    //Hexadezimalwert
    if (value!=null && !value.trim().equals(""))
    {
	    if (value.substring( 0, 1).compareTo( "#") == 0)
	    {
	      String hexval=value.substring( 1, 7);
	      rgb=createRgbFromHex( hexval );
	    }
	    else if (value.split( "," ).length==3)
	    {
	        String[] strings = value.split( "," );
	        int r=Integer.parseInt( strings[0].trim() );
	        int g=Integer.parseInt( strings[1].trim() );
	        int b=Integer.parseInt( strings[2].trim() );
	        rgb=new RGB(r, g, b);
	    }
    }
    return rgb;
  }

  /**
   * @see de.openali.diagram.factory.configuration.parameters.IStringParser#getFormatHint()
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
    int r = hexToInt(  hexstr.substring( 0,2 )  );
    int g = hexToInt(  hexstr.substring( 2,4 )  );
    int b = hexToInt(  hexstr.substring( 4,6 )  );
    return new RGB( r, g, b );
  }

  /**
   * transforms a hex string into an integer
   * 
   * @param strHex hexdec representation of the number as String 
   * @return integer representation
   */
  public static int hexToInt( String strHex )
  {
    String str;
    String hexVals = "0123456789ABCDEF";
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
