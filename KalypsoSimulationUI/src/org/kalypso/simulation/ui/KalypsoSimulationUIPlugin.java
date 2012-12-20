package org.kalypso.simulation.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class KalypsoSimulationUIPlugin extends AbstractUIPlugin
{
  // The shared instance.
  private static KalypsoSimulationUIPlugin plugin;

  public static String getID( )
  {
    return getDefault().getBundle().getSymbolicName();
  }

  /**
   * The constructor.
   */
  public KalypsoSimulationUIPlugin( )
  {
    super();
    plugin = this;
  }

  /**
   * Returns the shared instance.
   */
  public static KalypsoSimulationUIPlugin getDefault( )
  {
    return plugin;
  }
}
