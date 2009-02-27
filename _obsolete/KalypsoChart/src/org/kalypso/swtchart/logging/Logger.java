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
package org.kalypso.swtchart.logging;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.kalypso.swtchart.KalypsoChartPlugin;

/**
 * helper class for logging and tracing purposes
 * 
 * @author burtscher1
 *
 */
public class Logger
{
    public static final String TOPIC_LOG_GENERAL="org.kalypso.chart/debug";
    public static final String TOPIC_LOG_AXIS="org.kalypso.chart/debug/axis";
    public static final String TOPIC_LOG_LAYER="org.kalypso.chart/debug/layer";
    public static final String TOPIC_LOG_PLOT="org.kalypso.chart/debug/plot";
    public static final String TOPIC_LOG_LEGEND="org.kalypso.chart/debug/legend";
    public static final String TOPIC_LOG_STYLE="org.kalypso.chart/debug/style";
    public static final String TOPIC_TRACE="org.kalypso.chart/trace";
    public static final String TOPIC_LOG_CONFIG="org.kalypso.chart/debug/config";
  
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
     * writes a message into the error log and to sysout (if tracing topic is activated)
     */
    private static void log(int statusCode, String topic, String msg)
    {
      //Falls tracing aktiviert ist, soll die Ausgabe auch an Sysout gehen
      trace(statusCode, msg);
      if ("true".equals( Platform.getDebugOption( topic ) ))
        KalypsoChartPlugin.getDefault().getLog().log( new Status( statusCode, KalypsoChartPlugin.PLUGIN_ID, 0, msg, null ) );
    }
    
    /**
     *  generates a String from a status Code
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
     * shows tracing messages in console
     */
    public static void trace(String msg)
    {
      if ("true".equals( Platform.getDebugOption( Logger.TOPIC_TRACE ) ))
      {
        System.out.println("TRACE: "+msg);
      }
    }

    /**
     * shows tracing messages in console, including StatusCode (ERROR, WARNING, INFO)
     */
    private static void trace(int statusCode, String msg)
    {
      if ("true".equals( Platform.getDebugOption( Logger.TOPIC_TRACE ) ))
      {
        Logger.trace(statusToString( statusCode )+": "+msg);
      }
    }
    
    
}
