package org.kalypso.service.ods;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ODSOperationsPlugin extends Plugin
{

  // The plug-in ID
  public static final String PLUGIN_ID = "de.openali.ods.operations";

  // The shared instance
  private static ODSOperationsPlugin plugin;

  /**
   * The constructor
   */
  public ODSOperationsPlugin( )
  {
    plugin = this;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start( BundleContext context ) throws Exception
  {
    super.start( context );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop( BundleContext context ) throws Exception
  {
    plugin = null;
    super.stop( context );
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static ODSOperationsPlugin getDefault( )
  {
    return plugin;
  }

}
