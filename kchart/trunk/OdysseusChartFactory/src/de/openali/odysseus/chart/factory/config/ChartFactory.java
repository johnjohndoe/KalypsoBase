package de.openali.odysseus.chart.factory.config;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.chart.factory.configuration.exception.AxisProviderException;
import org.kalypso.chart.factory.configuration.exception.ConfigChartNotFoundException;
import org.kalypso.chart.factory.configuration.exception.ConfigurationException;
import org.kalypso.chart.factory.configuration.exception.LayerProviderException;
import org.kalypso.chart.factory.configuration.exception.MapperProviderException;
import org.kalypso.chart.factory.util.IReferenceResolver;

import de.openali.odysseus.chart.factory.provider.IAxisProvider;
import de.openali.odysseus.chart.factory.provider.IAxisRendererProvider;
import de.openali.odysseus.chart.factory.provider.ILayerProvider;
import de.openali.odysseus.chart.factory.provider.IMapperProvider;
import de.openali.odysseus.chart.factory.util.DummyLayerProvider;
import de.openali.odysseus.chart.framework.impl.logging.Logger;
import de.openali.odysseus.chart.framework.impl.model.mapper.CoordinateMapper;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.data.IDataContainer;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chartconfig.x010.AxisRendererType;
import de.openali.odysseus.chartconfig.x010.AxisType;
import de.openali.odysseus.chartconfig.x010.ChartType;
import de.openali.odysseus.chartconfig.x010.LayerType;
import de.openali.odysseus.chartconfig.x010.MapperType;
import de.openali.odysseus.chartconfig.x010.ProviderType;
import de.openali.odysseus.chartconfig.x010.ReferencingType;
import de.openali.odysseus.chartconfig.x010.RoleReferencingType;
import de.openali.odysseus.chartconfig.x010.ChartType.Layers;
import de.openali.odysseus.chartconfig.x010.LayerType.Mappers;

/**
 * Creates a chart object from a configuration
 * 
 * @author alibu
 */
public class ChartFactory
{
	/**
	 * Keys for saving providers in chart elements
	 */
	public static String LAYER_PROVIDER_KEY = "org.kalypso.chart.factory.layerprovider";

	public static String AXIS_PROVIDER_KEY = "org.kalypso.chart.factory.axisprovider";

	public static String AXISRENDERER_PROVIDER_KEY = "org.kalypso.chart.factory.axisrendererprovider";

	public static String MAPPER_PROVIDER_KEY = "org.kalypso.chart.factory.mapperprovider";

	public static String STYLE_PROVIDER_KEY = "org.kalypso.chart.factory.styleprovider";

	public static void configureChartModel(final IChartModel model,
	        final ChartConfigurationLoader cl, final String configChartName,
	        final URL context) throws ConfigurationException
	{
		// ChartConfig auslesen
		// TODO: move the search for the chart into a separate search method
		ChartType dt = null;
		if (cl != null)
		{
			final ChartType[] charts = cl.getCharts();
			for (final ChartType chart : charts)
			{
				if (chart.getId().equals(configChartName))
				{
					dt = chart;
					break;
				}
			}
		}
		if (dt == null)
		{
			throw new ConfigChartNotFoundException(configChartName);
		}

		doConfiguration(model, cl, dt, context);
	}

	public static void doConfiguration(final IChartModel model,
	        final IReferenceResolver rr, final ChartType chartType,
	        final URL context)
	{
		model.setId(chartType.getId());
		model.setTitle(chartType.getTitle());
		model.setDescription(chartType.getDescription());

		final Layers layers = chartType.getLayers();
		final ReferencingType[] layerRefArray = layers.getLayerRefArray();
		for (final ReferencingType layerRef : layerRefArray)
		{
			final String ref = layerRef.getRef();
			final LayerType layerType = (LayerType) rr.resolveReference(ref);
			if (layerType != null)
			{
				addLayer(model, rr, context, layerType);
			}
			else
			{
				Logger.logWarning(Logger.TOPIC_LOG_CONFIG,
				        "a reference to a layer type could not be resolved: "
				                + layerRef.getRef());
			}
		}

	}

	/**
	 */
	private static void addMapper(final IMapperRegistry ar,
	        final MapperType mapperType)
	{
		if (mapperType != null)
		{
			final String mpId = mapperType.getProvider().getEpid();
			if (mpId != null && mpId.length() > 0)
			{
				try
				{
					final IMapperProvider mp = ChartExtensionLoader
					        .createMapperProvider(mpId);
					mp.init(mapperType);
					final IMapper mapper = mp.getMapper();
					// Provider in Element setzen - f�rs speichern ben�tigt
					mapper.setData(ChartFactory.AXIS_PROVIDER_KEY, mp);
					ar.addMapper(mapper);
				}
				catch (final CoreException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (final MapperProviderException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				Logger.logError(Logger.TOPIC_LOG_CONFIG, "AxisProvider " + mpId
				        + " not known");
			}
		}
		else
		{
			Logger.logError(Logger.TOPIC_LOG_GENERAL,
			        "AxisFactory: given axis is NULL.");
		}
	}

	/**
	 * creates a concrete IAxis-Implementation from an AbstractAxisType derived
	 * from a ChartConfiguration, sets the corresponding renderer and adds both
	 * to a given Chart
	 */
	public static void addAxis(final IMapperRegistry ar,
	        final IReferenceResolver rr, final AxisType axisType)
	{
		if (axisType != null)
		{
			// wenn die Achse schon da ist, dann muss man sie nicht mehr
			// erzeugen
			if (ar.getAxis(axisType.getId()) != null)
			{
				return;
			}

			final String apId = axisType.getProvider().getEpid();
			if (apId != null && apId.length() > 0)
			{
				try
				{
					final IAxisProvider ap = ChartExtensionLoader
					        .createAxisProvider(apId);
					if (ap != null)
					{
						ap.init(axisType);
						final IAxis axis = ap.getAxis();
						axis.setRegistry(ar);
						// Provider in Element setzen - f�rs speichern ben�tigt
						axis.setData(ChartFactory.AXIS_PROVIDER_KEY, ap);
						ar.addMapper(axis);

						// Renderer nur erzeugen, wenn es noch keinen f�r die
						// Achse gibt
						if (ar.getRenderer(axis) == null)
						{
							IAxisRenderer axisRenderer = ar
							        .getRenderer(axisType.getRendererRef()
							                .getRef());
							// schon vorhanden => einfach zuweisen
							if (axisRenderer != null)
							{
								ar.setRenderer(axis.getIdentifier(),
								        axisRenderer);
							}
							// erzeugen, wenn noch nicht vorhanden
							else
							{
								final ReferencingType rendererRef = axisType
								        .getRendererRef();
								final AxisRendererType rendererType = (AxisRendererType) rr
								        .resolveReference(rendererRef.getRef());
								if (rendererType != null)
								{
									final String arpId = rendererType
									        .getProvider().getEpid();
									final IAxisRendererProvider arp = ChartExtensionLoader
									        .createAxisRendererProvider(arpId);
								}
							}
						}
					}
					else
					{
						Logger.logError(Logger.TOPIC_LOG_CONFIG,
						        "Axis could not be created. EPID was: " + apId);
					}
				}
				catch (final CoreException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (final AxisProviderException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				Logger.logError(Logger.TOPIC_LOG_CONFIG, "AxisProvider " + apId
				        + " not known");
			}
		}
		else
		{
			Logger.logError(Logger.TOPIC_LOG_GENERAL,
			        "AxisFactory: given axis is NULL.");
		}
	}

	public static void addLayer(IChartModel model, IReferenceResolver rr,
	        URL context, LayerType layerType)
	{
		// Achsen hinzuf�gen
		final Mappers mapper = layerType.getMappers();
		final AxisType domainAxisType = (AxisType) rr.resolveReference(mapper
		        .getDomainAxisRef().getRef());
		addAxis(model.getMapperRegistry(), rr, domainAxisType);
		final AxisType targetAxisType = (AxisType) rr.resolveReference(mapper
		        .getTargetAxisRef().getRef());
		addAxis(model.getMapperRegistry(), rr, targetAxisType);

		// Restliche Mapper hinzuf�gen
		final RoleReferencingType[] mapperRefArray = mapper.getMapperRefArray();
		for (final RoleReferencingType mapperRef : mapperRefArray)
		{
			final MapperType mapperType = (MapperType) rr
			        .resolveReference(mapperRef.getRef());
			if (mapperType != null)
			{
				// nur dann hinzuf�gen, wenn nicht schon vorhanden
				if (model.getMapperRegistry().getMapper(mapperType.getId()) == null)
				{
					addMapper(model.getMapperRegistry(), mapperType);
				}
			}
		}

		// Styles erzeugen
		// final LayerStyle ls = createStyle( layerType, rr );

		// Layer erzeugen
		final ProviderType provider2 = layerType.getProvider();
		final String providerId = provider2.getEpid();

		try
		{
			ILayerProvider provider = ChartExtensionLoader
			        .createLayerProvider(providerId);
			if (provider != null)
			{
				Logger.logInfo(Logger.TOPIC_LOG_GENERAL,
				        "LayerProvider loaded:"
				                + provider.getClass().toString());
			}
			else
			{
				provider = new DummyLayerProvider();
				Logger.logError(Logger.TOPIC_LOG_GENERAL,
				        "No LayerProvider for " + providerId);
			}

			provider.init(model, layerType, context);
			final IChartLayer icl = provider.getLayer(context);
			if (icl != null)
			{
				IAxis domainAxis = model.getMapperRegistry().getAxis(
				        layerType.getMappers().getDomainAxisRef().getRef());
				IAxis targetAxis = model.getMapperRegistry().getAxis(
				        layerType.getMappers().getTargetAxisRef().getRef());
				ICoordinateMapper cm = new CoordinateMapper(domainAxis,
				        targetAxis);
				icl.setCoordinateMapper(cm);
				// icl.setStyle(ls);
				icl.setId(layerType.getId());
				icl.setTitle(layerType.getTitle());
				icl.setDescription(layerType.getDescription());
				icl.setVisible(layerType.getVisible());
				// saving the provider into the layer so it can be reused to
				// save changed charts
				icl.setData(LAYER_PROVIDER_KEY, provider);
				IDataContainer dataContainer = provider.getDataContainer();
				// setting the data container
				icl.setDataContainer(dataContainer);
				// initialize layer
				icl.init();

				model.getLayerManager().addLayer(icl);
				Logger.logInfo(Logger.TOPIC_LOG_CONFIG, "Adding Layer: "
				        + icl.getTitle());
			}
		}
		// TODO: NO! Please throw these exceptions, else the code outside is not
		// able to react to error-conditions
		catch (final CoreException e)
		{
			e.printStackTrace();
		}
		catch (final LayerProviderException e)
		{
			e.printStackTrace();
		}
	}
}
