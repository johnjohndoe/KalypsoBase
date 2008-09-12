package org.kalypso.service.ods.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.opengis.ows.ServiceIdentificationDocument.ServiceIdentification;
import net.opengis.ows.ServiceProviderDocument.ServiceProvider;

import org.apache.xmlbeans.XmlException;
import org.ksp.chart.factory.ChartConfigurationType;
import org.ksp.service.odsimpl.ODSConfigurationDocument;
import org.ksp.service.odsimpl.ODSConfigurationType;
import org.ksp.service.odsimpl.ODSSceneDocument;
import org.ksp.service.odsimpl.ODSSceneType;
import org.ksp.service.odsimpl.ParameterType;
import org.ksp.service.odsimpl.SceneType;
import org.ksp.service.odsimpl.ScenesType;
import org.ksp.service.odsimpl.ServiceParametersType;

/**
 * singleton class which handles the configurations for the service and the individual scenes; if a scene does not
 * define its own ServiceProvider- and ServiceIdentification-Elements, they are inherited from the global configuration
 * 
 * @author burtscher1
 */
public class ODSConfigurationLoader
{

  private static ODSConfigurationLoader m_odsConfigruationLoader = null;

  private ODSConfigurationDocument m_ocd = null;

  private String m_defaultSceneId;

  private Map<String, ODSScene> m_scenes = null;

  private ServiceIdentification m_serviceIdentification;

  private Map<String, String> m_serviceParameters = null;

  private ServiceProvider m_serviceProvider;

  private String m_configFilename;

  private String m_configPath;

  /**
   * This is a singeleton class, so is must not be instantiated by others
   */
  private ODSConfigurationLoader( )
  {
    init();
  }

  /**
   * @return singelton object
   */
  public synchronized static ODSConfigurationLoader getInstance( )
  {
    if( m_odsConfigruationLoader == null )
    {
      m_odsConfigruationLoader = new ODSConfigurationLoader();
    }
    return m_odsConfigruationLoader;
  }

  private void init( )
  {
    System.out.println( "Reloading" );
    m_configPath = System.getProperty( IODSConstants.ODS_CONFIG_PATH_KEY, IODSConstants.ODS_CONFIG_PATH_DEFAULT );
    m_configFilename = System.getProperty( IODSConstants.ODS_CONFIG_NAME_KEY, IODSConstants.ODS_CONFIG_NAME_DEFAULT );
    m_ocd = null;
    m_serviceIdentification = null;

    try
    {
      final File configFile = new File( m_configPath, m_configFilename );
      System.out.println( "configFile: " + configFile.getAbsolutePath() );
      m_ocd = ODSConfigurationDocument.Factory.parse( configFile );
    }
    catch( final Exception e )
    {
      System.out.println( "Class not found" );
    }
    /*
     * catch (FileNotFoundException e) { //TODO Auto-generated catch block e.printStackTrace(); } catch (XmlException e) { //
     * TODO Auto-generated catch block e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated catch block
     * e.printStackTrace(); }
     */

    if( m_ocd != null )
    {
      final ODSConfigurationType configuration = m_ocd.getODSConfiguration();
      m_serviceIdentification = configuration.getServiceIdentification();
      m_serviceProvider = configuration.getServiceProvider();
      createServiceParameterMap( configuration.getServiceParameters() );
      createODSScenes( configuration.getScenes() );
    }

  }

  /**
   * initializes (resets) the scenes map and fills it with ODSScene-objects generated from ScenesType references
   * 
   * @param scenes
   */
  private void createODSScenes( ScenesType scenes )
  {
    m_scenes = new HashMap<String, ODSScene>();
    final SceneType defaultScene = scenes.getDefaultScene();
    m_defaultSceneId = defaultScene.getId();
    createODSScene( defaultScene );
    final SceneType[] sceneArray = scenes.getSceneArray();
    for( final SceneType sceneRef : sceneArray )
    {
      createODSScene( sceneRef );
    }
  }

  /**
   * adds one ODSScene to the Scenes-Map; if a scene does not have its own ServiceProvider and ServiceIdentifier, the
   * ones from the global configuration are used
   * 
   * @param sceneRef
   */
  private void createODSScene( SceneType sceneRef )
  {
    final String sceneId = sceneRef.getId();
    final String path = sceneRef.getPath();
    File sceneFile = null;
    sceneFile = new File( m_configPath, path );
    ODSSceneDocument sceneTypeDoc = null;
    try
    {
      sceneTypeDoc = ODSSceneDocument.Factory.parse( sceneFile );
    }
    catch( final XmlException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch( final IOException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    if( sceneTypeDoc != null )
    {
      final ODSSceneType sceneType = sceneTypeDoc.getODSScene();
      ServiceIdentification serviceIdentification = sceneType.getServiceIdentification();
      if( serviceIdentification == null )
        serviceIdentification = m_serviceIdentification;
      ServiceProvider serviceProvider = sceneType.getServiceProvider();
      if( serviceProvider == null )
        serviceProvider = m_serviceProvider;
      final ChartConfigurationType chartConfiguration = sceneType.getChartConfiguration();
      final ODSScene odsscene = new ODSScene( serviceIdentification, serviceProvider, chartConfiguration );
      m_scenes.put( sceneId, odsscene );
    }
  }

  /**
   * rereads the configuration files
   */
  public synchronized void reload( )
  {
    init();
  }

  public synchronized ODSScene getSceneById( String sceneId )
  {
    String usedSceneId = sceneId;
    if( sceneId == null || sceneId.trim().equals( "" ) )
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
    final ParameterType[] parameterArray = serviceParameters.getParameterArray();
    for( final ParameterType param : parameterArray )
    {
      m_serviceParameters.put( param.getName(), param.getValue() );
    }
  }

  public synchronized Map<String, String> getServiceParameters( )
  {
    return m_serviceParameters;
  }

}
