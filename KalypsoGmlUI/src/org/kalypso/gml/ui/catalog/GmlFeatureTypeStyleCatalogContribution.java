package org.kalypso.gml.ui.catalog;

import java.net.URL;

import org.kalypso.core.catalog.CatalogManager;
import org.kalypso.core.catalog.ICatalog;
import org.kalypso.core.catalog.ICatalogContribution;

public class GmlFeatureTypeStyleCatalogContribution implements ICatalogContribution
{
  public void contributeTo( final CatalogManager catalogManager )
  {
    final URL catalogURL = getClass().getResource( "resources/catalog.xml" ); //$NON-NLS-1$
    final ICatalog baseCatalog = catalogManager.getBaseCatalog();
    baseCatalog.addNextCatalog( catalogURL );
  }
}
