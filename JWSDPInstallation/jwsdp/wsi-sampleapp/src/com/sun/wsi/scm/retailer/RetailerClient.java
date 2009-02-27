/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.retailer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.rpc.Stub;

import com.sun.wsi.scm.retailer.order.Order;
import com.sun.wsi.scm.retailer.order.Orders;
import com.sun.wsi.scm.retailer.order.Part;

import com.sun.wsi.scm.retailer.vendor.Configurations;
import com.sun.wsi.scm.retailer.vendor.Configuration;
import com.sun.wsi.scm.retailer.vendor.ServiceURL;

import com.sun.wsi.scm.configuration.ConfigurationEndpointRole;
import com.sun.wsi.scm.configuration.ConfigurationEndpointType;
import com.sun.wsi.scm.configuration.ConfigurationType;
import com.sun.wsi.scm.logging.GetEventsFaultType;
import com.sun.wsi.scm.logging.GetEventsRequestType;
import com.sun.wsi.scm.logging.GetEventsResponseType;
import com.sun.wsi.scm.logging.LogEntry;
import com.sun.wsi.scm.logging.LoggingFacilityLogPortType;
import com.sun.wsi.scm.logging.LoggingFacilityService_Impl;
import com.sun.wsi.scm.util.AttributeNV;
import com.sun.wsi.scm.util.HTMLReporter;
import com.sun.wsi.scm.util.Localizer;
import com.sun.wsi.scm.util.WSIConstants;
import com.sun.wsi.scm.util.XMLWriter;
import com.sun.xml.rpc.client.StubBase;
import com.sun.xml.rpc.client.http.HttpClientTransportFactory;

public class RetailerClient implements WSIConstants {
	RetailerPortType _retailerStub = null;
	LoggingFacilityLogPortType _loggingStub = null;

	static String _className = null;
	static Logger _logger = null;
	static Localizer _localizer = null;

	static PropertyResourceBundle _rb = null;

	Hashtable _priceList = null;

	/**
	* Prepares the log files and initializes the XMLWriter
	*/
	public RetailerClient() {
		_logger = Logger.getLogger(LOGGER);

		_localizer = new Localizer();
		_className = getClass().getName();

		_priceList = new Hashtable();
		_priceList.put(
			"605001",
			(new BigDecimal(299.95)).setScale(2, BigDecimal.ROUND_HALF_UP));
		_priceList.put(
			"605002",
			(new BigDecimal(1499.99)).setScale(2, BigDecimal.ROUND_HALF_UP));
		_priceList.put(
			"605003",
			(new BigDecimal(5725.98)).setScale(2, BigDecimal.ROUND_HALF_UP));
		_priceList.put(
			"605004",
			(new BigDecimal(199.95)).setScale(2, BigDecimal.ROUND_HALF_UP));
		_priceList.put(
			"605005",
			(new BigDecimal(400.00)).setScale(2, BigDecimal.ROUND_HALF_UP));
		_priceList.put(
			"605006",
			(new BigDecimal(949.99)).setScale(2, BigDecimal.ROUND_HALF_UP));
		_priceList.put(
			"605007",
			(new BigDecimal(100.00)).setScale(2, BigDecimal.ROUND_HALF_UP));
		_priceList.put(
			"605008",
			(new BigDecimal(200.00)).setScale(2, BigDecimal.ROUND_HALF_UP));
		_priceList.put(
			"605009",
			(new BigDecimal(250.00)).setScale(2, BigDecimal.ROUND_HALF_UP));
		_priceList.put(
			"605010",
			(new BigDecimal(149.99)).setScale(2, BigDecimal.ROUND_HALF_UP));

		try {
			// Prepare the stream for retailer client resource bundle
			FileInputStream fis =
				new FileInputStream(System.getProperty("retailer.props"));
			_rb = new PropertyResourceBundle(fis);

			// Prepare the stream for dumping the logging entries
			// from LoggingFacility
			PrintStream xmlLog =
				new PrintStream(
					new FileOutputStream(
						System.getProperty("log.home")
							+ System.getProperty("file.separator")
							+ System.getProperty("log.file")));
			XMLWriter.setOutputStream(xmlLog);
		} catch (IOException ex) {
			ex.printStackTrace();
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
			System.exit(1);
		}

	}

	public static void main(String[] args) {
		RetailerClient client = new RetailerClient();
		HTMLReporter htmlReporter = HTMLReporter.getInstance();
		try {
			// process all the configurations
			AttributeNV[] attNVArray = new AttributeNV[2];
			attNVArray[0] = new AttributeNV("xmlns", ORDER_RESULTS_NAMESPACE);
			attNVArray[1] =
				new AttributeNV(
					"xmlns:" + VENDOR_CONFIG_PREFIX,
					VENDOR_CONFIG_NAMESPACE);
			XMLWriter.writeStartTag("all-orders", attNVArray);

			// log environment information
			htmlReporter.logEnvironment();

			Properties props = new Properties();
			// Read the endpoint properties file
			FileInputStream is =
				new FileInputStream(System.getProperty("endpoints.props"));
			props.load(is);

			JAXBContext jc =
				JAXBContext.newInstance("com.sun.wsi.scm.retailer.vendor");
			Unmarshaller u = jc.createUnmarshaller();
			Configurations configs =
				(Configurations) u.unmarshal(
					new File(System.getProperty("vendor.config.file")));
			List configList = configs.getConfiguration();

			for (int i = 0; i < configList.size(); i++) {
				Configuration config = (Configuration) configList.get(i);
				if (!config.isEnabled())
					continue;

				String vendor = config.getVendor();

				// process each configuration
				List serviceURLs = config.getServiceURL();

				// process all the serviceURLs
				ConfigurationType configHeader = new ConfigurationType();
				configHeader.setMustUnderstand(Boolean.FALSE);

				ConfigurationEndpointType[] configEndpoint =
					new ConfigurationEndpointType[8];

				URI loggingURI = null;
				URI retailerURI = null;
				for (int j = 0; j < serviceURLs.size(); j++) {
					ServiceURL serviceURL = (ServiceURL) serviceURLs.get(j);

					// process each serviceURL
					String serviceName = serviceURL.getName();
					String id = serviceURL.getValue();
					if (!props.containsKey(id))
						throw new IllegalArgumentException(
							_localizer.localize(
								_rb.getString("retailer.client.notFound"),
								id));

					URI endpointURI = new URI(props.getProperty(id));

					if (serviceName.equals("LoggingFacility"))
						loggingURI = endpointURI;
					else if (serviceName.equals("Retailer"))
						retailerURI = endpointURI;

					// populate configuration header
					configEndpoint[j] = new ConfigurationEndpointType();
					configEndpoint[j].setRole(
						ConfigurationEndpointRole.fromValue(serviceName));
					configEndpoint[j].set_value(endpointURI);

				}
				configHeader.setServiceUrl(configEndpoint);

				// set the logging and retailer endpoints
				client.setEndpoints(
					retailerURI.toString(),
					loggingURI.toString());

				_logger.log(
					Level.INFO,
					_rb.getString("retailer.client.invoking"),
					vendor);

				// invoke the orders
				XMLWriter.writeStartTag("orders");
				client.logConfigHeader(configHeader, vendor);

				client.placeTheOrder(configHeader, vendor);

				XMLWriter.writeEndTag("orders");
				_logger.log(
					Level.INFO,
					_rb.getString("retailer.client.success"),
					new String[] {
						System.getProperty("log.home"),
						System.getProperty("file.separator"),
						System.getProperty("log.file")});
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		} finally {
			XMLWriter.writeEndTag("all-orders");
			try {
				htmlReporter.prepareHTMLReport();
			} catch (Exception ex) {
				ex.printStackTrace();
				_logger.log(Level.SEVERE, ex.getMessage(), ex);
			}
		}
	}

	/**
	* Set the retailer and logging facility endpoint
	*/
	private void setEndpoints(String retailerURI, String loggingURI)
		throws IOException {
		RetailerService_Impl retailerService = new RetailerService_Impl();
		_retailerStub = retailerService.getRetailerPort();
		((Stub) _retailerStub)._setProperty(
			Stub.ENDPOINT_ADDRESS_PROPERTY,
			retailerURI);
		// Create SOAP message log files for FINE or higher logging level
		if (_logger.isLoggable(Level.FINE)) {
			// appends to the existing log file
			FileOutputStream soapLog =
				new FileOutputStream(
					System.getProperty("log.home")
						+ System.getProperty("file.separator")
						+ System.getProperty("retailer.soap.msgs.file"),
					true);

			((StubBase) _retailerStub)._setTransportFactory(
				new HttpClientTransportFactory(soapLog));
		}

		LoggingFacilityService_Impl loggingService =
			new LoggingFacilityService_Impl();
		_loggingStub = loggingService.getLoggingFacilityPort();
		((Stub) _loggingStub)._setProperty(
			Stub.ENDPOINT_ADDRESS_PROPERTY,
			loggingURI);
		// Create SOAP message log files for FINE or higher logging level
		if (_logger.isLoggable(Level.FINE)) {
			// appends to the existing log file
			FileOutputStream soapLog =
				new FileOutputStream(
					System.getProperty("log.home")
						+ System.getProperty("file.separator")
						+ System.getProperty("logging.soap.msgs.file"),
					true);

			((StubBase) _loggingStub)._setTransportFactory(
				new HttpClientTransportFactory(soapLog));
		}
	}

	/**
	* @return Unique user id for an order placed by a vendor combination
	*/
	private String getDemoUserId(String vendor, String orderId) {
		long now = (new Date()).getTime();

		String demoUserId =
			"order" + orderId + "." + vendor + "." + String.valueOf(now);

		return demoUserId;
	}

	/**
	* Reads the configuration with order information and places the order
	*/
	private void placeTheOrder(ConfigurationType configHeader, String vendor)
		throws IOException, JAXBException {
		_logger.log(Level.INFO, _rb.getString("retailer.client.order"), vendor);

		JAXBContext jc =
			JAXBContext.newInstance("com.sun.wsi.scm.retailer.order");
		Unmarshaller u = jc.createUnmarshaller();
		Orders orders =
			(Orders) u.unmarshal(
				new File(System.getProperty("retailer.config.file")));
		List orderList = orders.getOrder();

		_logger.log(Level.FINE, _rb.getString("retailer.client.got.order"));
		if (orderList.isEmpty()) {
			_logger.log(Level.INFO, _rb.getString("retailer.client.noOrders"));
			return;
		}

		for (int i = 0; i < orderList.size(); i++) {
			Order order = (Order) orderList.get(i);

			List partList = order.getPart();
			if (partList.isEmpty()) {
				_logger.log(
					Level.INFO,
					_rb.getString("retailer.client.noParts"));
				continue;
			}

			_logger.log(
				Level.INFO,
				_rb.getString("retailer.client.placing.order"),
				new String[] { String.valueOf(i + 1), vendor });

			XMLWriter.writeStartTag(
				"order",
				new AttributeNV("id", String.valueOf(i + 1)));

			try {
				PartsOrderType partsOrderType = new PartsOrderType();
				PartsOrderItem[] partsOrderItem =
					new PartsOrderItem[partList.size()];

				for (int j = 0; j < partList.size(); j++) {
					Part part = (Part) partList.get(j);

					partsOrderItem[j] = new PartsOrderItem();

					partsOrderItem[j].setProductNumber(
						new BigInteger(part.getProductNumber()));
					partsOrderItem[j].setQuantity(
						new BigInteger(part.getQuantity()));
					partsOrderItem[j].setPrice(
						(BigDecimal) _priceList.get(part.getProductNumber()));
				}
				logRequest(partsOrderItem);

				partsOrderType.setItem(partsOrderItem);

				String demoUserId =
					getDemoUserId(vendor, String.valueOf(i + 1));
				configHeader.setUserId(demoUserId);
				try {
					PartsOrderResponseType response =
						_retailerStub.submitOrder(
							partsOrderType,
							getCustomerDetails(),
							configHeader);
					logResponse(response, demoUserId);
				} catch (InvalidProductCodeType ex) {
					logResponse(null, demoUserId);
				}

			} catch (Exception ex) {
				ex.printStackTrace();
				writeException(ex);
				_logger.log(Level.SEVERE, ex.getMessage(), ex);
			} finally {
				XMLWriter.writeEndTag("order");
			}
		}
	}

	/**
	* Logs the configuration header (only the serviceURLs)
	*/
	private void logConfigHeader(
		ConfigurationType configHeader,
		String vendor) {
		XMLWriter.writeStartTag(
			"configuration",
			new AttributeNV("vendor", vendor));
		ConfigurationEndpointType[] configEndpoint =
			configHeader.getServiceUrl();
		for (int i = 0; i < configEndpoint.length; i++) {
			XMLWriter.writeContent(
				VENDOR_CONFIG_PREFIX + ":serviceURL",
				new AttributeNV("name", configEndpoint[i].getRole().getValue()),
				configEndpoint[i].get_value().toString());
		}
		XMLWriter.writeEndTag("configuration");
	}

	/**
	 * Prepare the customer details
	 */
	private CustomerDetailsType getCustomerDetails() {
		CustomerDetailsType customerDetails = new CustomerDetailsType();

		customerDetails.setCustnbr("A55555-99999BB-abc");
		customerDetails.setName("Sun Microsystems, Inc.");
		customerDetails.setStreet1("4140, Network Circle");
		customerDetails.setStreet2("MS: SCA14-304");
		customerDetails.setCity("Santa Clara");
		customerDetails.setState("CA");
		customerDetails.setZip("95054");
		customerDetails.setCountry("USA");

		return customerDetails;
	}

	/**
	 * Get the catalog from the service endpoint
	 */
	private void getCatalog(String vendor) {
		_logger.log(
			Level.INFO,
			_rb.getString("retailer.client.catalog.vendor"),
			vendor);
		XMLWriter.writeStartTag("order", new AttributeNV("id", "7"));
		try {
			CatalogType catalog = _retailerStub.getCatalog();
			XMLWriter.writeStartTag("catalog");

			CatalogItem[] catalogItems = catalog.getItem();
			_logger.log(
				Level.INFO,
				_rb.getString("retailer.client.catalog.entries"));
			for (int i = 0; i < catalogItems.length; i++) {
				_logger.log(
					Level.CONFIG,
					_rb.getString("retailer.client.catalog.thProduct"),
					String.valueOf(i + 1));

				_logger.log(
					Level.CONFIG,
					_rb.getString("retailer.client.catalog.productNumber"),
					catalogItems[i].getProductNumber().toString());

				_logger.log(
					Level.CONFIG,
					_rb.getString("retailer.client.catalog.name"),
					catalogItems[i].getName());

				_logger.log(
					Level.CONFIG,
					_rb.getString("retailer.client.catalog.description"),
					catalogItems[i].getDescription());

				_logger.log(
					Level.CONFIG,
					_rb.getString("retailer.client.catalog.category"),
					catalogItems[i].getCategory());

				_logger.log(
					Level.CONFIG,
					_rb.getString("retailer.client.catalog.brand"),
					catalogItems[i].getBrand());

				_logger.log(
					Level.CONFIG,
					_rb.getString("retailer.client.catalog.price"),
					catalogItems[i].getPrice().setScale(
						2,
						BigDecimal.ROUND_HALF_UP));

				XMLWriter.writeStartTag("catalog-item");
				XMLWriter.writeContent(
					"product",
					catalogItems[i].getProductNumber().toString());
				XMLWriter.writeContent("name", catalogItems[i].getName());
				XMLWriter.writeContent(
					"description",
					catalogItems[i].getDescription());
				XMLWriter.writeContent(
					"category",
					catalogItems[i].getCategory());
				XMLWriter.writeContent("brand", catalogItems[i].getBrand());
				XMLWriter.writeContent(
					"price",
					catalogItems[i].getPrice().toString());
				XMLWriter.writeEndTag("catalog-item");
			}

		} catch (Throwable ex) {
			ex.printStackTrace();
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		} finally {
			XMLWriter.writeEndTag("catalog");
			XMLWriter.writeEndTag("order");
		}
	}

	/**
	 * Log the request made by the client to the endpoint
	 */
	private void logRequest(PartsOrderItem[] requestItems) {

		_logger.entering(_className, "logRequest");
		// log the request

		XMLWriter.writeStartTag("request");
		_logger.log(
			Level.CONFIG,
			_rb.getString("retailer.client.request.entries"));
		try {
			for (int i = 0; i < requestItems.length; i++) {
				_logger.log(
					Level.CONFIG,
					_rb.getString("retailer.client.request.productNumber"),
					requestItems[i].getProductNumber().toString());

				_logger.log(
					Level.CONFIG,
					_rb.getString("retailer.client.request.quantity"),
					requestItems[i].getQuantity());

				XMLWriter.writeStartTag("request-item");
				XMLWriter.writeContent(
					"product",
					requestItems[i].getProductNumber().toString());
				XMLWriter.writeContent(
					"quantity",
					requestItems[i].getQuantity().toString());
				XMLWriter.writeEndTag("request-item");
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		} finally {
			XMLWriter.writeEndTag("request");
			_logger.exiting(_className, "logRequest");
		}
	}

	/**
	 * Log the response received by the client from the endpoint
	 */
	private void logResponse(
		PartsOrderResponseType response,
		String demoUserId) {

		_logger.entering(_className, "logResponse");
		// log the response
		// only if response != null (e.g. an invalid order passes null here)
		if (response != null) {

			PartsOrderResponseItem[] responseItem = response.getItem();
			XMLWriter.writeStartTag("response");
			_logger.log(
				Level.CONFIG,
				_rb.getString("retailer.client.response.entries"));
			try {
				for (int i = 0; i < responseItem.length; i++) {
					_logger.log(
						Level.CONFIG,
						_rb.getString("retailer.client.response.thProduct"),
						String.valueOf(i + 1));

					_logger.log(
						Level.CONFIG,
						_rb.getString("retailer.client.response.productNumber"),
						responseItem[i].getProductNumber().toString());

					_logger.log(
						Level.CONFIG,
						_rb.getString("retailer.client.response.quantity"),
						responseItem[i].getQuantity());

					_logger.log(
						Level.CONFIG,
						_rb.getString("retailer.client.response.price"),
						responseItem[i].getPrice());

					_logger.log(
						Level.CONFIG,
						_rb.getString("retailer.client.response.comment"),
						responseItem[i].getComment());

					XMLWriter.writeStartTag("response-item");
					XMLWriter.writeContent(
						"product",
						responseItem[i].getProductNumber().toString());
					XMLWriter.writeContent(
						"quantity",
						responseItem[i].getQuantity().toString());
					XMLWriter.writeContent(
						"price",
						responseItem[i]
							.getPrice()
							.setScale(2, BigDecimal.ROUND_HALF_UP)
							.toString());
					XMLWriter.writeContent(
						"comment",
						responseItem[i].getComment());
					XMLWriter.writeEndTag("response-item");
				}
			} catch (Throwable ex) {
				ex.printStackTrace();
				_logger.log(Level.SEVERE, ex.getMessage(), ex);
			} finally {
				XMLWriter.writeEndTag("response");
			}
		}

		// log the logging entries
		_logger.log(
			Level.CONFIG,
			_rb.getString("retailer.client.logging.entries"));
		try {
			XMLWriter.writeStartTag("logs");
			GetEventsRequestType request = new GetEventsRequestType();
			request.setDemoUserID(demoUserId);

			// Wait for double the callback delay
			// Just to make sure that all the entries are logged
			Thread.sleep(CALLBACK_DELAY + CALLBACK_DELAY);
			GetEventsResponseType logResponse = _loggingStub.getEvents(request);
			LogEntry[] logEntry = logResponse.getLogEntry();
			for (int i = 0; i < logEntry.length; i++) {
				_logger.log(
					Level.CONFIG,
					_rb.getString("retailer.client.logging.thEntry"),
					String.valueOf(i + 1));

				SimpleDateFormat sdf =
					new SimpleDateFormat("HH:mm:ss yyyy.MM.dd");
				String date = sdf.format(logEntry[i].getTimestamp().getTime());
				_logger.log(Level.CONFIG, date);
				_logger.log(Level.CONFIG, logEntry[i].getServiceID());
				_logger.log(Level.CONFIG, logEntry[i].getEventID());
				_logger.log(Level.CONFIG, logEntry[i].getEventDescription());

				XMLWriter.writeStartTag("log-item");
				XMLWriter.writeContent("timestamp", date);
				XMLWriter.writeContent("event-id", logEntry[i].getEventID());
				XMLWriter.writeContent(
					"service-id",
					logEntry[i].getServiceID());
				XMLWriter.writeContent(
					"description",
					logEntry[i].getEventDescription());
				XMLWriter.writeEndTag("log-item");
			}
		} catch (GetEventsFaultType ex) {
			ex.printStackTrace();
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		} catch (Throwable t) {
			t.printStackTrace();
			_logger.log(Level.SEVERE, t.getMessage(), t);
		} finally {
			XMLWriter.writeEndTag("logs");
			_logger.exiting(_className, "logResponse");
		}
	}

	private void writeException(Throwable ex) {
		StackTraceElement[] ste = ex.getStackTrace();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < ste.length; i++) {
			buffer.append(ste[i].toString() + "\n");
		}
		XMLWriter.writeContent("error", buffer.toString());
	}
}
