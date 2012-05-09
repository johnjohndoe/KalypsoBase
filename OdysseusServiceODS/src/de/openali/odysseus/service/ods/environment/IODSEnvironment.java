package de.openali.odysseus.service.ods.environment;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Status;
import org.kalypso.ogc.core.operations.IOGCOperation;

public interface IODSEnvironment
{
  public ODSConfigurationLoader getConfigLoader( );

  public Status getStatus( );

  public Map<String, List<IODSChart>> getScenes( );

  public String getDefaultSceneId( );

  public IOGCOperation[] getOperations( );

  public String getServiceUrl( );

  public File getTmpDir( );

  public File getConfigDir( );

  File getConfigFile( );

  boolean validateChartId( String sceneId, String chartId );

  boolean validateSceneId( String sceneId );

  boolean validateLayerId( String sceneId, String chartId, String layerId );
}