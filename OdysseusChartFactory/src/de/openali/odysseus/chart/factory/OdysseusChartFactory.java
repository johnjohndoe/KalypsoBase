package de.openali.odysseus.chart.factory;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class OdysseusChartFactory extends Plugin implements BundleActivator
{

  private static BundleContext CONTEXT;

  static BundleContext getContext( )
  {
    return CONTEXT;
  }

  private static OdysseusChartFactory PLUGIN;

  @Override
  public void start( final BundleContext context ) throws Exception
  {
    CONTEXT = context;
    PLUGIN = this;
  }

  @Override
  public void stop( final BundleContext context ) throws Exception
  {
    CONTEXT = null;
    PLUGIN = null;

  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static OdysseusChartFactory getDefault( )
  {
    return PLUGIN;
  }
}
