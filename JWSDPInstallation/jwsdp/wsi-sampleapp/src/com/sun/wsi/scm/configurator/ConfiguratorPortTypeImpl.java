/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.configurator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.FindQualifier;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.SpecificationLink;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import com.sun.wsi.scm.configuration.ConfigurationEndpointRole;
import com.sun.wsi.scm.configuration.ConfigurationEndpointType;
import com.sun.wsi.scm.configurator.cache.CService;
import com.sun.wsi.scm.configurator.cache.ObjectFactory;
import com.sun.wsi.scm.configurator.cache.Services;
import com.sun.wsi.scm.util.JAXRConstants;
import com.sun.wsi.scm.util.Localizer;
import com.sun.wsi.scm.util.WSIConstants;

public class ConfiguratorPortTypeImpl
	implements ConfiguratorPortType, ServiceLifecycle, WSIConstants, JAXRConstants {
	Hashtable _tModelKeys = new Hashtable();
	Hashtable _rolesCategoryBag = new Hashtable();

	BusinessQueryManager _bqm = null;
	BusinessLifeCycleManager _lcm = null;

	Logger _logger = null;
	ServletEndpointContext _servletEndpointContext = null;
	ServletContext _servletContext = null;

	String _className = getClass().getName();

	Localizer _localizer = null;
	PropertyResourceBundle _rb = null;

	public void init(Object context) {
		_servletEndpointContext = (ServletEndpointContext) context;
		_servletContext = _servletEndpointContext.getServletContext();

		_logger = Logger.getLogger(LOGGER);
		_logger.entering(_className, INIT);
		_localizer = new Localizer();

		// Prepare the stream for configurator resource bundle
		InputStream is =
			_servletContext.getResourceAsStream(CONFIGURATOR_RESOURCES);

		try {
			_rb = new PropertyResourceBundle(is);
		} catch (IOException ex) {
			ex.printStackTrace();
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		}

		_tModelKeys.put(LOGGING_TMODEL_KEY, "Logging");
		_tModelKeys.put(RETAILER_TMODEL_KEY, "Retailer");
		_tModelKeys.put(WAREHOUSE_TMODEL_KEY, "Warehouse");
		_tModelKeys.put(MANUFACTURER_TMODEL_KEY, "Manufacturer");
		// Configurator service is not required to be picked up

		_rolesCategoryBag.put(
			WAREHOUSE_KEYVALUE[WAREHOUSE_A],
			ConfigurationEndpointRole.WarehouseA);
		_rolesCategoryBag.put(
			WAREHOUSE_KEYVALUE[WAREHOUSE_B],
			ConfigurationEndpointRole.WarehouseB);
		_rolesCategoryBag.put(
			WAREHOUSE_KEYVALUE[WAREHOUSE_C],
			ConfigurationEndpointRole.WarehouseC);
		_rolesCategoryBag.put(
			MANUFACTURER_KEYVALUE[MANUFACTURER_A],
			ConfigurationEndpointRole.ManufacturerA);
		_rolesCategoryBag.put(
			MANUFACTURER_KEYVALUE[MANUFACTURER_B],
			ConfigurationEndpointRole.ManufacturerB);
		_rolesCategoryBag.put(
			MANUFACTURER_KEYVALUE[MANUFACTURER_C],
			ConfigurationEndpointRole.ManufacturerC);

		_logger.exiting(_className, INIT);
	}

	public ConfigOptionsType getConfigurationOptions(boolean refresh)
		throws ConfiguratorFailedFault {
		_logger.entering(_className, GET_CONFIG_OPTIONS);
		_logger.log(
			Level.INFO,
			_rb.getString("config.refresh"),
			String.valueOf(refresh));
		ConfigOptionsType configOptionsType = new ConfigOptionsType();

		ConfigOptionType[] configOptions = null;

		if (refresh)
			configOptions = getConfigOptionsFromUDDI();
		else {
			configOptions = getConfigOptionsFromCache();
		}

		configOptionsType.setConfigOption(configOptions);

		_logger.exiting(_className, GET_CONFIG_OPTIONS);
		return configOptionsType;
	}

	/**
	* Get the configuration options from UDDI registry
	*/
	private ConfigOptionType[] getConfigOptionsFromUDDI()
		throws ConfiguratorFailedFault {
		_logger.entering(_className, GET_CONFIG_OPTIONS_UDDI);

		// Prepare the stream for configurator resource bundle
		InputStream is = _servletContext.getResourceAsStream(UDDI_CONFIG);

		ConfigOptionType[] configOptions = null;
		Properties props = new Properties();
		try {
			props.load(is);
			props.setProperty(
				QUERY_MANAGER_URL,
				props.getProperty("query.manager"));
			props.setProperty(
				LIFECYCLE_MANAGER_URL,
				props.getProperty("lifecycle.manager"));
		} catch (IOException ex) {
			ex.printStackTrace();
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		}
		_logger.log(
			Level.INFO,
			_rb.getString("config.uddi"),
			props.getProperty("query.manager"));

		try {
			ConnectionFactory connectionFactory =
				ConnectionFactory.newInstance();
			connectionFactory.setProperties(props);
			Connection connection = connectionFactory.createConnection();

			RegistryService registryService = connection.getRegistryService();
			_bqm = registryService.getBusinessQueryManager();
			_lcm = registryService.getBusinessLifeCycleManager();

			// Retrieve the set of uddi:businessKeys for businesses 
			// that have reciprocal relationships with the WS-I
			// uddi:businessEntity
			ArrayList orgList = showcaseEntities();

			// Retrieve each uddi:businessEntity element to get to the
			// uddi:businessService categorization and name, and the
			// uddi:bindingTemplate accessPoint and instanceParams
			// elements.
			configOptions =
				showcaseServices(
					(Organization[]) (orgList.toArray(new Organization[0])));

			// Update the cache with the latest endpoints information
			updateCache(configOptions);

		} catch (Throwable t) {
			t.printStackTrace();
			_logger.log(Level.SEVERE, t.getMessage(), t);
			throw new ConfiguratorFailedFault(t.getMessage());
		} finally {
			_logger.exiting(_className, GET_CONFIG_OPTIONS_UDDI);
		}

		return configOptions;
	}

	/**
	* Retrieve the set of uddi:businessKeys for businesses 
	* that have reciprocal relationships with the WS-I
	* uddi:businessEntity
	*/
	private ArrayList showcaseEntities() throws JAXRException {
		_logger.log(Level.CONFIG, _rb.getString("config.uddi.relnship"));

		ArrayList orgList = new ArrayList();
		Key wsiKey = _lcm.createKey(WSI_BUSINESS_KEY);

		BulkResponse response =
			_bqm.findAssociations(null, WSI_BUSINESS_KEY, null, null);
		Iterator iter = response.getCollection().iterator();
		while (iter.hasNext()) {
			Association association = (Association) iter.next();
			Organization sourceOrg =
				(Organization) association.getSourceObject();
			Organization targetOrg =
				(Organization) association.getTargetObject();
			_logger.log(
				Level.FINER,
				_rb.getString("config.uddi.relatedTo"),
				new String[] {
					sourceOrg.getName().getValue(),
					targetOrg.getName().getValue()});
			_logger.log(
				Level.FINER,
				_rb.getString("config.uddi.assoc"),
				new Object[] { new Boolean(association.isConfirmed())});
			if (association.isConfirmed()) {
				_logger.log(
					Level.FINE,
					_rb.getString("config.uddi.confirmed"),
					targetOrg.getName().getValue());
				orgList.add(targetOrg);
			} else {
				_logger.log(
					Level.FINE,
					_rb.getString("config.uddi.notConfirmed"),
					targetOrg.getName().getValue());
			}
		}

		return orgList;
	}

	/**
	* Retrieve each uddi:businessEntity element to get to the
	* uddi:businessService categorization and name, and the
	* uddi:bindingTemplate accessPoint and instanceParams
	* elements
	*/
	private ConfigOptionType[] showcaseServices(Organization[] orgs)
		throws JAXRException, URISyntaxException {
		_logger.log(Level.CONFIG, _rb.getString("config.showcase.services"));

		Vector configVector = new Vector();
		_logger.log(
			Level.CONFIG,
			_rb.getString("config.showcase.orgLength"),
			String.valueOf(orgs.length));

		for (int i = 0; i < orgs.length; i++) {
			String orgName = orgs[i].getName().getValue();
			_logger.log(
				Level.INFO,
				_rb.getString("config.showcase.services.org"),
				new String[] { String.valueOf(i + 1), orgName });

			Collection findQualifiers = new ArrayList();
			findQualifiers.add(FindQualifier.OR_ALL_KEYS);
			Collection namePatterns = new ArrayList();
			namePatterns.add("%");
			BulkResponse response =
				_bqm.findServices(
					orgs[i].getKey(),
					findQualifiers,
					namePatterns,
					null,
					null);
			Collection services = response.getCollection();
			_logger.log(
				Level.INFO,
				_rb.getString("config.showcase.org.servicesLength"),
				new String[] { String.valueOf(services.size()), orgName });

			Iterator serviceIter = services.iterator();

			int serviceCount = 0;
			while (serviceIter.hasNext()) {
				Service service = (Service) serviceIter.next();
				String serviceName = service.getName().getValue();
				++serviceCount;

				Iterator bindingsIter = service.getServiceBindings().iterator();
				while (bindingsIter.hasNext()) {
					ServiceBinding bindingTemplate =
						(ServiceBinding) bindingsIter.next();
					String accessPoint = bindingTemplate.getAccessURI();
					if (serviceName == null) {
						_logger.log(
							Level.WARNING,
							_rb.getString(
								"config.showcase.service.name.notFound"),
							accessPoint);
						serviceName = accessPoint;
					}
					_logger.log(
						Level.CONFIG,
						_rb.getString("config.showcase.service.name"),
						new String[] {
							String.valueOf(serviceCount),
							serviceName,
							accessPoint });
					Collection specLinks =
						bindingTemplate.getSpecificationLinks();
					Iterator iter = specLinks.iterator();
					while (iter.hasNext()) {
						SpecificationLink specLink =
							(SpecificationLink) iter.next();
						Concept tModel =
							(Concept) specLink.getSpecificationObject();
						_logger.log(
							Level.FINEST,
							_rb.getString("config.showcase.service.tModel"),
							new String[] {
								tModel.getName().getValue(),
								tModel.getKey().getId()});

						Iterator tModelInstanceInfoIter =
							specLink.getUsageParameters().iterator();

						// if a tmodel is not a showcase model, then it
						// should not be included
						if (!isShowcaseTmodel(tModel)) {
							_logger.log(
								Level.WARNING,
								_rb.getString(
									"config.showcase.service.tModel.notShowcase"),
								new String[] {
									tModel.getName().getValue(),
									tModel.getKey().getId()});
							continue;
						}

						// prepare the selection params
						String instanceParams = "";
						while (tModelInstanceInfoIter.hasNext()) {
							String instanceParam =
								(String) tModelInstanceInfoIter.next();
							instanceParams += instanceParam;
						}
						_logger.log(
							Level.FINER,
							_rb.getString(
								"config.showcase.service.instanceParams"),
							instanceParams);

						// set the appropriate role
						ConfigurationEndpointRole role = null;
						Iterator categoryBag =
							service.getClassifications().iterator();
						_logger.log(
							Level.FINER,
							_rb.getString("config.showcase.service.category"));
						if (!categoryBag.hasNext()) {
							_logger.log(
								Level.FINER,
								_rb.getString(
									"config.showcase.service.category.empty"));
						} else {
							while (categoryBag.hasNext()) {
								Classification classifi =
									(Classification) categoryBag.next();
								String categoryBagKey =
									classifi
										.getClassificationScheme()
										.getKey()
										.getId();
								_logger.log(
									Level.FINER,
									_rb.getString(
										"config.showcase.service.category.value"),
									new String[] {
										classifi.getName().getValue(),
										classifi.getValue()});
								if (categoryBagKey
									.equalsIgnoreCase(CATEGORY_BAG_KEY)) {
									role =
										(
											ConfigurationEndpointRole) _rolesCategoryBag
												.get(
											classifi.getName().getValue()
												+ ","
												+ classifi.getValue());
								}
							}
						}
						_logger.log(
							Level.FINER,
							_rb.getString("config.showcase.service.search"));

						// role==null - true for configurator, loggingFacility
						// and retailer. configurator service is ignored earlier
						// itself and thus set the roles for retailer and
						// loggingFacility
						if (role == null) {
							if (tModel
								.getKey()
								.getId()
								.equalsIgnoreCase(RETAILER_TMODEL_KEY)) {
								_logger.log(
									Level.FINER,
									_rb.getString(
										"config.showcase.service.role.retailer"));
								role = ConfigurationEndpointRole.Retailer;
							} else if (
								tModel.getKey().getId().equalsIgnoreCase(
									LOGGING_TMODEL_KEY)) {
								_logger.log(
									Level.FINER,
									_rb.getString(
										"config.showcase.service.role.logging"));
								role =
									ConfigurationEndpointRole.LoggingFacility;
							} else {
								_logger.log(
									Level.WARNING,
									_rb.getString(
										"config.showcase.service.role.notSet"),
									new String[] {
										tModel.getName().getValue(),
										tModel.getKey().getId()});
							}
						}
						_logger.log(
							Level.FINE,
							_rb.getString("config.showcase.service.role"),
							(role == null) ? "null" : role.getValue());
						ConfigOptionType configOptionType =
							prepareConfigOption(
								serviceName,
								instanceParams,
								accessPoint,
								role);
						configVector.add(configOptionType);
					}
				}
			}
		}

		return (ConfigOptionType[]) configVector.toArray(
			new ConfigOptionType[0]);
	}

	/**
	* Determines whether a tModel is a showcase tModel or not
	*/
	boolean isShowcaseTmodel(Concept tModel) throws JAXRException {
		return _tModelKeys.containsKey(tModel.getKey().getId().toUpperCase());
	}

	ConfigOptionType prepareConfigOption(
		String name,
		String instanceParams,
		String accessPoint,
		ConfigurationEndpointRole role)
		throws JAXRException, URISyntaxException {
		ConfigOptionType configOptionType = new ConfigOptionType();

		configOptionType.setName(name);
		configOptionType.setSelectionParms(instanceParams);

		ConfigurationEndpointType configEndpoint =
			new ConfigurationEndpointType();
		configEndpoint.set_value(new URI(accessPoint));
		if (role != null)
			configEndpoint.setRole(role);
		configOptionType.setConfigurationEndpoint(configEndpoint);
		_logger.log(
			Level.FINE,
			_rb.getString("config.showcase.service.adding"),
			new String[] { name, accessPoint });

		return configOptionType;
	}

	/**
	* Get configuration options from local cache
	*/
	ConfigOptionType[] getConfigOptionsFromCache() {
		ConfigOptionType[] configOptions = null;
		_logger.log(Level.INFO, _rb.getString("config.cache"));

		// Cached data is stored in /conf/endpoints.xml.
		// This data is always updated whenever getConfigurationOptions
		// is invoked with refresh="true"
		ServletContext servletContext =
			_servletEndpointContext.getServletContext();
		InputStream is = servletContext.getResourceAsStream(CACHED_ENDPOINTS);

		try {
			// "localhost" need to be replaced by server's IP address
			// so that this URL can be resolved globally
			String serverIP = InetAddress.getLocalHost().getHostAddress();
			_logger.log(
				Level.CONFIG,
				_rb.getString("config.cache.ip"),
				serverIP);

			if (serverIP.equals("")) {
				_logger.log(
					Level.WARNING,
					_rb.getString("config.cache.ip.notDetect"));
				serverIP = "localhost";
			}

			JAXBContext jc =
				JAXBContext.newInstance("com.sun.wsi.scm.configurator.cache");
			Unmarshaller u = jc.createUnmarshaller();
			Services services = (Services) u.unmarshal(is);
			List serviceList = services.getService();

			_logger.log(
				Level.CONFIG,
				_rb.getString("config.cache.serviceLength"),
				String.valueOf(serviceList.size()));

			configOptions = new ConfigOptionType[serviceList.size()];

			// iterate over each of the organization
			for (int i = 0; i < serviceList.size(); i++) {
				configOptions[i] = new ConfigOptionType();

				CService service = (CService) serviceList.get(i);

				// set service name
				configOptions[i].setName(service.getName());

				String endpoint = service.getEndpoint();
				if (endpoint.indexOf("localhost") != -1) {
					_logger.log(
						Level.CONFIG,
						_rb.getString("config.cache.replace.host"),
						new String[] { endpoint, serverIP });
					endpoint = endpoint.replaceFirst("localhost", serverIP);
				}

				_logger.log(
					Level.FINE,
					_rb.getString("config.cache.selectionParams"),
					service.getSelectionParams());
				_logger.log(
					Level.CONFIG,
					_rb.getString("config.showcase.service.name"),
					new String[] {
						String.valueOf(i + 1),
						service.getName(),
						endpoint });

				// set service selection params
				configOptions[i].setSelectionParms(
					service.getSelectionParams());

				ConfigurationEndpointType configEndpointType =
					new ConfigurationEndpointType();
				// configuration endpoint - URI
				configEndpointType.set_value(new URI(endpoint));

				// configuration endpoint - role
				String serviceType = service.getType();
				ConfigurationEndpointRole role = null;
				if (serviceType.equals(SERVICE_NAMES[LOGGING_SERVICE]))
					role = ConfigurationEndpointRole.LoggingFacility;
				else if (serviceType.equals(SERVICE_NAMES[RETAILER_SERVICE]))
					role = ConfigurationEndpointRole.Retailer;
				else if (serviceType.equals(SERVICE_NAMES[WAREHOUSEA_SERVICE]))
					role = ConfigurationEndpointRole.WarehouseA;
				else if (serviceType.equals(SERVICE_NAMES[WAREHOUSEB_SERVICE]))
					role = ConfigurationEndpointRole.WarehouseB;
				else if (serviceType.equals(SERVICE_NAMES[WAREHOUSEC_SERVICE]))
					role = ConfigurationEndpointRole.WarehouseC;
				else if (
					serviceType.equals(SERVICE_NAMES[MANUFACTURERA_SERVICE]))
					role = ConfigurationEndpointRole.ManufacturerA;
				else if (
					serviceType.equals(SERVICE_NAMES[MANUFACTURERB_SERVICE]))
					role = ConfigurationEndpointRole.ManufacturerB;
				else if (
					serviceType.equals(SERVICE_NAMES[MANUFACTURERC_SERVICE]))
					role = ConfigurationEndpointRole.ManufacturerC;
				_logger.log(
					Level.FINE,
					_rb.getString("config.showcase.service.type"),
					serviceType);
				_logger.log(
					Level.FINE,
					_rb.getString("config.showcase.service.role"),
					role);
				configEndpointType.setRole(role);

				// set service endpoint
				configOptions[i].setConfigurationEndpoint(configEndpointType);
			}
		} catch (URISyntaxException ex) {
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		} catch (UnknownHostException ex) {
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		} catch (IOException ex) {
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		} catch (JAXBException ex) {
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		} catch (Throwable t) {
			t.printStackTrace();
			_logger.log(Level.SEVERE, t.getMessage(), t);
		}

		return configOptions;
	}

	/**
	* Sync the cache with the latest UDDI query
	*/
	private void updateCache(ConfigOptionType[] configOptions)
		throws JAXBException, IOException {

		_logger.log(
			Level.INFO,
			_rb.getString("config.cache.update"),
			String.valueOf(configOptions.length));

		JAXBContext jc =
			JAXBContext.newInstance("com.sun.wsi.scm.configurator.cache");
		ObjectFactory objectFactory = new ObjectFactory();

		// create a new Services
		Services services = objectFactory.createServices();

		// get a reference to the Service list
		List serviceList = services.getService();

		for (int i = 0; i < configOptions.length; i++) {
			// create a new Service object
			CService service = objectFactory.createCService();
			_logger.log(
				Level.CONFIG,
				_rb.getString("config.cache.update.thService"),
				new String[] {
					String.valueOf(i + 1),
					configOptions[i].getName()});

			if (configOptions[i].getConfigurationEndpoint().getRole() == null)
				throw new RuntimeException(
					_rb.getString("config.showcase.service.role.invalid"));

			String role =
				configOptions[i]
					.getConfigurationEndpoint()
					.getRole()
					.getValue();

			// serialize only if role has one of the
			// prescribed values
			if (!(role.equals(ConfigurationEndpointRole._LoggingFacility)
				|| role.equals(ConfigurationEndpointRole._Retailer)
				|| role.equals(ConfigurationEndpointRole._WarehouseA)
				|| role.equals(ConfigurationEndpointRole._WarehouseB)
				|| role.equals(ConfigurationEndpointRole._WarehouseC)
				|| role.equals(ConfigurationEndpointRole._ManufacturerA)
				|| role.equals(ConfigurationEndpointRole._ManufacturerB)
				|| role.equals(ConfigurationEndpointRole._ManufacturerC)))
				continue;

			// populate Service object
			service.setName(configOptions[i].getName());
			service.setType(role);
			service.setEndpoint(
				configOptions[i]
					.getConfigurationEndpoint()
					.get_value()
					.toString());
			service.setSelectionParams(configOptions[i].getSelectionParms());

			_logger.log(
				Level.FINE,
				_rb.getString("config.cache.update.type"),
				service.getType());

			_logger.log(
				Level.FINE,
				_rb.getString("config.cache.update.endpoint"),
				service.getEndpoint());

			_logger.log(
				Level.FINE,
				_rb.getString("config.cache.update.selectionParams"),
				service.getSelectionParams());

			// add Service objects into it
			serviceList.add(service);
		}

		// create a marshaller
		Marshaller m = jc.createMarshaller();

		ServletContext servletContext =
			_servletEndpointContext.getServletContext();
		String fileLocation = servletContext.getRealPath("/conf/endpoints.xml");
		FileOutputStream fos = new FileOutputStream(fileLocation);
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(services, fos);
	}

	public void destroy() {
	}
}
