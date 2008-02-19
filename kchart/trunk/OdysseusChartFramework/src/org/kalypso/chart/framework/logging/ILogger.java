package org.kalypso.chart.framework.logging;


/**
 * interface for logging purposes
 *
 * @author alibu
 */
public interface ILogger
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

  public void logInfo( String topic, String msg );

  public void logError( String topic, String msg );

  public void logWarning( String topic, String msg );

  
  
}
