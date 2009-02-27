/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.manufacturer;

import javax.xml.rpc.server.ServiceLifecycle;

import com.sun.wsi.scm.configuration.ConfigurationEndpointRole;

public class ManufacturerBPortTypeImpl
	extends ManufacturerPortTypeImpl
	implements ManufacturerPortType, ServiceLifecycle {

	public void init(Object context) {
		_className = getClass().getName();
		_mfrName = MANUFACTURERB;
		_mfrProducts = MANUFACTURERB_PRODUCTS;
		_mfrData = MANUFACTURERB_DATA;
		_mfrRole = ConfigurationEndpointRole.ManufacturerB;

		super.init(context);
	}
}
