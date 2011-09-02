package de.openali.odysseus.service.ods.environment;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.opengis.ows.ServiceIdentificationDocument.ServiceIdentification;
import net.opengis.ows.ServiceProviderDocument.ServiceProvider;

import org.apache.xmlbeans.XmlException;

import de.openali.odysseus.chart.framework.model.exception.ConfigurationException;
import de.openali.odysseus.chartconfig.x020.ChartConfigurationDocument;
import de.openali.odysseus.service.odsimpl.x020.ODSConfigurationDocument;
import de.openali.odysseus.service.odsimpl.x020.ODSConfigurationType;
import de.openali.odysseus.service.odsimpl.x020.ParameterType;
import de.openali.odysseus.service.odsimpl.x020.SceneType;
import de.openali.odysseus.service.odsimpl.x020.ScenesType;
import de.openali.odysseus.service.odsimpl.x020.ServiceParametersType;

/**
 * Class which handles the configurations for the service and the individual scenes; if a scene does not define its own
 * ServiceProvider- and ServiceIdentification-Elements, they are inherited from the global configuration.
 * 
 * @author Alexander Burtscher
 */
public class ODSConfigurationLoader
{
  private ODSConfigurationDocument m_ocd = null;

  private String m_defaultSceneId;

  private Map<String, ChartConfigurationDocument> m_scenes = null;

  private ServiceIdentification m_serviceIdentification;

  private Map<String, String> m_serviceParameters = null;

  private File m_configFile;

  private File m_configDir;

  private ServiceProvider m_serviceProvider;

  /**
   * @throws ConfigurationException
   * @throws IOException
   * @throws XmlException
   * @throws ConfigurationException
   * @throws IOException
   * @throws XmlException
   */
  public ODSConfigurationLoader( File configDir, File configFile ) throws XmlException, IOException, ConfigurationException
  {
    m_configFile = configFile;
    m_configDir = configDir;
    init();
  }

  private void init( ) throws XmlException, IOException, ConfigurationException
  {
    System.out.println( "Reloading" );

    System.out.println( "configFile: " + m_configFile.getAbsolutePath() );

    if( m_configFile.exists() )
    {
      m_ocd = ODSConfigurationDocument.Factory.parse( m_configFile );
      ODSConfigurationType configuration = m_ocd.getODSConfiguration();
      m_serviceIdentification = configuration.getServiceIdentification();
      m_serviceProvider = configuration.getServiceProvider();
      createServiceParameterMap( configuration.getServiceParameters() );
      createODSScenes( configuration.getScenes() );
    }
    else
      throw new ConfigurationException( "Config file cannot be opened." );

  }

  /**
   * initializes (resets) the scenes map and fills it with ODSScene-objects generated from ScenesType references
   * 
   * @param scenes
   * @throws ConfigurationException
   */
  private void createODSScenes( ScenesType scenes ) throws ConfigurationException
  {
    m_scenes = new HashMap<String, ChartConfigurationDocument>();
    SceneType defaultScene = scenes.getDefaultScene();
    m_defaultSceneId = defaultScene.getId();
    createODSScene( defaultScene );
    SceneType[] sceneArray = scenes.getSceneArray();
    for( SceneType sceneRef : sceneArray )
      createODSScene( sceneRef );
  }

  /**
   * adds one ODSScene to the Scenes-Map; if a scene does not have its own ServiceProvider and ServiceIdentifier, the
   * ones from the global configuration are used
   * 
   * @param sceneRef
   * @throws ConfigurationException
   */
  private void createODSScene( SceneType sceneRef ) throws ConfigurationException
  {
    String sceneId = sceneRef.getId();
    ChartConfigurationDocument chartConfigDoc = null;
    try
    {
      File chartFile = new File( m_configDir, sceneRef.getPath() );
      chartConfigDoc = ChartConfigurationDocument.Factory.parse( chartFile );
    }
    catch( XmlException e )
    {
      e.printStackTrace();
    }
    catch( IOException e )
    {
      e.printStackTrace();
    }

    if( chartConfigDoc != null )
      m_scenes.put( sceneId, chartConfigDoc );
  }

  /**
   * rereads the configuration files
   * 
   * @throws ConfigurationException
   * @throws IOException
   * @throws XmlException
   */
  public synchronized void reload( ) throws XmlException, IOException, ConfigurationException
  {
    init();
  }

  public synchronized String getDefaultSceneId( )
  {
    return m_defaultSceneId;
  }

  public synchronized ChartConfigurationDocument getSceneById( String sceneId )
  {
    String usedSceneId = sceneId;
    if( (sceneId == null) || sceneId.trim().equals( "" ) )
      usedSceneId = m_defaultSceneId;

    return m_scenes.get( usedSceneId );
  }

  public synchronized ODSConfigurationDocument getConfigurationDocument( )
  {
    return m_ocd;
  }

  private void createServiceParameterMap( ServiceParametersType serviceParameters )
  {
    m_serviceParameters = new HashMap<String, String>();
    ParameterType[] parameterArray = serviceParameters.getParameterArray();
    for( ParameterType param : parameterArray )
      m_serviceParameters.put( param.getName(), param.getValue() );
  }

  public synchronized Map<String, String> getServiceParameters( )
  {
    return m_serviceParameters;
  }

  public String[] getSceneIds( )
  {
    return m_scenes.keySet().toArray( new String[] {} );
  }

  public ServiceIdentification getServiceIdentification( )
  {
    return m_serviceIdentification;
  }

  public ServiceProvider getServiceProvider( )
  {
    return m_serviceProvider;
  }
}