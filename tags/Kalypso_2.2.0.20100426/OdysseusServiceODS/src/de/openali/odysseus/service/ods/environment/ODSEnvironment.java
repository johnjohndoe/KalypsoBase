package de.openali.odysseus.service.ods.environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletContext;

import org.apache.commons.lang.ArrayUtils;
import org.apache.xmlbeans.XmlException;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;

import de.openali.odysseus.chart.factory.config.ChartConfigurationLoader;
import de.openali.odysseus.chart.factory.config.ChartExtensionLoader;
import de.openali.odysseus.chart.factory.config.ChartFactory;
import de.openali.odysseus.chart.factory.config.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.impl.ChartModel;
import de.openali.odysseus.chartconfig.x020.ChartConfigurationDocument;
import de.openali.odysseus.chartconfig.x020.ChartConfigurationType;
import de.openali.odysseus.chartconfig.x020.ChartType;
import de.openali.odysseus.chartconfig.x020.LayerType;
import de.openali.odysseus.service.ods.util.CapabilitiesLoader;
import de.openali.odysseus.service.ods.util.IODSConstants;
import de.openali.odysseus.service.ods.x020.ChartOfferingType;
import de.openali.odysseus.service.odsimpl.x020.ODSConfigurationDocument;
import de.openali.odysseus.service.odsimpl.x020.SceneType;
import de.openali.odysseus.service.ows.extension.IOWSOperation;

@SuppressWarnings("restriction")
public class ODSEnvironment implements IODSEnvironment
{
	private ODSConfigurationLoader m_ocl;

	private final Exception m_exception = null;

	private File m_configDir;

	private File m_configFile;

	private final Status m_status;

	private File m_tmpDir;

	private final ServletContext m_context;

	private String m_defaultSceneID;

	private static ODSEnvironment m_instance = null;

	private static Map<String, List<IODSChart>> m_scenes = new TreeMap<String, List<IODSChart>>();

	private ODSEnvironment(ServletContext context)
	{
		m_context = context;
		try
		{
			checkPaths();
			m_ocl = new ODSConfigurationLoader(m_configDir, m_configFile);
			m_defaultSceneID = m_ocl.getDefaultSceneId();
			createScenes();
		}
		catch (final Throwable t)
		{
			m_status = new Status(Status.ERROR, "null",
			        t.getLocalizedMessage(), t);
			return;
		}
		m_status = new Status(Status.OK, "null", "", null);
	}

	/**
	 * @param reset
	 *            if set to true, the Environment is newly created instead of
	 *            reused; useful for testing configuration files
	 * 
	 * @return a singleton instance of ODSEnvironment
	 */
	public synchronized static ODSEnvironment getInstance(
	        ServletContext context, boolean reset)
	{
		if ((m_instance == null) || reset)
			m_instance = new ODSEnvironment(context);
		return m_instance;
	}

	public Exception getException()
	{
		return m_exception;
	}

	public ODSConfigurationLoader getConfigLoader()
	{
		return m_ocl;
	}

	public ODSConfigurationDocument getConfigurationDocument()
	{
		return m_ocl.getConfigurationDocument();
	}

	public void checkTmpDir() throws IOException
	{
		File tmpDir = (File) m_context
		        .getAttribute("javax.servlet.context.tempdir");
		if ((tmpDir == null) || !tmpDir.exists())
			throw new FileNotFoundException(
			        "Path to config file doesn't exist: "
			                + tmpDir.getAbsolutePath());
		if (!tmpDir.canWrite())
			throw new IOException("TmpDir is not writable: "
			        + tmpDir.getAbsolutePath());
		if (!tmpDir.canRead())
			throw new IOException("TmpDir is not readable: "
			        + tmpDir.getAbsolutePath());
		m_tmpDir = tmpDir;
	}

	private void checkConfigDir() throws IOException
	{
		final String pathString = FrameworkProperties.getProperty(
		        IODSConstants.ODS_CONFIG_PATH_KEY,
		        IODSConstants.ODS_CONFIG_PATH_DEFAULT);
		final File path = new File(pathString);
		if (!path.exists())
			throw new FileNotFoundException(
			        "Path to config file doesn't exist: "
			                + path.getAbsolutePath());
		if (!path.canRead())
			throw new IOException("Config dir is not readable: "
			        + path.getAbsolutePath());

		m_configDir = path;
	}

	private void checkConfigFile() throws IOException
	{
		final String fileString = FrameworkProperties.getProperty(
		        IODSConstants.ODS_CONFIG_FILENAME_KEY,
		        IODSConstants.ODS_CONFIG_FILENAME_DEFAULT);
		final File file = new File(getConfigDir(), fileString);
		if (!file.exists())
			throw new FileNotFoundException(
			        "Configuration File doesn't exist: "
			                + file.getAbsolutePath());
		if (!file.canRead())
			throw new IOException("Cannot read config file: " + fileString);
		m_configFile = file;
	}

	private File getChartFile(final String sceneID)
	        throws ConfigurationException, IOException
	{
		// URLs der ChartFiles für SceneID rausfinden
		final SceneType[] nonDefaultScenes = m_ocl.getConfigurationDocument()
		        .getODSConfiguration().getScenes().getSceneArray();
		final SceneType defaultScene = m_ocl.getConfigurationDocument()
		        .getODSConfiguration().getScenes().getDefaultScene();

		// Default Scene muss extra hinzugefügt werden
		final SceneType[] sceneArray = (SceneType[]) ArrayUtils.addAll(
		        nonDefaultScenes, new SceneType[] { defaultScene });

		String chartFilePath = null;
		String sceneIds = "";
		for (final SceneType sceneType : sceneArray)
		{
			sceneIds += sceneType.getId() + " ";
			if (sceneType.getId().equals(sceneID))
				chartFilePath = sceneType.getPath();
		}

		// Überprüfen, ob Datei vorhanden

		if ((chartFilePath == null) || chartFilePath.trim().equals(""))
			throw new ConfigurationException("Scene '" + sceneID
			        + "' not found; use one of " + sceneIds);
		final File chartFile = new File(getConfigDir(), chartFilePath);
		if (!chartFile.exists())
			throw new FileNotFoundException("ChartEile does not exist: "
			        + chartFile.getAbsolutePath());
		return chartFile;
	}

	private void checkPaths() throws IOException
	{
		checkConfigDir();
		checkConfigFile();
		checkTmpDir();
	}

	private void createScenes() throws ConfigurationException, IOException,
	        XmlException
	{
		final String[] sceneIds = m_ocl.getSceneIds();

		for (final String sceneId : sceneIds)
		{
			final File chartFile = getChartFile(sceneId);
			if (chartFile != null)
			{
				final ChartConfigurationDocument ccd = ChartConfigurationDocument.Factory
				        .parse(chartFile);
				final ChartConfigurationType cc = ccd.getChartConfiguration();
				final ChartType[] chartArray = cc.getChartArray();
				final List<IODSChart> sceneCharts = new ArrayList<IODSChart>();
				for (final ChartType chartType : chartArray)
				{
					final ChartConfigurationLoader ccl = new ChartConfigurationLoader(
					        chartType);

					// Offerings erzeugen
					final IChartModel model = new ChartModel();
					ChartFactory.configureChartModel(model, ccl, ccl
					        .getChartIds()[0], ChartExtensionLoader
					        .getInstance(), getConfigDir().toURI().toURL());

					final CapabilitiesLoader cl = new CapabilitiesLoader(this);

					final ChartOfferingType chartOffering = cl
					        .createChartOffering(model, sceneId);
					sceneCharts.add(new ODSChart(ccl, chartOffering));

				}
				m_scenes.put(sceneId, sceneCharts);

			}
		}
	}

	@Override
	public Status getStatus()
	{
		return m_status;
	}

	@Override
	public IOWSOperation[] getOperations()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, List<IODSChart>> getScenes()
	{
		return m_scenes;
	}

	@Override
	public String getServiceUrl()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public File getConfigDir()
	{
		return m_configDir;
	}

	public File getConfigFile()
	{
		return m_configFile;
	}

	public File getTmpDir()
	{
		return m_tmpDir;
	}

	@Override
	public String getDefaultSceneId()
	{
		return m_defaultSceneID;
	}

	@Override
	public boolean validateChartId(String sceneId, String chartId)
	{
		if (validateSceneId(sceneId))
		{
			ChartConfigurationDocument sceneById = m_ocl.getSceneById(sceneId);
			ChartType[] chartArray = sceneById.getChartConfiguration()
			        .getChartArray();
			for (ChartType c : chartArray)
				if (c.getId().equals(chartId))
					return true;
		}

		return false;
	}

	@Override
	public boolean validateLayerId(String sceneId, String chartId,
	        String layerId)
	{
		if (validateSceneId(sceneId) && validateChartId(sceneId, chartId))
		{
			ChartConfigurationDocument sceneById = m_ocl.getSceneById(sceneId);
			ChartType[] chartArray = sceneById.getChartConfiguration()
			        .getChartArray();
			for (ChartType c : chartArray)
				if (c.getId().equals(chartId))
				{
					LayerType[] layerArray = c.getLayers().getLayerArray();
					for (LayerType l : layerArray)
						if (l.getId().equals(layerId))
							return true;
				}
		}

		return false;
	}

	@Override
	public boolean validateSceneId(String sceneId)
	{

		for (String sId : m_ocl.getSceneIds())
			if (sceneId.equals(sId))
				return true;
		return false;
	}

}
