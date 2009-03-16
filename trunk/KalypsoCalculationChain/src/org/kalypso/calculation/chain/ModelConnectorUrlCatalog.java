package org.kalypso.calculation.chain;

import java.net.URL;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.kalypso.contribs.java.net.AbstractUrlCatalog;

public class ModelConnectorUrlCatalog extends AbstractUrlCatalog 
{

  public static String NS_CCHAIN = "org.kalypso.calculation.chain"; //$NON-NLS-1$

  public enum MODEL_CONNECTOR_TYPEID
  {
    CONNECTOR_NA_WSPM,
    CONNECTOR_WSPM_FLOOD,
    CONNECTOR_FLOOD_RISK;
    public String getValue( )
    {
      final MODEL_CONNECTOR_TYPEID kind = MODEL_CONNECTOR_TYPEID.valueOf( name() );

      switch( kind )
      {
        case CONNECTOR_NA_WSPM:
          return "KalypsoModelConnector_NA_WSPM";
        case CONNECTOR_WSPM_FLOOD:
          return "KalypsoModelConnector_WSPM_FM";
        case CONNECTOR_FLOOD_RISK:
          return "KalypsoModelConnector_FM_RM";
        default:
          throw new NotImplementedException();
      }
    }
  }

  
  @Override
  protected void fillCatalog( final Class< ? > myClass, final Map<String, URL> catalog, final Map<String, String> prefixes )
  {
    catalog.put( NS_CCHAIN, myClass.getResource( "binding/schema/CalculationChain.xsd" ) ); //$NON-NLS-1$
    prefixes.put( NS_CCHAIN, "cchain" ); //$NON-NLS-1$
  }

}
