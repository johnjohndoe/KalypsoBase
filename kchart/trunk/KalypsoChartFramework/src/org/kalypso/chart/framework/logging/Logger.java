package org.kalypso.chart.framework.logging;

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
public class Logger
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

  public static int count = 0;

  private Logger( )
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
   * TODO: Nachricht erscheint 8 Mal in der Log-Datei. (Das passiert auch, wenn man die Ilog.log()- Methode von einem
   * anderen Ort aufruft.)
   */
  private static void log( int statusCode, String topic, String msg )
  {
    // Falls tracing aktiviert ist, soll die Ausgabe auch an Sysout gehen
    trace( statusCode, msg );
    if( "true".equals( Platform.getDebugOption( topic ) ) )
    {
      final ILog log = Platform.getLog( ChartPlugin.getDefault().getBundle() );
      log.log( new Status( statusCode, ChartPlugin.PLUGIN_ID, IStatus.OK, msg, null ) );
      count++;
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
    return "(no status code)";
  }

  /**
   * Gibt Nachrichten auf der Konsole aus; verwendet trace(int, String) mit dem StatusCode Status.INFO
   */
  public static void trace( String msg )
  {
    trace( IStatus.INFO, msg );
  }

  /**
   * Gibt Nachrichten auf der Konsole aus; der Statuscode wird als String ausgegeben
   */
  private static void trace( int statusCode, String msg )
  {
    if( "true".equals( Platform.getDebugOption( Logger.TOPIC_TRACE ) ) )
    {
      System.out.println( statusToString( statusCode ) + ": " + msg );
    }
  }

}
