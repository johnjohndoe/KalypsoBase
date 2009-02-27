/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.util;

public interface JAXRConstants {
	String QUERY_MANAGER_URL = "javax.xml.registry.queryManagerURL";
	String LIFECYCLE_MANAGER_URL = "javax.xml.registry.lifeCycleManagerURL";

	String HTTP_PROXY_HOST = "com.sun.xml.registry.http.proxyHost";
	String HTTP_PROXY_PORT = "com.sun.xml.registry.http.proxyPort";
	String HTTPS_PROXY_HOST = "com.sun.xml.registry.https.proxyHost";
	String HTTPS_PROXY_PORT = "com.sun.xml.registry.https.proxyPort";

	String MAX_ROWS = "javax.xml.registry.uddi.maxRows";

	String SUN_BUSINESS_KEY = "7AEAA890-FDC2-11D6-97CF-000629DC0A53";
	String WSI_BUSINESS_KEY = "EE7A7A30-F67C-11D6-B618-000629DC0A53";

	String RETAILER_TMODEL_KEY = "UUID:44599540-CC06-11D6-9D4F-000629DC0A53";
	String WAREHOUSE_TMODEL_KEY = "UUID:79CF57F0-CC06-11D6-9D4F-000629DC0A53";
	String MANUFACTURER_TMODEL_KEY =
		"UUID:AD04EEA0-CC06-11D6-9D4F-000629DC0A53";
	String LOGGING_TMODEL_KEY = "UUID:FE462140-CC05-11D6-9D4F-000629DC0A53";
	String CONFIGURATOR_TMODEL_KEY =
		"UUID:C5FE2BC0-CC05-11D6-9D4F-000629DC0A53";

	String[] SERVICE_TMODEL_NAMES =
		{
			"Retailer",
			"Warehouse",
			"Manufacturer",
			"LoggingFacility",
			"Configurator" };

	String CATEGORY_BAG_KEY = "UUID:A035A07C-F362-44DD-8F95-E2B134BF43B4";

	String[] WAREHOUSE_KEYVALUE =
		{
			"ws-i:sampleRole:warehouse,NorthAmerica",
			"ws-i:sampleRole:warehouse,Europe",
			"ws-i:sampleRole:warehouse,Asia" };

	int WAREHOUSE_A = 0;
	int WAREHOUSE_B = 1;
	int WAREHOUSE_C = 2;

	String[] MANUFACTURER_KEYVALUE =
		{
			"ws-i:sampleRole:manufacturer,BrandA",
			"ws-i:sampleRole:manufacturer,BrandB",
			"ws-i:sampleRole:manufacturer,BrandC" };

	int MANUFACTURER_A = 0;
	int MANUFACTURER_B = 1;
	int MANUFACTURER_C = 2;
}
