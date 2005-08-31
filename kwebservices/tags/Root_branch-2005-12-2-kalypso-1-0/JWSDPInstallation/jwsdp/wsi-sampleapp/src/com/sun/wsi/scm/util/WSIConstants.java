/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.util;

public interface WSIConstants {
	String CONFIGURATION_NAMESPACE =
		"http://www.ws-i.org/SampleApplications/SupplyChainManagement/2002-08/Configuration.xsd";
	String RETAIL_ORDER_NAMESPACE =
		"http://www.ws-i.org/SampleApplications/SupplyChainManagement/2002-08/RetailOrder.xsd";
	String SOAP_ENVELOPE_NAMESPACE =
		"http://schemas.xmlsoap.org/soap/envelope/";

	String ORDER_NUMBER = "1";
	float PO_ITEM_PRICE = (float) 1.00;
	float PO_TOTAL_PRICE = (float) 2.00;

	String WAREHOUSE_CALLBACK_LOCATION = "/scm/warehouse/callback";
	String CONFIGURATOR_LOCATION = "/scm/configurator";

	int[] PRODUCT_ID =
		{
			605001,
			605002,
			605003,
			605004,
			605005,
			605006,
			605007,
			605008,
			605009 };

	int STOCK = 0;
	int MIN = 1;
	int MAX = 2;

	// Stock, Minimum, Maximum
	int[][] WAREHOUSEA_DATA = { { 10, 5, 25 }, {
			7, 4, 20 }, {
			15, 10, 50 }, {
			55, 10, 70 }, {
			10, 5, 10 }, {
			20, 5, 20 }, {
			70, 30, 100 }, {
			25, 10, 50 }, {
			20, 20, 50 }
	};

	// Stock, Minimum, Maximum
	int[][] WAREHOUSEB_DATA = { { 30, 5, 55 }, {
			10, 4, 20 }, {
			15, 10, 50 }, {
			70, 10, 70 }, {
			10, 5, 10 }, {
			20, 5, 20 }, {
			30, 30, 100 }, {
			35, 10, 50 }, {
			30, 20, 50 }
	};

	// Stock, Minimum, Maximum
	int[][] WAREHOUSEC_DATA = { { 45, 5, 55 }, {
			20, 4, 20 }, {
			15, 10, 50 }, {
			11, 10, 70 }, {
			10, 5, 10 }, {
			20, 5, 20 }, {
			85, 30, 100 }, {
			45, 10, 50 }, {
			40, 20, 50 }
	};

	int[] MANUFACTURERA_PRODUCTS = { 605001, 605004, 605007 };
	int[] MANUFACTURERB_PRODUCTS = { 605002, 605005, 605008 };
	int[] MANUFACTURERC_PRODUCTS = { 605003, 605006, 605009 };

	// Stock, Minimum, Maximum
	int[][] MANUFACTURERA_DATA = { { 10, 5, 25 }, {
			5570, 105, 70100 }, {
			70, 30, 100 }
	};

	// Stock, Minimum, Maximum
	int[][] MANUFACTURERB_DATA = { { 5, 5, 10 }, {
			10, 5, 10 }, {
			35, 10, 50 }
	};

	// Stock, Minimum, Maximum
	int[][] MANUFACTURERC_DATA = { { 15, 10, 50 }, {
			20, 5, 20 }, {
			40, 20, 50 }
	};

	long CALLBACK_DELAY = 5000; // 5000 milli seconds

	String GET_CATALOG = "getCatalog";
	String SUBMIT_ORDER = "submitOrder";
	String SUBMIT_SN = "submitSN";
	String SHIP_GOODS = "ShipGoods";
	String ERROR_PO = "errorPO";
	String SUBMIT_PO = "submitPO";

	String INIT = "init";
	String RUN = "run";
	String PROCESS_ORDER = "processOrder";
	String COMPLETE_ORDER = "completeOrder";

	String LOG_EVENT = "logEvent";
	String GET_EVENTS = "getEvents";

	String REPLENISH_STOCK = "replenishStock";

	String GET_CONFIG_OPTIONS = "getConfigurationOptions";
	String GET_CONFIG_OPTIONS_UDDI = "getConfigurationOptionsFromUDDI";

	String CALLBACK_INVOKER = "CallbackInvoker";

	String LOGGER = "com.sun.wsi.scm";

	String WAREHOUSEA = "WarehouseA";
	String WAREHOUSEB = "WarehouseB";
	String WAREHOUSEC = "WarehouseC";

	String MANUFACTURERA = "ManufacturerA";
	String MANUFACTURERB = "ManufacturerB";
	String MANUFACTURERC = "ManufacturerC";

	String[] SERVICE_NAMES =
		{
			"LoggingFacility",
			"Retailer",
			"WarehouseA",
			"WarehouseB",
			"WarehouseC",
			"ManufacturerA",
			"ManufacturerB",
			"ManufacturerC" };

	int LOGGING_SERVICE = 0;
	int RETAILER_SERVICE = 1;
	int WAREHOUSEA_SERVICE = 2;
	int WAREHOUSEB_SERVICE = 3;
	int WAREHOUSEC_SERVICE = 4;
	int MANUFACTURERA_SERVICE = 5;
	int MANUFACTURERB_SERVICE = 6;
	int MANUFACTURERC_SERVICE = 7;

	String RETAILER_SUBMIT_ORDER = "Retailer." + SUBMIT_ORDER;

	String CONF = "/conf";
	String RETAILER_RESOURCES =
		Localizer.localizedBundleName(
			CONF + "/com/sun/wsi/scm/resources/retailer.server");
	String MANUFACTURER_RESOURCES =
		Localizer.localizedBundleName(
			CONF + "/com/sun/wsi/scm/resources/manufacturer");
	String WAREHOUSE_RESOURCES =
		Localizer.localizedBundleName(
			CONF + "/com/sun/wsi/scm/resources/warehouse");
	String LOGGING_RESOURCES =
		Localizer.localizedBundleName(
			CONF + "/com/sun/wsi/scm/resources/logging");
	String CONFIGURATOR_RESOURCES =
		Localizer.localizedBundleName(
			CONF + "/com/sun/wsi/scm/resources/configurator.server");
	String DB_RESOURCES =
		Localizer.localizedBundleName(CONF + "/com/sun/wsi/scm/resources/db");

	String DB_PROPS = CONF + "/db.props";
	String CACHED_ENDPOINTS = CONF + "/endpoints.xml";

	String DEFAULT_CONFIGURATOR_ENDPOINT =
		"http://localhost:8080/wsi-server/scm/configurator";
		
	String UDDI_CONFIG = CONF + "/uddi-config.props";

	String HTML_FILE_EXTENSION = ".html";

	String ORDER_RESULTS_NAMESPACE =
		"http://java.sun.com/xml/ns/jax-rpc/wsi/order/results";
	String VENDOR_CONFIG_NAMESPACE =
		"http://java.sun.com/xml/ns/jax-rpc/wsi/vendor/config";
	String VENDOR_CONFIG_PREFIX = "vendorConfig";
	String IMAGE_MIME_TYPE = "image/jpeg";
	String XML_MIME_TYPE = "text/xml";
}
