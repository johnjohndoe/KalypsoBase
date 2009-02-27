package org.kalypso.chart.framework.impl.logging;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.kalypso.chart.framework.ChartPlugin;

/**
 * helper class for logging and tracing purposes
 *
 * @author alibu
 */
public final class Logger
{
  public static final String TOPIC_LOG_GENERAL = "org.kalypso.chart.framework/debug";

  public static final String TOPIC_LOG_AXIS = "org.kalypso.chart.framework/debug/axis";

  public static final String TOPIC_LOG_LAYER = "org.kalypso.chart.framework/debug/layer";

  public static final String TOPIC_LOG_PLOT = "org.kalypso.chart.framework/debug/plot";

  public static final String TOPIC_LOG_LEGEND = "org.kalypso.chart.framework/debug/legend";

  public static final String TOPIC_LOG_STYLE = "org.kalypso.chart.framework/debug/style";

  public static final String TOPIC_TRACE = "org.kalypso.chart.framework/trace";

  public static final String TOPIC_LOG_CONFIG = "org.kalypso.chart.framework/debug/config";

  public static final String TOPIC_LOG_CHART = "org.kalypso.chart.framework/debug/chart";

  public static final String TOPIC_LOG_DATA = "org.kalypso.chart.framework/debug/data";


  public Logger( )
  {
    // do not instantiate
  }

  public static void logInfo( String topic, String msg )
  {
    log( IStatus.INFO, topic, msg );
  }

  public static void logError( String topic, String msg )
  {
    log( IStatus.ERROR, topic, msg );
  }

  public static void logWarning( String topic, String msg )
  {
    log( IStatus.WARNING, topic, msg );
  }

  /**
   * Schreibt eine Nachricht ins Log des ChartFramework-Plugins, falls das Tracing-Flag des topics auf true gesetzt ist
   */
  private static void log( int statusCode, String topic, String msg )
  {
    if( "true".equals( Platform.getDebugOption( topic ) ) )
    {
      final ILog log = Platform.getLog( ChartPlugin.getDefault().getBundle() );
      log.log( new Status( statusCode, ChartPlugin.PLUGIN_ID, IStatus.OK, statusToString( statusCode ) + ": " + msg, null ) );
    }
  }

  /**
   * Erzeugt einen String aus einem StatusCode
   */
  private static String statusToString( int statusCode )
  {
    if( statusCode == IStatus.ERROR )
      return "ERROR";
    else if( statusCode == IStatus.WARNING )
      return "WARNING";
    else if( statusCode == IStatus.INFO )
      return "INFO";
    else if( statusCode == IStatus.CANCEL )
      return "CANCEL";
    return "(no status code)";
  }

}
