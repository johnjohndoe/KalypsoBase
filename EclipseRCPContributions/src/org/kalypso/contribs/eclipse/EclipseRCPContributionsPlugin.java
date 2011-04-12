package org.kalypso.contribs.eclipse;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.kalypso.contribs.eclipse.jobs.CronJobMutexCache;
import org.kalypso.contribs.eclipse.jobs.CronJobUtilities;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class EclipseRCPContributionsPlugin extends AbstractUIPlugin
{
  /**
   * The plug-in id.
   */
  public static final String ID = "org.kalypso.contribs.eclipsercp"; //$NON-NLS-1$

  /**
   * The shared instance.
   */
  private static EclipseRCPContributionsPlugin PLUGIN;

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
    return PLUGIN;
  }

  /**
   * Returns the string from the plugin's resource bundle, or 'key' if not found.
   * 
   * @return The string from the plugin's resource bundle, or 'key' if not found.
   */
  public static String getResourceString( final String key )
  {
    final ResourceBundle bundle = EclipseRCPContributionsPlugin.getDefault().getResourceBundle();

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
   * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start( final BundleContext context ) throws Exception
  {
    super.start( context );

    PLUGIN = this;

    try
    {
      m_resourceBundle = ResourceBundle.getBundle( "org.kalypso.contribs.eclipse.EclipseRCPContributionsPluginResources" );
    }
    catch( final MissingResourceException x )
    {
      m_resourceBundle = null;
    }

    /* Create a new cache for mutexes of defined cron jobs. */
    m_cronJobMutexCache = new CronJobMutexCache();

    /* Start all cron jobs. */
    final Job cronStarter = new Job( "Starting Cron-Jobs" )
    {
      @Override
      protected IStatus run( final IProgressMonitor monitor )
      {
        try
        {
          CronJobUtilities.startAllCronJobs();
          return Status.OK_STATUS;
        }
        catch( final CoreException e )
        {
          return e.getStatus();
        }
      }
    };
    cronStarter.setSystem( true );
    // Delay it by 5sec, else we may get a BundleStatusException (is 5sec always good?)
    cronStarter.schedule( 5000 );
  }

  /**
   * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop( final BundleContext context ) throws Exception
  {
    /* Cancel all remaining cron jobs. */
    CronJobUtilities.cancelAllCronJobs();

    /* Discard the cache for mutexes of defined cron jobs. */
    m_cronJobMutexCache = null;

    m_resourceBundle = null;
    PLUGIN = null;

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
  public ISchedulingRule getCronJobMutex( final String mutexString )
  {
    return m_cronJobMutexCache.getMutex( mutexString );
  }
}