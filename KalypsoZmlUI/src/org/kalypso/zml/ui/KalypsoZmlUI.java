package org.kalypso.zml.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class KalypsoZmlUI extends AbstractUIPlugin
{
  // The plug-in ID
  public static final String PLUGIN_ID = "org.kalypso.zml.ui"; //$NON-NLS-1$

  // The shared instance
  private static KalypsoZmlUI PLUGIN;

  public KalypsoZmlUI( )
  {
  }

  @Override
  public void start( final BundleContext context ) throws Exception
  {
    super.start( context );
    PLUGIN = this;
  }

  @Override
  public void stop( final BundleContext context ) throws Exception
  {
    PLUGIN = null;
    super.stop( context );
  }

  public static KalypsoZmlUI getDefault( )
  {
    return PLUGIN;
  }
}
