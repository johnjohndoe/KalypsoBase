package org.kalypso.contribs.eclipse;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class EclipsePlatformContributionsPlugin extends Plugin
{
  // The shared instance.
  private static EclipsePlatformContributionsPlugin INSTANCE;

  // Resource bundle.
  private ResourceBundle m_resourceBundle;

  /**
   * The constructor.
   */
  public EclipsePlatformContributionsPlugin( )
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
    m_resourceBundle = null;
  }

  /**
   * Returns the shared instance.
   */
  public static EclipsePlatformContributionsPlugin getDefault( )
  {
    return INSTANCE;
  }

  /**
   * Returns the string from the plugin's resource bundle, or 'key' if not found.
   */
  public static String getResourceString( final String key )
  {
    final ResourceBundle bundle = EclipsePlatformContributionsPlugin.getDefault().getResourceBundle();
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
    try
    {
      if( m_resourceBundle == null )
        m_resourceBundle = ResourceBundle.getBundle( "org.kalypso.contribs.eclipse.platform.EclipsePlatformContributionsPluginResources" ); //$NON-NLS-1$
    }
    catch( final MissingResourceException x )
    {
      m_resourceBundle = null;
    }
    return m_resourceBundle;
  }

  public static String getID( )
  {
    return getDefault().getBundle().getSymbolicName();
  }
}
