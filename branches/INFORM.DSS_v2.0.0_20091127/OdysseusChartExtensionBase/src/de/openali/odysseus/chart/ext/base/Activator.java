package de.openali.odysseus.chart.ext.base;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin
{
  // The plug-in ID
  public static final String PLUGIN_ID = "de.openali.odysseus.chart.ext.base";

  // The shared instance
  private static Activator plugin;

  /**
   * The constructor
   */
  public Activator( )
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
    plugin = null; // NOPMD by alibu on 17.02.08 17:43
    super.stop( context );
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static Activator getDefault( )
  {
    return plugin;
  }

}
