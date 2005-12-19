package org.bce.eclipse.ui.plugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

public class PluginUtilities
{
  /**
   * Logs the given data to the plugin *
   * 
   * @param severity
   *          the severity; one of <code>OK</code>, <code>ERROR</code>, <code>INFO</code>,
   *          <code>WARNING</code>, or <code>CANCEL</code>
   */
  public final static void logToPlugin( final Plugin plugin, final int severity,
      final String messsage, final Throwable t )
  {
    final IStatus status = new Status( severity, plugin.getBundle().getSymbolicName(), 0, messsage,
        t );
    plugin.getLog().log( status );
  }
}
