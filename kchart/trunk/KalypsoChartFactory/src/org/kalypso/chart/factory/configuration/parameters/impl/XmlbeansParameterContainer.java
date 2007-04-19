package org.kalypso.chart.factory.configuration.parameters.impl;

import java.util.HashMap;
import java.util.Map;

import org.kalypso.chart.factory.configuration.exception.MalformedValueException;
import org.kalypso.chart.factory.configuration.parameters.IParameterContainer;
import org.kalypso.chart.factory.configuration.parameters.IStringParser;
import org.kalypso.chart.framework.logging.Logger;
import org.ksp.chart.factory.ParameterType;
import org.ksp.chart.factory.ParametersType;


/**
 * The class provides some help for extracting values from a set of configuration parameters
 * 
 * @author alibu
 */
public class XmlbeansParameterContainer implements IParameterContainer
{

  private final Map<String, String> m_parameters=new HashMap<String, String>();
  private final String m_ownerId;
  private final String m_epId;
 
  
  
  public XmlbeansParameterContainer(String ownerId, String epId, ParametersType xmlParams)
  {
	  if (xmlParams!=null)
	  {
		  ParameterType[] parameterArray = xmlParams.getParameterArray();
		  for (ParameterType param : parameterArray) 
		  {
			  String name=param.getName();
			  String value=param.getValue();
			  m_parameters.put(name, value);
		  }
	  }
	  m_ownerId=ownerId;
	  m_epId=epId;
  }
  
  public String getOwnerId()
  {
	  return m_ownerId;
  }
  
  public String getExtensionPointId()
  {
	  return m_epId;
  }


  /**
   * returns the value of a parameter with the given name or the defaultValue if the parameter is not found.
   */
  public <T> T getParsedParameterValue( String paramName, String defaultValue, IStringParser<T> parser )
  {
    T value = null;
    String strValue = m_parameters.get(paramName);
    if( strValue == null )
    {
      strValue = defaultValue;
      Logger.logError( Logger.TOPIC_LOG_CONFIG, "Parameter " + m_ownerId + "/" + paramName + " not found in Config - using default value: " + defaultValue );
    }
    try
    {
      value = parser.createValueFromString( strValue );
    }
    catch( MalformedValueException e )
    {
      String hint = parser.getFormatHint();
      Logger.logError( Logger.TOPIC_LOG_CONFIG, "Parameter " + m_ownerId + "/" + paramName + ": " + strValue + " is wrong format - need " + hint );
    }
    return value;
  }

  /**
   * returns the String representation of a parameter with the given name or the defaultValue if the parameter is not
   * found.
   */
  public String getParameterValue( String paramName, String defaultValue )
  {
    String value = m_parameters.get(paramName);
    if( value == null )
    {
      Logger.logInfo( Logger.TOPIC_LOG_CONFIG, "Parameter " + m_ownerId + "/" + paramName + " not found in Config - using default value: " + defaultValue );
      value = defaultValue;
    }
    return value;
  }


}
