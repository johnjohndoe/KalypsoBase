package org.kalypso.gmlschema;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kalypso.commons.xml.NS;
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
  private final static Logger LOGGER = Logger.getLogger( GMLSchemaCache.class.getName() );

  static
  {
    LOGGER.setUseParentHandlers( KalypsoGmlSchemaTracing.traceSchemaParsing() );
  }

  private IUrlCatalog m_urlCatalog;

  private GMLSchemaCache m_cache;

  /**
   * @throws NullPointerException
   *           If catalog or cacheDirectory is null.
   */
  public GMLSchemaCatalog( final IUrlCatalog catalog, final File cacheDirectory )
  {
    if( catalog == null )
      throw new NullPointerException();

    m_urlCatalog = catalog;

    m_cache = new GMLSchemaCache( cacheDirectory );

    LOGGER.info( "Schema-Katalog initialisiert mit DIR=" + cacheDirectory );
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
    try
    {
      final GMLSchema schema = GMLSchemaFactory.createGMLSchema( gmlVersion, schemaLocation );
      final Date validity = new Date( schemaLocation.openConnection().getLastModified() );

      m_cache.addSchema( schema.getTargetNamespace(), new GMLSchemaCache.GMLSchemaWrapper( schema, validity ) );

      return schema;
    }
    catch( final Exception e )
    {
      LOGGER.log( Level.SEVERE, "Fehler beim laden eines Schema über die SchemaLocation: " + schemaLocation, e );

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
      version = gmlVersion;
    }

    final URL schemaUrl = catalogUrl == null ? schemaLocation : catalogUrl;
    if( schemaUrl == null )
      LOGGER.log( Level.WARNING, "No location for namespace: " + namespace + " - trying to load from cache." );

    // auch versuchen aus dem Cache zu laden, wenn die url null ist;
    // vielleicht ist der namespace ja noch im file-cache
    return m_cache.getSchema( namespace, version, schemaUrl );
  }

  /**
   * Clears the cache. Schematas are reloaded after this operation.
   * 
   * @param onlyMemoryCache
   *          If true, only the memory cache is cleared. Else, file and memory cache are cleared.
   */
  public void clearCache( final boolean onlyMemoryCache )
  {
    m_cache.clearCache( onlyMemoryCache );
  }
}
