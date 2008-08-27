/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 * 
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 * 
 *  ---------------------------------------------------------------------------*/
package org.kalypso.contribs.eclipse.core.runtime;

import java.io.PrintStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

/**
 * Helper class in order to support debug output via trace options.
 * <p>
 * Usage: Create static instances in your plugin for every configured trace option.
 * </p>
 * <code>public static final Debug PARSING = new Debug( "org.my.plugin/foo/tracebar", System.out );</code>
 * 
 * @see Platform#getDebugOption(String)
 * @author Gernot Belger
 */
public class Debug
{
  private final boolean m_doDebug;

  private final PrintStream m_printer;

  private final Plugin m_plugin;

  private final String m_pluginid;

  /**
   * Convienience method, same as {@link #Debug(Plugin, String, System.out)}
   */
  public Debug( final Plugin plugin, final String debugOption )
  {
    this( plugin, debugOption, System.out );
  }

  /**
   * Convinience method for {@link #Debug(Plugin, PluginUtilities.id( plugin ), String, PrintStream)}
   */
  public Debug( final Plugin plugin, final String debugOption, final PrintStream printer )
  {
    this( plugin, PluginUtilities.id( plugin ), debugOption, printer );
  }

  /**
   * Create a new instance for the given debug-option.
   * 
   * @param plugin
   *            The plugin to which this options apply. Also used to log non-info messages.
   * @param debugOption
   *            This debugger is active, if {@link Platform#getDebugOption(debugOptions)} returns true.
   * @param printer
   *            All output of this debugger is redirected to this printer.
   */
  public Debug( final Plugin plugin, final String pluginId, final String debugOption, final PrintStream printer )
  {
    m_plugin = plugin;
    m_pluginid = pluginId;
    final String debugPath = m_pluginid + debugOption;

    m_printer = printer;
    m_doDebug = Boolean.valueOf( Platform.getDebugOption( debugPath ) );
  }

  /**
   * Convenience method for {@link #printf(IStatus.INFO, String, Object...)}-
   */
  public void printf( final String format, final Object... args )
  {
    printf( IStatus.INFO, format, args );
  }

  /**
   * Format the given arguments to the printer.
   * <p>
   * If debugging is switched of, no output and no formatting (performance!) taes place.
   * </p>
   * IMPORTANT: Always use the format with arguments, do not concatenate the string outside of this scope. This can lead
   * to serious performance problems.
   * </p>
   * 
   * @param severity
   *            One of {@link org.eclipse.core.runtime.IStatus}#OK, ... . If it is not
   *            {@link org.eclipse.core.runtime.IStatus}#INFO, the message will also be logged to the plugin's log.
   */
  public void printf( final int severity, final String format, final Object... args )
  {
    if( m_doDebug )
      m_printer.printf( format, args );

    if( severity != IStatus.INFO )
    {
      /* PERFORMANCE: Only format the message if really necessary. Do not change this. */
      final String msg = String.format( format, args );
      final IStatus status = new Status( severity, m_pluginid, msg );
      if( m_plugin != null )
        m_plugin.getLog().log( status );
    }
  }

  /**
   * Same as {@link #printStackTrace(IStatus.INFO, Throwable)}
   */
  public void printStackTrace( final Throwable t )
  {
    printStackTrace( IStatus.ERROR, t );
  }

  /**
   * Prints a stacktrace to the printer.
   * <p>
   * If the given serverity is NOT {@link IStatus#INFO}, also logs to the plugin's log.
   * </p>
   * 
   * @param severity
   *            One of {@link IStatus#OK},...
   */
  public void printStackTrace( final int severity, final Throwable t )
  {
    if( m_doDebug )
      t.printStackTrace( m_printer );

    if( severity != IStatus.INFO )
    {
      /* PERFORMANCE: Only format the message if really necessary. Do not change this. */
      final String msg = StatusUtilities.messageFromThrowable( t );
      final IStatus status = new Status( severity, m_pluginid, msg, t );
      m_plugin.getLog().log( status );
    }
  }

  /**
   * Creates a utility for performance measurement.
   * 
   * @param message
   *            Inital message for the stopwatch
   * @param args
   *            Arguments, the message gets formatted with
   */
  public DebugPerf createStopWatch( final String message, final Object... args )
  {
    return new DebugPerf( this, message, args );
  }

  /**
   * This function will become true, if the tracing option is enabled.
   * 
   * @return True, if the tracing option is enabled.
   */
  public boolean isEnabled( )
  {
    return m_doDebug;
  }
}