package org.kalypso.chart.factory.configuration.parameters.impl;

import java.util.HashMap;
import java.util.Map;

import org.kalypso.chart.factory.configuration.parameters.IParameterContainer;
import org.kalypso.chart.framework.exception.MalformedValueException;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.data.IStringParser;
import org.ksp.chart.factory.ParameterType;
import org.ksp.chart.factory.ParametersType;

/**
 * The class provides some help for extracting values from a set of configuration parameters
 * 
 * @author alibu
 */
public class XmlbeansParameterContainer implements IParameterContainer
{

  private final Map<String, String> m_parameters = new HashMap<String, String>();

  private final String m_ownerId;

  private final String m_epId;

  public XmlbeansParameterContainer( String ownerId, String epId, ParametersType xmlParams )
  {
    if( xmlParams != null )
    {
      final ParameterType[] parameterArray = xmlParams.getParameterArray();
      for( final ParameterType param : parameterArray )
      {
        final String name = param.getName().trim();
        final String value = param.getValue().trim();
        m_parameters.put( name, value );
      }
    }
    m_ownerId = ownerId;
    m_epId = epId;
  }

  public String getOwnerId( )
  {
    return m_ownerId;
  }

  public String getExtensionPointId( )
  {
    return m_epId;
  }

  /**
   * returns the value of a parameter with the given name or the defaultValue if the parameter is not found.
   */
  public <T> T getParsedParameterValue( String paramName, String defaultValue, IStringParser<T> parser )
  {
    T value = null;
    String strValue = m_parameters.get( paramName );
    // Wert nicht vorhanden, versuche auf Default-Wert auszuweichen => Warnung
    if( strValue == null )
    {
      strValue = defaultValue;
      Logger.logWarning( Logger.TOPIC_LOG_CONFIG, "Parameter " + m_ownerId + "/" + paramName + " not found in Config - trying to use default value: " + defaultValue );
    }

    // kein Default-Wert vorhanden => Fehlermeldung
    if( strValue == null )
    {
      Logger.logError( Logger.TOPIC_LOG_CONFIG, "No default parameter for " + m_ownerId + "/" + paramName + "; I cannot continue normally" );
      Logger.logError( Logger.TOPIC_LOG_CONFIG, "Please correct configuration at " + m_ownerId + "/" + paramName + "\n format is: " + parser.getFormatHint() );
    }
    else
    {
      try
      {
        value = parser.stringToLogical( strValue );
      }
      catch( final MalformedValueException e )
      {
        Logger.logError( Logger.TOPIC_LOG_CONFIG, "Parameter " + m_ownerId + "/" + paramName + ": " + strValue + " uses wrongly formatted  default value; blame the developer for not using:"
            + parser.getFormatHint() );
      }
    }
    return value;
  }

  /**
   * returns the String representation of a parameter with the given name or the defaultValue if the parameter is not
   * found.
   */
  public String getParameterValue( String paramName, String defaultValue )
  {
    String value = m_parameters.get( paramName );
    if( value == null )
    {
      Logger.logInfo( Logger.TOPIC_LOG_CONFIG, "Parameter " + m_ownerId + "/" + paramName + " trying to use default value: " + defaultValue );
      value = defaultValue;
    }
    if( value == null )
      Logger.logError( Logger.TOPIC_LOG_CONFIG, "No default parameter for " + m_ownerId + "/" + paramName + ";  I cannot continue normally" );
    return value;
  }

}
