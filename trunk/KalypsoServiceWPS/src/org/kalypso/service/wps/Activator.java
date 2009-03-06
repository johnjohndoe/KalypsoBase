package org.kalypso.service.wps;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.Plugin;
import org.kalypso.service.wps.utils.MarshallUtilities;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin
{

  // The plug-in ID
  public static final String PLUGIN_ID = "org.kalypso.service.wps";

  // The shared instance
  private static Activator plugin;

  /**
   * The constructor
   */
  public Activator( )
  {
    plugin = this;
  }

  /**
   * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start( BundleContext context ) throws Exception
  {
    super.start( context );
    // TODO: try to do this some other way
    //workaround to force loading of marshall utilities in this place
    final InputStream inputStream = MarshallUtilities.getInputStream( "" );
    IOUtils.closeQuietly( inputStream );
  }

  /**
   * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop( BundleContext context ) throws Exception
  {
    plugin = null;
    super.stop( context );
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static Activator getDefault( )
  {
    return plugin;
  }
}