package de.openali.odysseus.chart.factory.config.parameters.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;


import de.openali.odysseus.chart.factory.config.parameters.IParameterContainer;
import de.openali.odysseus.chart.framework.exception.MalformedValueException;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.data.IStringParser;
import de.openali.odysseus.chartconfig.x010.ParameterListType;
import de.openali.odysseus.chartconfig.x010.ParameterMapType;
import de.openali.odysseus.chartconfig.x010.ParameterType;
import de.openali.odysseus.chartconfig.x010.ParametersType;
import de.openali.odysseus.chartconfig.x010.ParameterMapType.Element;


/**
 * The class provides some help for extracting values from a set of configuration parameters
 * 
 * @author alibu
 */
public class XmlbeansParameterContainer implements IParameterContainer
{

  private final Map<String, String> m_parameters = new HashMap<String, String>();

  private final Map<String, List<String>> m_parameterLists = new HashMap<String, List<String>>();

  private final Map<String, Map<String, String>> m_parameterMaps = new HashMap<String, Map<String, String>>();

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
      final ParameterListType[] parameterListArray = xmlParams.getParameterListArray();
      for( final ParameterListType param : parameterListArray )
      {
        final String name = param.getName().trim();
        String[] valueArray = param.getValueArray();
        List<String> list = new ArrayList<String>();
        for( String string : valueArray )
        {
          list.add( string );
        }
        m_parameterLists.put( name, list );
      }
      final ParameterMapType[] parameterMapArray = xmlParams.getParameterMapArray();
      for( final ParameterMapType param : parameterMapArray )
      {
        final String name = param.getName().trim();
        Element[] elementArray = param.getElementArray();
        Map<String, String> map = new TreeMap<String, String>();
        for( Element element : elementArray )
        {
          map.put( element.getKey(), element.getValue() );
        }
        m_parameterMaps.put( name, map );
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

  /**
   * @see org.kalypso.chart.factory.configuration.parameters.IParameterContainer#getParameterList(java.lang.String)
   */
  public List<String> getParameterList( String paramName )
  {
    return m_parameterLists.get( paramName );
  }

  /**
   * @see org.kalypso.chart.factory.configuration.parameters.IParameterContainer#getParameterMap(java.lang.String)
   */
  public Map<String, String> getParameterMap( String paramName )
  {
    return m_parameterMaps.get( paramName );
  }

  /**
   * @see org.kalypso.chart.factory.configuration.parameters.IParameterContainer#getParsedParameterList(java.lang.String,
   *      java.util.List, org.kalypso.chart.framework.model.data.IStringParser)
   */
  public <T> List<T> getParsedParameterList( String paramName, List<String> defaultValues, IStringParser<T> parser )
  {
    List<T> valueList = new ArrayList<T>();
    List<String> stringList = m_parameterLists.get( paramName );
    // Wert nicht vorhanden, versuche auf Default-Wert auszuweichen => Warnung
    if( stringList == null )
    {
      stringList = defaultValues;
      Logger.logWarning( Logger.TOPIC_LOG_CONFIG, "ParameterList " + m_ownerId + "/" + paramName + " not found in Config - trying to use default values: " + defaultValues );
    }

    // kein Default-Wert vorhanden => Fehlermeldung
    if( stringList == null )
    {
      Logger.logError( Logger.TOPIC_LOG_CONFIG, "No default parameter list for " + m_ownerId + "/" + paramName + "; I cannot continue normally" );
      Logger.logError( Logger.TOPIC_LOG_CONFIG, "Please correct configuration at " + m_ownerId + "/" + paramName + "\n format is: " + parser.getFormatHint() );
    }
    else
    {
      for( String stringValue : stringList )
      {
        T objValue = null;
        try
        {
          objValue = parser.stringToLogical( stringValue );
        }
        catch( final MalformedValueException e )
        {
          Logger.logError( Logger.TOPIC_LOG_CONFIG, "Parameter " + m_ownerId + "/" + paramName + ": value " + stringValue
              + " uses wrongly formatted  default value; blame the developer for not using:" + parser.getFormatHint() );
        }
        valueList.add( objValue );
      }
    }
    return valueList;
  }

  /**
   * @see org.kalypso.chart.factory.configuration.parameters.IParameterContainer#getParsedParameterList(java.lang.String,
   *      java.util.Map, org.kalypso.chart.framework.model.data.IStringParser)
   */
  public <T> Map<String, T> getParsedParameterMap( String paramName, Map<String, String> defaultValues, IStringParser<T> parser )
  {
    Map<String, T> valueMap = new TreeMap<String, T>();
    Map<String, String> stringMap = m_parameterMaps.get( paramName );
    // Wert nicht vorhanden, versuche auf Default-Wert auszuweichen => Warnung
    if( stringMap == null )
    {
      stringMap = defaultValues;
      Logger.logWarning( Logger.TOPIC_LOG_CONFIG, "ParameterMap " + m_ownerId + "/" + paramName + " not found in Config - trying to use default values: " + defaultValues );
    }

    // kein Default-Wert vorhanden => Fehlermeldung
    if( stringMap == null )
    {
      Logger.logError( Logger.TOPIC_LOG_CONFIG, "No default parameter map for " + m_ownerId + "/" + paramName + "; I cannot continue normally" );
      Logger.logError( Logger.TOPIC_LOG_CONFIG, "Please correct configuration at " + m_ownerId + "/" + paramName + "\n format is: " + parser.getFormatHint() );
    }
    else
    {
      for( Entry<String, String> entry : stringMap.entrySet() )
      {
        String key = entry.getKey();
        String stringValue = entry.getValue();
        T objValue = null;
        try
        {
          objValue = parser.stringToLogical( stringValue );
        }
        catch( final MalformedValueException e )
        {
          Logger.logError( Logger.TOPIC_LOG_CONFIG, "Parameter " + m_ownerId + "/" + paramName + ": key : " + key + " : value " + stringValue
              + " uses wrongly formatted  default value; blame the developer for not using:" + parser.getFormatHint() );
        }
        valueMap.put( key, objValue );
      }
    }
    return valueMap;
  }

}
