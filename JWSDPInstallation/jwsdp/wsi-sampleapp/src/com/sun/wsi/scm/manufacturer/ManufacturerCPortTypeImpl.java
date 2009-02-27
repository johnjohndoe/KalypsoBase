/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.manufacturer;

import javax.xml.rpc.server.ServiceLifecycle;

import com.sun.wsi.scm.configuration.ConfigurationEndpointRole;

public class ManufacturerCPortTypeImpl
	extends ManufacturerPortTypeImpl
	implements ManufacturerPortType, ServiceLifecycle {

	public void init(Object context) {
		_className = getClass().getName();
		_mfrName = MANUFACTURERC;
		_mfrProducts = MANUFACTURERC_PRODUCTS;
		_mfrData = MANUFACTURERC_DATA;
		_mfrRole = ConfigurationEndpointRole.ManufacturerC;

		super.init(context);
	}
}
