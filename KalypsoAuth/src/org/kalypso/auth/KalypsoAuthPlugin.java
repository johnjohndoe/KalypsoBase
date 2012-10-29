package org.kalypso.auth;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

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
  private static KalypsoAuthPlugin PLUGIN;

  // Resource bundle.
  private ResourceBundle m_bundle;

  private final Set<IKalypsoAuthListener> m_listeners = Collections.synchronizedSet( new LinkedHashSet<IKalypsoAuthListener>() );

  /**
   * the one and only one kalypso user. By default it is set to a default one in order to allow developers to start
   * kalypso bypassing the login procedure. Once the login procedure is started, the user is set to null unless
   * authentication succeeded.
   */
  private IKalypsoUser m_user = new KalypsoUser( "default", UserRights.NO_RIGHTS ); //$NON-NLS-1$ 

  /**
   * The constructor.
   */
  public KalypsoAuthPlugin( )
  {
    super();

    PLUGIN = this;

    try
    {
      m_bundle = ResourceBundle.getBundle( "org.kalypso.auth.KalypsoAuthPluginResources" ); //$NON-NLS-1$
    }
    catch( final MissingResourceException x )
    {
      m_bundle = null;
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
    return PLUGIN;
  }

  public void addListener( final IKalypsoAuthListener listener )
  {
    m_listeners.add( listener );
  }

  public void removeListener( final IKalypsoAuthListener listener )
  {
    m_listeners.remove( listener );
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
    return m_bundle;
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

    fireCurrentUserChanged();
  }

  private void fireCurrentUserChanged( )
  {
    final IKalypsoAuthListener[] listeners = m_listeners.toArray( new IKalypsoAuthListener[] {} );
    for( final IKalypsoAuthListener listener : listeners )
    {
      listener.eventCurrentUserChanged( m_user );
    }
  }
}