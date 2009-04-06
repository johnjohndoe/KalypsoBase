package org.kalypso.debug;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class KalypsoDebugPlugin extends AbstractUIPlugin
{
  //The shared instance.
  private static KalypsoDebugPlugin plugin;

  //Resource bundle.
  private ResourceBundle resourceBundle;

  /**
   * The constructor.
   */
  public KalypsoDebugPlugin()
  {
    super();
    plugin = this;
  }

  public final String getID()
  {
    return getBundle().getSymbolicName();
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
    plugin = null;
    resourceBundle = null;
  }

  /**
   * Returns the shared instance.
   */
  public static KalypsoDebugPlugin getDefault()
  {
    return plugin;
  }

  /**
   * Returns the string from the plugin's resource bundle, or 'key' if not found.
   */
  public static String getResourceString( String key )
  {
    ResourceBundle bundle = KalypsoDebugPlugin.getDefault().getResourceBundle();
    try
    {
      return ( bundle != null ) ? bundle.getString( key ) : key;
    }
    catch( MissingResourceException e )
    {
      return key;
    }
  }

  /**
   * Returns the plugin's resource bundle,
   */
  public ResourceBundle getResourceBundle()
  {
    try
    {
      if( resourceBundle == null )
        resourceBundle = ResourceBundle.getBundle( "kalypsoDebug.KalypsoDebugPluginResources" );
    }
    catch( MissingResourceException x )
    {
      resourceBundle = null;
    }
    return resourceBundle;
  }
}
