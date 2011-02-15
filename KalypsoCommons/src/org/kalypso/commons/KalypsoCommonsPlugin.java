package org.kalypso.commons;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.xml.transform.TransformerFactory;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class KalypsoCommonsPlugin extends AbstractUIPlugin
{
  // The shared instance.
  private static KalypsoCommonsPlugin plugin;

  // Resource bundle.
  private ResourceBundle resourceBundle;

  public static String getID( )
  {
    return getDefault().getBundle().getSymbolicName();
  }

  /**
   * The constructor.
   */
  public KalypsoCommonsPlugin( )
  {
    super();
    plugin = this;
    try
    {
      resourceBundle = ResourceBundle.getBundle( "org.kalypso.commons.KalypsoCommonsPluginResources" ); //$NON-NLS-1$
    }
    catch( final MissingResourceException x )
    {
      resourceBundle = null;
    }
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
  public static KalypsoCommonsPlugin getDefault( )
  {
    return plugin;
  }

  /**
   * Returns the string from the plugin's resource bundle, or 'key' if not found.
   */
  public static String getResourceString( final String key )
  {
    final ResourceBundle bundle = KalypsoCommonsPlugin.getDefault().getResourceBundle();
    try
    {
      return (bundle != null) ? bundle.getString( key ) : key;
    }
    catch( final MissingResourceException e )
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

  public TransformerFactory getTransformerFactory( )
  {
    return TransformerFactory.newInstance();
  }
}
