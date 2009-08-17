package org.kalypso.gmlschema;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolver;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolverPlugin;
import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.net.IUrlCatalog;

/**
 * <p>
 * GML Schema Catalog, benutzt den {@link org.kalypsodeegree_impl.gml.schema.GMLSchemaCache}.
 * </p>
 *
 * @author schlienger
 */
public final class GMLSchemaCatalog
{
  private final IUrlCatalog m_urlCatalog;

  private final GMLSchemaCache m_cache;

  /**
   * @throws NullPointerException
   *             If catalog or cacheDirectory is null.
   */
  public GMLSchemaCatalog( final IUrlCatalog catalog )
  {
    if( catalog == null )
      throw new NullPointerException();

    m_urlCatalog = catalog;

    m_cache = new GMLSchemaCache();
  }

  public IUrlCatalog getDefaultCatalog( )
  {
    return m_urlCatalog;
  }

  /**
   * Lädt ein Schema aus dieser URL (nicht aus dem Cache!) und fügt es dann dem cache hinzu (mit namespace als key).
   *
   * @return null, wenn schema nicht geladen werden konnte
   */
  public GMLSchema getSchema( final String gmlVersion, final URL schemaLocation )
  {
    Debug.CATALOG.printf( "Loading schema into cache for gmlVersion %s and schemaLocation %s%n", gmlVersion, schemaLocation );

    try
    {
      final GMLSchema schema = GMLSchemaFactory.createGMLSchema( gmlVersion, schemaLocation );
      final Date validity = new Date( schemaLocation.openConnection().getLastModified() );

      m_cache.addSchema( schema.getTargetNamespace(), schema, validity, System.currentTimeMillis() );

      Debug.CATALOG.printf( "Schema successfully loaded and put into the cache. Validity = %s%n", validity );

      return schema;
    }
    catch( final Exception e )
    {
      final String message = "Failed to load schema into cache via schemaLocation" + schemaLocation;
      StatusUtilities.statusFromThrowable( e, message );

      Debug.CATALOG.printf( message );
      Debug.CATALOG.printf( "%n" );

      return null;
    }
  }

  /**
   * Lädt ein (eventuell gecachetes Schema über den Katalog. Als CacheId wird dieser Name benutzt.
   */
  public GMLSchema getSchema( final String namespace, final String gmlVersion ) throws InvocationTargetException
  {
    return getSchema( namespace, gmlVersion, null );
  }

  /**
   * Lädt ein (eventuell gecachetes) Schema. Ist das Schema nicht bereits bekannt (=gecached), wird über den Katalog
   * oder über die gegebene URL geladen.
   * <p>
   * Ist im Katalog eine schema-location gegeben, wird diese bevorzugt.
   * </p>
   */
  public GMLSchema getSchema( final String namespace, final String gmlVersion, final URL schemaLocation ) throws InvocationTargetException
  {
    Debug.CATALOG.printf( "Trying to retrieve schema from cache for:%n\tnamespace: %s%n\tgmlVersion: %s%n\tschemaLocation: %s%n", namespace, gmlVersion, schemaLocation );

    // HACK: if we are looking for the gml namespace
    // tweak it and add the version number
    final URL catalogUrl;
    final String version;

    if( namespace.equals( NS.GML2 ) )
    {
      if( gmlVersion == null )
        // if we get here and don't know the version number, we are probably loading
        // a gml whichs schema is the gml schema directly.
        // This is only possible for gml3 documents.
        version = "3.1.1";
      else
        version = gmlVersion;

      catalogUrl = m_urlCatalog.getURL( namespace + "#" + version.charAt( 0 ) );
    }
    else
    {
      catalogUrl = m_urlCatalog.getURL( namespace );

      // HACK: crude hack to enforce GML3 for WFS
      if( NS.WFS.equals( namespace ) )
        version = "3.1.1";
      else
        version = gmlVersion;
    }


    Debug.CATALOG.printf( "Determined version and catalogUrl: %s - %s%n", version, catalogUrl );

    final URL schemaUrl = catalogUrl == null ? schemaLocation : catalogUrl;
    if( schemaUrl == null )
      Debug.CATALOG.printf( "No location for namespace: %s - trying to load from cache.%n", namespace );

    try
    {
      // else, try to get it from the external cache
      final URIResolver uriResolver = URIResolverPlugin.createResolver();
      final String externalForm = schemaUrl == null ? null : schemaUrl.toExternalForm();
      final String resolvedUri = uriResolver.resolve( externalForm, null, externalForm );
      final URL resolvedUrl = resolvedUri == null ? null : new URL( resolvedUri );

      // auch versuchen aus dem Cache zu laden, wenn die url null ist;
      // vielleicht ist der namespace ja noch im file-cache
      return m_cache.getSchema( namespace, version, resolvedUrl );
    }
    catch( final MalformedURLException e )
    {
      throw new InvocationTargetException( e );
    }
  }

  /**
   * Clears the cache. Schematas are reloaded after this operation.
   */
  public void clearCache( )
  {
    m_cache.clearCache();
    Debug.CATALOG.printf( "Cleared schema cache." );
  }
}
