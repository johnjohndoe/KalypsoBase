package org.kalypso.zml.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class KalypsoZmlCore extends Plugin
{

  private static BundleContext CONTEXT;

  static BundleContext getContext( )
  {
    return CONTEXT;
  }

  private static KalypsoZmlCore PLUGIN;

  public static final String PLUGIN_ID = "org.kalypso.zml.core"; //$NON-NLS-1$

  public KalypsoZmlCore( )
  {
  }

  @Override
  public void start( final BundleContext bundleContext ) throws Exception
  {
    super.start( bundleContext );

    CONTEXT = bundleContext;
    PLUGIN = this;
  }

  @Override
  public void stop( final BundleContext bundleContext ) throws Exception
  {
    super.start( bundleContext );

    CONTEXT = null;
    PLUGIN = null;
  }

  /**
   * @return the shared instance
   */
  public static KalypsoZmlCore getDefault( )
  {
    return PLUGIN;
  }


}
