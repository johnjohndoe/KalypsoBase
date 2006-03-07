package org.kalypso.gmlschema;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class KalypsoGMLSchemaPlugin extends Plugin
{

  // The shared instance.
  private static KalypsoGMLSchemaPlugin m_plugin;

  /**
   * The constructor.
   */
  public KalypsoGMLSchemaPlugin( )
  {
    m_plugin = this;
  }

  /**
   * This method is called upon plug-in activation
   */
  @Override
  public void start( BundleContext context ) throws Exception
  {
    super.start( context );
  }

  /**
   * This method is called when the plug-in is stopped
   */
  @Override
  public void stop( BundleContext context ) throws Exception
  {
    super.stop( context );
    m_plugin = null;
  }

  /**
   * Returns the shared instance.
   */
  public static KalypsoGMLSchemaPlugin getDefault( )
  {
    return m_plugin;
  }
}