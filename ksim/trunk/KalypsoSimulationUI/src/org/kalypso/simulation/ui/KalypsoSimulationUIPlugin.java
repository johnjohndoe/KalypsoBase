package org.kalypso.simulation.ui;

import org.eclipse.ui.plugin.*;
import org.kalypso.ui.KalypsoGisPlugin;
import org.osgi.framework.BundleContext;
import java.util.*;

/**
 * The main plugin class to be used in the desktop.
 */
public class KalypsoSimulationUIPlugin extends AbstractUIPlugin
{
  // The shared instance.
  private static KalypsoSimulationUIPlugin plugin;

  // Resource bundle.
  private ResourceBundle resourceBundle;

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
    try
    {
      resourceBundle = ResourceBundle.getBundle( "org.kalypso.simulation.ui.KalypsoSimulationUIPluginResources" );
    }
    catch( MissingResourceException x )
    {
      resourceBundle = null;
    }
    // force to initialize
    KalypsoGisPlugin default1 = KalypsoGisPlugin.getDefault();
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
  }

  /**
   * Returns the shared instance.
   */
  public static KalypsoSimulationUIPlugin getDefault( )
  {
    return plugin;
  }

  /**
   * Returns the string from the plugin's resource bundle, or 'key' if not found.
   */
  public static String getResourceString( String key )
  {
    ResourceBundle bundle = KalypsoSimulationUIPlugin.getDefault().getResourceBundle();
    try
    {
      return (bundle != null) ? bundle.getString( key ) : key;
    }
    catch( MissingResourceException e )
    {
      return key;
    }
  }

  /**
   * Returns the plugin's resource bundle,
   */
  public ResourceBundle getResourceBundle( )
  {
    return resourceBundle;
  }
}
