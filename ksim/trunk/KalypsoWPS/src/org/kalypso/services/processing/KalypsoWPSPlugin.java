package org.kalypso.services.processing;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class KalypsoWPSPlugin extends Plugin
{
  // The shared instance.
  private static KalypsoWPSPlugin plugin;

  private IWebProcessingService[] m_services;

  /**
   * The constructor.
   */
  public KalypsoWPSPlugin( )
  {
    plugin = this;
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
  }

  /**
   * Returns the shared instance.
   */
  public static KalypsoWPSPlugin getDefault( )
  {
    return plugin;
  }

  public static String getID( )
  {
    return getDefault().getBundle().getSymbolicName();
  }

  public IWebProcessingService[] getWebProcessingServices( )
  {
    if( m_services == null )
    {
      try
      {
        m_services = KalypsoWPSExtensions.getRegisteredServices();
      }
      catch( CoreException e )
      {
        e.printStackTrace();
      }
    }

    return m_services;
  }
}
