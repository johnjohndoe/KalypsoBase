package org.kalypso.gml.processes.schemata;

import java.net.URL;
import java.util.Map;

import org.kalypso.contribs.java.net.AbstractUrlCatalog;

public class GmlProcessesUrlCatalog extends AbstractUrlCatalog
{
  public static final String NS_MESH = "org.kalypso.gml.processes.mesh"; //$NON-NLS-1$

  /**
   * @see org.kalypso.contribs.java.net.AbstractUrlCatalog#fillCatalog(java.lang.Class, java.util.Map)
   */
  @Override
  protected void fillCatalog( final Class<?> myClass, final Map<String, URL> catalog, Map<String, String> prefixes )
  {
    catalog.put( NS_MESH, myClass.getResource( "mesh.xsd" ) ); //$NON-NLS-1$
    prefixes.put( NS_MESH, "mesh" ); //$NON-NLS-1$
  }
}
