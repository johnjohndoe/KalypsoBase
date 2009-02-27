/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.warehouse;

import javax.xml.rpc.server.ServiceLifecycle;

import com.sun.wsi.scm.configuration.ConfigurationEndpointRole;

public class WarehouseAPortTypeImpl
	extends WarehousePortTypeImpl
	implements WarehouseShipmentsPortType, ServiceLifecycle {

	public void init(Object context) {
		_className = getClass().getName();
		_warehouseName = WAREHOUSEA;
		_warehouseRole = ConfigurationEndpointRole._WarehouseAString;
		_warehouseData = WAREHOUSEA_DATA;

		super.init(context);
	}
}
