package de.openali.odysseus.chart.factory;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 */
public class OdysseusChartFactory extends Plugin
{
  /**
   * The plug-in ID.
   */
  public static final String PLUGIN_ID = "de.openali.odysseus.chart.factory"; //$NON-NLS-1$

  /**
   * The shared instance.
   */
  private static OdysseusChartFactory plugin;

  /**
   * The constructor.
   */
  public OdysseusChartFactory( )
  {
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start( final BundleContext context ) throws Exception
  {
    super.start( context );

    plugin = this;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop( final BundleContext context ) throws Exception
  {
    plugin = null;

    super.stop( context );
  }

  /**
   * Returns the shared instance.
   * 
   * @return The shared instance.
   */
  public static OdysseusChartFactory getDefault( )
  {
    return plugin;
  }
}