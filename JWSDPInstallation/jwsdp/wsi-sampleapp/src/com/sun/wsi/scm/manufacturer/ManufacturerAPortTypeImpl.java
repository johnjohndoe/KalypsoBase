/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.manufacturer;

import javax.xml.rpc.server.ServiceLifecycle;

import com.sun.wsi.scm.configuration.ConfigurationEndpointRole;

public class ManufacturerAPortTypeImpl
	extends ManufacturerPortTypeImpl
	implements ManufacturerPortType, ServiceLifecycle {

	public void init(Object context) {
		_className = getClass().getName();
		_mfrName = MANUFACTURERA;
		_mfrProducts = MANUFACTURERA_PRODUCTS;
		_mfrData = MANUFACTURERA_DATA;
		_mfrRole = ConfigurationEndpointRole.ManufacturerA;

		super.init(context);
	}
}
