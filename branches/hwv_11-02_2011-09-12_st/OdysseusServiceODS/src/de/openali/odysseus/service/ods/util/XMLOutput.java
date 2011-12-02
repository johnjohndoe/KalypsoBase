package de.openali.odysseus.service.ods.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import de.openali.odysseus.service.ows.request.ResponseBean;

public class XMLOutput
{

  /**
   * writes an xmlObject to the output stream of the ResponseBean
   */
  public static void xmlResponse( final ResponseBean response, final XmlObject xml )
  {
    XmlOptions options = new XmlOptions();
    // Ausgabe menschenlesbar formatieren
    options = options.setSavePrettyPrint();
    // Namespace prefixes setzen
    final Map<String, String> prefixes = (new HashMap<String, String>());
    prefixes.put( "http://www.w3.org/1999/xlink", "xlink" );
    prefixes.put( "http://www.opengis.net/ows", "ows" );
    prefixes.put( "http://www.openali.de/odysseus/service/ods/0.2.0", "" );
    prefixes.put( "http://www.openali.de/odysseus/service/ods/0.2.0", "ods" );
    prefixes.put( "http://www.w3.org/2001/XMLSchema-instance", "xsi" );
    options = options.setSaveSuggestedPrefixes( prefixes );
    options = options.setSaveNamespacesFirst();
    try
    {
      response.setContentType( "text/xml" );
      xml.save( response.getOutputStream(), options );
    }
    catch( final IOException e )
    {
      e.printStackTrace();
    }
  }

}
