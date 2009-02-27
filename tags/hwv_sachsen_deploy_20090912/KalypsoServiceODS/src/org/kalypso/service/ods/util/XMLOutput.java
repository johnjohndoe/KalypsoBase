package org.kalypso.service.ods.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.kalypso.service.ogc.ResponseBean;

public class XMLOutput
{

  /**
   * writes an xmlObject to the output stream of the ResponseBean
   */
  public static void xmlResponse( ResponseBean response, XmlObject xml )
  {
    XmlOptions options = new XmlOptions();
    // Ausgabe menschenlesbar formatieren
    options = options.setSavePrettyPrint();
    // Namespace prefixes setzen
    final Map<String, String> prefixes = (new HashMap<String, String>());
    prefixes.put( "http://www.w3.org/1999/xlink", "xlink" );
    prefixes.put( "http://www.opengis.net/ows", "ows" );
    prefixes.put( "http://www.ksp.org/service/ods/capabilities", "ods" );
    prefixes.put( "http://www.w3.org/2001/XMLSchema-instance", "xsi" );
    options = options.setSaveSuggestedPrefixes( prefixes );
    try
    {
      response.setContentType( "text/xml" );
      xml.save( response.getOutputStream(), options );
    }
    catch( final IOException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
