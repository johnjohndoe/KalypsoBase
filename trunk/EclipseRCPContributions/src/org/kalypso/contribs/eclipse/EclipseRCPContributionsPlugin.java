package org.kalypso.contribs.eclipse;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.kalypso.contribs.eclipse.jobs.CronJobMutexCache;
import org.kalypso.contribs.eclipse.jobs.CronJobUtilities;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class EclipseRCPContributionsPlugin extends Plugin
{
  /**
   * The plug-in id.
   */
  public static final String ID = "org.kalypso.contribs.eclipsercp";

  /**
   * The shared instance.
   */
  private static EclipseRCPContributionsPlugin plugin;

  /**
   * Resource bundle.
   */
  private ResourceBundle m_resourceBundle;

  /**
   * A cache for mutexes of defined cron jobs.
   */
  private CronJobMutexCache m_cronJobMutexCache;

  /**
   * The constructor.
   */
  public EclipseRCPContributionsPlugin( )
  {
  }

  /**
   * This function returns the ID of the plug-in.
   * 
   * @return The plug-in id.
   */
  public static String getID( )
  {
    // return getDefault().getBundle().getSymbolicName();
    // TRICKY: directly return the ID because this plugin is sometimes used outside of the eclipse framework (server
    // side for calc service...)
    return ID;
  }

  /**
   * Returns the shared instance.
   */
  public static EclipseRCPContributionsPlugin getDefault( )
  {
    return plugin;
  }

  /**
   * Returns the string from the plugin's resource bundle, or 'key' if not found.
   * 
   * @return The string from the plugin's resource bundle, or 'key' if not found.
   */
  public static String getResourceString( String key )
  {
    ResourceBundle bundle = EclipseRCPContributionsPlugin.getDefault().getResourceBundle();

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
   * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start( BundleContext context ) throws Exception
  {
    super.start( context );

    plugin = this;

    try
    {
      m_resourceBundle = ResourceBundle.getBundle( "org.kalypso.contribs.eclipse.EclipseRCPContributionsPluginResources" );
    }
    catch( MissingResourceException x )
    {
      m_resourceBundle = null;
    }

    /* Create a new cache for mutexes of defined cron jobs. */
    m_cronJobMutexCache = new CronJobMutexCache();

    /* Start all cron jobs. */
    CronJobUtilities.startAllCronJobs();
  }

  /**
   * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop( BundleContext context ) throws Exception
  {
    /* Cancel all remaining cron jobs. */
    CronJobUtilities.cancelAllCronJobs();

    /* Discard the cache for mutexes of defined cron jobs. */
    m_cronJobMutexCache = null;

    m_resourceBundle = null;
    plugin = null;

    super.stop( context );
  }

  /**
   * Returns the plugin's resource bundle.
   * 
   * @return The plugin's resource bundle.
   */
  public ResourceBundle getResourceBundle( )
  {
    return m_resourceBundle;
  }

  /**
   * This function returns a mutex for the given mutex string.
   * 
   * @see CronJobMutexCache#getMutex(String)
   * @param mutexString
   *          The mutex string.
   * @return The mutex for the given mutex string.
   */
  public ISchedulingRule getCronJobMutex( String mutexString )
  {
    return m_cronJobMutexCache.getMutex( mutexString );
  }
}