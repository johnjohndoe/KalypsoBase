package org.kalypso.auth;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.kalypso.auth.user.IKalypsoUser;
import org.kalypso.auth.user.KalypsoUser;
import org.kalypso.auth.user.UserRights;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class KalypsoAuthPlugin extends AbstractUIPlugin
{
  // The shared instance.
  private static KalypsoAuthPlugin plugin;

  // Resource bundle.
  private ResourceBundle resourceBundle;

  /**
   * the one and only one kalypso user. By default it is set to a default one in order to allow developers to start
   * kalypso bypassing the login procedure. Once the login procedure is started, the user is set to null unless
   * authentication succeeded.
   */
  private IKalypsoUser m_user = new KalypsoUser( "default", UserRights.NO_RIGHTS ); //$NON-NLS-1$ //$NON-NLS-2$

  /**
   * The constructor.
   */
  public KalypsoAuthPlugin( )
  {
    super();
    plugin = this;
    try
    {
      resourceBundle = ResourceBundle.getBundle( "org.kalypso.auth.KalypsoAuthPluginResources" ); //$NON-NLS-1$
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
  public static KalypsoAuthPlugin getDefault( )
  {
    return plugin;
  }

  /**
   * Returns the string from the plugin's resource bundle, or 'key' if not found.
   */
  public static String getResourceString( final String key )
  {
    final ResourceBundle bundle = KalypsoAuthPlugin.getDefault().getResourceBundle();
    try
    {
      return bundle != null ? bundle.getString( key ) : key;
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

  /**
   * @return current user
   * @throws IllegalStateException
   *           if login procedure was not started
   */
  public IKalypsoUser getCurrentUser( )
  {
    if( m_user == null )
      throw new IllegalStateException( "No user" ); //$NON-NLS-1$

    return m_user;
  }

  public void setCurrentUser( final IKalypsoUser user )
  {
    m_user = user;
  }
}
