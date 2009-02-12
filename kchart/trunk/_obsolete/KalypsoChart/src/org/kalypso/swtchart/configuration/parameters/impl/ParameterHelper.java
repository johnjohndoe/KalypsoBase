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
package org.kalypso.swtchart.configuration.parameters.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.kalypso.swtchart.configuration.parameters.IParameterContainer;
import org.kalypso.swtchart.configuration.parameters.IStringParser;
import org.kalypso.swtchart.logging.Logger;
import org.ksp.chart.configuration.ParameterType;
import org.ksp.chart.configuration.Parameters;

/**
 * The class provides some help for extracting values from a set of configuration parameters
 * 
 * @author burtscher1
 */
public class ParameterHelper implements IParameterContainer
{
  private Parameters m_parameters;

  private HashMap<String, HashMap<String, String>> m_paramMap = new HashMap<String, HashMap<String, String>>();

  public ParameterHelper( )
  {

  }

  public void addParameters( Parameters parameters, String namespace )
  {
    if( parameters != null )
    {
      List<ParameterType> ps = parameters.getParameter();
      HashMap<String, String> currentParams = new HashMap<String, String>();
      for( ParameterType p : ps )
      {
        currentParams.put( p.getName(), p.getValue() );
      }
      m_paramMap.put( namespace, currentParams );
    }
    else
    {
      Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "ParameterHelper: No parameters for " + namespace );
    }
  }

  /**
   * returns the value of a parameter with the given name or the defaultValue if the parameter is not found.
   */
  public <T> T getParsedParameterValue( String paramName, String defaultValue, String namespace, IStringParser<T> parser )
  {
    T value = null;
    String strValue = getParameterValue( paramName, defaultValue, namespace );
    if( strValue == null )
    {
      strValue = defaultValue;
      Logger.logError( Logger.TOPIC_LOG_CONFIG, "Parameter " + namespace + "/" + paramName + " not found in Config - using default value: " + defaultValue );
    }
    try
    {
      value = parser.createValueFromString( strValue );
    }
    catch( MalformedValueException e )
    {
      String hint = parser.getFormatHint();
      Logger.logError( Logger.TOPIC_LOG_CONFIG, "Parameter " + namespace + "/" + paramName + ": " + strValue + " is wrong format - need " + hint );
    }
    return value;
  }

  /**
   * returns the String representation of a parameter with the given name or the defaultValue if the parameter is not
   * found.
   */
  public String getParameterValue( String paramName, String defaultValue, String namespace )
  {
    String value = null;
    HashMap<String, String> keyvals = m_paramMap.get( namespace );
    if( keyvals != null )
    {
      value = keyvals.get( paramName );
    }
    if( value == null )
    {
      String domain = "";
      if( namespace.compareTo( "" ) == 0 )
        domain = "<defaultNamespace>";
      else
        domain = namespace;
      Logger.logInfo( Logger.TOPIC_LOG_CONFIG, "Parameter " + domain + "/" + paramName + " not found in Config - using default value: " + defaultValue );
      return defaultValue;
    }
    return value;
  }

  /**
   * returns a value without checking for the namespace; should only be used if there's only one namespace or if all
   * parameters have unique names
   */
  public String getParameterValue( String paramName, String defaultValue )
  {
    String value = "";
    String namespace = getParameterNamespace( paramName );
    if( namespace.compareTo( "" ) != 0 )
      value = getParameterValue( paramName, defaultValue, namespace );
    else
      return defaultValue;
    return value;
  }

  /**
   * returns a value without checking for the namespace; should only be used if there's only one namespace or if all
   * parameters have unique names
   */
  public <T> T getParsedParameterValue( String paramName, String defaultValue, IStringParser<T> parser )
  {
    T value = null;
    String namespace = getParameterNamespace( paramName );
    // if (namespace.compareTo( "" )!=0)
    value = getParsedParameterValue( paramName, defaultValue, namespace, parser );
    return value;
  }

  /**
   * returns the first namespace which posseses a parameter by the name paramName
   */
  public String getParameterNamespace( String paramName )
  {
    String paramNamespace = "";
    Set<String> namespaces = m_paramMap.keySet();
    boolean breakall = false;
    for( String namespace : namespaces )
    {
      HashMap<String, String> namespaceMap = m_paramMap.get( namespace );
      Set<String> names = namespaceMap.keySet();
      for( String name : names )
      {
        if( name.compareTo( paramName ) == 0 )
        {
          paramNamespace = namespace;
          breakall = true;
          break;
        }
      }
      if( breakall )
        break;
    }
    if( paramNamespace.compareTo( "" ) == 0 )
      Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "No namespace found for parameter '" + paramName + "' -> using default namespace" );
    return paramNamespace;
  }

  public HashMap<String, String> getParametersForNamespace( String namespace )
  {
    HashMap<String, String> paramMap = m_paramMap.get( namespace );
    Logger.logError( Logger.TOPIC_LOG_CONFIG, "No parameters found for  " + namespace );
    return paramMap;
  }
}
