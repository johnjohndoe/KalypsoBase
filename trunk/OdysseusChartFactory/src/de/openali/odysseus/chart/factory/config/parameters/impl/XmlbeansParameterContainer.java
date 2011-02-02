package de.openali.odysseus.chart.factory.config.parameters.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import de.openali.odysseus.chart.framework.exception.MalformedValueException;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.data.IStringParser;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chartconfig.x020.ParameterListType;
import de.openali.odysseus.chartconfig.x020.ParameterMapType;
import de.openali.odysseus.chartconfig.x020.ParameterMapType.Element;
import de.openali.odysseus.chartconfig.x020.ParameterType;
import de.openali.odysseus.chartconfig.x020.ParametersType;

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

  public XmlbeansParameterContainer( final String ownerId, final String epId, final ParametersType xmlParams )
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
        final String[] valueArray = param.getValueArray();
        final List<String> list = new ArrayList<String>();
        for( final String string : valueArray )
        {
          list.add( string );
        }
        m_parameterLists.put( name, list );
      }
      final ParameterMapType[] parameterMapArray = xmlParams.getParameterMapArray();
      for( final ParameterMapType param : parameterMapArray )
      {
        final String name = param.getName().trim();
        final Element[] elementArray = param.getElementArray();
        final Map<String, String> map = new TreeMap<String, String>();
        for( final Element element : elementArray )
        {
          map.put( element.getKey(), element.getValue() );
        }
        m_parameterMaps.put( name, map );
      }
    }
    m_ownerId = ownerId;
    m_epId = epId;
  }

  @Override
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
  @Override
  public <T> T getParsedParameterValue( final String paramName, final String defaultValue, final IStringParser<T> parser )
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
  @Override
  public String getParameterValue( final String paramName, final String defaultValue )
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
  @Override
  public List<String> getParameterList( final String paramName )
  {
    return m_parameterLists.get( paramName );
  }

  /**
   * @see org.kalypso.chart.factory.configuration.parameters.IParameterContainer#getParameterMap(java.lang.String)
   */
  @Override
  public Map<String, String> getParameterMap( final String paramName )
  {
    return m_parameterMaps.get( paramName );
  }

  /**
   * @see org.kalypso.chart.factory.configuration.parameters.IParameterContainer#getParsedParameterList(java.lang.String,
   *      java.util.List, org.kalypso.chart.framework.model.data.IStringParser)
   */
  @Override
  public <T> List<T> getParsedParameterList( final String paramName, final List<String> defaultValues, final IStringParser<T> parser )
  {
    final List<T> valueList = new ArrayList<T>();
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
      for( final String stringValue : stringList )
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
  @Override
  public <T> Map<String, T> getParsedParameterMap( final String paramName, final Map<String, String> defaultValues, final IStringParser<T> parser )
  {
    final Map<String, T> valueMap = new TreeMap<String, T>();
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
      for( final Entry<String, String> entry : stringMap.entrySet() )
      {
        final String key = entry.getKey();
        final String stringValue = entry.getValue();
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

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IParameterContainer#findAllKeys(java.lang.String)
   */
  @Override
  public String[] findAllKeys( final String prefix )
  {
    final Set<String> keys = new LinkedHashSet<String>();

    final Set<Entry<String, String>> entries = m_parameters.entrySet();
    for( final Entry<String, String> entry : entries )
    {
      final String key = entry.getKey();
      if( key.startsWith( prefix ) )
        keys.add( key );
    }

    return keys.toArray( new String[] {} );
  }
}
