package org.kalypso.chart.factory.util;

import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.factory.configuration.parameters.IParameterContainer;
import org.kalypso.chart.factory.configuration.parameters.impl.XmlbeansParameterContainer;
import org.ksp.chart.factory.ParametersType;
import org.ksp.chart.factory.ProviderType;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

public class ChartFactoryUtilities
{

  /**
   * creates a rgb object from 3byte as returned by the unmarshalling process of heybinary values
   */
  public static RGB createColor( byte[] cbyte )
  {
    final int r = hexToInt( HexBin.encode( new byte[] { cbyte[0] } ) );
    final int g = hexToInt( HexBin.encode( new byte[] { cbyte[1] } ) );
    final int b = hexToInt( HexBin.encode( new byte[] { cbyte[2] } ) );
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

  /**
   * The function tests if childClass is derived from parentClass by trying to cast the child TODO: should be tested
   * some more
   * 
   * @param childClass
   * @param parentClass
   * @return true if childClass is derived from parentClass
   */
  public static boolean isSubclassOf( Class< ? > childClass, Class< ? > parentClass )
  {
    if( childClass == parentClass )
      return true;
    try
    {
      final Object childObject = new Object();
      final Object child2parentObject = parentClass.cast( childObject );
    }
    catch( final ClassCastException e )
    {
      System.out.println( "kann nicht gecastet werden; " + childClass + " " + parentClass );
    }

    return false;
  }

  public static IParameterContainer createXmlbeansParameterContainer( String ownerId, ProviderType pt )
  {
    ParametersType parameters = null;
    if( pt != null )
    {
      parameters = pt.getParameters();
    }
    final IParameterContainer pc = new XmlbeansParameterContainer( ownerId, pt.getEpid(), parameters );
    return pc;
  }

}
