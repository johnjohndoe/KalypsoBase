package de.openali.odysseus.service.ods.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.kalypso.ogc.core.service.OGCResponse;

/**
 * @author Alexander Burtscher
 */
public class XMLOutput
{
  /**
   * This function writes an xmlObject to the output stream of the ResponseBean.
   */
  public static void xmlResponse( final OGCResponse response, final XmlObject xml )
  {
    try
    {
      XmlOptions options = new XmlOptions();
      options = options.setSavePrettyPrint();

      final Map<String, String> prefixes = new HashMap<>();
      prefixes.put( "http://www.w3.org/1999/xlink", "xlink" );
      prefixes.put( "http://www.opengis.net/ows", "ows" );
      prefixes.put( "http://www.openali.de/odysseus/service/ods/0.2.0", "" );
      prefixes.put( "http://www.openali.de/odysseus/service/ods/0.2.0", "ods" );
      prefixes.put( "http://www.w3.org/2001/XMLSchema-instance", "xsi" );
      options = options.setSaveSuggestedPrefixes( prefixes );
      options = options.setSaveNamespacesFirst();

      xml.save( response.getOutputStream(), options );
    }
    catch( final IOException ex )
    {
      ex.printStackTrace();
    }
  }
}