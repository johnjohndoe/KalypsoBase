package org.kalypso.simulation.ui;

import java.io.File;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class KalypsoSimulationUIPlugin extends AbstractUIPlugin
{
  // The shared instance.
  private static KalypsoSimulationUIPlugin plugin;

  public static String getID( )
  {
    return getDefault().getBundle().getSymbolicName();
  }

  /**
   * The constructor.
   */
  public KalypsoSimulationUIPlugin( )
  {
    super();
    plugin = this;
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
  }

  /**
   * Returns the shared instance.
   */
  public static KalypsoSimulationUIPlugin getDefault( )
  {
    return plugin;
  }

  public static File getServerModelRoot( )
  {
    final String property = System.getProperty( "kalypso.hwv.prognose.dir", null );
    if( property == null )
      return null;

    return new File( property );
  }

}
