package de.openali.diagram.factory.util;

import org.eclipse.swt.graphics.RGB;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

import de.openali.diagram.factory.configuration.parameters.IParameterContainer;
import de.openali.diagram.factory.configuration.parameters.impl.XmlbeansParameterContainer;
import de.openali.diagram.factory.configuration.xsd.ParametersType;
import de.openali.diagram.factory.configuration.xsd.ProviderType;

public class DiagramFactoryUtilities {
	
	public static IParameterContainer createParameterContainer(ProviderType p, String objectId)
	{
		XmlbeansParameterContainer ph=new XmlbeansParameterContainer();
		if (p!=null)
		{
			ParametersType parameters = p.getParameters();
			if (parameters!=null)
			{
				ph.addParameters(parameters, objectId);
			}		
		}
		return ph;
	}
	
	
	/**
	   * creates a rgb object from 3byte as returned by the unmarshalling process of heybinary values
	   */
	  public static RGB createColor( byte[] cbyte )
	  {
	    int r = hexToInt( HexBin.encode( new byte[] { cbyte[0] } ) );
	    int g = hexToInt( HexBin.encode( new byte[] { cbyte[1] } ) );
	    int b = hexToInt( HexBin.encode( new byte[] { cbyte[2] } ) );
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


	  /**
	   * The function tests if childClass is derived from parentClass by trying to cast the
	   * child
	   * 
	   * TODO: should be tested some more
	   * 
	   * @param childClass
	   * @param parentClass
	   * @return true if childClass is derived from parentClass
	   */
	public static boolean isSubclassOf(Class<?> childClass, Class<?> parentClass) {
		if (childClass == parentClass)
			return true;
		try
		{
			Object childObject=new Object();
			@SuppressWarnings("unused")
			Object child2parentObject=parentClass.cast(childObject);
		}
		catch (ClassCastException e)
		{
			System.out.println("kann nicht gecastet werden; "+childClass+" "+parentClass);
			System.out.println();
		}

		return false;
	}

}
