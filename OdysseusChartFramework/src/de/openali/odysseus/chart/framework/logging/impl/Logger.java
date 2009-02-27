package de.openali.odysseus.chart.framework.logging.impl;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

import de.openali.odysseus.chart.framework.Activator;

/**
 * helper class for logging and tracing purposes
 * 
 * @author alibu
 */
public class Logger
{

  public static final String TOPIC_LOG_GENERAL = getPluginID() + "/debug";

  public static final String TOPIC_LOG_AXIS = getPluginID() + "/debug/axis";

  public static final String TOPIC_LOG_LAYER = getPluginID() + "/debug/layer";

  public static final String TOPIC_LOG_PLOT = getPluginID() + "/debug/plot";

  public static final String TOPIC_LOG_LEGEND = getPluginID() + "/debug/legend";

  public static final String TOPIC_LOG_STYLE = getPluginID() + "/debug/style";

  public static final String TOPIC_TRACE = getPluginID() + "/trace";

  public static final String TOPIC_LOG_CONFIG = getPluginID() + "/debug/config";

  public static final String TOPIC_LOG_CHART = getPluginID() + "/debug/chart";

  public static final String TOPIC_LOG_DATA = getPluginID() + "/debug/data";

  private static final String CONFIG_LEVEL = getPluginID() + "/level";

  private static enum LEVEL
  {
    INFO(1),
    WARNING(2),
    ERROR(3),
    FATAL(4),
    NEVER(5);

    private final int m_levelInt;

    private LEVEL( int levelInt )
    {
      m_levelInt = levelInt;
    }

    public int toInt( )
    {
      return m_levelInt;
    }
  }

  public Logger( )
  {
    // do not instantiate
  }

  public static final void logInfo( String topic, String msg )
  {
    log( IStatus.INFO, topic, LEVEL.INFO, msg );
  }

  public static final void logWarning( String topic, String msg )
  {
    log( IStatus.WARNING, topic, LEVEL.WARNING, msg );
  }

  public static final void logError( String topic, String msg )
  {
    log( IStatus.ERROR, topic, LEVEL.ERROR, msg );
  }

  public static final void logFatal( String topic, String msg )
  {
    log( IStatus.WARNING, topic, LEVEL.FATAL, msg );
  }

  /**
   * Schreibt eine Nachricht ins Log des Chartframework.impl-Plugins, falls das Tracing-Flag des topics auf true gesetzt
   * ist
   */
  private static void log( int statusCode, String topic, LEVEL level, String msg )
  {
    String logMsg = level.toString() + ": " + msg;
    if( loggingAllowed( level, topic ) )
    {
      final ILog log = Platform.getLog( getBundle() );
      log.log( new Status( statusCode, getPluginID(), IStatus.OK, logMsg, null ) );
    }
  }

  /**
   * logging is allowed, if topic is set to true and messageLevel is smaller than level from options-file
   * 
   * @param messageLevel
   * @param topic
   * @return
   */
  private static boolean loggingAllowed( LEVEL msgLevel, String topic )
  {
    return logLevelAllowed( msgLevel ) && topicAllowed( topic );
  }

  /**
   * is topic allowed? if not, logging is not allowed
   */
  private static boolean topicAllowed( String topic )
  {
    if( "true".equals( Platform.getDebugOption( topic ) ) )
    {
      return true;
    }
    return true;
  }

  /**
   * is level smaller than allowed? then: logging is allowed
   * 
   * @param msgLevel
   * @return
   */
  private static boolean logLevelAllowed( LEVEL msgLevel )
  {
    String configLevelStr = Platform.getDebugOption( CONFIG_LEVEL );
    if( configLevelStr != null )
    {
      int allowed = LEVEL.values().length;
      for( LEVEL l : LEVEL.values() )
      {
        if( l.toString().toLowerCase().equals( configLevelStr.trim().toLowerCase() ) )
        {
          allowed = l.toInt();
          break;
        }
      }

      if( allowed <= msgLevel.toInt() )
      {
        return true;
      }
    }
    return false;
  }

  protected static Bundle getBundle( )
  {
    return Activator.getDefault().getBundle();
  }

  private static String getPluginID( )
  {
    return Activator.PLUGIN_ID;
  }

}
