package org.kalypso.chart.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class KalypsoChartUiPlugin extends AbstractUIPlugin
{
  public static final String ID = "org.kalypso.chart.ui"; //$NON-NLS-1$

  // The shared instance.
  private static KalypsoChartUiPlugin plugin;

  public KalypsoChartUiPlugin( )
  {
    plugin = this;
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
    plugin = null;
  }

  /**
   * Returns the shared instance.
   */
  public static KalypsoChartUiPlugin getDefault( )
  {
    return plugin;
  }

  /**
   * Returns an image descriptor for the image file at the given plug-in relative path.
   * 
   * @param path
   *          the path
   * @return the image descriptor
   */
  public static ImageDescriptor getImageDescriptor( final String path )
  {
    return AbstractUIPlugin.imageDescriptorFromPlugin( "org.kalypso.chart.ui", path ); //$NON-NLS-1$
  }
}
