package org.kalypso.contribs.eclipse;

import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.kalypso.contribs.eclipse.jobs.CronJob;
import org.kalypso.contribs.eclipse.jobs.CronJobUtilities;
import org.kalypso.contribs.eclipse.utils.Debug;
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
  private ResourceBundle resourceBundle;

  public static String getID( )
  {
    // return getDefault().getBundle().getSymbolicName();

    // TRICKY: directly return the ID because this plugin is sometimes
    // used outside of the eclipse framework (server side for calc service...)
    return ID;
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
      resourceBundle = ResourceBundle.getBundle( "org.kalypso.contribs.eclipse.EclipseRCPContributionsPluginResources" );
    }
    catch( MissingResourceException x )
    {
      resourceBundle = null;
    }

    List<CronJob> cronJobs = CronJobUtilities.getCronJobs();
    if( cronJobs.size() > 0 )
    {
      for( int i = 0; i < cronJobs.size(); i++ )
      {
        /* Get the cron job. */
        CronJob cronJob = cronJobs.get( i );

        /* Start the cron job. */
        IStatus status = CronJobUtilities.startCronJob( cronJob );

        if( Debug.CRON_JOB.isEnabled() )
        {
          /* Get the log. */
          ILog log = getLog();

          /* Log the result. */
          log.log( status );
        }
      }
    }
  }

  /**
   * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop( BundleContext context ) throws Exception
  {
    /* Cancel all remaining cron jobs. */
    CronJobUtilities.cancelAllCronJobs();

    plugin = null;

    super.stop( context );
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
   * Returns the plugin's resource bundle.
   * 
   * @return The plugin's resource bundle.
   */
  public ResourceBundle getResourceBundle( )
  {
    return resourceBundle;
  }
}