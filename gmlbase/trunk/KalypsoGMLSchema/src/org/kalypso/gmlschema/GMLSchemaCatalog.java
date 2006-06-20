package org.kalypso.gmlschema;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.java.net.IUrlCatalog;

/**
 * <p>
 * Singleton über dem {@link org.kalypsodeegree_impl.gml.schema.GMLSchemaCache}welches sich darüberhinaus noch um den
 * XML-Katalog kümmert.
 * </p>
 * <p>
 * Muss vor der ersten Benutzung durch {@link #init(IUrlCatalog, File)}initialisiert werden.
 * </p>
 * 
 * @author schlienger
 */
public final class GMLSchemaCatalog
{
  /** Temporary directory, used by the GMLSchemaCache */
  private static final File TMP_DIR = new File( FileUtilities.TMP_DIR, GMLSchemaCatalog.class.getName() );

  private final static Logger LOGGER = Logger.getLogger( GMLSchemaCache.class.getName() );

  private static GMLSchemaCache THE_CACHE;

  private static final IllegalStateException NOT_INITIALIZED = new IllegalStateException( "Schema-Katalog nicht initialisiert. Zuerst 'init' aufrufen!" );

  private static IUrlCatalog THE_CATALOG;

  private GMLSchemaCatalog( )
  {
    // wird nicht instantiiert
  }

  /**
   * Initializes the schema-cache. Empties it, if it already exists.
   * 
   * @throws NullPointerException
   *           If catalog or cacheDirectory is null.
   */
  public synchronized static void init( final IUrlCatalog catalog, final File cacheDirectory )
  {
    GMLSchemaCatalog.THE_CATALOG = catalog;

    if( catalog == null )
      throw new NullPointerException();

    // delete old tmp-dir
    FileUtilities.deleteRecursive( TMP_DIR );
    TMP_DIR.mkdirs();

    THE_CACHE = new GMLSchemaCache( cacheDirectory, TMP_DIR );

    LOGGER.info( "Schema-Katalog initialisiert mit DIR=" + cacheDirectory );
  }

  /** Should be called when this cache is no more used. Releases all blocked resources. */
  public synchronized static void release( )
  {
    FileUtilities.deleteRecursive( TMP_DIR );
  }

  public static IUrlCatalog getDefaultCatalog( )
  {
    if( THE_CATALOG == null )
      throw NOT_INITIALIZED;
    return THE_CATALOG;
  }

  /**
   * Lädt ein Schema aus dieser URL (nicht aus dem Cache!) und fügt es dann dem cache hinzu (mit namespace als key).
   * 
   * @return null, wenn schema nicht geladen werden konnte
   */
  public synchronized static GMLSchema getSchema( final String gmlVersion, final URL schemaLocation )
  {
    if( THE_CACHE == null )
      throw NOT_INITIALIZED;

    try
    {
      final GMLSchema schema = GMLSchemaFactory.createGMLSchema( gmlVersion, schemaLocation );
      final Date validity = new Date( schemaLocation.openConnection().getLastModified() );

      THE_CACHE.addSchema( schema.getTargetNamespace(), new GMLSchemaCache.GMLSchemaWrapper( schema, validity ) );

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
  public synchronized static GMLSchema getSchema( final String namespace, final String gmlVersion ) throws InvocationTargetException
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
  public synchronized static GMLSchema getSchema( final String namespace, final String gmlVersion, final URL schemaLocation ) throws InvocationTargetException
  {
    if( THE_CACHE == null )
      throw NOT_INITIALIZED;

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
        version = "3.1";
      else
        version = gmlVersion;

      catalogUrl = THE_CATALOG.getURL( namespace + "#" + version.charAt( 0 ) );
    }
    else
    {
      catalogUrl = THE_CATALOG.getURL( namespace );
      version = gmlVersion;
    }

    final URL schemaUrl = catalogUrl == null ? schemaLocation : catalogUrl;

    // auch versuchen aus dem Cache zu laden, wenn die url null ist;
    // vielleicht ist der namespace ja noch im file-cache
    return THE_CACHE.getSchema( namespace, version, schemaUrl );
  }

  /**
   * Clears the cache. Schematas are reloaded after this operation.
   * 
   * @param onlyMemoryCache
   *          If true, only the memory cache is cleared. Else, file and memory cache are cleared.
   */
  public static void clearCache( final boolean onlyMemoryCache )
  {
    if( THE_CACHE != null )
      THE_CACHE.clearCache( onlyMemoryCache );
  }
}
