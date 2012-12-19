package de.openali.odysseus.service.ods.environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.xmlbeans.XmlException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.osgi.FrameworkUtilities;
import org.kalypso.contribs.eclipse.utils.ConfigUtils;
import org.kalypso.ogc.core.exceptions.OWSException;
import org.kalypso.ogc.core.operations.IOGCOperation;

import de.openali.odysseus.chart.factory.config.ChartConfigurationLoader;
import de.openali.odysseus.chart.factory.config.ChartExtensionLoader;
import de.openali.odysseus.chart.factory.config.ChartFactory;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.impl.ChartModel;
import de.openali.odysseus.chartconfig.x020.ChartConfigurationDocument;
import de.openali.odysseus.chartconfig.x020.ChartConfigurationType;
import de.openali.odysseus.chartconfig.x020.ChartType;
import de.openali.odysseus.chartconfig.x020.DerivedLayerType;
import de.openali.odysseus.chartconfig.x020.LayerRefernceType;
import de.openali.odysseus.chartconfig.x020.LayerType;
import de.openali.odysseus.service.ods.util.CapabilitiesLoader;
import de.openali.odysseus.service.ods.util.IODSConstants;
import de.openali.odysseus.service.ods.x020.ChartOfferingType;
import de.openali.odysseus.service.odsimpl.x020.ODSConfigurationDocument;
import de.openali.odysseus.service.odsimpl.x020.SceneType;

public class ODSEnvironment implements IODSEnvironment
{
  private ODSConfigurationLoader m_ocl;

  private final Exception m_exception = null;

  private File m_configDir;

  private File m_configFile;

  private Status m_status;

  private File m_tmpDir;

  private String m_defaultSceneID;

  private static ODSEnvironment m_instance = null;

  private static Map<String, List<IODSChart>> m_scenes = new TreeMap<>();

  private ODSEnvironment( )
  {
    try
    {
      checkPaths();
      m_ocl = new ODSConfigurationLoader( m_configDir, m_configFile );
      m_defaultSceneID = m_ocl.getDefaultSceneId();
      createScenes();
    }
    catch( final Throwable t )
    {
      m_status = new Status( IStatus.ERROR, "null", t.getLocalizedMessage(), t );
      return;
    }
    m_status = new Status( IStatus.OK, "null", "", null );
  }

  /**
   * @param reset
   *          if set to true, the Environment is newly created instead of reused; useful for testing configuration files
   * @return a singleton instance of ODSEnvironment
   */
  public synchronized static ODSEnvironment getInstance( final boolean reset )
  {
    if( m_instance == null || reset )
      m_instance = new ODSEnvironment();
    return m_instance;
  }

  public Exception getException( )
  {
    return m_exception;
  }

  /**
   * @see de.openali.odysseus.service.ods.environment.IODSEnvironment#getConfigLoader()
   */
  @Override
  public ODSConfigurationLoader getConfigLoader( )
  {
    return m_ocl;
  }

  public ODSConfigurationDocument getConfigurationDocument( )
  {
    return m_ocl.getConfigurationDocument();
  }

  public void checkTmpDir( ) throws IOException
  {
    final File tmpDir = new File( System.getProperties().getProperty( "java.io.tmpdir" ) );
    if( !tmpDir.exists() )
      throw new FileNotFoundException( "Path to config file doesn't exist: " + tmpDir.getAbsolutePath() );

    if( !tmpDir.canWrite() )
      throw new IOException( "TmpDir is not writable: " + tmpDir.getAbsolutePath() );

    if( !tmpDir.canRead() )
      throw new IOException( "TmpDir is not readable: " + tmpDir.getAbsolutePath() );

    m_tmpDir = tmpDir;
  }

  private void checkConfigDir( ) throws IOException
  {
    // absolute path
    final String pathString = FrameworkUtilities.getProperty( IODSConstants.ODS_CONFIG_PATH_KEY, IODSConstants.ODS_CONFIG_PATH_DEFAULT );
    final File path = new File( pathString );
    if( path.exists() )
    {
      m_configDir = path;
      return;
    }

    // relative search, fallback test
//    final String fileString = FrameworkUtilities.getProperty( IODSConstants.ODS_CONFIG_FILENAME_KEY, IODSConstants.ODS_CONFIG_FILENAME_DEFAULT );
//    final File relPath = new File( pathString, fileString );
    final URL url = ConfigUtils.findCentralConfigLocation( pathString );
    final File file = new File( url.getPath() );
    m_configDir = file;
  }

  private void checkConfigFile( ) throws IOException
  {
    final String fileString = FrameworkUtilities.getProperty( IODSConstants.ODS_CONFIG_FILENAME_KEY, IODSConstants.ODS_CONFIG_FILENAME_DEFAULT );
    final File file = new File( getConfigDir(), fileString );
    if( !file.exists() )
      throw new FileNotFoundException( "Configuration File doesn't exist: " + file.getAbsolutePath() );

    if( !file.canRead() )
      throw new IOException( "Cannot read config file: " + fileString );

    m_configFile = file;
  }

  private File getChartFile( final String sceneID ) throws ConfigurationException, IOException
  {
    // URLs der ChartFiles für SceneID rausfinden
    final SceneType[] nonDefaultScenes = m_ocl.getConfigurationDocument().getODSConfiguration().getScenes().getSceneArray();
    final SceneType defaultScene = m_ocl.getConfigurationDocument().getODSConfiguration().getScenes().getDefaultScene();

    // Default Scene muss extra hinzugefügt werden
    final SceneType[] sceneArray = ArrayUtils.addAll( nonDefaultScenes, new SceneType[] { defaultScene } );

    String chartFilePath = null;
    String sceneIds = "";
    for( final SceneType sceneType : sceneArray )
    {
      sceneIds += sceneType.getId() + " ";
      if( sceneType.getId().equals( sceneID ) )
        chartFilePath = sceneType.getPath();
    }

    // Überprüfen, ob Datei vorhanden
    if( chartFilePath == null || chartFilePath.trim().equals( "" ) )
      throw new ConfigurationException( "Scene '" + sceneID + "' not found; use one of " + sceneIds );

    final File chartFile = new File( getConfigDir(), chartFilePath );
    if( !chartFile.exists() )
      throw new FileNotFoundException( "ChartEile does not exist: " + chartFile.getAbsolutePath() );

    return chartFile;
  }

  private void checkPaths( ) throws IOException
  {
    checkConfigDir();
    checkConfigFile();
    checkTmpDir();
  }

  private void createScenes( ) throws ConfigurationException, IOException, XmlException, OWSException
  {
    final String[] sceneIds = m_ocl.getSceneIds();
    for( final String sceneId : sceneIds )
    {
      final File chartFile = getChartFile( sceneId );
      if( chartFile != null )
      {
        final ChartConfigurationDocument ccd = ChartConfigurationDocument.Factory.parse( chartFile );
        final ChartConfigurationType cc = ccd.getChartConfiguration();
        final ChartType[] chartArray = cc.getChartArray();
        final List<IODSChart> sceneCharts = new ArrayList<>();
        for( final ChartType chartType : chartArray )
        {
          final ChartConfigurationLoader ccl = new ChartConfigurationLoader( chartType );

          // Offerings erzeugen
          final IChartModel model = new ChartModel();
          ChartFactory.configureChartModel( model, ccl, ccl.getChartIds()[0], ChartExtensionLoader.getInstance(), chartFile.toURI().toURL() );

          final CapabilitiesLoader cl = new CapabilitiesLoader( this );
          final ChartOfferingType chartOffering = cl.createChartOffering( model, sceneId );
          sceneCharts.add( new ODSChart( ccl, chartOffering ) );
        }

        m_scenes.put( sceneId, sceneCharts );
      }
    }
  }

  /**
   * @see de.openali.odysseus.service.ods.environment.IODSEnvironment#getStatus()
   */
  @Override
  public Status getStatus( )
  {
    return m_status;
  }

  /**
   * @see de.openali.odysseus.service.ods.environment.IODSEnvironment#getOperations()
   */
  @Override
  public IOGCOperation[] getOperations( )
  {
    return null;
  }

  /**
   * @see de.openali.odysseus.service.ods.environment.IODSEnvironment#getScenes()
   */
  @Override
  public Map<String, List<IODSChart>> getScenes( )
  {
    return m_scenes;
  }

  /**
   * @see de.openali.odysseus.service.ods.environment.IODSEnvironment#getServiceUrl()
   */
  @Override
  public String getServiceUrl( )
  {
    return null;
  }

  /**
   * @see de.openali.odysseus.service.ods.environment.IODSEnvironment#getConfigDir()
   */
  @Override
  public File getConfigDir( )
  {
    return m_configDir;
  }

  /**
   * @see de.openali.odysseus.service.ods.environment.IODSEnvironment#getConfigFile()
   */
  @Override
  public File getConfigFile( )
  {
    return m_configFile;
  }

  /**
   * @see de.openali.odysseus.service.ods.environment.IODSEnvironment#getTmpDir()
   */
  @Override
  public File getTmpDir( )
  {
    return m_tmpDir;
  }

  /**
   * @see de.openali.odysseus.service.ods.environment.IODSEnvironment#getDefaultSceneId()
   */
  @Override
  public String getDefaultSceneId( )
  {
    return m_defaultSceneID;
  }

  /**
   * @see de.openali.odysseus.service.ods.environment.IODSEnvironment#validateChartId(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public boolean validateChartId( final String sceneId, final String chartId )
  {
    if( !validateSceneId( sceneId ) )
      return false;

    final ChartConfigurationDocument sceneById = m_ocl.getSceneById( sceneId );
    final ChartType[] chartArray = sceneById.getChartConfiguration().getChartArray();
    for( final ChartType c : chartArray )
    {
      if( c.getId().equals( chartId ) )
        return true;
    }

    return false;
  }

  /**
   * @see de.openali.odysseus.service.ods.environment.IODSEnvironment#validateLayerId(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  @Override
  public boolean validateLayerId( final String sceneId, final String chartId, final String layerId )
  {
    if( !validateSceneId( sceneId ) || !validateChartId( sceneId, chartId ) )
      return false;

    final ChartConfigurationDocument sceneById = m_ocl.getSceneById( sceneId );
    final ChartType[] chartArray = sceneById.getChartConfiguration().getChartArray();
    for( final ChartType c : chartArray )
    {
      if( c.getId().equals( chartId ) )
      {
        final LayerType[] layerArray = c.getLayers().getLayerArray();
        for( final LayerType l : layerArray )
        {
          if( l.getId().equals( layerId ) )
            return true;
        }

        final LayerRefernceType[] referenceArray = c.getLayers().getLayerReferenceArray();
        for( final LayerRefernceType l : referenceArray )
        {
          if( l.getUrl().endsWith( layerId ) )
            return true;
        }

        final DerivedLayerType[] derivedArray = c.getLayers().getDerivedLayerArray();
        for( final DerivedLayerType l : derivedArray )
        {
          if( l.getId().equals( layerId ) )
            return true;
        }
      }
    }

    return false;
  }

  /**
   * @see de.openali.odysseus.service.ods.environment.IODSEnvironment#validateSceneId(java.lang.String)
   */
  @Override
  public boolean validateSceneId( final String sceneId )
  {
    for( final String sId : m_ocl.getSceneIds() )
      if( sceneId.equals( sId ) )
        return true;

    return false;
  }
}