package de.openali.odysseus.service.ods;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import de.openali.odysseus.service.ods.util.DisplayHelper;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator extends Plugin
{
  /**
   * The plug-in ID.
   */
  public static final String PLUGIN_ID = "de.openali.odysseus.service.ods"; //$NON-NLS-1$

  /**
   * The shared instance.
   */
  private static Activator plugin;

  /**
   * The constructor.
   */
  public Activator( )
  {
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start( final BundleContext context ) throws Exception
  {
    super.start( context );
    plugin = this;
    DisplayHelper.getInstance().getDisplay();
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
  public static Activator getDefault( )
  {
    return plugin;
  }
}