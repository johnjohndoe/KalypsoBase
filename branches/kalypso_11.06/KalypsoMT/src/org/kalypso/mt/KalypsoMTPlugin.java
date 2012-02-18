package org.kalypso.mt;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.kalypso.commons.eclipse.core.runtime.PluginImageProvider;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class KalypsoMTPlugin extends AbstractUIPlugin
{
  // The plug-in ID
  public static final String PLUGIN_ID = "org.kalypso.mt"; //$NON-NLS-1$

  // The shared instance
  private static KalypsoMTPlugin plugin;

  private PluginImageProvider m_imageProvider;

  @Override
  public void start( final BundleContext context ) throws Exception
  {
    super.start( context );

    // delete tmp images both on startup and shutdown
    m_imageProvider = new PluginImageProvider( this );
    m_imageProvider.resetTmpFiles();

    plugin = this;
  }

  @Override
  public void stop( final BundleContext context ) throws Exception
  {
    // delete tmp images both on startup and shutdown
    m_imageProvider.resetTmpFiles();
    m_imageProvider = null;

    plugin = null;
    super.stop( context );
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static KalypsoMTPlugin getDefault( )
  {
    return plugin;
  }

  public static ImageDescriptor getImageDescriptor( final String path )
  {
    return imageDescriptorFromPlugin( PLUGIN_ID, path );
  }

  public static PluginImageProvider getImageProvider( )
  {
    return getDefault().m_imageProvider;
  }

}
