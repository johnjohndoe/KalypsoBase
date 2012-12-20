package de.openali.odysseus.chart.framework.logging.impl;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

import de.openali.odysseus.chart.framework.OdysseusChartFramework;

/**
 * helper class for logging and tracing purposes
 * 
 * @author alibu
 */
public class Logger
{

  public static final String TOPIC_LOG_GENERAL = getPluginID() + "/debug"; //$NON-NLS-1$

  public static final String TOPIC_LOG_AXIS = getPluginID() + "/debug/axis"; //$NON-NLS-1$

  public static final String TOPIC_LOG_MAPPER = getPluginID() + "/debug/mapper"; //$NON-NLS-1$

  public static final String TOPIC_LOG_LAYER = getPluginID() + "/debug/layer"; //$NON-NLS-1$

  public static final String TOPIC_LOG_PLOT = getPluginID() + "/debug/plot"; //$NON-NLS-1$

  public static final String TOPIC_LOG_LEGEND = getPluginID() + "/debug/legend"; //$NON-NLS-1$

  public static final String TOPIC_LOG_STYLE = getPluginID() + "/debug/style"; //$NON-NLS-1$

  public static final String TOPIC_TRACE = getPluginID() + "/trace"; //$NON-NLS-1$

  public static final String TOPIC_LOG_CONFIG = getPluginID() + "/debug/config"; //$NON-NLS-1$

  public static final String TOPIC_LOG_CHART = getPluginID() + "/debug/chart"; //$NON-NLS-1$

  public static final String TOPIC_LOG_DATA = getPluginID() + "/debug/data"; //$NON-NLS-1$

  private static final String CONFIG_LEVEL = getPluginID() + "/level"; //$NON-NLS-1$

  private static enum LEVEL
  {
    INFO(1),
    WARNING(2),
    ERROR(3),
    FATAL(4),
    NEVER(5);

    private final int m_levelInt;

    private LEVEL( final int levelInt )
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

  public static final void logInfo( final String topic, final String msg )
  {
    log( IStatus.INFO, topic, LEVEL.INFO, msg );
  }

  public static final void logWarning( final String topic, final String msg )
  {
    log( IStatus.WARNING, topic, LEVEL.WARNING, msg );
  }

  public static final void logError( final String topic, final String msg )
  {
    log( IStatus.ERROR, topic, LEVEL.ERROR, msg );
  }

  public static final void logFatal( final String topic, final String msg )
  {
    log( IStatus.WARNING, topic, LEVEL.FATAL, msg );
  }

  /**
   * Schreibt eine Nachricht ins Log des Chartframework.impl-Plugins, falls das Tracing-Flag des topics auf true gesetzt
   * ist
   */
  private static void log( final int statusCode, final String topic, final LEVEL level, final String msg )
  {
    final String logMsg = level.toString() + ": " + msg; //$NON-NLS-1$
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
  private static boolean loggingAllowed( final LEVEL msgLevel, final String topic )
  {
    return logLevelAllowed( msgLevel ) && topicAllowed( topic );
  }

  /**
   * is topic allowed? if not, logging is not allowed
   */
  private static boolean topicAllowed( final String topic )
  {
    if( "true".equals( Platform.getDebugOption( topic ) ) ) //$NON-NLS-1$
      return true;
    return true;
  }

  /**
   * is level smaller than allowed? then: logging is allowed
   * 
   * @param msgLevel
   * @return
   */
  private static boolean logLevelAllowed( final LEVEL msgLevel )
  {
    final String configLevelStr = Platform.getDebugOption( CONFIG_LEVEL );
    if( configLevelStr != null )
    {
      int allowed = LEVEL.values().length;
      for( final LEVEL l : LEVEL.values() )
        if( l.toString().toLowerCase().equals( configLevelStr.trim().toLowerCase() ) )
        {
          allowed = l.toInt();
          break;
        }

      if( allowed <= msgLevel.toInt() )
        return true;
    }
    return false;
  }

  protected static Bundle getBundle( )
  {
    return OdysseusChartFramework.getDefault().getBundle();
  }

  private static String getPluginID( )
  {
    return OdysseusChartFramework.PLUGIN_ID;
  }

}
