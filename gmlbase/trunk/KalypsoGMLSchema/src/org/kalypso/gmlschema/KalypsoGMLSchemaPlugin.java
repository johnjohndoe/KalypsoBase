package org.kalypso.gmlschema;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Plugin;
import org.kalypso.contribs.java.JavaApiContributionsExtension;
import org.kalypso.contribs.java.net.IUrlCatalog;
import org.osgi.framework.BundleContext;

public class KalypsoGMLSchemaPlugin extends Plugin
{
  // The shared instance.
  private static KalypsoGMLSchemaPlugin INSTANCE;

  private GMLSchemaCatalog m_schemaCatalog = null;

  public KalypsoGMLSchemaPlugin( )
  {
    super();

    INSTANCE = this;
  }

  /**
   * This method is called upon plug-in activation
   */
  @Override
  public void start( final BundleContext context ) throws Exception
  {
    super.start( context );
  }

  /**
   * This method is called when the plug-in is stopped
   */
  @Override
  public void stop( final BundleContext context ) throws Exception
  {
    super.stop( context );

    INSTANCE = null;
    m_schemaCatalog = null;
  }

  public synchronized GMLSchemaCatalog getSchemaCatalog( )
  {
    if( m_schemaCatalog == null )
    {
      final IUrlCatalog theCatalog = JavaApiContributionsExtension.getAllRegisteredCatalogs();
      final IPath stateLocation = getStateLocation();
      final File cacheDir = new File( stateLocation.toFile(), "schemaCache" );
      cacheDir.mkdir();
      m_schemaCatalog = new GMLSchemaCatalog( theCatalog, cacheDir );
    }

    return m_schemaCatalog;
  }

  /**
   * Returns the shared instance.
   */
  public static KalypsoGMLSchemaPlugin getDefault( )
  {
    return INSTANCE;
  }

}