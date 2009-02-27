/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.swtchart.configuration.parameters.impl;

import org.eclipse.swt.graphics.RGB;
import org.kalypso.swtchart.configuration.parameters.IStringParser;

/**
 * @author burtscher1
 *
 */
public class RGBParser implements IStringParser<RGB>
{
  
  final String m_formatHint="'#abcdef' or '255,255,255'"; 

  /**
   * @see org.kalypso.swtchart.configuration.parameters.IStringParser#createValueFromString(java.lang.String)
   */
  public RGB createValueFromString( String value ) 
  {
    RGB rgb=null;
    //Hexadezimalwert
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
    return rgb;
  }

  /**
   * @see org.kalypso.swtchart.configuration.parameters.IStringParser#getFormatHint()
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
