package org.kalypso.calculation.chain;

import java.net.URL;
import java.util.Map;

import org.kalypso.contribs.java.net.AbstractUrlCatalog;

public class ModelConnectorUrlCatalog extends AbstractUrlCatalog
{
  public static String NS_CCHAIN = "org.kalypso.calculation.chain"; //$NON-NLS-1$

  @Override
  protected void fillCatalog( final Class< ? > myClass, final Map<String, URL> catalog, final Map<String, String> prefixes )
  {
    catalog.put( NS_CCHAIN, myClass.getResource( "binding/schema/CalculationChain.xsd" ) ); //$NON-NLS-1$
    prefixes.put( NS_CCHAIN, "cchain" ); //$NON-NLS-1$
  }

}
