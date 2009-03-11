package de.openali.odysseus.service.ods.environment;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Status;

import de.openali.odysseus.service.ows.extension.IOWSOperation;

public interface IODSEnvironment 
{

	public ODSConfigurationLoader getConfigLoader();

	public Status getStatus();

	public Map<String, List<IODSChart>> getScenes();
	
	public String getDefaultSceneId();

	public IOWSOperation[] getOperations();

	public String getServiceUrl();

	public File getTmpDir();

	public File getConfigDir();
}
