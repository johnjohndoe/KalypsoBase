package de.openali.diagram.framework.logging;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import de.openali.diagram.framework.DiagramPlugin;

/**
 * helper class for logging and tracing purposes
 * 
 * @author alibu
 *
 */
public class Logger
{
    public static final String TOPIC_LOG_GENERAL="de.openali.diagram.framework/debug";
    public static final String TOPIC_LOG_AXIS="de.openali.diagram.framework/debug/axis";
    public static final String TOPIC_LOG_LAYER="de.openali.diagram.framework/debug/layer";
    public static final String TOPIC_LOG_PLOT="de.openali.diagram.framework/debug/plot";
    public static final String TOPIC_LOG_LEGEND="de.openali.diagram.framework/debug/legend";
    public static final String TOPIC_LOG_STYLE="de.openali.diagram.framework/debug/style";
    public static final String TOPIC_TRACE="de.openali.diagram.framework/trace";
    public static final String TOPIC_LOG_CONFIG="de.openali.diagram.framework/debug/config";
    public static final String TOPIC_LOG_CHART="de.openali.diagram.framework/debug/chart";
    
  
    public static int count=0;
    
    private Logger()
    {
      //do not instantiate
    }
    
    public static void logInfo(String topic, String msg)
    {
      log(IStatus.INFO, topic, msg);
    }
    
    public static void logError(String topic, String msg)
    {
      log(IStatus.ERROR, topic, msg);
    }
    
    public static void logWarning(String topic, String msg)
    {
      log(IStatus.WARNING, topic, msg);
    }
    
    /**
     * Schreibt eine Nachricht ins Log des DiagramFramework-Plugins, falls das
     * Tracing-Flag des topics auf true gesetzt ist
     * 
     * TODO: Nachricht erscheint 8 Mal in der Log-Datei. (Das passiert auch, wenn man die Ilog.log()-
     * Methode von einem anderen Ort aufruft.)
     */
    private static void log(int statusCode, String topic, String msg)
    {
      //Falls tracing aktiviert ist, soll die Ausgabe auch an Sysout gehen
      trace(statusCode, msg);
      if ("true".equals( Platform.getDebugOption( topic ) ))
      {
    	  ILog log = Platform.getLog( DiagramPlugin.getDefault().getBundle() );
    	  log.log( new Status(statusCode, DiagramPlugin.PLUGIN_ID, Status.OK, msg, null) );
    	  count++;
      }
    }
    
    /**
     *  Erzeugt einen String aus einem StatusCode
     */
    private static String statusToString(int statusCode)
    {
      if (statusCode==IStatus.ERROR)
        return "ERROR";
      else if (statusCode==IStatus.WARNING)
        return "WARNING";
      else if (statusCode==IStatus.INFO)
        return "INFO";
      return "(no status code)";
    }
    
    /**
     * Gibt Nachrichten auf der Konsole aus; verwendet trace(int, String) mit dem StatusCode Status.INFO
     */
    public static void trace(String msg)
    {
        trace(Status.INFO, msg);
    }

    /**
     * Gibt Nachrichten auf der Konsole aus; der Statuscode wird als String ausgegeben
     */
    private static void trace(int statusCode, String msg)
    {
      if ("true".equals( Platform.getDebugOption( Logger.TOPIC_TRACE ) ))
      {
        System.out.println(statusToString( statusCode )+": "+msg);
      }
    }
    
}
