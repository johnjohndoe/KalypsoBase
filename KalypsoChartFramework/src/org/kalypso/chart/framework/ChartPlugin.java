package org.kalypso.chart.framework;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ChartPlugin extends Plugin
{

  // The plug-in ID
  public static final String PLUGIN_ID = "org.kalypso.chart.framework";

  // The shared instance
  private static ChartPlugin plugin;

  /**
   * The constructor
   */
  public ChartPlugin( )
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
  public static ChartPlugin getDefault( )
  {
    return plugin;
  }

}
